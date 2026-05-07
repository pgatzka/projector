// Shared helpers for timeline rendering (comments + activity).

export function formatActor(createdBy: string): string {
  // Server emits "user:<uuid>", "system:<...>", "job:<...>".
  // Until we fetch display names for actor refs, show a short, stable label.
  if (!createdBy) return "someone";
  const [prefix, rest = ""] = createdBy.split(":");
  if (prefix === "user") {
    const tail = rest.slice(0, 8);
    return tail ? `user ${tail}` : "user";
  }
  return prefix;
}

export function formatRelative(iso: string): string {
  const then = new Date(iso).getTime();
  if (Number.isNaN(then)) return iso;
  const diffMs = Date.now() - then;
  const sec = Math.round(diffMs / 1000);
  if (sec < 45) return "just now";
  const min = Math.round(sec / 60);
  if (min < 60) return `${min} minute${min === 1 ? "" : "s"} ago`;
  const hr = Math.round(min / 60);
  if (hr < 24) return `${hr} hour${hr === 1 ? "" : "s"} ago`;
  const day = Math.round(hr / 24);
  if (day < 30) return `${day} day${day === 1 ? "" : "s"} ago`;
  return new Date(iso).toLocaleDateString();
}

export function formatAbsolute(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString();
}
