import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed bottom-4 right-4 z-[100] flex w-80 flex-col gap-2">
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="animate-slide-in flex items-start gap-2.5 rounded-lg border px-3 py-2.5 shadow-popover text-sm"
          [ngClass]="{
            'bg-white border-slate-200 text-slate-700': toast.variant === 'info',
            'bg-emerald-50 border-emerald-200 text-emerald-800': toast.variant === 'success',
            'bg-red-50 border-red-200 text-red-800': toast.variant === 'error'
          }"
        >
          <span class="mt-0.5 shrink-0">
            @switch (toast.variant) {
              @case ('success') {
                <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M16.7 5.3a1 1 0 010 1.4l-7.4 7.4a1 1 0 01-1.4 0L3.3 9.5a1 1 0 111.4-1.4l3.9 3.9 6.7-6.7a1 1 0 011.4 0z" clip-rule="evenodd"/></svg>
              }
              @case ('error') {
                <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm-1-9a1 1 0 112 0v3a1 1 0 11-2 0V9zm1-4a1.1 1.1 0 100 2.2A1.1 1.1 0 0010 5z" clip-rule="evenodd"/></svg>
              }
              @default {
                <svg class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-4a1 1 0 100 2 1 1 0 000-2zm1 4a1 1 0 10-2 0v4a1 1 0 102 0v-4z" clip-rule="evenodd"/></svg>
              }
            }
          </span>
          <span class="flex-1 leading-snug">{{ toast.message }}</span>
          <button type="button" class="btn-icon !h-5 !w-5 shrink-0" (click)="toastService.dismiss(toast.id)" aria-label="Dismiss">
            <svg class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor"><path d="M6 6l8 8M14 6l-8 8" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastContainerComponent {
  readonly toastService = inject(ToastService);
}
