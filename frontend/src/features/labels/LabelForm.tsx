import { useState } from "react";
import { ApiError, LABEL_COLORS, type CreateLabelReq, type Label, type LabelColor, type UpdateLabelReq } from "@/api";
import { LabelBadge, swatchClasses } from "./LabelBadge";

export function LabelForm({
  initial,
  onSubmit,
  onCancel,
  submitLabel,
}: {
  initial?: Label;
  onSubmit: (req: CreateLabelReq | UpdateLabelReq) => Promise<void>;
  onCancel: () => void;
  submitLabel: string;
}) {
  const [name, setName] = useState(initial?.name ?? "");
  const [color, setColor] = useState<LabelColor>(initial?.color ?? "gray");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onSubmit({ name: name.trim(), color });
      if (!initial) {
        setName("");
        setColor("gray");
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.detail : "Save failed.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-3 rounded border border-slate-200 bg-slate-50 p-4">
      <div className="flex flex-wrap items-end gap-3">
        <label className="block flex-1 min-w-[12rem]">
          <span className="mb-1 block text-sm font-medium text-slate-700">Name</span>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full rounded border border-slate-300 px-3 py-2 text-sm"
            maxLength={50}
            required
          />
        </label>
        <div>
          <span className="mb-1 block text-sm font-medium text-slate-700">Preview</span>
          <LabelBadge name={name || "preview"} color={color} />
        </div>
      </div>
      <div>
        <span className="mb-1 block text-sm font-medium text-slate-700">Color</span>
        <div className="flex flex-wrap gap-2">
          {LABEL_COLORS.map((c) => (
            <button
              type="button"
              key={c}
              onClick={() => setColor(c)}
              title={c}
              aria-label={c}
              aria-pressed={color === c}
              className={`h-7 w-7 rounded-full border-2 ${swatchClasses[c]} ${
                color === c ? "border-slate-900" : "border-transparent hover:border-slate-400"
              }`}
            />
          ))}
        </div>
      </div>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <div className="flex gap-2">
        <button
          type="submit"
          disabled={submitting}
          className="rounded bg-slate-900 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
        >
          {submitting ? "Saving…" : submitLabel}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="rounded border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
