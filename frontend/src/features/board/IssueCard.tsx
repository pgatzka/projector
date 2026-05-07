import { Link } from "react-router-dom";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import type { Issue } from "@/api";
import { LabelBadge } from "@/features/labels/LabelBadge";
import { PriorityBadge } from "@/features/issues/PriorityBadge";

const MS_PER_DAY = 24 * 60 * 60 * 1000;

// Returns null when the date should not be shown (more than 7 days out, or in
// the past beyond a small overdue window). Returns a short relative phrase
// ("today", "tomorrow", "in 3 days", "overdue 2 days") otherwise.
function formatDueSoon(iso: string | null): { label: string; tone: "overdue" | "today" | "soon" } | null {
  if (!iso) return null;
  // Treat ISO date (yyyy-mm-dd) as a local-day boundary.
  const due = new Date(iso);
  if (Number.isNaN(due.getTime())) return null;
  const startOfDue = new Date(due.getFullYear(), due.getMonth(), due.getDate()).getTime();
  const now = new Date();
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
  const diffDays = Math.round((startOfDue - startOfToday) / MS_PER_DAY);

  if (diffDays < 0) {
    const overdue = Math.abs(diffDays);
    return { label: `overdue ${overdue} day${overdue === 1 ? "" : "s"}`, tone: "overdue" };
  }
  if (diffDays === 0) return { label: "today", tone: "today" };
  if (diffDays === 1) return { label: "tomorrow", tone: "soon" };
  if (diffDays <= 7) return { label: `in ${diffDays} days`, tone: "soon" };
  return null;
}

const dueToneClasses: Record<"overdue" | "today" | "soon", string> = {
  overdue: "bg-red-50 text-red-700 ring-1 ring-inset ring-red-200",
  today: "bg-amber-50 text-amber-800 ring-1 ring-inset ring-amber-200",
  soon: "bg-slate-50 text-slate-600 ring-1 ring-inset ring-slate-200",
};

export function IssueCard({
  issue,
  projectKey,
  overlay = false,
}: {
  issue: Issue;
  projectKey: string;
  overlay?: boolean;
}) {
  const due = formatDueSoon(issue.dueDate);
  const showPriority = issue.priority !== "medium";

  const sortable = useSortable({ id: issue.id, data: { status: issue.status } });
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = sortable;

  const style: React.CSSProperties = overlay
    ? {}
    : {
        transform: CSS.Transform.toString(transform),
        transition,
      };

  const baseClasses =
    "block rounded-md border bg-white p-3 shadow-sm transition hover:border-slate-300 hover:shadow-md";
  const stateClasses = overlay
    ? "border-slate-300 shadow-lg ring-2 ring-slate-900/10 cursor-grabbing"
    : isDragging
      ? "border-slate-300 opacity-40 ring-2 ring-slate-400 cursor-grabbing"
      : "border-slate-200 cursor-grab";

  const inner = (
    <>
      <div className="flex items-start justify-between gap-2">
        <span className="font-mono text-xs text-slate-500">{issue.identifier}</span>
        {showPriority && <PriorityBadge priority={issue.priority} />}
      </div>
      <h3 className="mt-1.5 line-clamp-2 text-sm font-medium leading-snug text-slate-900">
        {issue.title}
      </h3>
      {issue.labels.length > 0 && (
        <div className="mt-2 flex flex-wrap gap-1">
          {issue.labels.map((l) => (
            <LabelBadge key={l.id} name={l.name} color={l.color} />
          ))}
        </div>
      )}
      {due && (
        <div className="mt-2">
          <span className={`inline-block rounded px-2 py-0.5 text-xs font-medium ${dueToneClasses[due.tone]}`}>
            {due.label}
          </span>
        </div>
      )}
    </>
  );

  if (overlay) {
    return (
      <div className={`${baseClasses} ${stateClasses}`} style={style}>
        {inner}
      </div>
    );
  }

  return (
    <Link
      ref={setNodeRef}
      to={`/projects/${projectKey}/issues/${issue.number}`}
      className={`${baseClasses} ${stateClasses}`}
      style={style}
      data-dragging={isDragging || undefined}
      {...attributes}
      {...listeners}
    >
      {inner}
    </Link>
  );
}
