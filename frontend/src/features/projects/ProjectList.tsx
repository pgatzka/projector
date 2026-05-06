import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { projectsApi } from "@/api";

export function ProjectList() {
  const { data, isLoading } = useQuery({
    queryKey: ["projects"],
    queryFn: projectsApi.list,
  });

  if (isLoading) return <p className="text-slate-500">Loading projects…</p>;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-medium">Projects</h2>
        <Link
          to="/projects/new"
          className="rounded bg-slate-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-800"
        >
          New project
        </Link>
      </div>
      {data && data.length === 0 && (
        <p className="text-sm text-slate-500">No projects yet. Create one to get started.</p>
      )}
      <ul className="divide-y divide-slate-200 rounded border border-slate-200 bg-white">
        {data?.map((p) => (
          <li key={p.id} className="px-4 py-3 hover:bg-slate-50">
            <Link to={`/projects/${p.key}`} className="block">
              <div className="flex items-baseline gap-3">
                <span className="font-mono text-sm text-slate-500">{p.key}</span>
                <span className="font-medium">{p.name}</span>
              </div>
              {p.description && <p className="mt-1 text-sm text-slate-600 line-clamp-2">{p.description}</p>}
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
