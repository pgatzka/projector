import { useEffect, useRef, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  labelsApi,
  type IssueListQuery,
  type IssuePriority,
  type IssueStatus,
} from "@/api";
import { LabelBadge } from "@/features/labels/LabelBadge";

const STATUSES: IssueStatus[] = ["backlog", "todo", "in_progress", "done", "cancelled"];
const PRIORITIES: IssuePriority[] = ["low", "medium", "high", "urgent"];

const STATUS_LABELS: Record<IssueStatus, string> = {
  backlog: "Backlog",
  todo: "Todo",
  in_progress: "In progress",
  done: "Done",
  cancelled: "Cancelled",
};

function MultiSelectDropdown<T extends string>({
  label,
  options,
  selected,
  onChange,
  renderOption,
}: {
  label: string;
  options: T[];
  selected: T[];
  onChange: (next: T[]) => void;
  renderOption: (opt: T) => React.ReactNode;
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    function handleClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [open]);

  function toggle(opt: T) {
    if (selected.includes(opt)) onChange(selected.filter((x) => x !== opt));
    else onChange([...selected, opt]);
  }

  return (
    <div className="relative inline-block" ref={ref}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="rounded border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50"
      >
        {label} ({selected.length})
      </button>
      {open && (
        <div className="absolute z-10 mt-1 w-56 rounded border border-slate-200 bg-white p-2 shadow-lg">
          <ul className="space-y-1">
            {options.map((opt) => (
              <li key={opt}>
                <label className="flex cursor-pointer items-center gap-2 rounded px-2 py-1 hover:bg-slate-50">
                  <input
                    type="checkbox"
                    checked={selected.includes(opt)}
                    onChange={() => toggle(opt)}
                  />
                  {renderOption(opt)}
                </label>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

function LabelFilterDropdown({
  projectKey,
  selectedIds,
  onChange,
}: {
  projectKey: string;
  selectedIds: string[];
  onChange: (ids: string[]) => void;
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  const labelsQuery = useQuery({
    queryKey: ["projects", projectKey, "labels"],
    queryFn: () => labelsApi.list(projectKey),
  });

  useEffect(() => {
    if (!open) return;
    function handleClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [open]);

  function toggle(id: string) {
    if (selectedIds.includes(id)) onChange(selectedIds.filter((x) => x !== id));
    else onChange([...selectedIds, id]);
  }

  return (
    <div className="relative inline-block" ref={ref}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="rounded border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50"
      >
        Labels ({selectedIds.length})
      </button>
      {open && (
        <div className="absolute z-10 mt-1 max-h-72 w-64 overflow-auto rounded border border-slate-200 bg-white p-2 shadow-lg">
          {labelsQuery.isLoading ? (
            <p className="p-2 text-sm text-slate-500">Loading…</p>
          ) : labelsQuery.data && labelsQuery.data.length === 0 ? (
            <p className="p-2 text-sm text-slate-500">No labels in this project.</p>
          ) : (
            <ul className="space-y-1">
              {labelsQuery.data?.map((label) => (
                <li key={label.id}>
                  <label className="flex cursor-pointer items-center gap-2 rounded px-2 py-1 hover:bg-slate-50">
                    <input
                      type="checkbox"
                      checked={selectedIds.includes(label.id)}
                      onChange={() => toggle(label.id)}
                    />
                    <LabelBadge name={label.name} color={label.color} />
                  </label>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}

export function IssueListFilters({
  projectKey,
  query,
  onChange,
  hideStatus = false,
}: {
  projectKey: string;
  query: IssueListQuery;
  onChange: (q: IssueListQuery) => void;
  hideStatus?: boolean;
}) {
  const [searchInput, setSearchInput] = useState(query.q ?? "");

  useEffect(() => {
    setSearchInput(query.q ?? "");
  }, [query.q]);

  useEffect(() => {
    const trimmed = searchInput.trim();
    const next = trimmed === "" ? undefined : trimmed;
    if (next === (query.q ?? undefined)) return;
    const t = window.setTimeout(() => {
      onChange({ ...query, q: next, page: 0 });
    }, 300);
    return () => window.clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchInput]);

  function setStatus(next: IssueStatus[]) {
    onChange({ ...query, status: next.length > 0 ? next : undefined, page: 0 });
  }
  function setPriority(next: IssuePriority[]) {
    onChange({ ...query, priority: next.length > 0 ? next : undefined, page: 0 });
  }
  function setLabel(next: string[]) {
    onChange({ ...query, label: next.length > 0 ? next : undefined, page: 0 });
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      {!hideStatus && (
        <MultiSelectDropdown
          label="Status"
          options={STATUSES}
          selected={query.status ?? []}
          onChange={setStatus}
          renderOption={(s) => <span className="text-sm">{STATUS_LABELS[s]}</span>}
        />
      )}
      <MultiSelectDropdown
        label="Priority"
        options={PRIORITIES}
        selected={query.priority ?? []}
        onChange={setPriority}
        renderOption={(p) => <span className="text-sm capitalize">{p}</span>}
      />
      <LabelFilterDropdown
        projectKey={projectKey}
        selectedIds={query.label ?? []}
        onChange={setLabel}
      />
      <input
        type="search"
        value={searchInput}
        onChange={(e) => setSearchInput(e.target.value)}
        placeholder="Search title and description…"
        className="ml-auto w-64 rounded border border-slate-300 px-3 py-1.5 text-sm"
      />
    </div>
  );
}
