import { useDroppable } from "@dnd-kit/core";
import { SortableContext, verticalListSortingStrategy } from "@dnd-kit/sortable";
import type { Issue, IssueStatus } from "@/api";
import { IssueCard } from "./IssueCard";
import { STATUS_LABELS } from "./statusOrder";

export function KanbanColumn({
  status,
  issues,
  projectKey,
}: {
  status: IssueStatus;
  issues: Issue[];
  projectKey: string;
}) {
  const { setNodeRef, isOver } = useDroppable({ id: status, data: { status } });

  return (
    <section
      className="flex w-[280px] min-w-[280px] flex-col rounded-lg border border-slate-200 bg-slate-50/60"
      aria-label={STATUS_LABELS[status]}
    >
      <header className="flex items-center justify-between border-b border-slate-200 px-3 py-2">
        <h3 className="text-sm font-semibold text-slate-700">{STATUS_LABELS[status]}</h3>
        <span className="rounded-full bg-slate-200 px-2 py-0.5 text-xs font-medium text-slate-600">
          {issues.length}
        </span>
      </header>
      <div
        ref={setNodeRef}
        className={
          "flex flex-1 flex-col gap-2 rounded-b-lg p-2 transition-colors " +
          (isOver ? "bg-slate-200/70 ring-2 ring-inset ring-slate-300" : "")
        }
      >
        <SortableContext items={issues.map((i) => i.id)} strategy={verticalListSortingStrategy}>
          {issues.length === 0 ? (
            <p className="px-2 py-6 text-center text-xs text-slate-400">No issues</p>
          ) : (
            issues.map((issue) => (
              <IssueCard key={issue.id} issue={issue} projectKey={projectKey} />
            ))
          )}
        </SortableContext>
      </div>
    </section>
  );
}
