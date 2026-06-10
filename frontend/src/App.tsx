import { useEffect, useState } from 'react'
import './App.css'

type Health = { status: string }

function App() {
  const [health, setHealth] = useState<string>('checking…')

  useEffect(() => {
    fetch('/api/health')
      .then((r) => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`)
        return r.json() as Promise<Health>
      })
      .then((h) => setHealth(h.status))
      .catch((e) => setHealth(`unreachable: ${e.message}`))
  }, [])

  return (
    <main>
      <h1>projector</h1>
      <p>Diagrams as code — UML activity diagrams from YAML.</p>
      <p>
        Backend health: <strong>{health}</strong>
      </p>
    </main>
  )
}

export default App
