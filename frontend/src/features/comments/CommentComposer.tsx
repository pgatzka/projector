import { useState } from "react";
import { ApiError, commentsApi } from "@/api";
import { Markdown } from "@/components/Markdown";

const MAX_LEN = 10000;

type Tab = "write" | "preview";

export function CommentComposer({
  projectKey,
  issueNumber,
  onCreated,
}: {
  projectKey: string;
  issueNumber: number;
  onCreated: () => void;
}) {
  const [tab, setTab] = useState<Tab>("write");
  const [body, setBody] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const trimmed = body.trim();
  const tooLong = body.length > MAX_LEN;
  const canSubmit = trimmed.length > 0 && !tooLong && !submitting;

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!canSubmit) return;
    setError(null);
    setSubmitting(true);
    try {
      await commentsApi.create(projectKey, issueNumber, body);
      setBody("");
      setTab("write");
      onCreated();
    } catch (err) {
      setError(err instanceof ApiError ? err.detail : "Failed to post comment.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="rounded border border-slate-200 bg-white">
      <div className="flex items-center gap-1 border-b border-slate-200 bg-slate-50 px-2 py-1.5">
        <button
          type="button"
          onClick={() => setTab("write")}
          className={`rounded px-3 py-1 text-sm ${
            tab === "write"
              ? "bg-white font-medium text-slate-900 shadow-sm ring-1 ring-slate-200"
              : "text-slate-600 hover:text-slate-900"
          }`}
        >
          Write
        </button>
        <button
          type="button"
          onClick={() => setTab("preview")}
          className={`rounded px-3 py-1 text-sm ${
            tab === "preview"
              ? "bg-white font-medium text-slate-900 shadow-sm ring-1 ring-slate-200"
              : "text-slate-600 hover:text-slate-900"
          }`}
        >
          Preview
        </button>
      </div>
      <div className="p-3">
        {tab === "write" ? (
          <textarea
            value={body}
            onChange={(e) => setBody(e.target.value)}
            placeholder="Leave a comment. Markdown supported."
            className="h-32 w-full rounded border border-slate-300 px-3 py-2 font-mono text-sm focus:border-slate-500 focus:outline-none"
            maxLength={MAX_LEN}
          />
        ) : (
          <div className="min-h-32 rounded border border-slate-200 bg-slate-50 px-3 py-2">
            {trimmed ? (
              <Markdown>{body}</Markdown>
            ) : (
              <p className="text-sm italic text-slate-400">Nothing to preview.</p>
            )}
          </div>
        )}
      </div>
      {error && (
        <p className="px-3 pb-2 text-sm text-red-600" role="alert">
          {error}
        </p>
      )}
      <div className="flex items-center justify-between gap-3 border-t border-slate-100 bg-slate-50 px-3 py-2">
        <span
          className={`text-xs ${tooLong ? "text-red-600" : "text-slate-500"}`}
          aria-live="polite"
        >
          {body.length.toLocaleString()} / {MAX_LEN.toLocaleString()} characters
        </span>
        <button
          type="submit"
          disabled={!canSubmit}
          className="rounded bg-slate-900 px-4 py-1.5 text-sm font-medium text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {submitting ? "Posting…" : "Comment"}
        </button>
      </div>
    </form>
  );
}
