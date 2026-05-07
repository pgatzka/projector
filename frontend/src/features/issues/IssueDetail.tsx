import { useNavigate, useParams, Link } from "react-router-dom";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { issuesApi, labelsApi } from "@/api";
import { StatusBadge } from "./StatusBadge";
import { PriorityBadge } from "./PriorityBadge";
import { IssueLabelPicker } from "./IssueLabelPicker";
import { IssueTimeline } from "./IssueTimeline";
import { LabelBadge } from "@/features/labels/LabelBadge";
import { Markdown } from "@/components/Markdown";

export function IssueDetail() {
  const { key, number } = useParams<{ key: string; number: string }>();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const num = Number(number);
  const { data, isLoading } = useQuery({
    queryKey: ["projects", key, "issues", num],
    queryFn: () => issuesApi.get(key!, num),
    enabled: !!key && !Number.isNaN(num),
  });

  if (isLoading) return <p>Loading…</p>;
  if (!data) return <p>Not found.</p>;

  async function onDelete() {
    if (!confirm(`Delete ${data!.identifier}?`)) return;
    await issuesApi.delete(key!, num);
    await qc.invalidateQueries({ queryKey: ["projects", key, "issues"] });
    navigate(`/projects/${key}`, { replace: true });
  }

  async function onLabelsChange(nextIds: string[]) {
    if (!data) return;
    const currentIds = data.labels.map((l) => l.id);
    const toAdd = nextIds.filter((id) => !currentIds.includes(id));
    const toRemove = currentIds.filter((id) => !nextIds.includes(id));
    await Promise.all([
      ...toAdd.map((id) => labelsApi.assign(key!, num, id)),
      ...toRemove.map((id) => labelsApi.unassign(key!, num, id)),
    ]);
    await qc.invalidateQueries({ queryKey: ["projects", key, "issues", num] });
    await qc.invalidateQueries({ queryKey: ["projects", key, "issues"] });
  }

  return (
    <article className="space-y-4 rounded border border-slate-200 bg-white p-6">
      <header className="flex items-baseline justify-between">
        <h2 className="text-xl font-semibold">
          <span className="font-mono text-slate-500">{data.identifier}</span> · {data.title}
        </h2>
        <div className="flex gap-2">
          <Link
            to={`/projects/${key}/issues/${num}/edit`}
            className="rounded border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50"
          >
            Edit
          </Link>
          <button
            onClick={onDelete}
            className="rounded border border-rose-300 px-3 py-1.5 text-sm text-rose-700 hover:bg-rose-50"
          >
            Delete
          </button>
        </div>
      </header>
      <div className="flex flex-wrap items-center gap-2 text-sm">
        <StatusBadge status={data.status} />
        <PriorityBadge priority={data.priority} />
        {data.dueDate && <span className="rounded bg-slate-100 px-2 py-0.5 text-xs">Due {data.dueDate}</span>}
        {data.labels.map((l) => (
          <LabelBadge key={l.id} name={l.name} color={l.color} />
        ))}
      </div>
      <div>
        <IssueLabelPicker
          projectKey={key!}
          selectedIds={data.labels.map((l) => l.id)}
          onChange={onLabelsChange}
        />
      </div>
      {data.descriptionMd ? (
        <div className="rounded border border-slate-100 bg-slate-50 p-4">
          <Markdown>{data.descriptionMd}</Markdown>
        </div>
      ) : (
        <p className="text-sm text-slate-500">No description.</p>
      )}
      <IssueTimeline projectKey={key!} issueNumber={num} />
    </article>
  );
}
