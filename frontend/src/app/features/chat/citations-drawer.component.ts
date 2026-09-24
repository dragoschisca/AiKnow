import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ChatMessage } from '../../core/models/chat.model';
import { IconComponent } from '../../shared/components/icon.component';

@Component({
  selector: 'app-citations-drawer',
  standalone: true,
  imports: [CommonModule, RouterLink, IconComponent],
  template: `
    <aside class="flex h-full w-80 shrink-0 flex-col border-l border-slate-200 bg-white animate-slide-in">
      <div class="flex items-center justify-between border-b border-slate-100 px-4 py-3">
        <div>
          <h3 class="text-sm font-semibold text-slate-900">Sources</h3>
          <p class="text-xs text-slate-500">Verified against retrieved passages</p>
        </div>
        <button type="button" class="btn-icon" (click)="close.emit()" aria-label="Close sources">
          <app-icon name="x" [size]="15" />
        </button>
      </div>

      <div class="flex-1 space-y-2.5 overflow-y-auto p-3">
        @if (message?.citations?.length) {
          @for (citation of message!.citations; track $index) {
            <div class="card p-3">
              <div class="flex items-start gap-2">
                <app-icon name="file-text" [size]="15" class="mt-0.5 shrink-0 text-slate-400" />
                <div class="min-w-0 flex-1">
                  <a
                    [routerLink]="['/documents']"
                    [queryParams]="{ doc: citation.documentId }"
                    class="truncate text-sm font-medium text-slate-800 hover:text-brand-600 hover:underline"
                    [title]="citation.documentName"
                  >
                    {{ citation.documentName }}
                  </a>
                  @if (citation.pageNumber) {
                    <span class="badge-slate ml-1.5">Page {{ citation.pageNumber }}</span>
                  }
                </div>
              </div>
              @if (citation.quoteSnippet) {
                <blockquote class="mt-2 border-l-2 border-slate-200 pl-2.5 text-xs italic leading-relaxed text-slate-600">
                  "{{ citation.quoteSnippet }}"
                </blockquote>
              }
            </div>
          }
        } @else {
          <p class="px-1 py-4 text-center text-sm text-slate-400">No citations for this answer.</p>
        }
      </div>
    </aside>
  `,
})
export class CitationsDrawerComponent {
  @Input() message: ChatMessage | null = null;
  @Output() close = new EventEmitter<void>();
}
