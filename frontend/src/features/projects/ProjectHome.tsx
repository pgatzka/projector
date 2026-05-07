import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { issuesApi, projectsApi } from "@/api";
import { IssueList } from "@/features/issues/IssueList";
import { IssueListFilters } from "@/features/issues/IssueListFilters";
import { IssueListPagination } from "@/features/issues/IssueListPagination";
import { useIssueListQuery } from "@/features/issues/useIssueListQuery";

const DEFAULT_PAGE_SIZE = 50;

export function ProjectHome() {
  const { key } = useParams<{ key: string }>();
  const [query, setQuery] = useIssueListQuery();

  const projectQuery = useQuery({
    queryKey: ["projects", key],
    queryFn: () => projectsApi.get(key!),
    enabled: !!key,
  });

  const effectiveQuery = {
    ...query,
    page: query.page ?? 0,
    size: query.size ?? DEFAULT_PAGE_SIZE,
  };

  const issuesQuery = useQuery({
    queryKey: ["projects", key, "issues", effectiveQuery],
    queryFn: () => issuesApi.list(key!, effectiveQuery),
    enabled: !!key,
  });

  if (projectQuery.isLoading) return <p>Loading…</p>;
  if (!projectQuery.data) return <p>Project not found.</p>;

  const page = issuesQuery.data?.page ?? effectiveQuery.page;
  const size = issuesQuery.data?.size ?? effectiveQuery.size;
  const total = issuesQuery.data?.total ?? 0;

  return (
    <div className="space-y-6">
      <header className="flex items-baseline justify-between">
        <div>
          <h2 className="text-xl font-semibold">
            <span className="font-mono text-slate-500">{projectQuery.data.key}</span> · {projectQuery.data.name}
          </h2>
          {projectQuery.data.description && (
            <p className="mt-1 max-w-2xl text-sm text-slate-600">{projectQuery.data.description}</p>
          )}
        </div>
        <div className="flex gap-2">
          <Link
            to={`/projects/${key}/issues/new`}
            className="rounded bg-slate-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-800"
          >
            New issue
          </Link>
          <Link
            to={`/projects/${key}/labels`}
            className="rounded border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50"
          >
            Labels
          </Link>
          <Link
            to={`/projects/${key}/edit`}
            className="rounded border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50"
          >
            Edit project
          </Link>
        </div>
      </header>
      <div className="space-y-4 rounded border border-slate-200 bg-white p-4">
        <IssueListFilters projectKey={key!} query={query} onChange={setQuery} />
        {issuesQuery.isLoading ? (
          <p className="text-sm text-slate-500">Loading issues…</p>
        ) : (
          <>
            <IssueList items={issuesQuery.data?.items ?? []} projectKey={key!} />
            <IssueListPagination
              page={page}
              size={size}
              total={total}
              onChange={(p) => setQuery({ ...query, page: p, size: query.size })}
            />
          </>
        )}
      </div>
    </div>
  );
}
