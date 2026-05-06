import { api } from "./client";

export interface Project {
  id: string;
  key: string;
  name: string;
  description: string | null;
  nextIssueNumber: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProjectReq {
  key: string;
  name: string;
  description?: string;
}

export interface UpdateProjectReq {
  name?: string;
  description?: string;
}

export const projectsApi = {
  list: () => api<Project[]>("/api/projects"),
  get: (key: string) => api<Project>(`/api/projects/${key}`),
  create: (req: CreateProjectReq) => api<Project>("/api/projects", { method: "POST", json: req }),
  update: (key: string, req: UpdateProjectReq) => api<Project>(`/api/projects/${key}`, { method: "PATCH", json: req }),
  delete: (key: string) => api<void>(`/api/projects/${key}`, { method: "DELETE" }),
};
