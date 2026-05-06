import { IssueStatus } from "@/api";

const STYLES: Record<IssueStatus, string> = {
  backlog:     "bg-slate-100 text-slate-700",
  todo:        "bg-blue-100 text-blue-800",
  in_progress: "bg-amber-100 text-amber-800",
  done:        "bg-emerald-100 text-emerald-800",
  cancelled:   "bg-rose-100 text-rose-700",
};

const LABELS: Record<IssueStatus, string> = {
  backlog: "Backlog",
  todo: "Todo",
  in_progress: "In progress",
  done: "Done",
  cancelled: "Cancelled",
};

export function StatusBadge({ status }: { status: IssueStatus }) {
  return (
    <span className={`inline-block rounded px-2 py-0.5 text-xs font-medium ${STYLES[status]}`}>
      {LABELS[status]}
    </span>
  );
}
