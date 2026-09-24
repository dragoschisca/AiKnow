export type DocumentStatus = 'UPLOADED' | 'PROCESSING' | 'READY' | 'FAILED' | 'ARCHIVED';

export interface KnowledgeDocument {
  id: string;
  organizationId: string;
  workspaceId: string;
  name: string;
  originalFilename: string;
  fileSize: number;
  mimeType: string;
  status: DocumentStatus;
  errorMessage: string | null;
  version: number;
  uploadedBy: string;
  chunkCount: number;
  createdAt: string;
  updatedAt: string;
}
