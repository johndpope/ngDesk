import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { AppGlobals } from '../app.globals';

@Injectable({
  providedIn: 'root'
})
export class WalkthroughService {

  constructor(
    private http: HttpClient,
    private globals: AppGlobals
  ) { }

  // get walkthrough
  public getWalkthrough() {
    return this.http.get(`${this.globals.baseRestUrl}/walkthroughs`);
  }

  // save walkthrough status
  public postWalkthrough(key, value) {
    return this.http.post(`${this.globals.baseRestUrl}/walkthroughs?key=${key}&value=${value}`, {});
  }

}
