import { Injectable, computed, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Organization } from '../models/organization.model';
import { Workspace } from '../models/workspace.model';
import { OrganizationService } from '../services/organization.service';
import { WorkspaceService } from '../services/workspace.service';

const LAST_ORG_KEY = 'aiknow.lastOrgId';
const LAST_WORKSPACE_KEY = 'aiknow.lastWorkspaceId';

/**
 * Central, app-wide "where am I" state: which organization and workspace are active. The
 * sidebar switcher reads and writes this; feature pages read the current ids from here rather
 * than threading them through every route.
 */
@Injectable({ providedIn: 'root' })
export class WorkspaceContextService {
  private readonly organizationsSignal = signal<Organization[]>([]);
  private readonly workspacesSignal = signal<Workspace[]>([]);
  private readonly currentOrgIdSignal = signal<string | null>(localStorage.getItem(LAST_ORG_KEY));
  private readonly currentWorkspaceIdSignal = signal<string | null>(localStorage.getItem(LAST_WORKSPACE_KEY));
  private readonly loadingSignal = signal(false);
  private initialized = false;

  readonly organizations = this.organizationsSignal.asReadonly();
  readonly workspaces = this.workspacesSignal.asReadonly();
  readonly currentOrgId = this.currentOrgIdSignal.asReadonly();
  readonly currentWorkspaceId = this.currentWorkspaceIdSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();

  readonly currentOrg = computed(() => this.organizationsSignal().find((o) => o.id === this.currentOrgIdSignal()) ?? null);
  readonly currentWorkspace = computed(() =>
    this.workspacesSignal().find((w) => w.id === this.currentWorkspaceIdSignal()) ?? null,
  );

  constructor(
    private readonly organizationService: OrganizationService,
    private readonly workspaceService: WorkspaceService,
  ) {}

  async ensureInitialized(): Promise<void> {
    if (this.initialized) return;
    this.initialized = true;
    await this.reloadOrganizations();
  }

  async reloadOrganizations(): Promise<void> {
    this.loadingSignal.set(true);
    try {
      const orgs = await firstValueFrom(this.organizationService.list());
      this.organizationsSignal.set(orgs);

      const preferredOrgId = this.currentOrgIdSignal();
      const org = orgs.find((o) => o.id === preferredOrgId) ?? orgs[0] ?? null;
      if (org) {
        await this.selectOrganization(org.id);
      } else {
        this.currentOrgIdSignal.set(null);
        this.workspacesSignal.set([]);
        this.currentWorkspaceIdSignal.set(null);
      }
    } finally {
      this.loadingSignal.set(false);
    }
  }

  async selectOrganization(orgId: string): Promise<void> {
    this.currentOrgIdSignal.set(orgId);
    localStorage.setItem(LAST_ORG_KEY, orgId);

    this.loadingSignal.set(true);
    try {
      const workspaces = await firstValueFrom(this.workspaceService.list(orgId));
      this.workspacesSignal.set(workspaces);

      const preferredWorkspaceId = this.currentWorkspaceIdSignal();
      const workspace = workspaces.find((w) => w.id === preferredWorkspaceId) ?? workspaces[0] ?? null;
      this.selectWorkspace(workspace?.id ?? null);
    } finally {
      this.loadingSignal.set(false);
    }
  }

  selectWorkspace(workspaceId: string | null): void {
    this.currentWorkspaceIdSignal.set(workspaceId);
    if (workspaceId) {
      localStorage.setItem(LAST_WORKSPACE_KEY, workspaceId);
    } else {
      localStorage.removeItem(LAST_WORKSPACE_KEY);
    }
  }

  async addWorkspace(workspace: Workspace): Promise<void> {
    this.workspacesSignal.update((list) => [...list, workspace]);
    this.selectWorkspace(workspace.id);
  }

  async addOrganization(org: Organization): Promise<void> {
    this.organizationsSignal.update((list) => [...list, org]);
    await this.selectOrganization(org.id);
  }

  reset(): void {
    this.initialized = false;
    this.organizationsSignal.set([]);
    this.workspacesSignal.set([]);
    this.currentOrgIdSignal.set(null);
    this.currentWorkspaceIdSignal.set(null);
  }
}
