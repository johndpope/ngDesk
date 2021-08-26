import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
 
import { AppGlobals } from '../app.globals';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient, private globals: AppGlobals) { }

  public getAllKeys(sortBy, orderBy, page, pageSize) {
    return this.http.get(`${this.globals.baseRestUrl}/api/tokens?
sort=${sortBy}&order=${orderBy}&page=${page}&page_size=${pageSize}`);
  }

  public postKey(name, dataId) {
    const httpParams = new HttpParams().set('name', name).set('user_id', dataId);
    return this.http.post(`${this.globals.baseRestUrl}/api/tokens`, null, { params: httpParams });
  }

  public deleteAPIKey(tokenID) {
    return this.http.delete(`${this.globals.baseRestUrl}/api/tokens/${tokenID}`);
  }

}
