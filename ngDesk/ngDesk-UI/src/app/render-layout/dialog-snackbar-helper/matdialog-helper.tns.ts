import { Injectable } from '@angular/core';
import * as dialogs from "@nativescript/core";

import { transition } from '@angular/animations';
import { Router } from '@angular/router';
import { Page, isIOS, isAndroid } from '@nativescript/core';
import { RouterExtensions } from '@nativescript/angular';
import { android, AndroidApplication, AndroidActivityBackPressedEventData } from '@nativescript/core/application';
import { getString } from '@nativescript/core/application-settings';
import { DataApiService } from '@ngdesk/data-api';
@Injectable()
export class MatDialogHelper{
    public dialogs:any;
    public page:Page;
    constructor(
        private router:RouterExtensions,
        private route:Router,
        private dataApiService:DataApiService
        // private page:Page
    ){
        this.dialogs=dialogs;
        this.dataApiService.configuration.basePath =
        'https://' + getString('subdomain') + '.ngdesk.com/api/ngdesk-data-service-v1';
    }

    public updateEntries(body,moduleId,fields){
        return null;
    }

    public deleteEntries(name){
        return null;
    }

    public inviteUsers(){
        return null;
    }



    public pickListDialogListLayout(layouts,listlayoutMobile){
        console.log("here")
        let layoutNames:any=[];
        let result:any;
         layouts.forEach(element => {
             layoutNames.push(element.NAME);
         });

        dialogs.action({
			message: 'layout',
			cancelButtonText: "Cancel",
			actions:layoutNames
		}).then(result => {
			console.log("Dialog result: " + result);
            result=result;
	
			if(result !== 'Cancel'){
                console.log(layouts);
                console.log(result);
                // console.log( layouts.filter( f=> f.NAME == result));
                listlayoutMobile=layouts.filter( f=> f.NAME == result)[0].LAYOUT_ID;
                console.log(listlayoutMobile);

                // return listlayoutMobile[0].LAYOUT_ID;
			}
		 
        });

      
    }

    public isAndroid(){
		if(isAndroid){
			return true;
		}else{
			return false;
		}
	}

	public isIOS(){
		if(isIOS){
			return true;
		}else{
			return false;
		}
    }
    
    public onBackButton(moduleId){
       
        if(isAndroid && this.route.url === '/render/'+moduleId){
		android.on(AndroidApplication.activityBackPressedEvent, (args: AndroidActivityBackPressedEventData) => {
           
            if(this.route.url === '/render/'+moduleId){
            args.cancel=true;
            this.router.navigate(['/sidebar'],{clearHistory:true});
            } 
            // const frame:Frame= this.page.frame;
            // frame.navigate('/logout');

            // args.cancel=disable;
            
            // this.router.navigate(['render/5e5f6f2c0cc6b80001ac6139']);
            // android.removeEventListener(AndroidApplication.activityBackPressedEvent);
        });
    }
    }
    
    public backToSidebar(){
       console.log(this.router.locationStrategy);
        this.router.navigate(['/sidebar'],{clearHistory:true,transition:{name:'slideRight',duration:300,curve:'easeIn'}});
    }

}