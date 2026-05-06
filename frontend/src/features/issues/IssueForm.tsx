import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiError, IssuePriority, IssueStatus, issuesApi } from "@/api";

const STATUSES: IssueStatus[] = ["backlog", "todo", "in_progress", "done", "cancelled"];
const PRIORITIES: IssuePriority[] = ["low", "medium", "high", "urgent"];

export function IssueForm({ mode }: { mode: "create" | "edit" }) {
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { key, number } = useParams<{ key: string; number?: string }>();
  const num = number ? Number(number) : null;
  const isEdit = mode === "edit";

  const { data: existing } = useQuery({
    queryKey: ["projects", key, "issues", num],
    queryFn: () => issuesApi.get(key!, num!),
    enabled: isEdit && num !== null,
  });

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [status, setStatus] = useState<IssueStatus>("todo");
  const [priority, setPriority] = useState<IssuePriority>("medium");
  const [dueDate, setDueDate] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (isEdit && existing) {
      setTitle(existing.title);
      setDescription(existing.descriptionMd ?? "");
      setStatus(existing.status);
      setPriority(existing.priority);
      setDueDate(existing.dueDate ?? "");
    }
  }, [isEdit, existing]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      if (isEdit) {
        await issuesApi.update(key!, num!, {
          title, descriptionMd: description, status, priority,
          dueDate: dueDate || undefined,
        });
        await qc.invalidateQueries({ queryKey: ["projects", key, "issues"] });
        navigate(`/projects/${key}/issues/${num}`, { replace: true });
      } else {
        const created = await issuesApi.create(key!, {
          title, descriptionMd: description || undefined, status, priority,
          dueDate: dueDate || undefined,
        });
        await qc.invalidateQueries({ queryKey: ["projects", key, "issues"] });
        navigate(`/projects/${key}/issues/${created.number}`, { replace: true });
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.detail : "Save failed.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="max-w-2xl space-y-4 rounded border border-slate-200 bg-white p-6">
      <h2 className="text-lg font-medium">{isEdit ? "Edit issue" : "New issue"}</h2>
      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">Title</span>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="w-full rounded border border-slate-300 px-3 py-2 text-sm"
          maxLength={200}
          required
        />
      </label>
      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">Description (markdown)</span>
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          className="h-48 w-full rounded border border-slate-300 px-3 py-2 font-mono text-sm"
          maxLength={50000}
        />
      </label>
      <div className="grid grid-cols-3 gap-4">
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-700">Status</span>
          <select value={status} onChange={(e) => setStatus(e.target.value as IssueStatus)} className="w-full rounded border border-slate-300 px-3 py-2 text-sm">
            {STATUSES.map((s) => <option key={s} value={s}>{s.replace("_", " ")}</option>)}
          </select>
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-700">Priority</span>
          <select value={priority} onChange={(e) => setPriority(e.target.value as IssuePriority)} className="w-full rounded border border-slate-300 px-3 py-2 text-sm">
            {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
          </select>
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-700">Due date</span>
          <input type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} className="w-full rounded border border-slate-300 px-3 py-2 text-sm" />
        </label>
      </div>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <div className="flex gap-2">
        <button
          type="submit"
          disabled={submitting}
          className="rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
        >
          {submitting ? "Saving…" : "Save"}
        </button>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="rounded border border-slate-300 px-4 py-2 text-sm hover:bg-slate-50"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
