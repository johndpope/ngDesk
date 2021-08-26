import { Injectable } from '@angular/core';
import * as dialogs from "@nativescript/core";
import { TranslateService } from '@ngx-translate/core';
import { Feedback, FeedbackType, FeedbackPosition } from "nativescript-feedback";

import * as application from "@nativescript/core";

import { Page, ScrollView, StackLayout, isAndroid, isIOS } from '@nativescript/core';
import { RouterExtensions } from '@nativescript/angular';
import { getString, setBoolean } from '@nativescript/core/application-settings';
import { LowerCasePipe } from '@angular/common';
// import { MatDialog } from '@angular/material/dialog';
declare var UIKeyboardFrameEndUserInfoKey: string;
declare var UIKeyboardWillChangeFrameNotification: any;

@Injectable()
export class RenderDetailHelper {

    public dataMaterialModule: any = {};
    public config :any;
	public _snackBar:any;
	public application:any;
	public page:Page;
	public dialog:any;
	// private  topicSend = '/topic/send';
	// public stompClientManager: StompClient;
	// public stompClientRest: StompClient;

    constructor(
        public translateService:TranslateService,
        private feedback :Feedback,
		private router:RouterExtensions,
		private pageReference:Page,
		// private dialog:MatDialog,
    ){
		// super();
	 this.application=application;
	//  this.stompClientManager = new StompClient(this);
	//  this.stompClientRest = new StompClient(this);
    }

	
	public scrollLayout:ScrollView=null;
	public stackContainer:StackLayout=null;

    public configSetup(){
      
    }

    public checkPressToEnterhelper(enterToSend){
        return null;
    }
    

    public snackbarHelper(){

    }

	public oneToManyMapDialog(relmoduleId,moduleId,fieldName, currentOneToManyData){
     
		return null;
		
	}
	
    public pickListDialog(value,field,formControlsMobile){
        dialogs.action({
			message: field.DISPLAY_LABEL,
			cancelButtonText: "Cancel",
			actions:value[0].PICKLIST_VALUES
		}).then(result => {
		
	
			if(result !== 'Cancel'){
			formControlsMobile[field.NAME].setValue(result);
			}
		
		});
    }


    public countryDialCodeDialogHelper(countryName,formControlsMobile){
        dialogs.action({
			message: "Country Dial Code",
			cancelButtonText: "Cancel",
			actions:countryName
		}).then(result => {
			let country=result.split('|');
			if(result !== 'Cancel'){

			formControlsMobile['DIAL_CODE'].setValue(country[1]);
			}
		
		});
    }

    public bannerNotification(status){
        this.feedback.success({title: this.translateService.instant(status),
        message: ''});
					
    }

    public navigateToSidebar(path){
		this.router.navigate([path],{clearHistory:true});
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

	public scrollBottom(args){
		this.page=<Page>args.object;
		this.scrollLayout = this.page.getViewById('myScoller') as ScrollView;
		this.stackContainer =this.page.getViewById('stackContainer') as StackLayout;
		
		this.scrollLayout.scrollToVerticalOffset(this.scrollLayout.scrollableHeight,true);
	
	}

	public navigateToListLayout(moduleId){
		this.router.navigate([`render/${moduleId}`],{clearHistory:true});
	}

	public disableKeyboard(args){
		let textField = args.object;
		textField.dismissSoftInput();

		if (isAndroid) {
			let page=<Page>args.object;
			let textField = page.getViewById('textField');
			textField.android.setFocusable(false);
			// setTimeout(function () {
			// 	p.android.setFocusableInTouchMode(true);
			// }, 300);
		} 
	}


	public getEntryValues(){
        let data={
       'MODULE_ID': getString("moduleId"),
       'DATA_ID': getString("dataId")
        }
        return data;
	}

	public setshowBackButton(){
		setBoolean("showBackButton",true);
	}

	public subscribeToTopic(sessionUuid){
		

	}
	
}