import { Link } from "react-router-dom";

export function MobileNotSupportedBanner({ projectKey }: { projectKey: string }) {
  return (
    <div className="flex flex-col items-center justify-center gap-4 rounded border border-blue-200 bg-blue-50 px-6 py-8">
      <div className="text-center">
        <p className="mb-2 text-sm font-medium text-blue-900">Kanban board requires a larger screen</p>
        <p className="mb-4 text-sm text-blue-800">
          The kanban board needs at least a tablet-size screen (768px wide). Open this page on a larger device, or use the{" "}
          <Link to={`/projects/${projectKey}`} className="font-medium underline hover:text-blue-700">
            List view
          </Link>
          {" "}which works on phones.
        </p>
      </div>
    </div>
  );
}
