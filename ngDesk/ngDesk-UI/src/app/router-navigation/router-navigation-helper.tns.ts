

import { Injectable } from '@angular/core'
import { Router } from '@angular/router';
import { setString, getString, clear } from '@nativescript/core/application-settings';
import { RouterExtensions } from '@nativescript/angular';



@Injectable()
export class RouterNavigationHelper {
    
    constructor(
        private router:RouterExtensions
    ){
      
    }

    public navigate(response){
        this.router.navigate([`/sidebar`],{clearHistory:true});
    }

    public logout(){
        this.router.navigate([''],{clearHistory:true});
    }

    public navigateOnActive(response){
        this.router.navigate([`/sidebar`],{clearHistory:true});
    }

    public navigateBack(){
        console.log("from user details")
        this.router.navigate([`/sidebar`],{ clearHistory: true });
    }

    public setAuthentication(auth,expiredDate,subdomain){
        setString("subdomain",subdomain);
        setString("authentication_token",auth);
    }
    
    public getAuthentication(){
        return getString("authentication_token");
    }

    public clear(){
        clear();
   }

   public onBack(){
    this.router.navigate([''],{clearHistory:true});
    
   }

   public getSubdomain(){
     return getString('subdomain');
   }
   
 }