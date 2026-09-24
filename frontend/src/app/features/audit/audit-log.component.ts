import { CommonModule } from '@angular/common';
import { Component, effect, inject, signal } from '@angular/core';
import { AuditService } from '../../core/services/audit.service';
import { AuditEventType, AuditLogEntry } from '../../core/models/audit.model';
import { WorkspaceContextService } from '../../core/state/workspace-context.service';
import { ToastService } from '../../shared/toast/toast.service';
import { IconComponent } from '../../shared/components/icon.component';

const PAGE_SIZE = 25;

const EVENT_TYPES: AuditEventType[] = [
  'USER_REGISTERED', 'USER_LOGIN', 'USER_LOGOUT', 'ORGANIZATION_CREATED', 'WORKSPACE_CREATED',
  'MEMBER_ADDED', 'MEMBER_REMOVED', 'ROLE_CHANGED', 'DOCUMENT_UPLOADED', 'DOCUMENT_DELETED',
  'DOCUMENT_REPROCESSED', 'QUESTION_ASKED',
];

@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [CommonModule, IconComponent],
  templateUrl: './audit-log.component.html',
})
export class AuditLogComponent {
  private readonly auditService = inject(AuditService);
  readonly workspaceContext = inject(WorkspaceContextService);
  private readonly toast = inject(ToastService);

  readonly entries = signal<AuditLogEntry[]>([]);
  readonly loading = signal(true);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly eventTypeFilter = signal<AuditEventType | null>(null);
  readonly eventTypes = EVENT_TYPES;

  constructor() {
    effect(() => {
      const orgId = this.workspaceContext.currentOrgId();
      // Track filter/page changes too by reading them inside the effect.
      const page = this.page();
      const filter = this.eventTypeFilter();
      if (orgId) {
        this.load(orgId, page, filter);
      }
    });
  }

  private load(orgId: string, page: number, filter: AuditEventType | null): void {
    this.loading.set(true);
    this.auditService.list(orgId, page, PAGE_SIZE, filter).subscribe({
      next: (result) => {
        this.entries.set(result.content);
        this.totalPages.set(result.totalPages);
        this.totalElements.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Failed to load audit logs');
      },
    });
  }

  onFilterChange(value: string): void {
    this.eventTypeFilter.set((value || null) as AuditEventType | null);
    this.page.set(0);
  }

  prevPage(): void {
    if (this.page() > 0) this.page.update((p) => p - 1);
  }

  nextPage(): void {
    if (this.page() + 1 < this.totalPages()) this.page.update((p) => p + 1);
  }

  eventBadgeClass(eventType: string): string {
    if (eventType.startsWith('DOCUMENT_DELETED') || eventType === 'MEMBER_REMOVED') return 'badge-red';
    if (eventType.startsWith('DOCUMENT') || eventType === 'QUESTION_ASKED') return 'badge-blue';
    if (eventType.includes('CREATED') || eventType === 'MEMBER_ADDED') return 'badge-green';
    if (eventType === 'ROLE_CHANGED') return 'badge-amber';
    return 'badge-slate';
  }

  formatEventType(eventType: string): string {
    return eventType.replace(/_/g, ' ').toLowerCase().replace(/^./, (c) => c.toUpperCase());
  }

  formatDetails(details: Record<string, unknown> | null): string {
    if (!details) return '—';
    return Object.entries(details)
      .map(([k, v]) => `${k}: ${v}`)
      .join(', ');
  }
}
