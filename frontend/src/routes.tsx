import { Routes, Route, Navigate } from "react-router-dom";
import { LoginPage } from "@/features/auth/LoginPage";
import { SetupPage } from "@/features/auth/SetupPage";
import { SetupGate } from "@/features/auth/SetupGate";
import { RequireAuth } from "@/features/auth/RequireAuth";
import { AppShell } from "@/components/AppShell";
import { Home } from "@/features/home/Home";

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
                <Route path="/" element={<Home />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </AppShell>
          </RequireAuth>
        }
      />
    </Routes>
  );
}
