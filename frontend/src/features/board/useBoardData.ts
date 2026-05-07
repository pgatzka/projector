import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useCallback } from "react";
import { issuesApi, type Issue, type IssueListQuery, type IssueStatus, type Page } from "@/api";
import { STATUS_ORDER } from "./statusOrder";

const BOARD_PAGE_SIZE = 100;

export interface BoardData {
  columns: Record<IssueStatus, Issue[]>;
  total: number;
  isLoading: boolean;
  error: unknown;
  refetch: () => void;
  moveIssueOptimistic: (issueId: string, fromStatus: IssueStatus, toStatus: IssueStatus) => { revert: () => void };
  invalidate: () => void;
}

function emptyColumns(): Record<IssueStatus, Issue[]> {
  const out = {} as Record<IssueStatus, Issue[]>;
  for (const s of STATUS_ORDER) out[s] = [];
  return out;
}

export function useBoardData(projectKey: string | undefined, query: IssueListQuery): BoardData {
  // Strip status from the query — the board renders ALL 5 columns regardless of
  // any status facet selected elsewhere. Other facets (priority, label, q) flow through.
  const effectiveQuery: IssueListQuery = {
    priority: query.priority,
    label: query.label,
    q: query.q,
    page: 0,
    size: BOARD_PAGE_SIZE,
  };

  const queryKey = ["projects", projectKey, "board", effectiveQuery] as const;
  const queryClient = useQueryClient();

  const result = useQuery({
    queryKey,
    queryFn: () => issuesApi.list(projectKey!, effectiveQuery),
    enabled: !!projectKey,
  });

  const columns = emptyColumns();
  const items = result.data?.items ?? [];
  for (const issue of items) {
    const bucket = columns[issue.status];
    if (bucket) bucket.push(issue);
  }
  // Server returns number desc — preserve insertion order; fetched items are
  // already in that order, so no resort needed.

  const moveIssueOptimistic = useCallback(
    (issueId: string, fromStatus: IssueStatus, toStatus: IssueStatus) => {
      const snapshot = queryClient.getQueryData<Page<Issue>>(queryKey);
      queryClient.setQueryData<Page<Issue>>(queryKey, (old) => {
        if (!old) return old;
        const moved = old.items.find((i) => i.id === issueId);
        if (!moved || moved.status !== fromStatus) return old;
        const without = old.items.filter((i) => i.id !== issueId);
        const updated: Issue = { ...moved, status: toStatus };
        // Insert at the top of the destination column. Since the items list is
        // sorted by number desc and rendered per-column in iteration order,
        // placing the moved issue ahead of any other item with toStatus puts
        // it at the top of the destination column.
        const firstDestIdx = without.findIndex((i) => i.status === toStatus);
        const next = firstDestIdx === -1 ? [updated, ...without] : [
          ...without.slice(0, firstDestIdx),
          updated,
          ...without.slice(firstDestIdx),
        ];
        return { ...old, items: next };
      });
      return {
        revert: () => {
          queryClient.setQueryData<Page<Issue>>(queryKey, snapshot);
        },
      };
    },
    [queryClient, queryKey],
  );

  const invalidate = useCallback(() => {
    void queryClient.invalidateQueries({ queryKey: ["projects", projectKey, "board"] });
  }, [queryClient, projectKey]);

  return {
    columns,
    total: result.data?.total ?? 0,
    isLoading: result.isLoading,
    error: result.error,
    refetch: () => {
      void result.refetch();
    },
    moveIssueOptimistic,
    invalidate,
  };
}
