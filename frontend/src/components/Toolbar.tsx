type Props = {
  name: string
  onNameChange: (name: string) => void
  onSave: () => void
  onNew: () => void
  saving: boolean
  currentId: string | null
}

/** Top bar: diagram name, Save (create/update), and New. */
export function Toolbar({ name, onNameChange, onSave, onNew, saving, currentId }: Props) {
  return (
    <header className="flex items-center gap-3 border-b border-gray-200 bg-white px-4 py-2">
      <span className="text-lg font-semibold text-gray-800">projector</span>
      <input
        className="ml-2 w-64 rounded border border-gray-300 px-2 py-1 text-sm focus:border-blue-500 focus:outline-none"
        value={name}
        onChange={(e) => onNameChange(e.target.value)}
        placeholder="Diagram name"
        aria-label="Diagram name"
      />
      <button
        className="rounded bg-blue-600 px-3 py-1 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        onClick={onSave}
        disabled={saving || name.trim() === ''}
      >
        {saving ? 'Saving…' : currentId ? 'Save' : 'Create'}
      </button>
      <button
        className="rounded border border-gray-300 px-3 py-1 text-sm font-medium text-gray-700 hover:bg-gray-100"
        onClick={onNew}
      >
        New
      </button>
    </header>
  )
}
