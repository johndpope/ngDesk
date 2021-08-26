import { Injectable } from "@angular/core";

import { TranslateService } from '@ngx-translate/core';
import { EditModuleDialogComponent } from '@src/app/dialogs/edit-module-dialog/edit-module-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '@src/app/dialogs/confirm-dialog/confirm-dialog.component';
import { CloneDialogComponent } from '@src/app/dialogs/clone-dialog/clone-dialog.component';
import { InviteUsersDialogComponent } from '@src/app/dialogs/invite-users-dialog/invite-users-dialog.component';

@Injectable()
export class MatDialogHelper{
	public dialogs:any;
	public page:any;
    constructor(
        private dialog:MatDialog,
        private translateService: TranslateService,
    ){

    }

    public updateEntries(body,moduleId,fields){
     
		const dialogRef = this.dialog.open(EditModuleDialogComponent, {
			data: {
				body: body,
				moduleId: moduleId,
				fields: fields,
				buttonText: this.translateService.instant('UPDATE'),
				closeDialog: this.translateService.instant('CANCEL')
			}
        });
        return dialogRef;
		// EVENT AFTER MODAL DIALOG IS CLOSED
		// dialogRef.afterClosed().subscribe(result => {
		// 	this.getEntries();
		// });
	}
	
	public deleteEntries(name){
		const dialogRef =this.dialog.open(ConfirmDialogComponent, {
			data: {
				message: name,
				buttonText: this.translateService.instant('DELETE'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('DELETE'),
				executebuttonColor: 'warn',
			},
		});

        return dialogRef;
	}

	public inviteUsers(){
		const dialogRef = this.dialog.open(InviteUsersDialogComponent, {});
		return dialogRef;
	}

	public cloneEntry(dialogMessage){
		const dialogRef = this.dialog.open(CloneDialogComponent,{
			data: {
				message: dialogMessage,
				buttonText: this.translateService.instant('OK'),
				closeDialog: this.translateService.instant('CANCEL'),
				action: this.translateService.instant('OK'),
				executebuttonColor: 'warn',
			},
		});
		return dialogRef;
	}

	public pickListDialogListLayout(layouts,listlayoutMobile){
		return null;
	}

	public isAndroid(){
			return false;
	}

	public isIOS(){
			return false;
	}

	public onBackButton(moduleId){

	}

	public backToSidebar(){
	}

}