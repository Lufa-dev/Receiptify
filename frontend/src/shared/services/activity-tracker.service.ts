import { Injectable, NgZone } from '@angular/core';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ActivityTrackerService {
  private readonly ACTIVITY_EVENTS = [
    'mousedown', 'mousemove', 'keydown',
    'scroll', 'touchstart', 'click', 'wheel'
  ];

  private readonly THROTTLE_DELAY = 30000; // 30 seconds - more frequent updates
  private lastActivityTime = Date.now();
  private active = false;

  constructor(
    private authService: AuthService,
    private ngZone: NgZone
  ) {}

  startTracking(): void {
    if (this.active) {
      return;
    }

    // Add event listeners for all tracked events
    this.ACTIVITY_EVENTS.forEach(eventName => {
      window.addEventListener(eventName, this.activityHandler.bind(this), { passive: true });
    });

    this.active = true;

    // Immediately record activity when tracking starts
    this.ngZone.run(() => {
      this.authService.recordActivity();
    });
  }

  stopTracking(): void {
    if (!this.active) {
      return;
    }

    // Remove event listeners
    this.ACTIVITY_EVENTS.forEach(eventName => {
      window.removeEventListener(eventName, this.activityHandler.bind(this));
    });

    this.active = false;
  }

  private activityHandler(): void {
    const now = Date.now();

    // Throttle activity notifications to the auth service
    if (now - this.lastActivityTime > this.THROTTLE_DELAY) {
      // Update last activity time
      this.lastActivityTime = now;

      // Use NgZone to ensure this runs in Angular's zone
      this.ngZone.run(() => {
        this.authService.recordActivity();
      });
    }
  }
}
