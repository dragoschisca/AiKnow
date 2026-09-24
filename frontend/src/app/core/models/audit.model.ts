export type AuditEventType =
  | 'USER_REGISTERED'
  | 'USER_LOGIN'
  | 'USER_LOGOUT'
  | 'ORGANIZATION_CREATED'
  | 'WORKSPACE_CREATED'
  | 'MEMBER_ADDED'
  | 'MEMBER_REMOVED'
  | 'ROLE_CHANGED'
  | 'DOCUMENT_UPLOADED'
  | 'DOCUMENT_DELETED'
  | 'DOCUMENT_REPROCESSED'
  | 'QUESTION_ASKED';

export interface AuditLogEntry {
  id: string;
  organizationId: string | null;
  workspaceId: string | null;
  actorUserId: string | null;
  eventType: string;
  resourceType: string | null;
  resourceId: string | null;
  details: Record<string, unknown> | null;
  ipAddress: string | null;
  createdAt: string;
}
