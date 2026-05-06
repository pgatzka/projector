import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi, ApiError } from "@/api";
import { useAuth } from "./AuthProvider";

export function SetupPage() {
  const navigate = useNavigate();
  const { refresh } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await authApi.setup({ email, password, displayName });
      await authApi.login({ email, password });
      await refresh();
      navigate("/", { replace: true });
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setError("Setup has already been completed. Try logging in instead.");
      } else if (err instanceof ApiError) {
        setError(err.detail);
      } else {
        setError("Setup failed. Please try again.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto mt-16 max-w-md rounded border border-slate-200 bg-white p-6 shadow-sm">
      <h1 className="mb-1 text-2xl font-semibold">Welcome to Projector</h1>
      <p className="mb-6 text-sm text-slate-500">Create your admin account to get started.</p>
      <form onSubmit={onSubmit} className="space-y-4">
        <Field label="Display name" value={displayName} onChange={setDisplayName} type="text" autoComplete="name" required />
        <Field label="Email" value={email} onChange={setEmail} type="email" autoComplete="email" required />
        <Field label="Password" value={password} onChange={setPassword} type="password" autoComplete="new-password" minLength={8} required />
        {error && <p className="text-sm text-red-600">{error}</p>}
        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-50"
        >
          {submitting ? "Creating…" : "Create admin account"}
        </button>
      </form>
    </div>
  );
}

function Field(props: {
  label: string; value: string; onChange: (v: string) => void;
  type: string; autoComplete?: string; required?: boolean; minLength?: number;
}) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium text-slate-700">{props.label}</span>
      <input
        className="w-full rounded border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
        value={props.value}
        onChange={(e) => props.onChange(e.target.value)}
        type={props.type}
        autoComplete={props.autoComplete}
        required={props.required}
        minLength={props.minLength}
      />
    </label>
  );
}
