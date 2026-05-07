import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import App from "./App";
import { AuthProvider } from "./features/auth/AuthProvider";
import { AuthGate } from "./features/auth/AuthGate";
import { ToastProvider } from "./components/Toast";
import "./index.css";

const queryClient = new QueryClient();

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <ToastProvider>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <AuthProvider>
            <AuthGate>
              <App />
            </AuthGate>
          </AuthProvider>
        </BrowserRouter>
      </QueryClientProvider>
    </ToastProvider>
  </React.StrictMode>,
);
