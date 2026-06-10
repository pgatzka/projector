import Editor from '@monaco-editor/react'

type Props = {
  value: string
  onChange: (value: string) => void
}

/** Monaco code editor configured for the projector YAML DSL. */
export function EditorPane({ value, onChange }: Props) {
  return (
    <Editor
      height="100%"
      defaultLanguage="yaml"
      value={value}
      onChange={(v) => onChange(v ?? '')}
      options={{
        minimap: { enabled: false },
        fontSize: 13,
        scrollBeyondLastLine: false,
        tabSize: 2,
        automaticLayout: true,
      }}
    />
  )
}
