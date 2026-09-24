import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

export type IconName =
  | 'chat' | 'document' | 'audit' | 'chevron-down' | 'chevron-right' | 'chevron-up'
  | 'plus' | 'upload' | 'trash' | 'refresh' | 'logout' | 'search' | 'external-link'
  | 'check' | 'x' | 'spinner' | 'building' | 'layers' | 'gauge' | 'user' | 'send'
  | 'sparkle-off' | 'quote' | 'chevron-left' | 'menu' | 'warning' | 'file-text';

@Component({
  selector: 'app-icon',
  standalone: true,
  imports: [CommonModule],
  template: `<svg [attr.width]="size" [attr.height]="size" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" [class]="class" [innerHTML]="path"></svg>`,
})
export class IconComponent {
  @Input() name: IconName = 'document';
  @Input() size = 16;
  @Input() class = '';

  private readonly paths: Record<IconName, string> = {
    chat: '<path d="M4 5h16v11H8l-4 4V5z"/>',
    document: '<path d="M6 3h8l4 4v14H6V3z"/><path d="M14 3v4h4"/><path d="M9 12h6M9 16h6"/>',
    'file-text': '<path d="M6 3h8l4 4v14H6V3z"/><path d="M14 3v4h4"/><path d="M9 12h6M9 16h6M9 8h2"/>',
    audit: '<path d="M9 3h6l3 3v15H6V6l3-3z"/><path d="M9 10h6M9 14h6M9 18h3"/>',
    'chevron-down': '<path d="M6 9l6 6 6-6"/>',
    'chevron-up': '<path d="M6 15l6-6 6 6"/>',
    'chevron-right': '<path d="M9 6l6 6-6 6"/>',
    'chevron-left': '<path d="M15 6l-6 6 6 6"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    upload: '<path d="M12 16V4M7 9l5-5 5 5"/><path d="M4 16v3a2 2 0 002 2h12a2 2 0 002-2v-3"/>',
    trash: '<path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13"/>',
    refresh: '<path d="M4 12a8 8 0 0114-5.3M20 12a8 8 0 01-14 5.3"/><path d="M18 3v5h-5M6 21v-5h5"/>',
    logout: '<path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>',
    search: '<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/>',
    'external-link': '<path d="M14 4h6v6"/><path d="M20 4L10 14"/><path d="M18 13v5a2 2 0 01-2 2H6a2 2 0 01-2-2V8a2 2 0 012-2h5"/>',
    check: '<path d="M5 12l5 5 9-9"/>',
    x: '<path d="M6 6l12 12M18 6L6 18"/>',
    spinner: '<circle cx="12" cy="12" r="9" opacity="0.25"/><path d="M21 12a9 9 0 00-9-9" opacity="0.9"/>',
    building: '<path d="M4 21V6l8-3 8 3v15"/><path d="M4 21h16M9 9h1M14 9h1M9 13h1M14 13h1M9 21v-4h6v4"/>',
    layers: '<path d="M12 3l9 5-9 5-9-5 9-5z"/><path d="M3 13l9 5 9-5"/>',
    gauge: '<path d="M12 21a9 9 0 100-18 9 9 0 000 18z"/><path d="M12 12l4-4M12 8v1"/>',
    user: '<circle cx="12" cy="8" r="3.5"/><path d="M5 20c1.2-3.5 4-5 7-5s5.8 1.5 7 5"/>',
    send: '<path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4 20-7z"/>',
    'sparkle-off': '',
    quote: '<path d="M7 7h4v5a4 4 0 01-4 4v-2a2 2 0 002-2H7V7z"/><path d="M14 7h4v5a4 4 0 01-4 4v-2a2 2 0 002-2h-2V7z"/>',
    menu: '<path d="M4 6h16M4 12h16M4 18h16"/>',
    warning: '<path d="M12 3l10 18H2L12 3z"/><path d="M12 10v4M12 17h.01"/>',
  };

  get path(): string {
    return this.paths[this.name] ?? '';
  }
}
