import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login-dialog',
  templateUrl: './login-dialog.component.html',
  styleUrls: ['./login-dialog.component.scss']
})
export class LoginDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<LoginDialogComponent>,
    private router: Router
  ) {}

  onLogin(): void {
    this.dialogRef.close();
    this.router.navigate(['/login']);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
