import { api } from "./client";

export interface CommentDto {
  id: string;
  createdAt: string;
  createdBy: string;
  bodyMd: string;
}

export const commentsApi = {
  list: (projectKey: string, issueNumber: number) =>
    api<CommentDto[]>(`/api/projects/${projectKey}/issues/${issueNumber}/comments`),
  create: (projectKey: string, issueNumber: number, bodyMd: string) =>
    api<CommentDto>(`/api/projects/${projectKey}/issues/${issueNumber}/comments`, {
      method: "POST",
      json: { bodyMd },
    }),
};
