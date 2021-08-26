

import { Injectable } from '@angular/core'
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';

@Injectable()
export class RouterNavigationHelper {

    private  topicSend :any;
    private stompClient: any

    constructor(
        private router:Router,
        private cookieService: CookieService,
    ){

    }

    public navigate(response){
        this.router.navigate([`render/${response.MODULE_ID}`]);
    }

    public logout(){
        
    }

    public navigateOnActive(response){
        this.router.navigate([`render/${response.MODULE_ID}`]);
    }

    public navigateBack(){
        
    }

    public setAuthentication(token,expiredDate,subdomain){
        this.cookieService.set(
			'authentication_token',
			token,
			expiredDate,
			'/',
			window.location.host,
			true,
			'None'
		);
    }
    
    public getAuthentication(){
        return null;
    }
    
    public clear(){
      
   }

   public onBack(){
       
   }

   public getSubdomain(){
    
   }
}