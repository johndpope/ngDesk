import { Injectable } from '@angular/core';
import { config } from '../../tiny-mce/tiny-mce-config';

import { CommonModule } from '@angular/common';

import { CdkTableModule } from '@angular/cdk/table';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { SharedModule } from '@src/app/shared-module/shared.module';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search';
import { MatDialog } from '@angular/material/dialog';
import {
	OwlDateTimeModule,
	OwlNativeDateTimeModule,
} from '@danielmoncada/angular-datetime-picker';

@Injectable()
export class RenderDetailHelper {
	public application: any;

	public page = null;
	public scrollLayout = null;
	public stackContainer = null;

	// private  topicSend :string;
	// public stompClientManager: any;
	// public stompClientRest: any;
	public dataMaterialModule: any = {
		imports: [
			CommonModule,
			FormsModule,
			ReactiveFormsModule,
			FlexLayoutModule,
			CdkTableModule,
			MatAutocompleteModule,
			MatButtonModule,
			MatCardModule,
			MatCheckboxModule,
			MatChipsModule,
			MatDatepickerModule,
			MatDialogModule,
			MatExpansionModule,
			MatIconModule,
			MatInputModule,
			MatListModule,
			MatMenuModule,
			MatNativeDateModule,
			MatRadioModule,
			MatRippleModule,
			MatSelectModule,
			MatSlideToggleModule,
			MatSidenavModule,
			MatTableModule,
			MatToolbarModule,
			MatTooltipModule,
			TranslateModule,
			SharedModule,
			OwlDateTimeModule,
			OwlNativeDateTimeModule,
			MatTabsModule,
			NgxMatSelectSearchModule,
			MatPaginatorModule,
			MatSortModule,
			PdfViewerModule,
		],
		exports: [],
	};
	public config = config;
	constructor(
		public dialog:MatDialog,
		public _snackBar: MatSnackBar,
		private translateService: TranslateService
	) {
		this.config['height'] = '100%';
	}

	public configSetup() {
		this.config['setup'] = (ed) => {
			ed.on('SetContent', function (ed1) {
				ed1.target.editorCommands.execCommand('fontName', false, 'Arial');
			});
		};
	}

	public checkPressToEnterhelper(enterToSend) {
		this.config['setup'] = (ed) => {
			ed.on('SetContent', function (ed1) {
				ed1.target.editorCommands.execCommand('fontName', false, 'Arial');
			});
			ed.on('KeyUp', (e) => {
				if (enterToSend && e.keyCode === 13) {
					return e.keyCode;
				}
			});
			ed.on('KeyDown', (e) => {
				if (enterToSend && e.keyCode === 13) {
					e.preventDefault();
				}
			});
		};

		return null;
	}

	public snackbarHelper() {
		this._snackBar
			.open(this.translateService.instant('DATA_UPDATED'), 'OK', {
				horizontalPosition: 'center',
			})
			.afterDismissed()
			.subscribe((data) => {
				return data;
			});
		return null;
	}

	public countryDialCodeDialogHelper(countryName, formControlsMobile) {}

	public bannerNotification(message) {}

	public pickListDialog(value, field, formController) {}

	public navigateToSidebar(path) {}

	public isAndroid() {
		return false;
	}

	public isIOS() {
		return false;
	}
	// public keyboardHelper(stackPanelHeight){
	// 	return null;
	// }

	public scrollBottom(args) {}

	public navigateToListLayout(moduleId) {}

	public disableKeyboard(args) {}
	public getEntryValues() {
		return null;
	}
	public setshowBackButton() {}
	public subscribeToTopic(sessionUuid) {}

	// public connect(urlManager,urlRest) {

	// }

	// public subscribeToTopic(topic){

	// }

	// public disconnect() {

	// }

	// public sendMessage(topic,message) {

	// }

}
