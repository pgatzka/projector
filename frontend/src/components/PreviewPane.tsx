import type { RenderError } from '../api'

type Props = {
  svg: string
  errors: RenderError[]
}

/** Shows the rendered SVG, or the list of parse/validation errors when invalid. */
export function PreviewPane({ svg, errors }: Props) {
  if (errors.length > 0) {
    return (
      <div className="h-full overflow-auto bg-red-50 p-4">
        <h2 className="mb-2 text-sm font-semibold text-red-700">
          {errors.length} problem{errors.length > 1 ? 's' : ''}
        </h2>
        <ul className="space-y-1">
          {errors.map((e, i) => (
            <li key={i} className="font-mono text-sm text-red-800">
              {e.line != null && <span className="text-red-500">line {e.line}: </span>}
              {e.message}
            </li>
          ))}
        </ul>
      </div>
    )
  }

  if (!svg) {
    return <div className="flex h-full items-center justify-center text-gray-400">No diagram yet</div>
  }

  return (
    <div
      className="flex h-full items-center justify-center overflow-auto bg-white p-4 [&_svg]:max-h-full [&_svg]:max-w-full"
      // SVG is produced by our backend with all text XML-escaped, so it is trusted.
      dangerouslySetInnerHTML={{ __html: svg }}
    />
  )
}
