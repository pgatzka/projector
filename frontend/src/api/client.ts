/**
 * Single fetch wrapper for the Projector API.
 *  - Sends JSON, parses JSON.
 *  - Forwards XSRF-TOKEN cookie as X-XSRF-TOKEN header on state-changing requests.
 *  - On 401, redirects to /login?next=<current-path> (skipping during the /login call itself).
 */

const STATE_CHANGING = new Set(["POST", "PUT", "PATCH", "DELETE"]);

function readCookie(name: string): string | undefined {
  const match = document.cookie.match(new RegExp("(?:^|; )" + name + "=([^;]*)"));
  return match ? decodeURIComponent(match[1]) : undefined;
}

export class ApiError extends Error {
  constructor(public status: number, public detail: string, public body?: unknown) {
    super(`[${status}] ${detail}`);
  }
}

export async function api<T = unknown>(
  path: string,
  init: RequestInit & { json?: unknown; skipAuthRedirect?: boolean } = {},
): Promise<T> {
  const method = (init.method ?? "GET").toUpperCase();
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");

  if (init.json !== undefined) {
    headers.set("Content-Type", "application/json");
    init.body = JSON.stringify(init.json);
  }

  if (STATE_CHANGING.has(method)) {
    const token = readCookie("XSRF-TOKEN");
    if (token) headers.set("X-XSRF-TOKEN", token);
  }

  const res = await fetch(path, { ...init, headers, credentials: "same-origin" });

  if (res.status === 401 && !init.skipAuthRedirect) {
    const next = encodeURIComponent(window.location.pathname + window.location.search);
    window.location.assign(`/login?next=${next}`);
    throw new ApiError(401, "Unauthenticated; redirecting to /login");
  }

  if (res.status === 204) return undefined as T;

  const text = await res.text();
  const body = text ? JSON.parse(text) : undefined;
  if (!res.ok) {
    const detail = (body as { detail?: string } | undefined)?.detail ?? res.statusText;
    throw new ApiError(res.status, detail, body);
  }
  return body as T;
}
