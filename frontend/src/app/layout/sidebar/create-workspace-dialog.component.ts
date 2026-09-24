import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { WorkspaceService } from '../../core/services/workspace.service';
import { WorkspaceContextService } from '../../core/state/workspace-context.service';
import { ToastService } from '../../shared/toast/toast.service';
import { ModalComponent } from '../../shared/components/modal.component';
import { IconComponent } from '../../shared/components/icon.component';

@Component({
  selector: 'app-create-workspace-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, ModalComponent, IconComponent],
  template: `
    <app-modal title="Create workspace" (close)="close.emit()">
      <form (ngSubmit)="submit()">
        <label class="field-label" for="ws-name">Workspace name</label>
        <input id="ws-name" class="input" type="text" [(ngModel)]="name" name="name" placeholder="Customer Support" autofocus required />

        <label class="field-label mt-3" for="ws-desc">Description (optional)</label>
        <textarea id="ws-desc" class="input" rows="2" [(ngModel)]="description" name="description" placeholder="What lives in this workspace?"></textarea>

        <div class="mt-4 flex justify-end gap-2">
          <button type="button" class="btn-secondary" (click)="close.emit()">Cancel</button>
          <button type="submit" class="btn-primary" [disabled]="!name.trim() || submitting()">
            @if (submitting()) { <app-icon name="spinner" class="animate-spin" [size]="14" /> }
            Create workspace
          </button>
        </div>
      </form>
    </app-modal>
  `,
})
export class CreateWorkspaceDialogComponent {
  @Output() close = new EventEmitter<void>();
  @Output() created = new EventEmitter<void>();

  private readonly workspaceService = inject(WorkspaceService);
  private readonly workspaceContext = inject(WorkspaceContextService);
  private readonly toast = inject(ToastService);

  name = '';
  description = '';
  readonly submitting = signal(false);

  submit(): void {
    const orgId = this.workspaceContext.currentOrgId();
    if (!this.name.trim() || this.submitting() || !orgId) return;
    this.submitting.set(true);
    this.workspaceService.create(orgId, this.name.trim(), this.description.trim() || null).subscribe({
      next: async (workspace) => {
        await this.workspaceContext.addWorkspace(workspace);
        this.toast.success(`Workspace "${workspace.name}" created`);
        this.submitting.set(false);
        this.created.emit();
      },
      error: () => {
        this.submitting.set(false);
        this.toast.error('Failed to create workspace');
      },
    });
  }
}
