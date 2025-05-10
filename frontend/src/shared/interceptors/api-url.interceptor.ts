import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiUrlProvider } from '../services/api-url.provider';

@Injectable()
export class ApiUrlInterceptor implements HttpInterceptor {
  constructor(private apiUrlProvider: ApiUrlProvider) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const apiUrl = this.apiUrlProvider.getApiUrl();

    // Check if the request URL is to localhost:8080
    if (request.url.startsWith('http://localhost:8080')) {
      // Replace localhost:8080 with the correct API URL
      const updatedUrl = request.url.replace('http://localhost:8080', apiUrl);

      // Clone the request with the new URL
      const modifiedRequest = request.clone({
        url: updatedUrl
      });

      return next.handle(modifiedRequest);
    }

    return next.handle(request);
  }
}
