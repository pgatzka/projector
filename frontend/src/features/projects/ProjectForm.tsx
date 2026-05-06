import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiError, projectsApi } from "@/api";

export function ProjectForm({ mode }: { mode: "create" | "edit" }) {
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { key: paramKey } = useParams<{ key: string }>();
  const isEdit = mode === "edit";

  const { data: existing } = useQuery({
    queryKey: ["projects", paramKey],
    queryFn: () => projectsApi.get(paramKey!),
    enabled: isEdit,
  });

  const [key, setKey] = useState("");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (isEdit && existing) {
      setKey(existing.key);
      setName(existing.name);
      setDescription(existing.description ?? "");
    }
  }, [isEdit, existing]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      if (isEdit) {
        await projectsApi.update(paramKey!, { name, description });
        await qc.invalidateQueries({ queryKey: ["projects"] });
        navigate(`/projects/${paramKey}`, { replace: true });
      } else {
        const created = await projectsApi.create({ key, name, description: description || undefined });
        await qc.invalidateQueries({ queryKey: ["projects"] });
        navigate(`/projects/${created.key}`, { replace: true });
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.detail : "Save failed.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="max-w-xl space-y-4 rounded border border-slate-200 bg-white p-6">
      <h2 className="text-lg font-medium">{isEdit ? "Edit project" : "New project"}</h2>
      {!isEdit && (
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-700">Key (2–10 uppercase letters)</span>
          <input
            value={key}
            onChange={(e) => setKey(e.target.value.toUpperCase())}
            className="w-full rounded border border-slate-300 px-3 py-2 font-mono text-sm uppercase"
            pattern="^[A-Z][A-Z]{1,9}$"
            required
          />
        </label>
      )}
      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">Name</span>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="w-full rounded border border-slate-300 px-3 py-2 text-sm"
          maxLength={100}
          required
        />
      </label>
      <label className="block">
        <span className="mb-1 block text-sm font-medium text-slate-700">Description</span>
        <textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          className="h-32 w-full rounded border border-slate-300 px-3 py-2 text-sm"
          maxLength={5000}
        />
      </label>
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
