import { Link } from "react-router-dom";
import { Issue } from "@/api";
import { StatusBadge } from "./StatusBadge";
import { PriorityBadge } from "./PriorityBadge";

export function IssueList({ issues, projectKey }: { issues: Issue[]; projectKey: string }) {
  if (issues.length === 0) {
    return <p className="text-sm text-slate-500">No issues yet.</p>;
  }
  return (
    <table className="w-full border-collapse">
      <thead>
        <tr className="border-b border-slate-200 text-left text-xs uppercase tracking-wide text-slate-500">
          <th className="px-3 py-2">ID</th>
          <th className="px-3 py-2">Title</th>
          <th className="px-3 py-2">Status</th>
          <th className="px-3 py-2">Priority</th>
          <th className="px-3 py-2">Due</th>
        </tr>
      </thead>
      <tbody>
        {issues.map((i) => (
          <tr key={i.id} className="border-b border-slate-100 hover:bg-slate-50">
            <td className="px-3 py-2 font-mono text-sm text-slate-500">
              <Link to={`/projects/${projectKey}/issues/${i.number}`}>{i.identifier}</Link>
            </td>
            <td className="px-3 py-2">
              <Link to={`/projects/${projectKey}/issues/${i.number}`} className="text-sm font-medium">
                {i.title}
              </Link>
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
