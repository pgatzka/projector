import { authApi } from "@/api";
import { useAuth } from "./AuthProvider";
import { useNavigate } from "react-router-dom";

export function LogoutButton() {
  const { refresh } = useAuth();
  const navigate = useNavigate();
  return (
    <button
      onClick={async () => {
        await authApi.logout();
        await refresh();
        navigate("/login", { replace: true });
      }}
      className="rounded border border-slate-300 bg-white px-3 py-1.5 text-sm hover:bg-slate-50"
    >
      Log out
    </button>
  );
}
