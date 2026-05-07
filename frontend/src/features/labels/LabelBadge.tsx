import type { LabelColor } from "@/api";

// Tailwind has no native "brown"; we map it to amber-200/amber-900.
// Class strings are written literally so Tailwind's JIT keeps them.
// (The tailwind.config safelist also pins these — belt-and-braces.)
export const colorClasses: Record<LabelColor, string> = {
  gray: "bg-gray-200 text-gray-800",
  red: "bg-red-100 text-red-900",
  orange: "bg-orange-100 text-orange-900",
  yellow: "bg-yellow-100 text-yellow-900",
  green: "bg-green-100 text-green-900",
  teal: "bg-teal-100 text-teal-900",
  blue: "bg-blue-100 text-blue-900",
  indigo: "bg-indigo-100 text-indigo-900",
  violet: "bg-violet-100 text-violet-900",
  pink: "bg-pink-100 text-pink-900",
  brown: "bg-amber-200 text-amber-900",
  slate: "bg-slate-200 text-slate-800",
};

export const swatchClasses: Record<LabelColor, string> = {
  gray: "bg-gray-300",
  red: "bg-red-300",
  orange: "bg-orange-300",
  yellow: "bg-yellow-300",
  green: "bg-green-300",
  teal: "bg-teal-300",
  blue: "bg-blue-300",
  indigo: "bg-indigo-300",
  violet: "bg-violet-300",
  pink: "bg-pink-300",
  brown: "bg-amber-400",
  slate: "bg-slate-300",
};

export function LabelBadge({ name, color }: { name: string; color: LabelColor }) {
  return (
    <span
      className={`inline-block rounded px-2 py-0.5 text-xs font-medium ${colorClasses[color]}`}
    >
      {name}
    </span>
  );
}
