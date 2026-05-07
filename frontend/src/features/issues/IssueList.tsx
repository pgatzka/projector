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
    <table className="w-full border-collapse">
      <thead>
        <tr className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
          <th className="px-3 py-2">ID</th>
          <th className="px-3 py-2">Title</th>
          <th className="px-3 py-2">Labels</th>
          <th className="px-3 py-2">Status</th>
          <th className="px-3 py-2">Priority</th>
          <th className="px-3 py-2">Due</th>
        </tr>
      </thead>
      <tbody>
        {items.map((i) => (
          <tr key={i.id} className="border-b border-slate-100 hover:bg-slate-50">
            <td className="px-3 py-2 font-mono text-sm text-slate-500">
              <Link to={`/projects/${projectKey}/issues/${i.number}`}>{i.identifier}</Link>
            </td>
            <td className="px-3 py-2">
              <Link to={`/projects/${projectKey}/issues/${i.number}`} className="text-sm font-medium">
                {i.title}
              </Link>
            </td>
            <td className="px-3 py-2">
              <div className="flex flex-wrap gap-1">
                {i.labels.map((l) => (
                  <LabelBadge key={l.id} name={l.name} color={l.color} />
                ))}
              </div>
            </td>
            <td className="px-3 py-2"><StatusBadge status={i.status} /></td>
            <td className="px-3 py-2"><PriorityBadge priority={i.priority} /></td>
            <td className="px-3 py-2 text-sm text-slate-500">{i.dueDate ?? "—"}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
