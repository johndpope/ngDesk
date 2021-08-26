import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { AppGlobals } from '../../app.globals';

@Injectable({
  providedIn: 'root'
})
export class AutocompleteService {

  constructor(private http: HttpClient, private globals: AppGlobals) { }

  public getAutocomplete(moduleId: string, query: string) {
    const httpParams = new HttpParams().set('module_id', moduleId).set('q', query);
    return this.http.get(`${this.globals.baseRestUrl}/autocomplete`, { params: httpParams });
  }
}
