import { CommonModule } from '@angular/common';
import { Component, effect, inject, signal } from '@angular/core';
import { OrganizationService } from '../../core/services/organization.service';
import { UsageSummary } from '../../core/models/organization.model';
import { WorkspaceContextService } from '../../core/state/workspace-context.service';
import { ToastService } from '../../shared/toast/toast.service';
import { IconComponent } from '../../shared/components/icon.component';

@Component({
  selector: 'app-usage',
  standalone: true,
  imports: [CommonModule, IconComponent],
  templateUrl: './usage.component.html',
})
export class UsageComponent {
  private readonly organizationService = inject(OrganizationService);
  readonly workspaceContext = inject(WorkspaceContextService);
  private readonly toast = inject(ToastService);

  readonly usage = signal<UsageSummary | null>(null);
  readonly loading = signal(true);

  constructor() {
    effect(() => {
      const orgId = this.workspaceContext.currentOrgId();
      if (orgId) this.load(orgId);
    });
  }

  private load(orgId: string): void {
    this.loading.set(true);
    this.organizationService.getUsage(orgId).subscribe({
      next: (usage) => {
        this.usage.set(usage);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Failed to load usage');
      },
    });
  }

  private readonly INT_MAX = 2147483647;

  /** The ENTERPRISE plan uses Integer.MAX_VALUE / Long.MAX_VALUE server-side to mean "unlimited". */
  private isUnlimited(max: number): boolean {
    return max >= this.INT_MAX || max > Number.MAX_SAFE_INTEGER;
  }

  percent(used: number, max: number): number {
    if (max <= 0 || this.isUnlimited(max)) return 0;
    return Math.min(100, Math.round((used / max) * 100));
  }

  formatBytes(bytes: number): string {
    if (this.isUnlimited(bytes)) return 'Unlimited';
    if (bytes < 1024) return `${bytes} B`;
    const units = ['KB', 'MB', 'GB', 'TB'];
    let value = bytes / 1024;
    let unitIndex = 0;
    while (value >= 1024 && unitIndex < units.length - 1) {
      value /= 1024;
      unitIndex++;
    }
    return `${value.toFixed(value < 10 ? 1 : 0)} ${units[unitIndex]}`;
  }

  formatCount(count: number): string {
    return this.isUnlimited(count) ? 'Unlimited' : count.toLocaleString();
  }

  barClass(pct: number): string {
    if (pct >= 100) return 'bg-red-500';
    if (pct >= 80) return 'bg-amber-500';
    return 'bg-brand-600';
  }
}
