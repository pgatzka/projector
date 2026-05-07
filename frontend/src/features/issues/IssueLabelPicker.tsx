import { useEffect, useRef, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { labelsApi } from "@/api";
import { LabelBadge } from "@/features/labels/LabelBadge";

export function IssueLabelPicker({
  projectKey,
  selectedIds,
  onChange,
}: {
  projectKey: string;
  selectedIds: string[];
  onChange: (ids: string[]) => void;
}) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const labelsQuery = useQuery({
    queryKey: ["projects", projectKey, "labels"],
    queryFn: () => labelsApi.list(projectKey),
  });

  useEffect(() => {
    if (!open) return;
    function handleClick(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [open]);

  function toggle(id: string) {
    if (selectedIds.includes(id)) {
      onChange(selectedIds.filter((x) => x !== id));
    } else {
      onChange([...selectedIds, id]);
    }
  }

  const count = selectedIds.length;

  return (
    <div className="relative inline-block" ref={containerRef}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="rounded border border-slate-300 px-3 py-1.5 text-sm hover:bg-slate-50"
      >
        Labels ({count})
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
