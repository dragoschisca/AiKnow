import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PagedResponse } from '../models/api.model';
import { AuditEventType, AuditLogEntry } from '../models/audit.model';

@Injectable({ providedIn: 'root' })
export class AuditService {
  constructor(private readonly http: HttpClient) {}

  list(orgId: string, page: number, size: number, eventType?: AuditEventType | null): Observable<PagedResponse<AuditLogEntry>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (eventType) {
      params = params.set('eventType', eventType);
    }
    return this.http
      .get<ApiResponse<PagedResponse<AuditLogEntry>>>(`${environment.apiUrl}/organizations/${orgId}/audit-logs`, { params })
      .pipe(map((r) => r.data));
  }
}
