/** Starter YAML shown when creating a new diagram. */
export const STARTER_YAML = `diagram: New diagram
nodes:
  - { id: start, type: start }
  - { id: do, type: action, label: Do something }
  - { id: end, type: end }
edges:
  - { from: start, to: do }
  - { from: do, to: end }
`
