import { useQuery } from "@tanstack/react-query";

interface Health {
  status: string;
}

async function fetchHealth(): Promise<Health> {
  const res = await fetch("/api/health");
  if (!res.ok) throw new Error(`health check failed: ${res.status}`);
  return res.json();
}

export default function App() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["health"],
    queryFn: fetchHealth,
  });

  return (
    <div className="min-h-full bg-slate-50 text-slate-900">
      <header className="border-b border-slate-200 bg-white px-6 py-4">
        <h1 className="text-xl font-semibold">Projector</h1>
        <p className="text-sm text-slate-500">v0.1 skeleton</p>
      </header>
      <main className="px-6 py-8">
        <section className="max-w-md rounded border border-slate-200 bg-white p-4">
          <h2 className="mb-2 text-sm font-medium text-slate-500">Backend health</h2>
          {isLoading && <p>Checking…</p>}
          {isError && <p className="text-red-600">Error: {(error as Error).message}</p>}
          {data && (
            <p className="font-mono text-lg">
              status: <span className="text-emerald-600">{data.status}</span>
            </p>
          )}
        </section>
      </main>
    </div>
  );
}
