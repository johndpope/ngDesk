import { Injectable } from "@angular/core";
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';

@Injectable()
export class MatSnackBarHelper{
    
    constructor(
        private _snackBar:MatSnackBar,
        private translateService: TranslateService,
    ){

    }

    public snackbarOpen(){
        	this._snackBar.open(
					this.translateService.instant('LIST_DATA_UPDATED'),
					'',
					{
						duration: 10000
					}
				);
    }

}