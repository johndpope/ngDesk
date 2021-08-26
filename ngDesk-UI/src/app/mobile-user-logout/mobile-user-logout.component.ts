
import { Component, OnInit } from '@angular/core';
import { UsersService } from '../users/users.service';
import { CookieService } from 'ngx-cookie-service';
import { Router } from '@angular/router';
import { RouterNavigationHelper } from '../router-navigation/router-navigation-helper';

@Component({
  selector: 'app-mobile-user-logout',
  templateUrl: './mobile-user-logout.component.html',
  styleUrls: ['./mobile-user-logout.component.css'],
  providers:[RouterNavigationHelper]
})
export class MobileUserLogoutComponent implements OnInit {


  public userName: string;
  public userEmail:string;
  public isSubmitting=false;

  constructor(
    private usersService:UsersService,
    private cookieService:CookieService,
    private router:Router,
    private routerNavigationHelper:RouterNavigationHelper
  ) { }

  ngOnInit() {
    this.userName =this.usersService.user.FIRST_NAME +' '+this.usersService.user.LAST_NAME;
    this.userEmail=this.usersService.user.EMAIL_ADDRESS;
  }

  public logout(){
  
    this.isSubmitting=true;
    this.usersService.logout().subscribe(
      (logoutReponse: any) => {
        this.isSubmitting=false;
        // this.cookieService.delete('authentication_token', '/', window.location.host);
        this.routerNavigationHelper.clear();
        this.usersService.setAuthenticationToken(null);
        // this.cookieService.delete('authentication_token', '/', window.location.host);
      this.usersService.setUserDetails(null);
      this.usersService.setCompanyUuid(null);
      this.usersService.setSubdomain(null);
      
        // this._stompService.disconnect();
        // this.router.navigate([''],{clearHistory:true});
        this.routerNavigationHelper.logout();
      },
      (error: any) => {
        this.isSubmitting=false;
        console.log(error);
      }
    );

  }
  
  public onBackTap(){
    this.routerNavigationHelper.navigateBack();

  }

}

