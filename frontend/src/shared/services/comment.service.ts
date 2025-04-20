import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Comment } from '../models/comment.model';
import { PagedResponse } from '../models/paged-response.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = `${environment.API_URL}/api/comments`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  getRecipeComments(recipeId: number, page: number = 0, size: number = 10): Observable<PagedResponse<Comment>> {
    return this.http.get<PagedResponse<Comment>>(`${this.apiUrl}/recipe/${recipeId}?page=${page}&size=${size}`);
  }

  addComment(comment: Comment): Observable<Comment> {
    return this.http.post<Comment>(this.apiUrl, comment, {
      headers: this.authService.getAuthHeaders()
    });
  }

  updateComment(id: number, comment: Comment): Observable<Comment> {
    return this.http.put<Comment>(`${this.apiUrl}/${id}`, comment, {
      headers: this.authService.getAuthHeaders()
    });
  }

  deleteComment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers: this.authService.getAuthHeaders()
    });
  }
}
