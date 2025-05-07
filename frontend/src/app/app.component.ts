import {Component, OnDestroy, OnInit} from '@angular/core';
import {environment} from "../environments/environment";
import {ApiUrlProvider} from "../shared/services/api-url.provider";
import {AuthService} from "../shared/services/auth.service";
import {ActivityTrackerService} from "../shared/services/activity-tracker.service";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, OnDestroy {
  private userSubscription: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private activityTracker: ActivityTrackerService
  ) {}

  ngOnInit(): void {

    // Start activity tracking if the user is logged in at app start
    if (this.authService.isLoggedIn()) {
      this.activityTracker.startTracking();
    }

    // Subscribe to the auth state to start/stop tracking when login state changes
    this.userSubscription = this.authService.user$.subscribe(user => {
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

    // Clean up the subscription
    if (this.userSubscription) {
      this.userSubscription.unsubscribe();
      this.userSubscription = null;
    }
  }
}


