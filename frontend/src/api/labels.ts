import { api } from "./client";

export type LabelColor =
  | "gray" | "red" | "orange" | "yellow" | "green" | "teal"
  | "blue" | "indigo" | "violet" | "pink" | "brown" | "slate";

export const LABEL_COLORS: LabelColor[] = [
  "gray", "red", "orange", "yellow", "green", "teal",
  "blue", "indigo", "violet", "pink", "brown", "slate",
];

export interface Label {
  id: string;
  projectId: string;
  name: string;
  color: LabelColor;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLabelReq {
  name: string;
  color: LabelColor;
}

export interface UpdateLabelReq {
  name?: string;
  color?: LabelColor;
}

export const labelsApi = {
  list: (projectKey: string) =>
    api<Label[]>(`/api/projects/${projectKey}/labels`),
  create: (projectKey: string, req: CreateLabelReq) =>
    api<Label>(`/api/projects/${projectKey}/labels`, { method: "POST", json: req }),
  update: (projectKey: string, id: string, req: UpdateLabelReq) =>
    api<Label>(`/api/projects/${projectKey}/labels/${id}`, { method: "PATCH", json: req }),
  delete: (projectKey: string, id: string) =>
    api<void>(`/api/projects/${projectKey}/labels/${id}`, { method: "DELETE" }),
  assign: (projectKey: string, issueNumber: number, labelId: string) =>
    api<unknown>(`/api/projects/${projectKey}/issues/${issueNumber}/labels`, {
      method: "POST",
      json: { labelId },
    }),
  unassign: (projectKey: string, issueNumber: number, labelId: string) =>
    api<void>(`/api/projects/${projectKey}/issues/${issueNumber}/labels/${labelId}`, {
      method: "DELETE",
    }),
};
