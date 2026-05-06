import { IssuePriority } from "@/api";

const STYLES: Record<IssuePriority, string> = {
  low:    "bg-slate-100 text-slate-600",
  medium: "bg-sky-100 text-sky-700",
  high:   "bg-orange-100 text-orange-700",
  urgent: "bg-red-100 text-red-700",
};

const LABELS: Record<IssuePriority, string> = {
  low: "Low",
  medium: "Medium",
  high: "High",
  urgent: "Urgent",
};

export function PriorityBadge({ priority }: { priority: IssuePriority }) {
  return (
    <span className={`inline-block rounded px-2 py-0.5 text-xs font-medium ${STYLES[priority]}`}>
      {LABELS[priority]}
    </span>
  );
}
