import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import {
  labelsApi,
  projectsApi,
  type CreateLabelReq,
  type Label,
  type UpdateLabelReq,
} from "@/api";
import { LabelBadge } from "./LabelBadge";
import { LabelForm } from "./LabelForm";
import { DeleteLabelConfirm } from "./DeleteLabelConfirm";

export function LabelsPage() {
  const { key } = useParams<{ key: string }>();
  const qc = useQueryClient();

  const projectQuery = useQuery({
    queryKey: ["projects", key],
    queryFn: () => projectsApi.get(key!),
    enabled: !!key,
  });

  const labelsQuery = useQuery({
    queryKey: ["projects", key, "labels"],
    queryFn: () => labelsApi.list(key!),
    enabled: !!key,
  });

  const [creating, setCreating] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [deletingLabel, setDeletingLabel] = useState<Label | null>(null);

  async function invalidateLabels() {
    await qc.invalidateQueries({ queryKey: ["projects", key, "labels"] });
  }

  async function handleCreate(req: CreateLabelReq | UpdateLabelReq) {
    await labelsApi.create(key!, req as CreateLabelReq);
    await invalidateLabels();
    setCreating(false);
  }

  async function handleUpdate(id: string, req: CreateLabelReq | UpdateLabelReq) {
    await labelsApi.update(key!, id, req as UpdateLabelReq);
    await invalidateLabels();
    setEditingId(null);
  }

  async function handleDelete() {
    if (!deletingLabel) return;
    await labelsApi.delete(key!, deletingLabel.id);
    await invalidateLabels();
    setDeletingLabel(null);
  }

  if (projectQuery.isLoading) return <p>Loading…</p>;
  if (!projectQuery.data) return <p>Project not found.</p>;

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-3 md:items-baseline md:justify-between">
        <div>
          <h2 className="text-lg md:text-xl font-semibold">
            <span className="font-mono text-slate-500">{projectQuery.data.key}</span> · Labels
          </h2>
          <Link to={`/projects/${key}`} className="text-sm text-slate-600 hover:underline">
            ← Back to project
          </Link>
        </div>
        {!creating && (
          <button
            onClick={() => {
              setCreating(true);
              setEditingId(null);
            }}
            className="rounded bg-slate-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-800"
          >
            New label
          </button>
        )}
      </header>

      {creating && (
        <LabelForm
          submitLabel="Create"
          onSubmit={handleCreate}
          onCancel={() => setCreating(false)}
        />
      )}

      <div className="rounded border border-slate-200 bg-white">
        {labelsQuery.isLoading ? (
          <p className="p-4 text-sm text-slate-500">Loading labels…</p>
        ) : labelsQuery.data && labelsQuery.data.length === 0 ? (
          <p className="p-4 text-sm text-slate-500">No labels yet.</p>
        ) : (
          <ul className="divide-y divide-slate-200">
            {labelsQuery.data?.map((label) => (
              <li key={label.id} className="p-4">
                {editingId === label.id ? (
                  <LabelForm
                    initial={label}
                    submitLabel="Save"
                    onSubmit={(req) => handleUpdate(label.id, req)}
                    onCancel={() => setEditingId(null)}
                  />
                ) : (
                  <div className="flex items-center justify-between gap-3">
                    <div className="flex items-center gap-3">
                      <LabelBadge name={label.name} color={label.color} />
                      <span className="text-sm text-slate-500">{label.color}</span>
                    </div>
                    <div className="flex gap-2">
                      <button
                        onClick={() => {
                          setEditingId(label.id);
                          setCreating(false);
                        }}
                        className="rounded border border-slate-300 px-3 py-1 text-sm hover:bg-slate-50"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => setDeletingLabel(label)}
                        className="rounded border border-rose-300 px-3 py-1 text-sm text-rose-700 hover:bg-rose-50"
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>

      {deletingLabel && (
        <DeleteLabelConfirm
          projectKey={key!}
          label={deletingLabel}
          onConfirm={handleDelete}
          onCancel={() => setDeletingLabel(null)}
        />
      )}
    </div>
  );
}
