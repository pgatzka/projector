import { useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import type { IssueListQuery, IssuePriority, IssueStatus } from "@/api";

const VALID_STATUSES: IssueStatus[] = ["backlog", "todo", "in_progress", "done", "cancelled"];
const VALID_PRIORITIES: IssuePriority[] = ["low", "medium", "high", "urgent"];

const FILTER_FACETS: Array<keyof IssueListQuery> = ["status", "priority", "label", "q"];

function splitCsv(value: string | null): string[] {
  if (!value) return [];
  return value.split(",").map((s) => s.trim()).filter(Boolean);
}

function parseQuery(params: URLSearchParams): IssueListQuery {
  const status = splitCsv(params.get("status")).filter((s): s is IssueStatus =>
    (VALID_STATUSES as string[]).includes(s),
  );
  const priority = splitCsv(params.get("priority")).filter((p): p is IssuePriority =>
    (VALID_PRIORITIES as string[]).includes(p),
  );
  const label = splitCsv(params.get("label"));
  const q = params.get("q") ?? undefined;
  const pageStr = params.get("page");
  const sizeStr = params.get("size");
  const page = pageStr !== null && !Number.isNaN(Number(pageStr)) ? Number(pageStr) : undefined;
  const size = sizeStr !== null && !Number.isNaN(Number(sizeStr)) ? Number(sizeStr) : undefined;

  return {
    status: status.length > 0 ? status : undefined,
    priority: priority.length > 0 ? priority : undefined,
    label: label.length > 0 ? label : undefined,
    q: q && q !== "" ? q : undefined,
    page,
    size,
  };
}

function facetChanged(prev: IssueListQuery, next: IssueListQuery): boolean {
  for (const facet of FILTER_FACETS) {
    const a = prev[facet];
    const b = next[facet];
    if (Array.isArray(a) || Array.isArray(b)) {
      const aa = Array.isArray(a) ? a : [];
      const bb = Array.isArray(b) ? b : [];
      if (aa.length !== bb.length) return true;
      if (aa.some((v, i) => v !== bb[i])) return true;
    } else if (a !== b) {
      return true;
    }
  }
  return false;
}

export function useIssueListQuery(): [IssueListQuery, (next: IssueListQuery) => void] {
  const [searchParams, setSearchParams] = useSearchParams();
  const query = parseQuery(searchParams);

  const setQuery = useCallback(
    (next: IssueListQuery) => {
      const prev = parseQuery(searchParams);
      const resetPage = facetChanged(prev, next);
      const effective: IssueListQuery = resetPage ? { ...next, page: 0 } : next;

      const params = new URLSearchParams();
      if (effective.status && effective.status.length > 0) params.set("status", effective.status.join(","));
      if (effective.priority && effective.priority.length > 0) params.set("priority", effective.priority.join(","));
      if (effective.label && effective.label.length > 0) params.set("label", effective.label.join(","));
      if (effective.q && effective.q !== "") params.set("q", effective.q);
      if (effective.page !== undefined && effective.page !== 0) params.set("page", String(effective.page));
      if (effective.size !== undefined) params.set("size", String(effective.size));
      setSearchParams(params, { replace: true });
    },
    [searchParams, setSearchParams],
  );

  return [query, setQuery];
}
