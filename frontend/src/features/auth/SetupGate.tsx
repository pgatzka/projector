import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { authApi, ApiError } from "@/api";

/**
 * On first render, ping /api/setup with an empty POST to detect 409 (already set up)
 * vs 400 (validation error means setup is still open).
 *
 *   - 400 (validation error) → setup is OPEN; send /login → /setup, leave /setup alone.
 *   - 409 (conflict)         → setup is CLOSED; send /setup → /login, leave /login alone.
 */
export function SetupGate({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        await authApi.setup({ email: "", password: "", displayName: "" });
      } catch (err) {
        if (cancelled) return;
        if (err instanceof ApiError && err.status === 409) {
          if (location.pathname === "/setup") navigate("/login", { replace: true });
        } else if (err instanceof ApiError && (err.status === 400 || err.status === 422)) {
          if (location.pathname === "/login") navigate("/setup", { replace: true });
        }
      } finally {
        if (!cancelled) setChecked(true);
      }
    })();
    return () => { cancelled = true; };
  }, [location.pathname, navigate]);

  if (!checked) return <p className="p-6 text-slate-500">Loading…</p>;
  return <>{children}</>;
}
