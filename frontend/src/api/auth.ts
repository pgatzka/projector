import { api } from "./client";

export interface Me {
  id: string;
  email: string;
  displayName: string;
  lastLoginAt: string | null;
}

export interface SetupReq {
  email: string;
  password: string;
  displayName: string;
}

export interface LoginReq {
  email: string;
  password: string;
}

export const authApi = {
  me: () => api<Me>("/api/me", { skipAuthRedirect: true }),
  setupRequired: () => api<{ required: boolean }>("/api/setup-required", { skipAuthRedirect: true }),
  setup: (req: SetupReq) => api<Me>("/api/setup", { method: "POST", json: req, skipAuthRedirect: true }),
  login: (req: LoginReq) => api<Me>("/api/login", { method: "POST", json: req, skipAuthRedirect: true }),
  logout: () => api<void>("/api/logout", { method: "POST", json: {}, skipAuthRedirect: true }),
};
