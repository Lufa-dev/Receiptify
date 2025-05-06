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

  private readonly THROTTLE_DELAY = 60000; // 1 minute - don't update too frequently
  private lastActivityTime = Date.now();

  constructor(
    private authService: AuthService,
    private ngZone: NgZone
  ) {}

  startTracking(): void {
    // Add event listeners for all tracked events
    this.ACTIVITY_EVENTS.forEach(eventName => {
      window.addEventListener(eventName, this.activityHandler.bind(this), { passive: true });
    });

    console.log('Activity tracking started');
  }

  stopTracking(): void {
    // Remove event listeners
    this.ACTIVITY_EVENTS.forEach(eventName => {
      window.removeEventListener(eventName, this.activityHandler.bind(this));
    });

    console.log('Activity tracking stopped');
  }

  private activityHandler(): void {
    // Throttle activity tracking to prevent excessive timer resets
    const now = Date.now();
    if (now - this.lastActivityTime > this.THROTTLE_DELAY) {
      this.lastActivityTime = now;

      // Use NgZone to ensure this runs in Angular's zone
      this.ngZone.run(() => {
        this.authService.recordActivity();
      });
    }
  }
}
