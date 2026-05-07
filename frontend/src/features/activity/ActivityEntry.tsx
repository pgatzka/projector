import type { ReactNode } from "react";
import type { TimelineEntryDto } from "@/api";
import type { LabelColor } from "@/api";
import { LabelBadge } from "@/features/labels/LabelBadge";
import { formatActor, formatAbsolute, formatRelative } from "@/features/comments/timelineFormat";

function asString(v: unknown): string | null {
  if (v === null || v === undefined) return null;
  return String(v);
}

function humanize(v: string | null): string {
  if (!v) return "";
  return v.replace(/_/g, " ");
}

function describe(activity: TimelineEntryDto): ReactNode {
  const actor = <span className="font-medium text-slate-700">{formatActor(activity.createdBy)}</span>;
  const payload = activity.payload ?? {};
  const before = asString(payload.before);
  const after = asString(payload.after);

  switch (activity.action) {
    case "issue_created":
      return <>{actor} created this issue</>;
    case "status_changed":
      return (
        <>
          {actor} changed status from <code className="rounded bg-slate-100 px-1 text-[0.75rem] text-slate-700">{humanize(before)}</code>{" "}
          to <code className="rounded bg-slate-100 px-1 text-[0.75rem] text-slate-700">{humanize(after)}</code>
        </>
      );
    case "priority_changed":
      return (
        <>
          {actor} changed priority from <code className="rounded bg-slate-100 px-1 text-[0.75rem] text-slate-700">{humanize(before)}</code>{" "}
          to <code className="rounded bg-slate-100 px-1 text-[0.75rem] text-slate-700">{humanize(after)}</code>
        </>
      );
    case "due_date_changed":
      if (after === null && before !== null) return <>{actor} cleared the due date</>;
      if (after !== null && before === null) return <>{actor} set due date to {after}</>;
      return (
        <>
          {actor} changed due date from {before} to {after}
        </>
      );
    case "title_edited":
      return <>{actor} edited the title</>;
    case "description_edited":
      return <>{actor} edited the description</>;
    case "label_added": {
      const name = asString(payload.labelName) ?? "label";
      const color = (asString(payload.labelColor) as LabelColor | null) ?? "slate";
      return (
        <>
          {actor} added label <LabelBadge name={name} color={color} />
        </>
      );
    }
    case "label_removed": {
      const name = asString(payload.labelName) ?? "label";
      const color = (asString(payload.labelColor) as LabelColor | null) ?? "slate";
      return (
        <>
          {actor} removed label <LabelBadge name={name} color={color} />
        </>
      );
    }
    default:
      return (
        <>
          {actor} {humanize(activity.action ?? "did something")}
        </>
      );
  }
}

export function ActivityEntry({ activity }: { activity: TimelineEntryDto }) {
  return (
    <div className="flex flex-wrap items-center gap-x-2 gap-y-1 px-1 py-1.5 text-xs text-slate-500">
      <span className="inline-flex items-center gap-2">{describe(activity)}</span>
      <span aria-hidden="true" className="text-slate-300">·</span>
      <time
        dateTime={activity.createdAt}
        title={formatAbsolute(activity.createdAt)}
        className="text-slate-400"
      >
        {formatRelative(activity.createdAt)}
      </time>
    </div>
  );
}
