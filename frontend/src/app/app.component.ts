import {Component, OnDestroy, OnInit} from '@angular/core';
import {environment} from "../environments/environment";
import {ApiUrlProvider} from "../shared/services/api-url.provider";
import {AuthService} from "../shared/services/auth.service";
import {ActivityTrackerService} from "../shared/services/activity-tracker.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, OnDestroy {
  constructor(
    private authService: AuthService,
    private activityTracker: ActivityTrackerService
  ) {}

  ngOnInit(): void {
    // Start activity tracking if the user is logged in
    if (this.authService.isLoggedIn()) {
      this.activityTracker.startTracking();
    }

    // Subscribe to the auth state to start/stop tracking when login state changes
    this.authService.user$.subscribe(user => {
      if (user) {
        this.activityTracker.startTracking();
      } else {
        this.activityTracker.stopTracking();
      }
    });
  }

  ngOnDestroy(): void {
    // Stop tracking when the app component is destroyed
    this.activityTracker.stopTracking();
  }
}

