import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ConfirmService } from './confirm.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (confirmService.state(); as state) {
      <div class="fixed inset-0 z-[90] flex items-center justify-center bg-slate-900/30 p-4 animate-fade-in" (click)="confirmService.resolve(false)">
        <div class="w-full max-w-sm panel p-4 animate-fade-in" (click)="$event.stopPropagation()">
          <h3 class="text-sm font-semibold text-slate-900">{{ state.title }}</h3>
          <p class="mt-1.5 text-sm text-slate-600 leading-relaxed">{{ state.message }}</p>
          <div class="mt-4 flex justify-end gap-2">
            <button type="button" class="btn-secondary" (click)="confirmService.resolve(false)">
              {{ state.cancelLabel ?? 'Cancel' }}
            </button>
            <button
              type="button"
              [class]="state.danger ? 'btn bg-red-600 text-white hover:bg-red-700' : 'btn-primary'"
              (click)="confirmService.resolve(true)"
            >
              {{ state.confirmLabel ?? 'Confirm' }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ConfirmDialogComponent {
  readonly confirmService = inject(ConfirmService);
}
