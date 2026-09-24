import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', redirectTo: 'chat', pathMatch: 'full' },
      {
        path: 'chat',
        loadComponent: () => import('./features/chat/chat.component').then((m) => m.ChatComponent),
      },
      {
        path: 'documents',
        loadComponent: () =>
          import('./features/documents/document-library.component').then((m) => m.DocumentLibraryComponent),
      },
      {
        path: 'audit-log',
        loadComponent: () => import('./features/audit/audit-log.component').then((m) => m.AuditLogComponent),
      },
      {
        path: 'usage',
        loadComponent: () => import('./features/usage/usage.component').then((m) => m.UsageComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
