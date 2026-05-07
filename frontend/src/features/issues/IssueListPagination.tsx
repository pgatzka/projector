export function IssueListPagination({
  page,
  size,
  total,
  onChange,
}: {
  page: number;
  size: number;
  total: number;
  onChange: (page: number) => void;
}) {
  const pageCount = Math.max(1, Math.ceil(total / size));
  const prevDisabled = page <= 0;
  const nextDisabled = (page + 1) * size >= total;

  return (
    <div className="flex items-center justify-between text-sm text-slate-600">
      <span>
        Page {page + 1} of {pageCount} · {total} total
      </span>
      <div className="flex gap-2">
        <button
          type="button"
          onClick={() => onChange(page - 1)}
          disabled={prevDisabled}
          className="rounded border border-slate-300 px-3 py-1 hover:bg-slate-50 disabled:opacity-50"
        >
          Prev
        </button>
        <button
          type="button"
          onClick={() => onChange(page + 1)}
          disabled={nextDisabled}
          className="rounded border border-slate-300 px-3 py-1 hover:bg-slate-50 disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  );
}
