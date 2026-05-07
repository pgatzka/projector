import type { Config } from "tailwindcss";

// Belt-and-braces safelist for label colors. LabelBadge.tsx already names
// every class string literally so JIT preserves them, but pinning them here
// guarantees survival through any future refactor.
const labelColorSafelist = [
  // Base colors used by colorClasses (bg-{c}-100/200, text-{c}-800/900)
  "bg-gray-200", "text-gray-800",
  "bg-red-100", "text-red-900",
  "bg-orange-100", "text-orange-900",
  "bg-yellow-100", "text-yellow-900",
  "bg-green-100", "text-green-900",
  "bg-teal-100", "text-teal-900",
  "bg-blue-100", "text-blue-900",
  "bg-indigo-100", "text-indigo-900",
  "bg-violet-100", "text-violet-900",
  "bg-pink-100", "text-pink-900",
  "bg-amber-200", "text-amber-900", // brown alias
  "bg-slate-200", "text-slate-800",
  // Swatches (bg-{c}-300, brown -> amber-400)
  "bg-gray-300", "bg-red-300", "bg-orange-300", "bg-yellow-300",
  "bg-green-300", "bg-teal-300", "bg-blue-300", "bg-indigo-300",
  "bg-violet-300", "bg-pink-300", "bg-amber-400", "bg-slate-300",
];

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  safelist: labelColorSafelist,
  theme: {
    extend: {},
  },
  plugins: [],
} satisfies Config;
