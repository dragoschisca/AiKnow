import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { WorkspaceContextService } from '../../core/state/workspace-context.service';
import { IconComponent } from '../../shared/components/icon.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { CreateOrganizationDialogComponent } from '../sidebar/create-organization-dialog.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent, IconComponent, CreateOrganizationDialogComponent],
  templateUrl: './shell.component.html',
})
export class ShellComponent implements OnInit {
  readonly workspaceContext = inject(WorkspaceContextService);
  readonly initializing = signal(true);
  readonly showCreateOrgDialog = signal(false);

  async ngOnInit(): Promise<void> {
    await this.workspaceContext.ensureInitialized();
    this.initializing.set(false);
  }
}
