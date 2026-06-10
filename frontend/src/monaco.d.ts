// Monaco's package "exports" map doesn't expose these deep ESM subpaths to TypeScript,
// but the bundler (Vite/Rollup) resolves them to real files. These shims let us import
// only the editor API + the YAML language instead of the full monaco-editor bundle.
declare module 'monaco-editor/esm/vs/editor/editor.api' {
  export * from 'monaco-editor'
}

declare module 'monaco-editor/esm/vs/basic-languages/yaml/yaml.contribution'
