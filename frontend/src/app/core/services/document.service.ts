import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.model';
import { KnowledgeDocument } from '../models/document.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  constructor(private readonly http: HttpClient) {}

  private base(orgId: string, workspaceId: string): string {
    return `${environment.apiUrl}/organizations/${orgId}/workspaces/${workspaceId}/documents`;
  }

  list(orgId: string, workspaceId: string): Observable<KnowledgeDocument[]> {
    return this.http.get<ApiResponse<KnowledgeDocument[]>>(this.base(orgId, workspaceId)).pipe(map((r) => r.data));
  }

  get(orgId: string, workspaceId: string, documentId: string): Observable<KnowledgeDocument> {
    return this.http
      .get<ApiResponse<KnowledgeDocument>>(`${this.base(orgId, workspaceId)}/${documentId}`)
      .pipe(map((r) => r.data));
  }

  upload(orgId: string, workspaceId: string, file: File): Observable<HttpEvent<ApiResponse<KnowledgeDocument>>> {
    const formData = new FormData();
    formData.append('file', file);
    const req = new HttpRequest('POST', this.base(orgId, workspaceId), formData, {
      reportProgress: true,
    });
    return this.http.request<ApiResponse<KnowledgeDocument>>(req);
  }

  delete(orgId: string, workspaceId: string, documentId: string): Observable<void> {
    return this.http.delete<void>(`${this.base(orgId, workspaceId)}/${documentId}`);
  }

  reprocess(orgId: string, workspaceId: string, documentId: string): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.base(orgId, workspaceId)}/${documentId}/reprocess`, {})
      .pipe(map(() => undefined));
  }
}
