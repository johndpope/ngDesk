import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UsersSidebarService {
  // default value.
  // this variable track the value between sessions.
  private _sideState: any = 'close';

  /**
   * This is the mini variant solution with animations trick.
   */
  public sideNavListener: any = new Subject();

  get sideNavState() {
    return this._sideState;
  }

  public setSideNavState(state) {
    this._sideState = state;
  }

  constructor() {
    this.sideNavListener.subscribe(state => {
      this.setSideNavState(state);
    });
  }
}
