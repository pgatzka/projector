import { api } from "./client";

export type TimelineEntryType = "comment" | "activity";

export interface TimelineEntryDto {
  type: TimelineEntryType;
  id: string;
  createdAt: string;
  createdBy: string;
  bodyMd?: string;
  action?: string;
  payload?: Record<string, unknown>;
}

export interface TimelineDto {
  entries: TimelineEntryDto[];
  total: number;
}

export const timelineApi = {
  get: (projectKey: string, issueNumber: number) =>
    api<TimelineDto>(`/api/projects/${projectKey}/issues/${issueNumber}/timeline`),
};
