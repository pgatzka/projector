import { SKIP, visit } from 'unist-util-visit';
import type { Node, Parent } from 'unist';
import type { Text, Link } from 'mdast';

const KEY_N_PATTERN = /\b([A-Z]{2,10})-(\d+)\b/g;

interface MdastNode extends Node {
  children?: MdastNode[];
}

export default function remarkKeyN() {
  return (tree: MdastNode) => {
    visit(tree, 'text', (node: Text, index: number | undefined, parent: Parent | undefined) => {
      if (index === undefined || parent === undefined) {
        return;
      }

      const parentType = (parent as MdastNode).type;
      if (parentType === 'inlineCode' || parentType === 'code' || parentType === 'link' || parentType === 'linkReference') {
        return;
      }

      const text = node.value;
      const matches = Array.from(text.matchAll(KEY_N_PATTERN));

      if (matches.length === 0) {
        return;
      }

      const newNodes: (Text | Link)[] = [];
      let lastIndex = 0;

      matches.forEach((match) => {
        const matchStart = match.index!;
        const matchEnd = matchStart + match[0].length;
        const key = match[1];
        const number = match[2];

        if (matchStart > lastIndex) {
          newNodes.push({
            type: 'text',
            value: text.slice(lastIndex, matchStart),
          } as Text);
        }

        newNodes.push({
          type: 'link',
          url: `/projects/${key}/issues/${number}`,
          children: [
            {
              type: 'text',
              value: match[0],
            } as Text,
          ],
        } as unknown as Link);

        lastIndex = matchEnd;
      });

      if (lastIndex < text.length) {
        newNodes.push({
          type: 'text',
          value: text.slice(lastIndex),
        } as Text);
      }

      (parent as any).children.splice(index, 1, ...newNodes);
      return [SKIP, index + newNodes.length];
    });
  };
}
