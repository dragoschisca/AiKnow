import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.model';
import { Organization, OrganizationMemberRole, UsageSummary } from '../models/organization.model';

@Injectable({ providedIn: 'root' })
export class OrganizationService {
  private readonly baseUrl = `${environment.apiUrl}/organizations`;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<Organization[]> {
    return this.http.get<ApiResponse<Organization[]>>(this.baseUrl).pipe(map((r) => r.data));
  }

  get(orgId: string): Observable<Organization> {
    return this.http.get<ApiResponse<Organization>>(`${this.baseUrl}/${orgId}`).pipe(map((r) => r.data));
  }

  create(name: string): Observable<Organization> {
    return this.http.post<ApiResponse<Organization>>(this.baseUrl, { name }).pipe(map((r) => r.data));
  }

  addMember(orgId: string, userId: string, role: OrganizationMemberRole): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/${orgId}/members`, { userId, role }).pipe(map(() => undefined));
  }

  removeMember(orgId: string, userId: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${orgId}/members/${userId}`).pipe(map(() => undefined));
  }

  changeMemberRole(orgId: string, userId: string, role: OrganizationMemberRole): Observable<void> {
    return this.http
      .patch<ApiResponse<void>>(`${this.baseUrl}/${orgId}/members/${userId}/role`, { role })
      .pipe(map(() => undefined));
  }

  getUsage(orgId: string): Observable<UsageSummary> {
    return this.http.get<ApiResponse<UsageSummary>>(`${this.baseUrl}/${orgId}/usage`).pipe(map((r) => r.data));
  }
}
