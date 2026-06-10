// Client for the projector backend API (proxied at /api during dev).

export type DiagramSummary = {
  id: string
  name: string
  createdAt: string
  updatedAt: string
}

export type DiagramResponse = DiagramSummary & { yaml: string }

export type RenderError = { message: string; line?: number; column?: number }

export type RenderResult = { ok: true; svg: string } | { ok: false; errors: RenderError[] }

const JSON_HEADERS = { 'Content-Type': 'application/json' }

/** Renders YAML to SVG. On invalid input (422) returns the collected errors. */
export async function render(yaml: string): Promise<RenderResult> {
  const res = await fetch('/api/render', {
    method: 'POST',
    headers: { 'Content-Type': 'text/plain' },
    body: yaml,
  })
  if (res.ok) {
    return { ok: true, svg: await res.text() }
  }
  if (res.status === 422) {
    const body = (await res.json()) as { errors?: RenderError[] }
    return { ok: false, errors: body.errors ?? [] }
  }
  return { ok: false, errors: [{ message: `Render failed: HTTP ${res.status}` }] }
}

export async function listDiagrams(): Promise<DiagramSummary[]> {
  return getJson('/api/diagrams')
}

export async function getDiagram(id: string): Promise<DiagramResponse> {
  return getJson(`/api/diagrams/${id}`)
}

export async function createDiagram(name: string, yaml: string): Promise<DiagramResponse> {
  return sendJson('/api/diagrams', 'POST', { name, yaml })
}

export async function updateDiagram(id: string, name: string, yaml: string): Promise<DiagramResponse> {
  return sendJson(`/api/diagrams/${id}`, 'PUT', { name, yaml })
}

export async function deleteDiagram(id: string): Promise<void> {
  const res = await fetch(`/api/diagrams/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error(`Delete failed: HTTP ${res.status}`)
}

async function getJson<T>(url: string): Promise<T> {
  const res = await fetch(url)
  if (!res.ok) throw new Error(`Request failed: HTTP ${res.status}`)
  return res.json() as Promise<T>
}

async function sendJson<T>(url: string, method: string, body: unknown): Promise<T> {
  const res = await fetch(url, { method, headers: JSON_HEADERS, body: JSON.stringify(body) })
  if (!res.ok) throw new Error(`Request failed: HTTP ${res.status}`)
  return res.json() as Promise<T>
}
