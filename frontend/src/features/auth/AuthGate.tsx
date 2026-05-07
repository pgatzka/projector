import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { authApi } from "@/api";
import { useAuth } from "./AuthProvider";
import { stashIntendedRoute } from "@/utils/intendedRoute";

const AUTH_ROUTES = new Set(["/login", "/setup"]);

/**
 * Root-level gate. Probes /api/me + /api/setup-required on mount and:
 *   - keeps the app rendered if logged in
 *   - sends to /setup when no admin exists
 *   - sends to /login otherwise (stashing the deep link in sessionStorage
 *     so LoginForm can restore it post-login)
 */
export function AuthGate({ children }: { children: React.ReactNode }) {
  const { me, isLoading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [setupRequired, setSetupRequired] = useState<boolean | null>(null);

  useEffect(() => {
    let cancelled = false;
    authApi
      .setupRequired()
      .then((res) => { if (!cancelled) setSetupRequired(res.required); })
      .catch(() => { if (!cancelled) setSetupRequired(false); });
    return () => { cancelled = true; };
  }, []);

  useEffect(() => {
    if (isLoading || setupRequired === null) return;
    const onAuthRoute = AUTH_ROUTES.has(location.pathname);

    if (setupRequired) {
      if (location.pathname !== "/setup") {
        navigate("/setup", { replace: true });
      }
      return;
    }

    if (!me && !onAuthRoute) {
      stashIntendedRoute(location.pathname + location.search);
      navigate("/login", { replace: true });
      return;
    }

    if (me && onAuthRoute) {
      navigate("/projects", { replace: true });
    }
  }, [me, isLoading, setupRequired, location.pathname, location.search, navigate]);

  if (isLoading || setupRequired === null) {
    return <p className="p-6 text-slate-500">Loading…</p>;
  }
  return <>{children}</>;
}
