import { useAuth } from "@/features/auth/AuthProvider";

export function Home() {
  const { me } = useAuth();
  return (
    <section className="max-w-xl rounded border border-slate-200 bg-white p-6">
      <h2 className="mb-2 text-lg font-medium">You're signed in</h2>
      <p className="text-sm text-slate-600">
        Hi {me?.displayName}. v0.3 will add Projects and Issues here.
      </p>
    </section>
  );
}
