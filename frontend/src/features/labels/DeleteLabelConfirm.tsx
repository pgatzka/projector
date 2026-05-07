import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { ApiError, issuesApi, type Label } from "@/api";
import { LabelBadge } from "./LabelBadge";

export function DeleteLabelConfirm({
  projectKey,
  label,
  onConfirm,
  onCancel,
}: {
  projectKey: string;
  label: Label;
  onConfirm: () => Promise<void>;
  onCancel: () => void;
}) {
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const countQuery = useQuery({
    queryKey: ["projects", projectKey, "issues", "label-count", label.id],
    queryFn: () => issuesApi.list(projectKey, { label: [label.id], size: 1, page: 0 }),
  });

  async function handleConfirm() {
    setError(null);
    setSubmitting(true);
    try {
      await onConfirm();
    } catch (err) {
      setError(err instanceof ApiError ? err.detail : "Delete failed.");
      setSubmitting(false);
    }
  }

  const count = countQuery.data?.total ?? null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4">
      <div className="w-full max-w-md space-y-4 rounded bg-white p-6 shadow-lg">
        <h3 className="text-lg font-semibold">Delete label</h3>
        <div className="flex items-center gap-2">
          <LabelBadge name={label.name} color={label.color} />
        </div>
        {countQuery.isLoading ? (
          <p className="text-sm text-slate-500">Checking assignments…</p>
        ) : count !== null ? (
          <p className="text-sm text-slate-700">
            This label is assigned to <strong>{count}</strong> {count === 1 ? "issue" : "issues"}. Delete?
          </p>
        ) : (
          <p className="text-sm text-red-600">Could not load assignment count.</p>
        )}
        {error && <p className="text-sm text-red-600">{error}</p>}
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onCancel}
            disabled={submitting}
            className="rounded border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50 disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            disabled={submitting || countQuery.isLoading}
            className="rounded border border-rose-300 bg-rose-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-rose-700 disabled:opacity-50"
          >
            {submitting ? "Deleting…" : "Delete"}
          </button>
        </div>
      </div>
    </div>
  );
}
