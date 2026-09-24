import { Injectable, signal } from '@angular/core';

export interface ConfirmOptions {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  danger?: boolean;
}

interface ConfirmState extends ConfirmOptions {
  resolve: (value: boolean) => void;
}

@Injectable({ providedIn: 'root' })
export class ConfirmService {
  private readonly stateSignal = signal<ConfirmState | null>(null);
  readonly state = this.stateSignal.asReadonly();

  ask(options: ConfirmOptions): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.stateSignal.set({ ...options, resolve });
    });
  }

  resolve(result: boolean): void {
    this.stateSignal()?.resolve(result);
    this.stateSignal.set(null);
  }
}
