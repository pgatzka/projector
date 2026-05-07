import type { CommentDto } from "@/api";
import { Markdown } from "@/components/Markdown";
import { formatActor, formatAbsolute, formatRelative } from "./timelineFormat";

export function CommentEntry({ comment }: { comment: CommentDto }) {
  return (
    <article className="rounded border border-slate-200 bg-white">
      <header className="flex items-baseline justify-between gap-3 border-b border-slate-100 bg-slate-50 px-4 py-2 text-sm">
        <span className="font-medium text-slate-700">{formatActor(comment.createdBy)}</span>
        <time
          dateTime={comment.createdAt}
          title={formatAbsolute(comment.createdAt)}
          className="text-xs text-slate-500"
        >
          {formatRelative(comment.createdAt)}
        </time>
      </header>
      <div className="px-4 py-3">
        <Markdown>{comment.bodyMd}</Markdown>
      </div>
    </article>
  );
}
