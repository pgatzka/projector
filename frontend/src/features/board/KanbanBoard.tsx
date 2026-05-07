import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import {
  DndContext,
  DragOverlay,
  KeyboardSensor,
  PointerSensor,
  closestCorners,
  useSensor,
  useSensors,
  type DragEndEvent,
  type DragStartEvent,
} from "@dnd-kit/core";
import { sortableKeyboardCoordinates } from "@dnd-kit/sortable";
import { ApiError, issuesApi, projectsApi, type Issue, type IssueStatus } from "@/api";
import { IssueListFilters } from "@/features/issues/IssueListFilters";
import { useIssueListQuery } from "@/features/issues/useIssueListQuery";
import { useToast } from "@/components/Toast";
import { MobileNotSupportedBanner } from "@/components/MobileNotSupportedBanner";
import { IssueCard } from "./IssueCard";
import { KanbanColumn } from "./KanbanColumn";
import { STATUS_ORDER } from "./statusOrder";
import { useBoardData } from "./useBoardData";

const STATUS_SET = new Set<string>(STATUS_ORDER);

function isStatus(value: unknown): value is IssueStatus {
  return typeof value === "string" && STATUS_SET.has(value);
}

export function KanbanBoard() {
  const { key } = useParams<{ key: string }>();
  const [query, setQuery] = useIssueListQuery();
  const { push } = useToast();

  const projectQuery = useQuery({
    queryKey: ["projects", key],
    queryFn: () => projectsApi.get(key!),
    enabled: !!key,
  });

  const board = useBoardData(key, query);
  const [activeIssue, setActiveIssue] = useState<Issue | null>(null);

  const isMd = typeof window !== "undefined" && window.matchMedia("(min-width: 768px)").matches;
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: isMd ? 8 : 99999 } }),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  );

  const findIssueById = (id: string): Issue | undefined => {
    for (const status of STATUS_ORDER) {
      const found = board.columns[status].find((i) => i.id === id);
      if (found) return found;
    }
    return undefined;
  };

  const handleDragStart = (event: DragStartEvent) => {
    const id = String(event.active.id);
    setActiveIssue(findIssueById(id) ?? null);
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveIssue(null);
    if (!over || !key) return;

    const issueId = String(active.id);
    const issue = findIssueById(issueId);
    if (!issue) return;

    // The droppable id is the column status; if dropping on another card, use that card's status.
    const overId = over.id;
    let toStatus: IssueStatus | undefined;
    if (isStatus(overId)) {
      toStatus = overId;
    } else {
      const overData = over.data.current as { status?: IssueStatus } | undefined;
      if (overData && isStatus(overData.status)) toStatus = overData.status;
    }
    if (!toStatus || toStatus === issue.status) return;

    const fromStatus = issue.status;
    const { revert } = board.moveIssueOptimistic(issueId, fromStatus, toStatus);

    try {
      await issuesApi.update(key, issue.number, { status: toStatus });
      board.invalidate();
    } catch (err) {
      revert();
      const message = err instanceof ApiError ? err.detail : err instanceof Error ? err.message : "unknown error";
      push({ kind: "error", message: `Couldn't move ${issue.identifier}: ${message}` });
    }
  };

  if (projectQuery.isLoading) return <p>Loading…</p>;
  if (!projectQuery.data) return <p>Project not found.</p>;

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-3 md:items-baseline md:justify-between">
        <div>
          <h2 className="text-lg md:text-xl font-semibold">
            <span className="font-mono text-slate-500">{projectQuery.data.key}</span> · {projectQuery.data.name}
          </h2>
          {projectQuery.data.description && (
            <p className="mt-1 max-w-2xl text-sm text-slate-600">{projectQuery.data.description}</p>
          )}
        </div>
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-2">
          <div className="inline-flex overflow-hidden rounded border border-slate-300 text-sm" role="group" aria-label="View">
            <Link
              to={`/projects/${key}`}
              className="px-3 py-1.5 text-slate-600 hover:bg-slate-50"
            >
              List
            </Link>
            <span className="bg-slate-900 px-3 py-1.5 font-medium text-white">Board</span>
          </div>
          <Link
            to={`/projects/${key}/issues/new`}
            className="rounded bg-slate-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-800"
          >
            New issue
          </Link>
        </div>
      </header>

      <div className="block md:hidden">
        <MobileNotSupportedBanner projectKey={key!} />
      </div>

      <div className="hidden space-y-4 rounded border border-slate-200 bg-white p-4 md:block">
        <IssueListFilters projectKey={key!} query={query} onChange={setQuery} hideStatus />

        {board.isLoading ? (
          <p className="text-sm text-slate-500">Loading issues…</p>
        ) : board.error ? (
          <p className="text-sm text-red-600">Failed to load issues.</p>
        ) : (
          <DndContext
            sensors={sensors}
            collisionDetection={closestCorners}
            onDragStart={handleDragStart}
            onDragEnd={handleDragEnd}
            onDragCancel={() => setActiveIssue(null)}
          >
            <div className="-mx-4 overflow-x-auto px-4 pb-2">
              <div className="flex gap-3">
                {STATUS_ORDER.map((status) => (
                  <KanbanColumn
                    key={status}
                    status={status}
                    issues={board.columns[status]}
                    projectKey={key!}
                  />
                ))}
              </div>
            </div>
            <DragOverlay>
              {activeIssue ? <IssueCard issue={activeIssue} projectKey={key!} overlay /> : null}
            </DragOverlay>
          </DndContext>
        )}
      </div>
    </div>
  );
}
