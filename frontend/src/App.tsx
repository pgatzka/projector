import { useCallback, useEffect, useState } from 'react'
import {
  createDiagram,
  deleteDiagram,
  getDiagram,
  listDiagrams,
  render,
  updateDiagram,
  type DiagramSummary,
  type RenderError,
} from './api'
import { DiagramSidebar } from './components/DiagramSidebar'
import { EditorPane } from './components/EditorPane'
import { PreviewPane } from './components/PreviewPane'
import { Toolbar } from './components/Toolbar'
import { STARTER_YAML } from './sample'

const RENDER_DEBOUNCE_MS = 400

function App() {
  const [yaml, setYaml] = useState(STARTER_YAML)
  const [name, setName] = useState('Untitled')
  const [currentId, setCurrentId] = useState<string | null>(null)
  const [svg, setSvg] = useState('')
  const [errors, setErrors] = useState<RenderError[]>([])
  const [diagrams, setDiagrams] = useState<DiagramSummary[]>([])
  const [saving, setSaving] = useState(false)

  const refreshList = useCallback(async () => {
    try {
      setDiagrams(await listDiagrams())
    } catch (e) {
      console.error('Failed to list diagrams', e)
    }
  }, [])

  useEffect(() => {
    void refreshList()
  }, [refreshList])

  // Debounced live render whenever the YAML changes.
  useEffect(() => {
    let cancelled = false
    const handle = setTimeout(async () => {
      const result = await render(yaml)
      if (cancelled) return
      if (result.ok) {
        setSvg(result.svg)
        setErrors([])
      } else {
        setErrors(result.errors)
      }
    }, RENDER_DEBOUNCE_MS)
    return () => {
      cancelled = true
      clearTimeout(handle)
    }
  }, [yaml])

  async function handleSave() {
    if (name.trim() === '') return
    setSaving(true)
    try {
      const saved = currentId
        ? await updateDiagram(currentId, name.trim(), yaml)
        : await createDiagram(name.trim(), yaml)
      setCurrentId(saved.id)
      await refreshList()
    } catch (e) {
      console.error('Save failed', e)
      alert('Save failed. See console for details.')
    } finally {
      setSaving(false)
    }
  }

  function handleNew() {
    setCurrentId(null)
    setName('Untitled')
    setYaml(STARTER_YAML)
  }

  async function handleLoad(id: string) {
    try {
      const d = await getDiagram(id)
      setCurrentId(d.id)
      setName(d.name)
      setYaml(d.yaml)
    } catch (e) {
      console.error('Load failed', e)
    }
  }

  async function handleDelete(id: string) {
    try {
      await deleteDiagram(id)
      if (id === currentId) handleNew()
      await refreshList()
    } catch (e) {
      console.error('Delete failed', e)
    }
  }

  return (
    <div className="flex h-full flex-col">
      <Toolbar
        name={name}
        onNameChange={setName}
        onSave={handleSave}
        onNew={handleNew}
        saving={saving}
        currentId={currentId}
      />
      <div className="flex min-h-0 flex-1">
        <DiagramSidebar
          diagrams={diagrams}
          currentId={currentId}
          onLoad={handleLoad}
          onDelete={handleDelete}
        />
        <div className="min-w-0 flex-1 border-r border-gray-200">
          <EditorPane value={yaml} onChange={setYaml} />
        </div>
        <div className="min-w-0 flex-1">
          <PreviewPane svg={svg} errors={errors} />
        </div>
      </div>
    </div>
  )
}

export default App
