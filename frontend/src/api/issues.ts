import { api } from "./client";

export type IssueStatus = "backlog" | "todo" | "in_progress" | "done" | "cancelled";
export type IssuePriority = "low" | "medium" | "high" | "urgent";

export interface Issue {
  id: string;
  projectId: string;
  projectKey: string;
  number: number;
  identifier: string;
  title: string;
  descriptionMd: string | null;
  status: IssueStatus;
  priority: IssuePriority;
  dueDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateIssueReq {
  title: string;
  descriptionMd?: string;
  status?: IssueStatus;
  priority?: IssuePriority;
  dueDate?: string;
}

export interface UpdateIssueReq {
  title?: string;
  descriptionMd?: string;
  status?: IssueStatus;
  priority?: IssuePriority;
  dueDate?: string;
}

export const issuesApi = {
  list: (projectKey: string) => api<Issue[]>(`/api/projects/${projectKey}/issues`),
  get: (projectKey: string, number: number) => api<Issue>(`/api/projects/${projectKey}/issues/${number}`),
  create: (projectKey: string, req: CreateIssueReq) =>
    api<Issue>(`/api/projects/${projectKey}/issues`, { method: "POST", json: req }),
  update: (projectKey: string, number: number, req: UpdateIssueReq) =>
    api<Issue>(`/api/projects/${projectKey}/issues/${number}`, { method: "PATCH", json: req }),
  delete: (projectKey: string, number: number) =>
    api<void>(`/api/projects/${projectKey}/issues/${number}`, { method: "DELETE" }),
};
