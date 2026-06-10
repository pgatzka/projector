// Self-hosts Monaco instead of @monaco-editor/react's default CDN load. We bundle only
// the editor API + YAML language (not the full monaco-editor, which pulls every language)
// and the editor web worker, so the app works offline and as a single deployable.
import { loader } from '@monaco-editor/react'
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api'
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import 'monaco-editor/esm/vs/basic-languages/yaml/yaml.contribution'

// We only use the YAML language (Monarch tokenizer, no language worker), so the base
// editor worker is sufficient for every requested worker label.
self.MonacoEnvironment = {
  getWorker: () => new editorWorker(),
}

loader.config({ monaco })
