import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.model';
import { OrganizationMemberRole } from '../models/organization.model';
import { Workspace } from '../models/workspace.model';

@Injectable({ providedIn: 'root' })
export class WorkspaceService {
  constructor(private readonly http: HttpClient) {}

  private base(orgId: string): string {
    return `${environment.apiUrl}/organizations/${orgId}/workspaces`;
  }

  list(orgId: string): Observable<Workspace[]> {
    return this.http.get<ApiResponse<Workspace[]>>(this.base(orgId)).pipe(map((r) => r.data));
  }

  get(orgId: string, workspaceId: string): Observable<Workspace> {
    return this.http.get<ApiResponse<Workspace>>(`${this.base(orgId)}/${workspaceId}`).pipe(map((r) => r.data));
  }

  create(orgId: string, name: string, description: string | null): Observable<Workspace> {
    return this.http.post<ApiResponse<Workspace>>(this.base(orgId), { name, description }).pipe(map((r) => r.data));
  }

  addMember(orgId: string, workspaceId: string, userId: string, role: OrganizationMemberRole): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.base(orgId)}/${workspaceId}/members`, { userId, role })
      .pipe(map(() => undefined));
  }
}
