import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { OrganizationService } from '../../core/services/organization.service';
import { WorkspaceContextService } from '../../core/state/workspace-context.service';
import { ToastService } from '../../shared/toast/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';
import { IconComponent } from '../../shared/components/icon.component';

@Component({
  selector: 'app-create-organization-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent, IconComponent],
  template: `
    <app-modal title="Create organization" (close)="close.emit()">
      <form (ngSubmit)="submit()">
        <label class="field-label" for="org-name">Organization name</label>
        <input id="org-name" class="input" type="text" [(ngModel)]="name" name="name" placeholder="Acme Inc." autofocus required />
        <p class="mt-1.5 text-xs text-slate-500">You'll be the owner and can invite teammates afterward.</p>
        <div class="mt-4 flex justify-end gap-2">
          <button type="button" class="btn-secondary" (click)="close.emit()">Cancel</button>
          <button type="submit" class="btn-primary" [disabled]="!name.trim() || submitting()">
            @if (submitting()) { <app-icon name="spinner" class="animate-spin" [size]="14" /> }
            Create organization
          </button>
        </div>
      </form>
    </app-modal>
  `,
})
export class CreateOrganizationDialogComponent {
  @Output() close = new EventEmitter<void>();
  @Output() created = new EventEmitter<void>();

  private readonly organizationService = inject(OrganizationService);
  private readonly workspaceContext = inject(WorkspaceContextService);
  private readonly toast = inject(ToastService);

  name = '';
  readonly submitting = signal(false);

  submit(): void {
    if (!this.name.trim() || this.submitting()) return;
    this.submitting.set(true);
    this.organizationService.create(this.name.trim()).subscribe({
      next: async (org) => {
        await this.workspaceContext.addOrganization(org);
        this.toast.success(`Organization "${org.name}" created`);
        this.submitting.set(false);
        this.created.emit();
      },
      error: () => {
        this.submitting.set(false);
        this.toast.error('Failed to create organization');
      },
    });
  }
}
