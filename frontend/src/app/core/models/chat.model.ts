export type ChatSenderType = 'USER' | 'ASSISTANT';

export interface Conversation {
  id: string;
  workspaceId: string;
  userId: string;
  title: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Citation {
  documentId: string;
  documentName: string;
  pageNumber: number | null;
  chunkId: string | null;
  quoteSnippet: string | null;
}

export interface ChatMessage {
  id: string;
  conversationId: string;
  senderType: ChatSenderType;
  content: string;
  insufficientInformation: boolean;
  citations: Citation[];
  createdAt: string;
}

export interface SendMessageResponse {
  userMessage: ChatMessage;
  assistantMessage: ChatMessage;
}
