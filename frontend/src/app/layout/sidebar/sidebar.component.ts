import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { WorkspaceContextService } from '../../core/state/workspace-context.service';
import { IconComponent } from '../../shared/components/icon.component';
import { CreateOrganizationDialogComponent } from './create-organization-dialog.component';
import { CreateWorkspaceDialogComponent } from './create-workspace-dialog.component';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, IconComponent, CreateOrganizationDialogComponent, CreateWorkspaceDialogComponent],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  readonly workspaceContext = inject(WorkspaceContextService);
  private readonly authService = inject(AuthService);

  readonly orgMenuOpen = signal(false);
  readonly workspaceMenuOpen = signal(false);
  readonly userMenuOpen = signal(false);
  readonly showCreateOrgDialog = signal(false);
  readonly showCreateWorkspaceDialog = signal(false);

  readonly user = this.authService.currentUser;

  readonly currentRole = computed(() => this.workspaceContext.currentOrg()?.role ?? null);
  readonly canManage = computed(() => {
    const role = this.currentRole();
    return role === 'OWNER' || role === 'ADMIN';
  });

  readonly orgInitial = computed(() => (this.workspaceContext.currentOrg()?.name ?? '?').charAt(0).toUpperCase());
  readonly userInitials = computed(() => {
    const name = this.user()?.fullName ?? '';
    const parts = name.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return '?';
    return (parts[0][0] + (parts[1]?.[0] ?? '')).toUpperCase();
  });

  toggleOrgMenu(): void {
    this.workspaceMenuOpen.set(false);
    this.userMenuOpen.set(false);
    this.orgMenuOpen.update((v) => !v);
  }

  toggleWorkspaceMenu(): void {
    this.orgMenuOpen.set(false);
    this.userMenuOpen.set(false);
    this.workspaceMenuOpen.update((v) => !v);
  }

  toggleUserMenu(): void {
    this.orgMenuOpen.set(false);
    this.workspaceMenuOpen.set(false);
    this.userMenuOpen.update((v) => !v);
  }

  closeMenus(): void {
    this.orgMenuOpen.set(false);
    this.workspaceMenuOpen.set(false);
    this.userMenuOpen.set(false);
  }

  async selectOrg(orgId: string): Promise<void> {
    this.closeMenus();
    if (orgId === this.workspaceContext.currentOrgId()) return;
    await this.workspaceContext.selectOrganization(orgId);
  }

  selectWorkspace(workspaceId: string): void {
    this.closeMenus();
    this.workspaceContext.selectWorkspace(workspaceId);
  }

  logout(): void {
    this.authService.logout();
  }
}
