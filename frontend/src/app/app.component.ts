import {Component, OnInit} from '@angular/core';
import {environment} from "../environments/environment";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit{
  title = 'frontend';

  ngOnInit() {
    console.log('Current environment:', environment);
    console.log('API URL:', environment.API_URL);
  }
}
