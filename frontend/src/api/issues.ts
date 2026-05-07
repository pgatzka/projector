import { api } from "./client";
import type { Label } from "./labels";

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
  labels: Label[];
  createdAt: string;
  updatedAt: string;
}

export interface Page<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

export interface IssueListQuery {
  status?: IssueStatus[];
  priority?: IssuePriority[];
  label?: string[];
  q?: string;
  page?: number;
  size?: number;
}

export interface CreateIssueReq {
  title: string;
  descriptionMd?: string;
  status?: IssueStatus;
  priority?: IssuePriority;
  dueDate?: string;
  labelIds?: string[];
}

export interface UpdateIssueReq {
  title?: string;
  descriptionMd?: string;
  status?: IssueStatus;
  priority?: IssuePriority;
  dueDate?: string;
}

function buildIssueListQuery(query?: IssueListQuery): string {
  if (!query) return "";
  const params = new URLSearchParams();
  if (query.status && query.status.length > 0) params.set("status", query.status.join(","));
  if (query.priority && query.priority.length > 0) params.set("priority", query.priority.join(","));
  if (query.label && query.label.length > 0) params.set("label", query.label.join(","));
  if (query.q !== undefined && query.q !== "") params.set("q", query.q);
  if (query.page !== undefined) params.set("page", String(query.page));
  if (query.size !== undefined) params.set("size", String(query.size));
  const s = params.toString();
  return s ? `?${s}` : "";
}

export const issuesApi = {
  list: (projectKey: string, query?: IssueListQuery) =>
    api<Page<Issue>>(`/api/projects/${projectKey}/issues${buildIssueListQuery(query)}`),
  get: (projectKey: string, number: number) =>
    api<Issue>(`/api/projects/${projectKey}/issues/${number}`),
  create: (projectKey: string, req: CreateIssueReq) =>
    api<Issue>(`/api/projects/${projectKey}/issues`, { method: "POST", json: req }),
  update: (projectKey: string, number: number, req: UpdateIssueReq) =>
    api<Issue>(`/api/projects/${projectKey}/issues/${number}`, { method: "PATCH", json: req }),
  delete: (projectKey: string, number: number) =>
    api<void>(`/api/projects/${projectKey}/issues/${number}`, { method: "DELETE" }),
};
