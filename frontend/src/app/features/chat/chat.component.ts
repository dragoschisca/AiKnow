import { CommonModule } from '@angular/common';
import { AfterViewChecked, Component, ElementRef, ViewChild, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ChatService } from '../../core/services/chat.service';
import { ChatMessage, Conversation } from '../../core/models/chat.model';
import { WorkspaceContextService } from '../../core/state/workspace-context.service';
import { ToastService } from '../../shared/toast/toast.service';
import { IconComponent } from '../../shared/components/icon.component';
import { MarkdownComponent } from '../../shared/components/markdown.component';
import { CitationsDrawerComponent } from './citations-drawer.component';

let tempMessageId = 1;

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, IconComponent, MarkdownComponent, CitationsDrawerComponent],
  templateUrl: './chat.component.html',
})
export class ChatComponent implements AfterViewChecked {
  private readonly chatService = inject(ChatService);
  readonly workspaceContext = inject(WorkspaceContextService);
  private readonly toast = inject(ToastService);

  @ViewChild('scrollAnchor') private scrollAnchor?: ElementRef<HTMLDivElement>;
  private shouldScroll = false;

  readonly conversations = signal<Conversation[]>([]);
  readonly loadingConversations = signal(true);
  readonly activeConversationId = signal<string | null>(null);
  readonly messages = signal<ChatMessage[]>([]);
  readonly loadingMessages = signal(false);
  readonly sending = signal(false);
  readonly draft = signal('');
  readonly citationsMessage = signal<ChatMessage | null>(null);

  readonly activeConversationTitle = computed(() => {
    const id = this.activeConversationId();
    if (!id) return 'New conversation';
    const conversation = this.conversations().find((c) => c.id === id);
    return conversation ? this.conversationLabel(conversation) : 'New conversation';
  });

  constructor() {
    effect(() => {
      const orgId = this.workspaceContext.currentOrgId();
      const workspaceId = this.workspaceContext.currentWorkspaceId();
      if (orgId && workspaceId) {
        this.startNewConversation();
        this.loadConversations(orgId, workspaceId);
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.shouldScroll = false;
      this.scrollAnchor?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }
  }

  private loadConversations(orgId: string, workspaceId: string): void {
    this.loadingConversations.set(true);
    this.chatService.listConversations(orgId, workspaceId).subscribe({
      next: (list) => {
        this.conversations.set(list);
        this.loadingConversations.set(false);
      },
      error: () => {
        this.loadingConversations.set(false);
        this.toast.error('Failed to load conversations');
      },
    });
  }

  startNewConversation(): void {
    this.activeConversationId.set(null);
    this.messages.set([]);
    this.citationsMessage.set(null);
  }

  selectConversation(conversation: Conversation): void {
    if (conversation.id === this.activeConversationId()) return;
    this.activeConversationId.set(conversation.id);
    this.citationsMessage.set(null);
    this.loadingMessages.set(true);
    this.chatService.getMessages(conversation.id).subscribe({
      next: (msgs) => {
        this.messages.set(msgs);
        this.loadingMessages.set(false);
        this.shouldScroll = true;
      },
      error: () => {
        this.loadingMessages.set(false);
        this.toast.error('Failed to load messages');
      },
    });
  }

  async send(): Promise<void> {
    const content = this.draft().trim();
    if (!content || this.sending()) return;

    const orgId = this.workspaceContext.currentOrgId();
    const workspaceId = this.workspaceContext.currentWorkspaceId();
    if (!orgId || !workspaceId) return;

    this.draft.set('');
    this.sending.set(true);

    const optimisticId = `temp-${tempMessageId++}`;
    this.messages.update((list) => [
      ...list,
      {
        id: optimisticId,
        conversationId: this.activeConversationId() ?? '',
        senderType: 'USER',
        content,
        insufficientInformation: false,
        citations: [],
        createdAt: new Date().toISOString(),
      },
    ]);
    this.shouldScroll = true;

    try {
      let conversationId = this.activeConversationId();
      if (!conversationId) {
        const conversation = await firstValueFrom(this.chatService.createConversation(orgId, workspaceId));
        conversationId = conversation.id;
        this.activeConversationId.set(conversationId);
        this.conversations.update((list) => [conversation, ...list]);
      }

      const response = await firstValueFrom(this.chatService.sendMessage(conversationId, content));
      this.messages.update((list) => {
        const withoutOptimistic = list.filter((m) => m.id !== optimisticId);
        return [...withoutOptimistic, response.userMessage, response.assistantMessage];
      });
      this.conversations.update((list) => {
        const idx = list.findIndex((c) => c.id === conversationId);
        if (idx === -1) return list;
        const updated = { ...list[idx], updatedAt: new Date().toISOString() };
        const rest = list.filter((_, i) => i !== idx);
        return [updated, ...rest];
      });
      this.shouldScroll = true;
    } catch {
      this.messages.update((list) => list.filter((m) => m.id !== optimisticId));
      this.draft.set(content);
      this.toast.error('Failed to send message — please try again');
    } finally {
      this.sending.set(false);
    }
  }

  onComposerKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  toggleCitations(message: ChatMessage): void {
    this.citationsMessage.set(this.citationsMessage()?.id === message.id ? null : message);
  }

  conversationLabel(conversation: Conversation): string {
    return conversation.title?.trim() || 'New conversation';
  }
}
