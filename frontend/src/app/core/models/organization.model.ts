export type OrganizationMemberRole = 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER';
export type SubscriptionPlan = 'FREE' | 'STARTER' | 'BUSINESS' | 'ENTERPRISE';

export interface Organization {
  id: string;
  name: string;
  slug: string;
  role: OrganizationMemberRole;
  createdAt: string;
}

export interface UsageSummary {
  plan: SubscriptionPlan;
  documentCount: number;
  maxDocuments: number;
  storageBytes: number;
  maxStorageBytes: number;
  questionsThisMonth: number;
  maxQuestionsPerMonth: number;
  resetDate: string;
}
