import { useQuery, useQueryClient } from "@tanstack/react-query";
import { timelineApi } from "@/api";
import { CommentEntry } from "@/features/comments/CommentEntry";
import { CommentComposer } from "@/features/comments/CommentComposer";
import { ActivityEntry } from "@/features/activity/ActivityEntry";

export function IssueTimeline({
  projectKey,
  issueNumber,
}: {
  projectKey: string;
  issueNumber: number;
}) {
  const qc = useQueryClient();
  const queryKey = ["projects", projectKey, "issues", issueNumber, "timeline"];

  const { data, isLoading, isError } = useQuery({
    queryKey,
    queryFn: () => timelineApi.get(projectKey, issueNumber),
  });

  async function refresh() {
    await qc.invalidateQueries({ queryKey });
  }

  return (
    <section className="space-y-4">
      <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Activity</h3>

      {isLoading && <p className="text-sm text-slate-500">Loading timeline…</p>}
      {isError && <p className="text-sm text-red-600">Failed to load timeline.</p>}

      {data && data.entries.length === 0 && (
        <p className="text-sm text-slate-500">No activity yet.</p>
      )}

      {data && data.entries.length > 0 && (
        <ol className="space-y-3">
          {data.entries.map((entry) => (
            <li key={entry.id}>
              {entry.type === "comment" ? (
                <CommentEntry
                  comment={{
                    id: entry.id,
                    createdAt: entry.createdAt,
                    createdBy: entry.createdBy,
                    bodyMd: entry.bodyMd ?? "",
                  }}
                />
              ) : (
                <ActivityEntry activity={entry} />
              )}
            </li>
          ))}
        </ol>
      )}

      <CommentComposer
        projectKey={projectKey}
        issueNumber={issueNumber}
        onCreated={refresh}
      />
    </section>
  );
}
