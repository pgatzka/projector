import type { DiagramSummary } from '../api'

type Props = {
  diagrams: DiagramSummary[]
  currentId: string | null
  onLoad: (id: string) => void
  onDelete: (id: string) => void
}

/** List of saved diagrams with load (click) and delete actions. */
export function DiagramSidebar({ diagrams, currentId, onLoad, onDelete }: Props) {
  return (
    <aside className="flex w-64 shrink-0 flex-col border-r border-gray-200 bg-gray-50">
      <h2 className="border-b border-gray-200 px-3 py-2 text-xs font-semibold tracking-wide text-gray-500 uppercase">
        Saved diagrams
      </h2>
      <ul className="flex-1 overflow-auto">
        {diagrams.length === 0 && <li className="px-3 py-2 text-sm text-gray-400">None yet</li>}
        {diagrams.map((d) => (
          <li
            key={d.id}
            className={`group flex items-center justify-between px-3 py-2 text-sm hover:bg-gray-100 ${
              d.id === currentId ? 'bg-blue-50 text-blue-700' : 'text-gray-700'
            }`}
          >
            <button className="min-w-0 flex-1 truncate text-left" onClick={() => onLoad(d.id)} title={d.name}>
              {d.name}
            </button>
            <button
              className="ml-2 hidden text-gray-400 hover:text-red-600 group-hover:block"
              onClick={() => onDelete(d.id)}
              title="Delete"
              aria-label={`Delete ${d.name}`}
            >
              ✕
            </button>
          </li>
        ))}
      </ul>
    </aside>
  )
}
