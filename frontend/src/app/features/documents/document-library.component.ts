import { CommonModule } from '@angular/common';
import { HttpErrorResponse, HttpEventType } from '@angular/common/http';
import { Component, DestroyRef, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { interval } from 'rxjs';
import { DocumentService } from '../../core/services/document.service';
import { KnowledgeDocument } from '../../core/models/document.model';
import { WorkspaceContextService } from '../../core/state/workspace-context.service';
import { ConfirmService } from '../../shared/confirm/confirm.service';
import { ToastService } from '../../shared/toast/toast.service';
import { IconComponent } from '../../shared/components/icon.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';

interface UploadTask {
  id: number;
  fileName: string;
  fileSize: number;
  progress: number;
  error: string | null;
}

let nextUploadId = 1;
const POLL_INTERVAL_MS = 3000;
const UNSETTLED_STATUSES = new Set(['UPLOADED', 'PROCESSING']);

@Component({
  selector: 'app-document-library',
  standalone: true,
  imports: [CommonModule, IconComponent, StatusBadgeComponent],
  templateUrl: './document-library.component.html',
})
export class DocumentLibraryComponent {
  private readonly documentService = inject(DocumentService);
  readonly workspaceContext = inject(WorkspaceContextService);
  private readonly confirmService = inject(ConfirmService);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly route = inject(ActivatedRoute);

  readonly documents = signal<KnowledgeDocument[]>([]);
  readonly loading = signal(true);
  readonly isDragging = signal(false);
  readonly uploads = signal<UploadTask[]>([]);
  readonly highlightedDocId = signal<string | null>(null);

  private dragCounter = 0;

  constructor() {
    this.route.queryParamMap.subscribe((params) => {
      const docId = params.get('doc');
      if (docId) {
        this.highlightedDocId.set(docId);
        setTimeout(() => {
          document.getElementById(`doc-row-${docId}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }, 200);
        setTimeout(() => this.highlightedDocId.set(null), 3000);
      }
    });

    effect(() => {
      const orgId = this.workspaceContext.currentOrgId();
      const workspaceId = this.workspaceContext.currentWorkspaceId();
      if (orgId && workspaceId) {
        this.loadDocuments(orgId, workspaceId);
      }
    });

    interval(POLL_INTERVAL_MS)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        const orgId = this.workspaceContext.currentOrgId();
        const workspaceId = this.workspaceContext.currentWorkspaceId();
        if (orgId && workspaceId && this.documents().some((d) => UNSETTLED_STATUSES.has(d.status))) {
          this.loadDocuments(orgId, workspaceId, { silent: true });
        }
      });
  }

  private loadDocuments(orgId: string, workspaceId: string, opts: { silent?: boolean } = {}): void {
    if (!opts.silent) this.loading.set(true);
    this.documentService.list(orgId, workspaceId).subscribe({
      next: (docs) => {
        this.documents.set([...docs].sort((a, b) => b.createdAt.localeCompare(a.createdAt)));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        if (!opts.silent) this.toast.error('Failed to load documents');
      },
    });
  }

  onFileInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.uploadFiles(Array.from(input.files));
    }
    input.value = '';
  }

  onDragEnter(event: DragEvent): void {
    event.preventDefault();
    this.dragCounter++;
    this.isDragging.set(true);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.dragCounter = Math.max(0, this.dragCounter - 1);
    if (this.dragCounter === 0) this.isDragging.set(false);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragCounter = 0;
    this.isDragging.set(false);
    const files = event.dataTransfer?.files;
    if (files?.length) {
      this.uploadFiles(Array.from(files));
    }
  }

  private uploadFiles(files: File[]): void {
    const orgId = this.workspaceContext.currentOrgId();
    const workspaceId = this.workspaceContext.currentWorkspaceId();
    if (!orgId || !workspaceId) return;

    for (const file of files) {
      if (file.type !== 'application/pdf') {
        this.toast.error(`"${file.name}" is not a PDF and was skipped`);
        continue;
      }

      const task: UploadTask = { id: nextUploadId++, fileName: file.name, fileSize: file.size, progress: 0, error: null };
      this.uploads.update((list) => [...list, task]);

      this.documentService.upload(orgId, workspaceId, file).subscribe({
        next: (event) => {
          if (event.type === HttpEventType.UploadProgress && event.total) {
            const progress = Math.round((event.loaded / event.total) * 100);
            this.updateTask(task.id, { progress });
          } else if (event.type === HttpEventType.Response) {
            this.removeTask(task.id);
            this.toast.success(`"${file.name}" uploaded — processing started`);
            this.loadDocuments(orgId, workspaceId, { silent: true });
          }
        },
        error: (err: HttpErrorResponse) => {
          const message = err.error?.error ?? 'Upload failed';
          this.updateTask(task.id, { error: message });
          this.toast.error(`"${file.name}": ${message}`);
          setTimeout(() => this.removeTask(task.id), 4000);
        },
      });
    }
  }

  private updateTask(id: number, patch: Partial<UploadTask>): void {
    this.uploads.update((list) => list.map((t) => (t.id === id ? { ...t, ...patch } : t)));
  }

  private removeTask(id: number): void {
    this.uploads.update((list) => list.filter((t) => t.id !== id));
  }

  async deleteDocument(doc: KnowledgeDocument): Promise<void> {
    const confirmed = await this.confirmService.ask({
      title: 'Delete document',
      message: `Delete "${doc.name}"? This removes all of its indexed chunks and cannot be undone.`,
      confirmLabel: 'Delete',
      danger: true,
    });
    if (!confirmed) return;

    this.documentService.delete(doc.organizationId, doc.workspaceId, doc.id).subscribe({
      next: () => {
        this.documents.update((list) => list.filter((d) => d.id !== doc.id));
        this.toast.success('Document deleted');
      },
      error: () => this.toast.error('Failed to delete document'),
    });
  }

  reprocessDocument(doc: KnowledgeDocument): void {
    this.documentService.reprocess(doc.organizationId, doc.workspaceId, doc.id).subscribe({
      next: () => {
        this.toast.info('Reprocessing started');
        this.loadDocuments(doc.organizationId, doc.workspaceId, { silent: true });
      },
      error: () => this.toast.error('Failed to reprocess document'),
    });
  }

  formatBytes(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    const units = ['KB', 'MB', 'GB'];
    let value = bytes / 1024;
    let unitIndex = 0;
    while (value >= 1024 && unitIndex < units.length - 1) {
      value /= 1024;
      unitIndex++;
    }
    return `${value.toFixed(value < 10 ? 1 : 0)} ${units[unitIndex]}`;
  }
}
