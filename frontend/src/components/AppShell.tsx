import { useAuth } from "@/features/auth/AuthProvider";
import { LogoutButton } from "@/features/auth/LogoutButton";

export function AppShell({ children }: { children: React.ReactNode }) {
  const { me } = useAuth();
  return (
    <div className="min-h-full bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white px-6 py-4 flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold">Projector</h1>
          <p className="text-sm text-slate-500">v0.2 auth</p>
        </div>
        {me && (
          <div className="flex items-center gap-3">
            <span className="text-sm text-slate-600">{me.displayName}</span>
            <LogoutButton />
          </div>
        )}
      </header>
      <main className="px-6 py-8">{children}</main>
    </div>
  );
}
