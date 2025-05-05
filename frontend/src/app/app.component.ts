import {Component, OnInit} from '@angular/core';
import {environment} from "../environments/environment";
import {ApiUrlProvider} from "../shared/services/api-url.provider";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'frontend';

  constructor(private apiUrlProvider: ApiUrlProvider) {}

  ngOnInit(): void {
    // Log environment information
    console.log('Current environment:', environment);
    console.log('Environment API URL:', environment.API_URL);
    console.log('Effective API URL:', this.apiUrlProvider.getApiUrl());
    console.log('Current hostname:', window.location.hostname);
  }
}
