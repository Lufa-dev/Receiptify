
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ApiUrlProvider {
  private _apiUrl: string;

  constructor() {
    // Determine API URL based on hostname
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
      // Development environment
      this._apiUrl = 'http://localhost:8080';
    } else {
      // Production environment - hardcode the production URL
      this._apiUrl = 'https://receiptify-backend.onrender.com';
    }

    console.log('ApiUrlProvider initialized with URL:', this._apiUrl);
  }

  getApiUrl(): string {
    return this._apiUrl;
  }
}
