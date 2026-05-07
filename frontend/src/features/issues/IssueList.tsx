import { Link } from "react-router-dom";
import type { Issue } from "@/api";
import { StatusBadge } from "./StatusBadge";
import { PriorityBadge } from "./PriorityBadge";
import { LabelBadge } from "@/features/labels/LabelBadge";

export function IssueList({ items, projectKey }: { items: Issue[]; projectKey: string }) {
  if (items.length === 0) {
    return <p className="text-sm text-slate-500">No issues match.</p>;
  }
  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse">
        <thead>
          <tr className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
            <th className="px-3 py-2">ID</th>
            <th className="px-3 py-2">Title</th>
            <th className="hidden px-3 py-2 md:table-cell">Labels</th>
            <th className="px-3 py-2">Status</th>
            <th className="hidden px-3 py-2 sm:table-cell">Priority</th>
            <th className="hidden px-3 py-2 sm:table-cell">Due</th>
          </tr>
        </thead>
        <tbody>
          {items.map((i) => (
            <tr key={i.id} className="border-b border-slate-100 hover:bg-slate-50">
              <td className="px-3 py-2 font-mono text-xs sm:text-sm text-slate-500">
                <Link to={`/projects/${projectKey}/issues/${i.number}`}>{i.identifier}</Link>
              </td>
              <td className="px-3 py-2">
                <Link to={`/projects/${projectKey}/issues/${i.number}`} className="text-sm font-medium">
                  {i.title}
                </Link>
              </td>
              <td className="hidden px-3 py-2 md:table-cell">
                <div className="flex flex-wrap gap-1">
                  {i.labels.map((l) => (
                    <LabelBadge key={l.id} name={l.name} color={l.color} />
                  ))}
                </div>
              </td>
              <td className="px-3 py-2"><StatusBadge status={i.status} /></td>
              <td className="hidden px-3 py-2 text-sm sm:table-cell"><PriorityBadge priority={i.priority} /></td>
              <td className="hidden px-3 py-2 text-sm text-slate-500 sm:table-cell">{i.dueDate ?? "—"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
