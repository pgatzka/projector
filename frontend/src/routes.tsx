import { Routes, Route, Navigate } from "react-router-dom";
import { LoginPage } from "@/features/auth/LoginPage";
import { SetupPage } from "@/features/auth/SetupPage";
import { SetupGate } from "@/features/auth/SetupGate";
import { RequireAuth } from "@/features/auth/RequireAuth";
import { AppShell } from "@/components/AppShell";
import { ProjectList } from "@/features/projects/ProjectList";
import { ProjectForm } from "@/features/projects/ProjectForm";
import { ProjectHome } from "@/features/projects/ProjectHome";
import { IssueDetail } from "@/features/issues/IssueDetail";
import { IssueForm } from "@/features/issues/IssueForm";
import { LabelsPage } from "@/features/labels/LabelsPage";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/setup" element={<SetupGate><SetupPage /></SetupGate>} />
      <Route path="/login" element={<SetupGate><LoginPage /></SetupGate>} />
      <Route
        path="/*"
        element={
          <RequireAuth>
            <AppShell>
              <Routes>
                <Route path="/" element={<Navigate to="/projects" replace />} />
                <Route path="/projects" element={<ProjectList />} />
                <Route path="/projects/new" element={<ProjectForm mode="create" />} />
                <Route path="/projects/:key" element={<ProjectHome />} />
                <Route path="/projects/:key/edit" element={<ProjectForm mode="edit" />} />
                <Route path="/projects/:key/labels" element={<LabelsPage />} />
                <Route path="/projects/:key/issues/new" element={<IssueForm mode="create" />} />
                <Route path="/projects/:key/issues/:number" element={<IssueDetail />} />
                <Route path="/projects/:key/issues/:number/edit" element={<IssueForm mode="edit" />} />
                <Route path="*" element={<Navigate to="/projects" replace />} />
              </Routes>
            </AppShell>
          </RequireAuth>
        }
      />
    </Routes>
  );
}
