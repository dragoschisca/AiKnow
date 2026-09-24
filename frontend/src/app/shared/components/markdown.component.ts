import { Component, HostBinding, Input } from '@angular/core';
import DOMPurify from 'dompurify';
import { marked } from 'marked';

marked.setOptions({ breaks: true, gfm: true });

/**
 * Renders AI-generated or user-generated text as sanitized markdown. The AI answer is
 * untrusted model output, so it is always parsed with `marked` and scrubbed with DOMPurify
 * before touching the DOM — never trust-as-html directly. Angular's own [innerHTML] binding
 * sanitizes again on top of that as defense in depth.
 */
@Component({
  selector: 'app-markdown',
  standalone: true,
  template: `<div [innerHTML]="renderedHtml"></div>`,
})
export class MarkdownComponent {
  @HostBinding('class') readonly hostClass = 'markdown-body';

  renderedHtml = '';

  @Input() set content(value: string | null | undefined) {
    const raw = value ?? '';
    const parsed = marked.parse(raw, { async: false }) as string;
    this.renderedHtml = DOMPurify.sanitize(parsed, { USE_PROFILES: { html: true } });
  }
}
