import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { CompaniesService } from 'src/app/companies/companies.service';
import { BannerMessageService } from 'src/app/custom-components/banner-message/banner-message.service';
import { CustomTableService } from 'src/app/custom-table/custom-table.service';
import { ModulesService } from 'src/app/modules/modules.service';

import {
	FormBuilder,
	FormGroup,
	FormsModule,
	ReactiveFormsModule,
	Validators,
} from '@angular/forms';

import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSpinner } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderChange, MatSliderModule } from '@angular/material/slider';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { SharedModule } from '@src/app/shared-module/shared.module';
import { UsersService } from 'src/app/users/users.service';
import { CommonModule } from '@angular/common';

@Component({
	selector: 'app-forms-detail',
	templateUrl: './forms-detail.component.html',
	styleUrls: ['./forms-detail.component.scss'],
})
export class FormsDetailComponent implements OnInit {
	public moduleId;
	private module: any;
	public errorMessage: string;
	public forms: FormGroup;
	public fieldsMap = {};
	public requiredFields = [];
	public cellFlexSize = 100 / 3;
	public isLoading = true;
	public grids: any[][] = [[]];
	public showView = {};
	public script;
	public dropList = [];
	public customLayout = '<html><mat-spinner></mat-spinner></html>';
	public fields = [];
	public allFields = [];
	public formId = '';
	public saveButton = {
		LABEL: 'Submit',
		ALIGNMENT: 'end',
		SAVE_TYPE: 'message',
		SAVE_MESSAGE: 'Thanks for submitting the form.',
		URL: '',
	};
	public loading = true;
	public subdomain: String;
	public layoutStyle = 'outline';
	public dataMaterialModule: any = {
		imports: [
			FormsModule,
			ReactiveFormsModule,
			DragDropModule,
			FlexLayoutModule,
			MatAutocompleteModule,
			MatButtonModule,
			MatCardModule,
			MatCheckboxModule,
			MatChipsModule,
			MatDatepickerModule,
			MatIconModule,
			MatInputModule,
			MatListModule,
			MatMenuModule,
			MatNativeDateModule,
			MatRadioModule,
			MatRippleModule,
			MatSelectModule,
			MatSidenavModule,
			MatSlideToggleModule,
			MatToolbarModule,
			MatTooltipModule,
			MatTabsModule,
			SharedModule,
			TranslateModule,
			MatSliderModule,
			CommonModule,
		],
		exports: [],
	};

	constructor(
		private router: Router,
		private route: ActivatedRoute,
		private translateService: TranslateService,
		private modulesService: ModulesService,
		private bannerMessageService: BannerMessageService,
		public customTableService: CustomTableService,
		public companiesService: CompaniesService,
		private _formBuilder: FormBuilder,
		private usersService: UsersService
	) {
		this.subdomain = this.usersService.getSubdomain();
	}

