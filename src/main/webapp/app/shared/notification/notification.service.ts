import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface NotificationConfig {
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
  action?: string;
}

export interface Notification extends NotificationConfig {
  id: string;
  timestamp: number;
}

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private notifications$ = new BehaviorSubject<Notification[]>([]);

  getNotifications() {
    return this.notifications$.asObservable();
  }

  show(config: NotificationConfig): void {
    const notification: Notification = {
      ...config,
      id: this.generateId(),
      timestamp: Date.now(),
      duration: config.duration || 10000,
    };

    const currentNotifications = this.notifications$.value;
    this.notifications$.next([...currentNotifications, notification]);

    setTimeout(() => {
      this.remove(notification.id);
    }, notification.duration);
  }

  remove(id: string): void {
    const currentNotifications = this.notifications$.value;
    this.notifications$.next(currentNotifications.filter(n => n.id !== id));
  }

  success(message: string, duration?: number): void {
    this.show({ message, type: 'success', duration });
  }

  error(message: string, duration?: number): void {
    this.show({ message, type: 'error', duration });
  }

  warning(message: string, duration?: number): void {
    this.show({ message, type: 'warning', duration });
  }

  info(message: string, duration?: number): void {
    this.show({ message, type: 'info', duration });
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9);
  }
}
