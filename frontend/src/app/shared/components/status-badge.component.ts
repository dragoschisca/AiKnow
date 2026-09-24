import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { DocumentStatus } from '../../core/models/document.model';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="badge" [ngClass]="classesFor(status)">
      @if (status === 'PROCESSING') {
        <svg class="h-2.5 w-2.5 animate-spin" viewBox="0 0 24 24" fill="none">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3"/>
          <path class="opacity-90" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.4 0 0 5.4 0 12h4z"/>
        </svg>
      } @else {
        <span class="h-1.5 w-1.5 rounded-full" [ngClass]="dotClassFor(status)"></span>
      }
      {{ label }}
    </span>
  `,
})
export class StatusBadgeComponent {
  @Input({ required: true }) status!: DocumentStatus;

  get label(): string {
    switch (this.status) {
      case 'UPLOADED': return 'Uploaded';
      case 'PROCESSING': return 'Processing';
      case 'READY': return 'Ready';
      case 'FAILED': return 'Failed';
      case 'ARCHIVED': return 'Archived';
      default: return this.status;
    }
  }

  classesFor(status: DocumentStatus): string {
    switch (status) {
      case 'READY': return 'badge-green';
      case 'PROCESSING': return 'badge-blue';
      case 'UPLOADED': return 'badge-slate';
      case 'FAILED': return 'badge-red';
      case 'ARCHIVED': return 'badge-slate';
      default: return 'badge-slate';
    }
  }

  dotClassFor(status: DocumentStatus): string {
    switch (status) {
      case 'READY': return 'bg-emerald-500';
      case 'UPLOADED': return 'bg-slate-400';
      case 'FAILED': return 'bg-red-500';
      case 'ARCHIVED': return 'bg-slate-400';
      default: return 'bg-slate-400';
    }
  }
}
