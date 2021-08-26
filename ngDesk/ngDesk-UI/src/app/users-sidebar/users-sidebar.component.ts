import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, OnInit } from '@angular/core';

import { UsersService } from '../users/users.service';
import { UsersSidebarService } from './users-sidebar.service';

@Component({
  selector: 'app-users-sidebar',
  templateUrl: './users-sidebar.component.html',
  styleUrls: ['./users-sidebar.component.scss'],
  animations: [

    // animate sidenave
    trigger('onSideNavChange', [
      state('close',
        style({
          width: '60px'
        })
      ),
      state('open',
        style({
          width: '350px'
        })
      ),
      transition('close => open', animate('200ms ease-in')),
      transition('open => close', animate('200ms ease-in')),

    ])

  ]
})
export class UsersSidebarComponent implements OnInit {
  public sideNavState: string = this.usersSidebarService.sideNavState;
  public users: any[] = [];
  public overflowState: any = 'auto';

  constructor(
    private usersSidebarService: UsersSidebarService,
    public usersService: UsersService
  ) {}

  public ngOnInit() {

    this.usersSidebarService.sideNavListener.subscribe(sideNavState => {
      this.sideNavState = sideNavState;
    });


  }

  public openSideNav() {
    this.sideNavState = 'open';
    this.usersSidebarService.setSideNavState(this.sideNavState);
  }

  public closeSideNav() {
    this.sideNavState = this.sideNavState === 'open' ? 'close' : 'open';
    this.usersSidebarService.setSideNavState(this.sideNavState);
  }
}
