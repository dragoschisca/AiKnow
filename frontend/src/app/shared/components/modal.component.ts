import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed inset-0 z-[80] flex items-center justify-center bg-slate-900/30 p-4 animate-fade-in" (click)="close.emit()">
      <div class="w-full panel animate-fade-in" [style.max-width]="maxWidth" (click)="$event.stopPropagation()">
        <div class="flex items-center justify-between border-b border-slate-100 px-4 py-3">
          <h3 class="text-sm font-semibold text-slate-900">{{ title }}</h3>
          <button type="button" class="btn-icon" (click)="close.emit()" aria-label="Close">
            <svg class="h-4 w-4" viewBox="0 0 20 20" fill="none"><path d="M6 6l8 8M14 6l-8 8" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
          </button>
        </div>
        <div class="p-4">
          <ng-content></ng-content>
        </div>
      </div>
    </div>
  `,
})
export class ModalComponent {
  @Input() title = '';
  @Input() maxWidth = '420px';
  @Output() close = new EventEmitter<void>();
}
