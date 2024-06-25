import {ChangeDetectorRef, Component} from '@angular/core';
import { AuthService } from '../../services/auth.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {
  isLoggedIn$: Observable<boolean>;

  constructor(private authService: AuthService, private cd: ChangeDetectorRef) {
    this.isLoggedIn$ = this.authService.isLoggedIn();
  }

  logout() {
    this.authService.logout().then(() => {
      this.cd.markForCheck();
    });
  }


}