	public ngOnInit() {
		this.forms = this._formBuilder.group({
			NAME: ['', Validators.required],
			DESCRIPTION: [''],
		});

		this.moduleId = this.route.snapshot.params['moduleId'];
		this.formId = this.route.snapshot.params['formId'];
		this.script =
			'<script id ="ngdesk-form-widget"> var script = document.createElement("script"); script.type = "text/javascript"; script.src = "https://' +
			this.subdomain +
			'.ngdesk.com/forms-widget/' +
			this.moduleId +
			'/' +
			this.formId +
			'/ngdesk_form_widget.js";document.getElementsByTagName("head")[0].appendChild(script); </script>';

		this.modulesService.getModuleById(this.moduleId).subscribe(
			(response: any) => {
				this.module = response;
				this.fields = JSON.parse(JSON.stringify(this.module.FIELDS));
				this.allFields = JSON.parse(JSON.stringify(this.module.FIELDS));
				this.module.FIELDS = this.module.FIELDS.filter(
					(field) =>
						field.NAME !== 'CREATED_BY' &&
						field.NAME !== 'SOURCE_TYPE' &&
						field.NAME !== 'CHANNEL' &&
						field.NAME !== 'DATE_CREATED' &&
						field.NAME !== 'LAST_UPDATED_BY' &&
						field.NAME !== 'DATE_UPDATED' &&
						field.NAME !== 'PASSWORD' &&
						field.NAME !== 'DELETED' &&
						field.DATA_TYPE.DISPLAY !== 'Relationship' &&
						field.DATA_TYPE.DISPLAY !== 'Chronometer' &&
						field.DATA_TYPE.DISPLAY !== 'Auto Number'
				);
				this.module.FIELDS.forEach((field) => {
					if (field.REQUIRED && field.DATA_TYPE.DISPLAY !== 'Auto Number') {
						this.requiredFields.push(field.FIELD_ID);
					}
					this.fieldsMap[field.FIELD_ID] = field;
				});

				if (this.formId !== 'new') {
					this.modulesService
						.getForm(this.moduleId, this.formId)
						.subscribe((form: any) => {
							this.grids = form.GRIDS;
							this.forms.controls.NAME.setValue(form.NAME);
							this.forms.controls.DESCRIPTION.setValue(form.DESCRIPTION);
							if (
								form.hasOwnProperty('LAYOUT_STYLE') &&
								form.LAYOUT_STYLE !== null
							) {
								this.layoutStyle = form.LAYOUT_STYLE;
							}
							this.saveButton = form.SAVE_BUTTON;
							this.loadCustomLayout(this.grids);
						});
				} else {
					this.loadCustomLayout(this.grids);
				}
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}

	// DROP HERE TEMPLATE
	public initialTemplate(grids, i, j) {
		if (grids[i][j].WIDTH !== 0) {
			return `<div class='CELL_${i}_${j}'
    style="height: 40px;"
       fxLayoutAlign="start center"
       cdkDropList (cdkDropListDropped)="context.dropField($event)"
       id='${i}_${j}'
       [ngStyle]="{'border': '1px dashed #ccc','border-radius': '5px','min-width': '32.3%'}">
      <div class="mat-caption" style="color:#888;padding:10px;">
        Drop Here
      </div>
    </div><!--END_CELL_${i}_${j}-->`;
		} else {
			return `<div class='CELL_${i}_${j}' *ngIf="${grids[i][j].WIDTH} !== 0"></div><!--END_CELL_${i}_${j}-->`;
		}
	}

	// TEMPLATE AFTER DROP
	public replaceCellTemplate(grids, i, j) {
		// const index =
		//   this.customLayouts.findIndex(f => f.name === name) === -1
		//     ? this.globalIndex
		//     : this.customLayouts.findIndex(f => f.name === name);
		const fieldId = grids[i][j].FIELD_ID;
		const displayLabel = this.fieldsMap[fieldId].DISPLAY_LABEL;
		return `<div class='CELL_${i}_${j} mat-caption'
    fxLayout="row" fxLayoutAlign="center center"
  [ngStyle]="{ 'border': '1px solid #ccc','border-radius': '5px', 'height': '40px','background-color':'rgb(235, 236, 236)', 'min-width': (context.grids[${i}][${j}].WIDTH == 33) ? '32.3%' : (context.grids[${i}][${j}].WIDTH == 66) ? '65%' : (context.grids[${i}][${j}].WIDTH) ? '97.6%' : '0%'}">
  <div fxFlex=90 *ngIf="context.showView.CELL_${i}_${j} ==='LABEL'"
    class="mat-body" style="padding: 10px;">
        <span>{{context.fieldsMap['${fieldId}'].DISPLAY_LABEL}}</span>
  </div>

    <div fxFlex=90 *ngIf="context.showView.CELL_${i}_${j} ==='EDIT'">
    <mat-label style="padding-left:10px"> Width : </mat-label>
    <mat-slider (change)="context.resizeField(${i},${j},$event)"
    color='primary'
    thumbLabel
    [min]="context.determineMinSliderValue(${i},${j})"
    [max]="context.determineMaxSliderValue(${i},${j})"
    [step]="context.getStepValue(${i},${j})"
    [displayWith]="context.displayFn"
    [value]="context.determineSliderValue(${i},${j})">
  </mat-slider>
  </div>

    <div class="pointer" fxFlex fxLayoutAlign="end center">
    <span (click)="context.toggleView(${i},${j})"><mat-icon class="layout-icons mat-caption"
     class="grey-black-color" fontSet="material-icons-outlined" matTooltip="{{'RESIZE'|translate}}">
	 <img src="../../assets/icons/pencil_icon.svg" style="width:15px;height:15px;"></mat-icon></span>
    <span (click)="context.removeField(${i},${j})">
	<mat-icon class="layout-icons" class="grey-black-color" fontSet="material-icons-outlined" matTooltip="{{'REMOVE'|translate}}">
	<div style="padding-right:5px">
	<img src="../../assets/icons/delete_icon.svg" style="width:15px;height:15px;"> </div></mat-icon></span>
    </div>
</div>
<!--END_CELL_${i}_${j}-->`;
	}

	// DISPLAY FOR SLIDER
	private displayFn(value) {
		return value + 1 + '%';
	}

	// SLIDER MIN VALUE
	public determineMinSliderValue(i, j) {
		return Math.floor(100 / 3);
	}

	// SLIDER STEP
	public getStepValue(i, j) {
		return Math.floor(100 / 3);
	}

	// SLIDER VALUE
	public determineSliderValue(i, j) {
		return this.grids[i][j].WIDTH;
	}

	// SLIDER MAX VALUE TO WHAT EVER IT CAN BE RESIZED TO
	public determineMaxSliderValue(i, j) {
		let max = this.grids[i][j].WIDTH;
		for (let f = j + 1; f < 3; f++) {
			if (this.grids[i][f].IS_EMPTY) {
				max = max + this.grids[i][f].WIDTH;
			} else {
				break;
			}
		}
		return max;
	}

	public toggleView(i, j) {
		if (this.showView[`CELL_${i}_${j}`] === 'LABEL') {
			this.showView[`CELL_${i}_${j}`] = 'EDIT';
		} else {
			this.showView[`CELL_${i}_${j}`] = 'LABEL';
		}
	}

	public loadCustomLayout(grids) {
		let layout = `<div fxLayout="column" fxLayoutGap=5px fxFlex>`;
		const fields = [];
		const size = grids[0].length === 0 ? 10 : grids.length;
		for (let i = 0; i < size; i++) {
			layout = layout + `<div class='ROW_${i}' fxLayout="row" fxLayoutGap=5px>`;
			grids[i] = grids[i] ? grids[i] : [];
			for (let j = 0; j < 3; j++) {
				grids[i][j] = grids[i][j]
					? grids[i][j]
					: {
							IS_EMPTY: true,
							HEIGHT: 10,
							WIDTH: Math.floor(100 / 3),
							FIELD_ID: '',
					  };
				// USED FOR LINKING THE DRAG AND DROP
				this.dropList.push(`${i}_${j}`);

				// TO CONTROL THE VIEW (EDIT/LABEL)
				this.showView[`CELL_${i}_${j}`] = 'LABEL';
				if (grids[i][j].IS_EMPTY) {
					layout = layout + this.initialTemplate(grids, i, j);
				} else {
					this.module.FIELDS = this.module.FIELDS.filter(
						(element) => element.FIELD_ID !== grids[i][j].FIELD_ID
					);
					if (
						fields.map((v) => v.FIELD_ID).indexOf(grids[i][j].FIELD_ID) === -1
					) {
						fields.push(
							this.allFields.find((f) => f.FIELD_ID === grids[i][j].FIELD_ID)
						);
					}
					layout = layout + this.replaceCellTemplate(grids, i, j);
				}
			}
			layout =
				layout +
				`<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
				(click)="context.gridRow(${i}, -1)">close</mat-icon></div></div><!--END_ROW_${i}-->`;
		}
		layout = layout + `</div>`;
		// FINAL LAYOUT USED BY P3X
		this.customLayout = `<!--CUSTOM_LAYOUT_START-->
            <!--START_REPLACABLE_LAYOUT-->
            ${layout}
      <!--END_REPLACABLE_LAYOUT-->`;
		this.loading = false;

		return fields;
	}

	public resizeField(i, j, event: MatSliderChange) {
		this.toggleView(i, j);
		if (event.value > this.grids[i][j].WIDTH) {
			this.removeCells(i, j + 1, event.value);
		} else {
			let maxGrid = j;
			for (let y = j + 1; y < 3; y++) {
				if (!this.grids[i][y].IS_EMPTY || this.grids[i][y].WIDTH !== 0) {
					break;
				}
				maxGrid = y;
			}
			this.addCells(i, maxGrid, this.grids[i][j].WIDTH - event.value);
		}
		this.grids[i][j].WIDTH = Math.floor(event.value);
	}

	public removeCells(i, j, width) {
		if (
			width > this.determineMinSliderValue(i, j) &&
			this.grids[i][j] !== undefined
		) {
			this.grids[i][j].WIDTH = 0;
			const cellRegex = new RegExp(
				`<div class='CELL_${i}_${j}([\\s\\S]*?)<!--END_CELL_${i}_${j}-->`
			);
			this.customLayout = this.customLayout.replace(
				cellRegex,
				`<div class='CELL_${i}_${j}' *ngIf="${this.grids[i][j].WIDTH} !== 0"></div><!--END_CELL_${i}_${j}-->`
			);
			return this.removeCells(
				i,
				j + 1,
				width - this.determineMinSliderValue(i, j)
			);
		} else {
			return;
		}
	}

	// GENERIC FUNCTION TO ADD THE CELLS ON DELETE
	public addCells(i, j, width) {
		if (width >= this.determineMinSliderValue(i, j)) {
			const cellRegex = new RegExp(
				`<div class='CELL_${i}_${j}([\\s\\S]*?)<!--END_CELL_${i}_${j}-->`
			);
			this.grids[i][j].WIDTH = this.determineMinSliderValue(i, j);
			this.customLayout = this.customLayout.replace(
				cellRegex,
				this.initialTemplate(this.grids, i, j)
			);
			return this.addCells(
				i,
				j - 1,
				width - this.determineMinSliderValue(i, j)
			);
		} else {
			return;
		}
	}

	public gridRow(index, num) {
		if (num === -1) {
			if (this.grids.length > 1) {
				this.grids[index].forEach((v) => {
					if (!v.IS_EMPTY) {
						if (this.module.FIELDS.indexOf(v.FIELD_ID) === -1) {
							this.module.FIELDS.push(
								this.fields.find((f) => f.FIELD_ID === v.FIELD_ID)
							);
						}
					}
				});
				this.grids.splice(index, 1);
			} else {
				this.bannerMessageService.errorNotifications.push({
					message: 'Cannot remove the only row',
				});
			}
		} else {
			if (this.grids.length < 20) {
				const grid = [];
				for (let j = 0; j < 3; j++) {
					grid[j] = {
						IS_EMPTY: true,
						HEIGHT: 10,
						WIDTH: Math.floor(100 / 3),
						FIELD_ID: '',
					};
				}
				this.grids.push(grid);
			} else {
				this.bannerMessageService.errorNotifications.push({
					message: 'Maximum 20 rows allowed',
				});
			}
		}
		const fields = this.loadCustomLayout(this.grids);
		this.customLayout = this.customLayout;
		this.fields = fields;
	}

	public dropField(event: CdkDragDrop<string[]>) {
		// GETTING FIELD ID FROM THE ELEMENT
		const fieldId = event.item.element.nativeElement.id;

		// POSITION FROM THE ELEMENT
		const droppedIndex = event.container.element.nativeElement.id.substr(
			event.container.element.nativeElement.id.indexOf('_', 6) + 1
		);
		const xPos = parseInt(droppedIndex.split('_')[0], 10);
		const yPos = parseInt(droppedIndex.split('_')[1], 10);

		// FIND THE FIELD
		const field = this.module.FIELDS.find(
			(element) => element.FIELD_ID === fieldId
		);

		const cellRegex = new RegExp(
			`<div class='CELL_${xPos}_${yPos}([\\s\\S]*?)<!--END_CELL_${xPos}_${yPos}-->`
		);

		// REMOVE THE FIELD FROM THE LIST
		this.module.FIELDS = this.module.FIELDS.filter(
			(element) => element.FIELD_ID !== fieldId
		);

		// SET THE GRID WITH THE FIELD
		this.grids[xPos][yPos].IS_EMPTY = false;
		this.grids[xPos][yPos].FIELD_ID = fieldId;
		this.customLayout = this.customLayout.replace(
			cellRegex,
			this.replaceCellTemplate(this.grids, xPos, yPos)
		);
	}

	public removeField(i, j) {
		const removedField = this.fieldsMap[this.grids[i][j].FIELD_ID];
		this.module.FIELDS.unshift(removedField);
		this.fields = this.fields.filter(
			(f) => f.FIELD_ID !== this.grids[i][j].FIELD_ID
		);

		this.grids[i][j].IS_EMPTY = true;
		let maxGrid = j;
		for (let y = j + 1; y < 3; y++) {
			if (!this.grids[i][y].IS_EMPTY || this.grids[i][y].WIDTH !== 0) {
				break;
			}
			maxGrid = y;
		}
		this.addCells(i, maxGrid, this.grids[i][j].WIDTH);
	}

	public save() {
		// if (!this.checkRequiredFields()) {
		// 	return;
		// }
		const formsToSave = JSON.parse(JSON.stringify(this.forms.value));
		formsToSave['GRIDS'] = this.grids;
		formsToSave['FORM_ID'] = this.formId;
		formsToSave['LAYOUT_STYLE'] = this.layoutStyle;
		formsToSave['SAVE_BUTTON'] = this.saveButton;
		if (this.formId === 'new') {
			this.modulesService.postForm(this.moduleId, formsToSave).subscribe(
				(form) => {
					const formValue = JSON.parse(JSON.stringify(form));
					this.companiesService.trackEvent(`Created Form`, {
						FORM_ID: formValue.FORM_ID,
						MODULE_ID: this.moduleId,
					});
					this.router.navigate([`modules/${this.moduleId}/forms`]);
				},
				(error) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					});
				}
			);
		} else {
			this.modulesService
				.putForm(this.moduleId, formsToSave, this.formId)
				.subscribe(
					(form) => {
						this.companiesService.trackEvent(`Updated Form`, {
							FORM_ID: this.formId,
							MODULE_ID: this.moduleId,
						});
						this.router.navigate([`modules/${this.moduleId}/forms`]);
					},
					(error) => {
						this.bannerMessageService.errorNotifications.push({
							message: error.error.ERROR,
						});
					}
				);
		}
	}

	public copyToClipboard() {
		const selBox = document.createElement('textarea');
		selBox.style.position = 'fixed';
		selBox.style.left = '0';
		selBox.style.top = '0';
		selBox.style.opacity = '0';
		selBox.value = this.script;
		document.body.appendChild(selBox);
		selBox.focus();
		selBox.select();
		document.execCommand('copy');
		document.body.removeChild(selBox);
		this.bannerMessageService.successNotifications.push({
			message: this.translateService.instant('COPIED'),
		});
	}

	// public checkRequiredFields() {
	// 	const fieldsInGrid = [];
	// 	for (let i = 0; i < this.grids.length; i++) {
	// 		const grid = this.grids[i];
	// 		for (let j = 0; j < grid.length; j++) {
	// 			if (!grid[j].IS_EMPTY) {
	// 				fieldsInGrid.push(grid[j].FIELD_ID);
	// 			}
	// 		}
	// 	}
	// 	let requiredFieldDropped = true;
	// 	const requiredFieldsName = [];
	// 	this.requiredFields.forEach(requiredField => {
	// 		if (fieldsInGrid.indexOf(requiredField) === -1) {
	// 			requiredFieldsName.push(this.fieldsMap[requiredField].DISPLAY_LABEL);
	// 			requiredFieldDropped = false;
	// 		}
	// 	});
	// 	if (!requiredFieldDropped) {
	// 		this.bannerMessageService.errorNotifications.push({
	// 			message:
	// 				requiredFieldsName.join(',') +
	// 				' ' +
	// 				this.translateService.instant('REQUIRED_TO_SAVE')
	// 		});
	// 		return false;
	// 	}
	// 	return true;
	// }
}
