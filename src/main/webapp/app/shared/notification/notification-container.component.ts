import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { NotificationService, Notification } from './notification.service';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faCheckCircle, faExclamationCircle, faExclamationTriangle, faInfoCircle, faTimes } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'jhi-notification-container',
  standalone: true,
  imports: [CommonModule, FontAwesomeModule],
  template: `
    <div class="notification-container">
      <div
        *ngFor="let notification of notifications"
        class="notification notification-{{ notification.type }}"
        [class.notification-entering]="isEntering(notification)"
        [class.notification-leaving]="isLeaving(notification)"
      >
        <div class="notification-icon">
          <fa-icon [icon]="getIcon(notification.type)" [class]="'icon-' + notification.type"> </fa-icon>
        </div>

        <div class="notification-content">
          <span class="notification-message">{{ notification.message }}</span>
        </div>

        <button class="notification-close" (click)="dismiss(notification.id)" type="button">
          <fa-icon [icon]="closeIcon"></fa-icon>
        </button>
      </div>
    </div>
  `,
  styles: [
    `
      .notification-container {
        position: fixed;
        bottom: 20px;
        left: 50%;
        transform: translateX(-50%);
        z-index: 1050;
        max-width: 400px;
        width: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
      }

      .notification {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        padding: 16px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        color: white;
        font-weight: 500;
        min-height: 48px;
        max-width: 100%;
        width: 100%;
        word-wrap: break-word;
        opacity: 1;
        transform: translateY(0);
        transition: all 0.3s ease-in-out;
      }

      .notification-entering {
        animation: slideInUp 0.3s ease-out forwards;
      }

      .notification-leaving {
        animation: slideOutDown 0.3s ease-in forwards;
      }

      @keyframes slideInUp {
        from {
          transform: translateY(100%);
          opacity: 0;
        }
        to {
          transform: translateY(0);
          opacity: 1;
        }
      }

      @keyframes slideOutDown {
        from {
          transform: translateY(0);
          opacity: 1;
        }
        to {
          transform: translateY(100%);
          opacity: 0;
        }
      }

      .notification-success {
        background-color: #43a047;
      }

      .notification-error {
        background-color: #d32f2f;
      }

      .notification-warning {
        background-color: #ef6c00;
      }

      .notification-info {
        background-color: #1976d2;
      }

      .notification-icon {
        flex-shrink: 0;
        margin-right: 12px;
        font-size: 20px;
      }

      .notification-content {
        flex: 1;
        margin-right: 12px;
      }

      .notification-message {
        font-size: 14px;
        line-height: 1.4;
      }

      .notification-close {
        background: none;
        border: none;
        color: white;
        cursor: pointer;
        padding: 4px;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        opacity: 0.8;
        transition: opacity 0.2s;
      }

      .notification-close:hover {
        opacity: 1;
        background-color: rgba(255, 255, 255, 0.1);
      }

      .icon-success {
        color: white;
      }
      .icon-error {
        color: white;
      }
      .icon-warning {
        color: white;
      }
      .icon-info {
        color: white;
      }

      @media (max-width: 480px) {
        .notification-container {
          left: 20px;
          right: 20px;
          max-width: none;
          transform: none;
        }

        .notification {
          margin-bottom: 8px;
        }
      }
    `,
  ],
})
export class NotificationContainerComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  private subscription: Subscription = new Subscription();
  private enteringNotifications = new Set<string>();
  private leavingNotifications = new Set<string>();

  successIcon = faCheckCircle;
  errorIcon = faExclamationCircle;
  warningIcon = faExclamationTriangle;
  infoIcon = faInfoCircle;
  closeIcon = faTimes;

  constructor(private notificationService: NotificationService) {}

  ngOnInit() {
    this.subscription.add(
      this.notificationService.getNotifications().subscribe(notifications => {
        const newNotifications = notifications.filter(n => !this.notifications.find(existing => existing.id === n.id));

        newNotifications.forEach(notification => {
          this.enteringNotifications.add(notification.id);
          setTimeout(() => {
            this.enteringNotifications.delete(notification.id);
          }, 400);
        });

        this.notifications = notifications;
      }),
    );
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  dismiss(id: string): void {
    this.leavingNotifications.add(id);
    setTimeout(() => {
      this.notificationService.remove(id);
      this.leavingNotifications.delete(id);
    }, 400);
  }

  isEntering(notification: Notification): boolean {
    return this.enteringNotifications.has(notification.id);
  }

  isLeaving(notification: Notification): boolean {
    return this.leavingNotifications.has(notification.id);
  }

  getIcon(type: string) {
    switch (type) {
      case 'success':
        return this.successIcon;
      case 'error':
        return this.errorIcon;
      case 'warning':
        return this.warningIcon;
      case 'info':
        return this.infoIcon;
      default:
        return this.infoIcon;
    }
  }
}
