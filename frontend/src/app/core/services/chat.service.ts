import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api.model';
import { ChatMessage, Conversation, SendMessageResponse } from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatService {
  constructor(private readonly http: HttpClient) {}

  private conversationsBase(orgId: string, workspaceId: string): string {
    return `${environment.apiUrl}/organizations/${orgId}/workspaces/${workspaceId}/conversations`;
  }

  listConversations(orgId: string, workspaceId: string): Observable<Conversation[]> {
    return this.http.get<ApiResponse<Conversation[]>>(this.conversationsBase(orgId, workspaceId)).pipe(map((r) => r.data));
  }

  createConversation(orgId: string, workspaceId: string, title?: string): Observable<Conversation> {
    return this.http
      .post<ApiResponse<Conversation>>(this.conversationsBase(orgId, workspaceId), { title: title ?? null })
      .pipe(map((r) => r.data));
  }

  getConversation(orgId: string, workspaceId: string, conversationId: string): Observable<Conversation> {
    return this.http
      .get<ApiResponse<Conversation>>(`${this.conversationsBase(orgId, workspaceId)}/${conversationId}`)
      .pipe(map((r) => r.data));
  }

  getMessages(conversationId: string): Observable<ChatMessage[]> {
    return this.http
      .get<ApiResponse<ChatMessage[]>>(`${environment.apiUrl}/conversations/${conversationId}/messages`)
      .pipe(map((r) => r.data));
  }

  sendMessage(conversationId: string, content: string): Observable<SendMessageResponse> {
    return this.http
      .post<ApiResponse<SendMessageResponse>>(`${environment.apiUrl}/conversations/${conversationId}/messages`, { content })
      .pipe(map((r) => r.data));
  }
}
