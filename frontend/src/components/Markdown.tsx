import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import remarkKeyN from "../utils/remarkKeyN";
import rehypeSanitize, { defaultSchema } from "rehype-sanitize";
import { Link } from "react-router-dom";
import type { Schema } from "hast-util-sanitize";

const schema: Schema = {
  ...defaultSchema,
  tagNames: [
    ...(defaultSchema.tagNames ?? []),
    "input",
    "table",
    "thead",
    "tbody",
    "tr",
    "th",
    "td",
    "del",
  ],
  attributes: {
    ...(defaultSchema.attributes ?? {}),
    input: ["type", "checked", "disabled"],
    th: ["align"],
    td: ["align"],
    a: ["href", "title"],
  },
  protocols: {
    ...(defaultSchema.protocols ?? {}),
    href: ["http", "https", "mailto"],
  },
};

export function Markdown({ children }: { children: string }) {
  return (
    <div className="prose prose-sm prose-slate max-w-none">
      <ReactMarkdown
        remarkPlugins={[remarkKeyN, remarkGfm]}
        rehypePlugins={[[rehypeSanitize, schema]]}
        components={{
          a: ({ node: _node, href, children, ...rest }) => {
            if (href?.startsWith("/")) {
              return (
                <Link to={href} {...rest}>
                  {children}
                </Link>
              );
            }
            return (
              <a href={href} target="_blank" rel="noopener noreferrer" {...rest}>
                {children}
              </a>
            );
          },
        }}
      >
        {children}
      </ReactMarkdown>
    </div>
  );
}
