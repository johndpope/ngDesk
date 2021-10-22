import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FlexLayoutModule } from '@angular/flex-layout';
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
import { MatChipInputEvent, MatChipsModule } from '@angular/material/chips';
import { MatNativeDateModule, MatRippleModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderChange, MatSliderModule } from '@angular/material/slider';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, Router } from '@angular/router';
import { Form, FormApiService } from '@ngdesk/module-api';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { LoaderService } from '@src/app/custom-components/loader/loader.service';
import { ModulesService } from '@src/app/modules/modules.service';
import { SharedModule } from '@src/app/shared-module/shared.module';
import { WorkflowApiService } from '@ngdesk/workflow-api';
import { forkJoin, Subject } from 'rxjs';
import {
	debounceTime,
	distinctUntilChanged,
	map,
	switchMap,
} from 'rxjs/operators';
import { ServiceCatalogueService } from './service-catalogue.service';

@Component({
	selector: 'app-service-catalogue-detail',
	templateUrl: './service-catalogue-detail.component.html',
	styleUrls: ['./service-catalogue-detail.component.scss'],
})
export class ServiceCatalogueDetailComponent implements OnInit {
	public forms: FormGroup;
	public module: any;
	public workflow: any;
	public workflows: any;
	public layoutStyle = 'outline';
	public serviceCatalogueId;
	public dropList = [];
	public showView = {};
	public cellFlexSize = 25;
	public loading = true;
	public fields = [];
	public allFields = [];
	public fieldsMap: any = [];
	public customLayout = '';
	public requiredFields: any[] = [];
	public image = '';
	public requiredLayoutFields = [];
	public rowSize = 10;
	public customLayouts = [];
	public globalIndex = 0;
	public visibleTo = [];
	public teamNames = [];
	public moduleId;
	public teamsModule;
	public step = 0;
	public teamScrollSubject = new Subject<any>();

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

	public workflowSubjects = new Subject<any>();
	public selectedValues: any[] = [];

	constructor(
		private _formBuilder: FormBuilder,
		private modulesService: ModulesService,
		private cd: ChangeDetectorRef,
		private route: ActivatedRoute,
		private bannerMessageService: BannerMessageService,
		private formApi: FormApiService,
		private serviceCatalogueService: ServiceCatalogueService,
		private router: Router,
		private workflowApiService: WorkflowApiService,
		private translateService: TranslateService,
		private loaderService: LoaderService
	) {}

	ngOnInit(): void {
		this.moduleId = this.route.snapshot.paramMap.get('moduleId');
		this.serviceCatalogueId =
			this.route.snapshot.paramMap.get('serviceCatalogueId');
		this.forms = this._formBuilder.group({
			name: ['', Validators.required],
			description: [''],
			visibleTo: [''],
		});
		this.setWorkflows();
		this.initializeWorkflow();
		this.modulesService.getModuleByName('Teams').subscribe((response: any) => {
			this.teamsModule = response;
			this.serviceCatalogueService
				.getTeamsData(0, '', this.teamsModule)
				.subscribe((teamResponse) => {
					this.teamNames = teamResponse['DATA'];
					this.teamsDataScroll();
				});
		});
		if (this.serviceCatalogueId && this.serviceCatalogueId === 'new') {
			this.modulesService
				.getModuleById(this.moduleId)
				.subscribe((response: any) => {
					this.loading = false;
					this.module = response;
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
							field.NAME !== 'EFFECTIVE_TO' &&
							field.NAME !== 'EFFECTIVE_FROM' &&
							field.DATA_TYPE.DISPLAY !== 'Time Window' &&
							field.DATA_TYPE.DISPLAY !== 'Approval' &&
							field.DATA_TYPE.DISPLAY !== 'Aggregate' &&
							field.DISPLAY_LABEL !== 'Data ID' &&
							field.DISPLAY_LABEL !== 'Post Id' &&
							field.DATA_TYPE.DISPLAY !== 'Button' &&
							field.DATA_TYPE.DISPLAY !== 'File Upload' &&
							field.DATA_TYPE.DISPLAY !== 'File Preview' &&
							field.DATA_TYPE.DISPLAY !== 'Formula' &&
							field.DATA_TYPE.DISPLAY !== 'Image' &&
							field.DATA_TYPE.DISPLAY !== 'PDF' &&
							field.DATA_TYPE.DISPLAY !== 'Chronometer' &&
							field.DATA_TYPE.DISPLAY !== 'Auto Number'
					);
					this.fields = JSON.parse(JSON.stringify(this.module.FIELDS)); // Making a fresh copy
					this.allFields = response.FIELDS;
					this.module.FIELDS.forEach((field) => {
						if (field.REQUIRED && field.DATA_TYPE.DISPLAY !== 'Auto Number') {
							this.requiredFields.push(field.FIELD_ID);
						}
						this.fieldsMap[field.FIELD_ID] = field;
					});
					this.loadNewGridAndView(null);
				});
		} else {
			forkJoin([
				this.modulesService.getModuleById(this.moduleId),
				this.serviceCatalogueService.getForm(
					this.moduleId,
					this.serviceCatalogueId
				),
			]).subscribe((response: any) => {
				this.loading = false;
				this.module = response[0];
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
						field.NAME !== 'EFFECTIVE_TO' &&
						field.NAME !== 'EFFECTIVE_FROM' &&
						field.DATA_TYPE.DISPLAY !== 'Time Window' &&
						field.DATA_TYPE.DISPLAY !== 'Approval' &&
						field.DISPLAY_LABEL !== 'Data ID' &&
						field.DISPLAY_LABEL !== 'Post Id' &&
						field.DATA_TYPE.DISPLAY !== 'Button' &&
						field.DATA_TYPE.DISPLAY !== 'File Upload' &&
						field.DATA_TYPE.DISPLAY !== 'File Preview' &&
						field.DATA_TYPE.DISPLAY !== 'Formula' &&
						field.DATA_TYPE.DISPLAY !== 'Image' &&
						field.DATA_TYPE.DISPLAY !== 'PDF' &&
						field.DATA_TYPE.DISPLAY !== 'Chronometer' &&
						field.DATA_TYPE.DISPLAY !== 'Aggregate' &&
						field.DATA_TYPE.DISPLAY !== 'Auto Number'
				);
				this.fields = JSON.parse(JSON.stringify(this.module.FIELDS)); // Making a fresh copy
				this.allFields = this.module.FIELDS;
				this.module.FIELDS.forEach((field) => {
					if (field.REQUIRED && field.DATA_TYPE.DISPLAY !== 'Auto Number') {
						this.requiredFields.push(field.FIELD_ID);
					}
					this.fieldsMap[field.FIELD_ID] = field;
				});
				const serviceCatalogue = response[1].FORM;
				serviceCatalogue['visibleTo'].forEach((element) => {
					this.visibleTo.push(element._id);
				});
				this.initializeTeam();
				this.forms.get('visibleTo').setValue(this.visibleTo);
				this.forms.setValue({
					name: serviceCatalogue.name,
					description: serviceCatalogue.description,
					visibleTo: serviceCatalogue.visibleTo,
				});
				this.setSelectedValues(serviceCatalogue);
				this.image = serviceCatalogue.displayImage;
				this.layoutStyle = serviceCatalogue.layoutStyle;
				this.workflow = serviceCatalogue.workflow;
				// Load Layout
				serviceCatalogue.panels.forEach((value) => {
					this.loadNewGridAndView(value);
					this.globalIndex++;
				});
			});
		}
	}

	public initializeTeam() {
		const teamIds = this.visibleTo;
		let teamData;
		const teamObj = [];
		teamIds.forEach((teamId) => {
			if (teamId) {
				this.serviceCatalogueService
					.getForm(this.moduleId, this.serviceCatalogueId)
					.subscribe((response) => {
						const data = response.FORM;
						data['visibleTo'].forEach((element) => {
							if (element._id === teamId) {
								teamData = {
									name: element['NAME'],
									id: element['_id'],
								};
								teamObj.push(teamData);
							}
						});
					});
			}
		});
		this.visibleTo = teamObj;
		const newTeams = [];
		this.teamNames.forEach((team) => {
			const data = teamObj.find((teamData) => teamData.id === team.id);
			if (!data) {
				newTeams.push(team);
			}
		});
		this.teamNames = newTeams;
	}

	public loadNewGridAndView(value) {
		this.rowSize = 10;
		if (this.customLayouts.length === 0) {
			this.module.FIELDS = JSON.parse(JSON.stringify(this.fields));
		}
		let name = '';
		let panelDisplayName = '';
		let grids = [[]];
		if (value) {
			this.rowSize = value.grids.length;
			grids = value.grids.map((grids: any[]) => {
				return grids.map((grid) => {
					return {
						empty: grid.empty,
						height: grid.height,
						width: grid.width,
						fieldId: grid.fieldId,
					};
				});
			});
			panelDisplayName = value.panelDisplayName;
			name = value.ID;
		} else {
			name = `PANEL_${this.customLayouts.length + 1}`;
			// DEFAULT GRID LAYOUT
			panelDisplayName = this.toTitleCase(name);
			this.rowSize = this.rowSize < 1 || this.rowSize > 20 ? 20 : this.rowSize;
		}
		const fields = this.loadCustomLayout(name, grids);
		this.customLayouts.push({
			name,
			panelDisplayName,
			customLayout: this.customLayout,
			collapse: value ? value.collapse : false,
			grids: JSON.parse(JSON.stringify(grids)),
			fields,
		});
	}

	private toTitleCase(phrase: string) {
		return phrase
			.toLowerCase()
			.split('_')
			.map((word) => word.charAt(0).toUpperCase() + word.slice(1))
			.join(' ');
	}

	public gridRow(name, index, num) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (num === -1) {
			if (custom.grids.length > 1) {
				custom.grids[index].forEach((v) => {
					if (!v.empty) {
						if (this.module.FIELDS.indexOf(v.fieldId) === -1) {
							this.module.FIELDS.push(
								this.fields.find((f) => {
									return f.FIELD_ID === v.fieldId;
								})
							);
						}
					}
				});
				custom.grids.splice(index, 1);
			} else {
				this.bannerMessageService.errorNotifications.push({
					message: 'Cannot remove the only row',
				});
			}
		} else {
			if (custom.grids.length < 20) {
				const grid = [];
				for (let j = 0; j < 4; j++) {
					grid[j] = {
						empty: true,
						height: 10,
						width: Math.floor(100 / 4),
						fieldId: '',
					};
				}
				custom.grids.push(grid);
			} else {
				this.bannerMessageService.errorNotifications.push({
					message: 'Maximum 20 rows allowed',
				});
			}
		}
		const fields = this.loadCustomLayout(name, custom.grids);
		custom.customLayout = this.customLayout;
		custom.fields = fields;
	}

	public dropField(name: string, event: CdkDragDrop<string[]>) {
		const custom = this.customLayouts.find((f) => f.name === name);
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
			`<div class='CELL_${name}_${xPos}_${yPos}([\\s\\S]*?)<!--END_CELL_${name}_${xPos}_${yPos}-->`
		);

		// REMOVE THE FIELD FROM THE LIST
		this.module.FIELDS = this.module.FIELDS.filter(
			(element) => element.FIELD_ID !== fieldId
		);

		// SET THE GRID WITH THE FIELD
		custom.grids[xPos][yPos].empty = false;
		custom.grids[xPos][yPos].fieldId = fieldId;
		custom.customLayout = custom.customLayout.replace(
			cellRegex,
			this.replaceCellTemplate(custom.grids, name, xPos, yPos)
		);
	}

	public removeTeam(element): void {
		const index = this.visibleTo.indexOf(element);
		if (index >= 0) {
			const data = this.visibleTo;
			data.splice(index, 1);
			this.selectedValues.splice(index, 1);
		}
	}

	public resetInput(event: MatChipInputEvent): void {
		const input = event.input;
		if (input) {
			input.value = '';
		}
	}

	public addTeam(event) {
		this.visibleTo.push(event.option.value);
		const newTeam = [];
		this.teamNames.forEach((team) => {
			if (team.id !== event.option.value.id) {
				newTeam.push(team);
			}
		});
		this.selectedValues.push(event.option.value.id);
		this.teamNames = newTeam;
	}

	public removeLayout(index) {
		this.customLayouts[index].fields.forEach((v) => this.module.FIELDS.push(v));
		this.customLayouts.splice(index, 1);
	}

	public removeField(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		const removedField = this.fieldsMap[custom.grids[i][j].fieldId];

		this.module.FIELDS.unshift(removedField);
		custom.fields = custom.fields.filter(
			(f) => f.FIELD_ID !== custom.grids[i][j].fieldId
		);
		custom.grids[i][j].empty = true;
		custom.grids[i][j].fieldId = '';
		let maxGrid = j;
		for (let y = j + 1; y < 4; y++) {
			if (!custom.grids[i][y].empty || custom.grids[i][y].width !== 0) {
				break;
			}
			maxGrid = y;
		}
		this.addCells(name, i, maxGrid, custom.grids[i][j].width);
	}

	// GENERIC FUNCTION TO ADD THE CELLS ON DELETE
	public addCells(name, i, j, width) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (width >= this.determineMinSliderValue(name, i, j)) {
			const cellRegex = new RegExp(
				`<div class='CELL_${name}_${i}_${j}([\\s\\S]*?)<!--END_CELL_${name}_${i}_${j}-->`
			);
			custom.grids[i][j].width = this.determineMinSliderValue(name, i, j);
			custom.customLayout = custom.customLayout.replace(
				cellRegex,
				this.initialTemplate(custom.grids, name, i, j)
			);
			return this.addCells(
				name,
				i,
				j - 1,
				width - this.determineMinSliderValue(name, i, j)
			);
		} else {
			return;
		}
	}

	// DISPLAY FOR SLIDER
	public displayFn(value) {
		return value + '%';
	}

	// SLIDER MIN VALUE
	public determineMinSliderValue(name, i, j) {
		return Math.floor(100 / 4);
	}

	// SLIDER STEP
	public getStepValue(name, i, j) {
		return Math.floor(100 / 4);
	}

	// SLIDER VALUE
	public determineSliderValue(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		return custom.grids[i][j].width;
	}

	// SLIDER MAX VALUE TO WHAT EVER IT CAN BE RESIZED TO
	public determineMaxSliderValue(name, i, j) {
		const custom = this.customLayouts.find((f) => f.name === name);
		let max = custom.grids[i][j].width;
		for (let f = j + 1; f < 4; f++) {
			if (custom.grids[i][f].empty) {
				max = max + custom.grids[i][f].width;
			} else {
				break;
			}
		}
		return max;
	}

	// DROP HERE TEMPLATE
	public initialTemplate(grids, name, i, j) {
		if (grids[i][j].width !== 0) {
			return `<div class='CELL_${name}_${i}_${j}'
    style="height: 40px;"
       fxLayoutAlign="start center"
       cdkDropList (cdkDropListDropped)="context.dropField('${name}', $event)"
       id='${name}_${i}_${j}'
       [ngStyle]="{'border': '1px dashed #ccc','border-radius': '5px','min-width': '24%'}">
      <div class="mat-caption" style="color:#888;padding:10px;">
        Drop Here
      </div>
    </div><!--END_CELL_${name}_${i}_${j}-->`;
		} else {
			return `<div class='CELL_${name}_${i}_${j}' *ngIf="${grids[i][j].width} !== 0"></div><!--END_CELL_${name}_${i}_${j}-->`;
		}
	}

	public toggleView(name, i, j) {
		if (this.showView[`CELL_${name}_${i}_${j}`] === 'LABEL') {
			this.showView[`CELL_${name}_${i}_${j}`] = 'EDIT';
		} else {
			this.showView[`CELL_${name}_${i}_${j}`] = 'LABEL';
		}
	}

	public replaceCellTemplate(grids, name, i, j) {
		const index =
			this.customLayouts.findIndex((f) => f.name === name) === -1
				? this.globalIndex
				: this.customLayouts.findIndex((f) => f.name === name);
		const fieldId = grids[i][j].fieldId;
		this.cellFlexSize = 25;
		return `<div class='CELL_${name}_${i}_${j} mat-caption'
    fxLayout="row" fxLayoutAlign="center center"
  [ngStyle]="{ 'border': '1px solid #ccc','border-radius': '5px', 'height': '40px','background-color':'rgb(235, 236, 236)', 'min-width': context.customLayouts[${index}].grids[${i}][${j}].width -1 + '%'} ">
  <div fxFlex=90 *ngIf="context.showView.CELL_${name}_${i}_${j} ==='LABEL'"
    class="mat-body" style="padding: 10px;">
        <span>{{context.fieldsMap['${fieldId}'].DISPLAY_LABEL}}</span>
  </div>

    <div fxFlex=90 *ngIf="context.showView.CELL_${name}_${i}_${j} ==='EDIT'">
    <mat-label style="padding-left:10px"> width : </mat-label>
    <mat-slider (change)="context.resizeField('${name}',${i},${j},$event)"
    color='primary'
    thumbLabel
    [min]="context.determineMinSliderValue('${name}',${i},${j})"
    [max]="context.determineMaxSliderValue('${name}',${i},${j})"
    [step]="context.getStepValue('${name}',${i},${j})"
    [displayWith]="context.displayFn"
    [value]="context.determineSliderValue('${name}',${i},${j})">
  </mat-slider>
  </div>

    <div class="pointer" fxFlex fxLayoutAlign="end center">
    <span (click)="context.toggleView('${name}',${i},${j})"><mat-icon class="layout-icons mat-caption"
     class="grey-black-color" fontSet="material-icons-outlined" matTooltip="{{'RESIZE'|translate}}">
	 <img src="../../assets/icons/pencil_icon.svg" style="width:15px;height:15px;"></mat-icon></span>
    <span (click)="context.removeField('${name}',${i},${j})">
	<mat-icon class="layout-icons" class="grey-black-color" fontSet="material-icons-outlined" matTooltip="{{'REMOVE'|translate}}">
	<div style="padding-right:5px">
	<img src="../../assets/icons/delete_icon.svg" style="width:15px;height:15px;"> </div></mat-icon></span>
    </div>
</div>
<!--END_CELL_${name}_${i}_${j}-->`;
	}

	public loadCustomLayout(name, grids) {
		let layout = `<div fxLayout="column" fxLayoutGap=5px fxFlex>`;
		const fields = [];
		const size = grids[0].length === 0 ? 10 : grids.length;
		for (let i = 0; i < size; i++) {
			layout = layout + `<div class='ROW_${i}' fxLayout="row" fxLayoutGap=5px>`;
			grids[i] = grids[i] ? grids[i] : [];
			for (let j = 0; j < 4; j++) {
				grids[i][j] = grids[i][j]
					? grids[i][j]
					: {
							empty: true,
							height: 10,
							width: Math.floor(100 / 4),
							fieldId: '',
					  };
				// USED FOR LINKING THE DRAG AND DROP
				this.dropList.push(`${name}_${i}_${j}`);

				// TO CONTROL THE VIEW (EDIT/LABEL)
				this.showView[`CELL_${name}_${i}_${j}`] = 'LABEL';
				if (grids[i][j].empty) {
					layout = layout + this.initialTemplate(grids, name, i, j);
				} else {
					this.module.FIELDS = this.module.FIELDS.filter(
						(element) => element.FIELD_ID !== grids[i][j].fieldId
					);
					if (
						fields.map((v) => v.FIELD_ID).indexOf(grids[i][j].fieldId) === -1
					) {
						fields.push(
							this.allFields.find((f) => f.FIELD_ID === grids[i][j].fieldId)
						);
					}
					layout = layout + this.replaceCellTemplate(grids, name, i, j);
				}
			}
			layout =
				layout +
				`<div fxLayoutAlign="center center"><mat-icon style="cursor: pointer;"
				(click)="context.gridRow('${name}',${i}, -1)">close</mat-icon></div></div><!--END_ROW_${i}-->`;
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

	public resizeField(name, i, j, event: MatSliderChange) {
		this.toggleView(name, i, j);
		const custom = this.customLayouts.find((f) => f.name === name);
		if (event.value > custom.grids[i][j].width) {
			this.removeCells(name, i, j + 1, event.value);
		} else {
			let maxGrid = j;
			for (let y = j + 1; y < 4; y++) {
				if (!custom.grids[i][y].empty || custom.grids[i][y].width !== 0) {
					break;
				}
				maxGrid = y;
			}
			this.addCells(name, i, maxGrid, custom.grids[i][j].width - event.value);
		}
		custom.grids[i][j].width = Math.floor(event.value);
	}

	public removeCells(name, i, j, width) {
		const custom = this.customLayouts.find((f) => f.name === name);
		if (
			width > this.determineMinSliderValue(name, i, j) &&
			custom.grids[i][j] !== undefined
		) {
			custom.grids[i][j].width = 0;
			const cellRegex = new RegExp(
				`<div class='CELL_${name}_${i}_${j}([\\s\\S]*?)<!--END_CELL_${name}_${i}_${j}-->`
			);
			custom.customLayout = custom.customLayout.replace(
				cellRegex,
				`<div class='CELL_${name}_${i}_${j}' *ngIf="${custom.grids[i][j].width} !== 0"></div><!--END_CELL_${name}_${i}_${j}-->`
			);
			return this.removeCells(
				name,
				i,
				j + 1,
				width - this.determineMinSliderValue(name, i, j)
			);
		} else {
			return;
		}
	}

	public newLayout() {
		if (this.customLayout.indexOf('<mat-spinner></mat-spinner>') === -1) {
			this.loadNewGridAndView(null);
		}
	}

	public setStep(index) {
		this.step = index;
	}

	public save() {
		let formsToSave: any = {};
		let requiredLayoutFieldCheck: boolean = false;
		this.loaderService.isLoading = false;
		this.module.FIELDS.forEach((field) => {
			if (
				field.REQUIRED &&
				field.DATA_TYPE.DISPLAY !== 'Auto Number' &&
				field.DEFAULT_VALUE == null &&
				!requiredLayoutFieldCheck
			) {
				if (this.requiredLayoutFields != field.FIELD_ID) {
					var displayName = field.DISPLAY_LABEL;
					this.bannerMessageService.errorNotifications.push({
						message: this.translateService.instant('REQUIRED_LAYOUT_FIELD', {
							vars: displayName,
						}),
					});
				}
				requiredLayoutFieldCheck = true;
			}
		});

		if (this.forms.valid && requiredLayoutFieldCheck == false) {
			formsToSave = this.forms.value;
			formsToSave['moduleId'] = this.module.MODULE_ID;
			formsToSave['formId'] =
				this.serviceCatalogueId === 'new' ? undefined : this.serviceCatalogueId;
			const panels = this.customLayouts.map((value) => {
				return {
					panelDisplayName: value.panelDisplayName,
					collapse: value.collapse,
					id: value.name,
					grids: value.grids.map((grids) => {
						return grids.map((grid) => {
							return {
								empty: grid.empty,
								height: grid.height,
								width: grid.width,
								fieldId: grid.fieldId,
							};
						});
					}),
				};
			});
			formsToSave['panels'] = panels;
			formsToSave['layoutStyle'] = this.layoutStyle;
			if (this.workflow) {
				formsToSave['workflow'] = this.workflow.WORKFLOW_ID;
			}
			formsToSave['displayImage'] = this.image;
			const teams = [];
			if (this.visibleTo.length === 0) {
				this.loaderService.isLoading = false;
				this.bannerMessageService.errorNotifications.push({
					message: this.translateService.instant('VISIBLE_FIELD_REQUIRED'),
				});
			} else {
				this.visibleTo.forEach((team) => {
					teams.push(team['id']);
				});
				this.visibleTo = teams;
				formsToSave['visibleTo'] = this.visibleTo;
				if (!formsToSave.formId) {
					this.loaderService.isLoading = false;
					this.formApi.postForm(this.module.MODULE_ID, formsToSave).subscribe(
						() => {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('SAVED_SUCCESSFULLY'),
							});
							this.navigateToMaster();
						},
						(error) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
				} else {
					this.loaderService.isLoading3 = false;
					this.formApi.putForm(this.module.MODULE_ID, formsToSave).subscribe(
						() => {
							this.bannerMessageService.successNotifications.push({
								message: this.translateService.instant('SAVED_SUCCESSFULLY'),
							});
							this.navigateToMaster();
						},
						(error) => {
							this.bannerMessageService.errorNotifications.push({
								message: error.error.ERROR,
							});
						}
					);
				}
			}
		}
	}

	public navigateToMaster() {
		this.router.navigate([
			`modules/${this.module.MODULE_ID}/service-catalogue`,
		]);
	}

	public setWorkflows() {
		this.workflowApiService
			.getWorkflows(this.moduleId, 0, 10, ['NAME', 'Asc'])
			.subscribe(
				(workflowResponse: any) => {
					this.workflows = workflowResponse.content;
				},
				(error: any) => {
					error.error.ERROR;
				}
			);
	}

	public teamsDataScroll() {
		this.teamScrollSubject
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value, search]) => {
					let searchValue = '';
					if (value !== '') {
						searchValue = 'NAME' + '=' + value;
					}
					let page = 0;
					if (this.teamNames && !search) {
						page = Math.ceil(this.teamNames.length / 10);
					}
					return this.serviceCatalogueService
						.getTeamsData(page, searchValue, this.teamsModule)
						.pipe(
							map((results: any) => {
								if (search) {
									this.teamNames = results['DATA'];
								} else if (results['DATA'].length > 0) {
									this.teamNames = this.teamNames.concat(results['DATA']);
								}
								return results['DATA'];
							})
						);
				})
			)
			.subscribe();
	}

	// When scrolling the dropdown.
	public onScrollTeams() {
		this.teamScrollSubject.next(['', false]);
	}

	// While entering any text to the input start searching.
	public onSearch() {
		const teams = this.forms.value['visibleTo'];
		if (typeof teams !== 'object') {
			const searchText = teams;
			this.teamScrollSubject.next([searchText, true]);
		}
	}

	public autocompleteClosed() {
		this.teamScrollSubject.next(['', true]);
	}

	//TODO: Validation for required fields has to be made.

	public onFileChangeForGeneral(event) {
		const reader = new FileReader();
		if (event.target.files && event.target.files.length) {
			const [file] = event.target.files;
			reader.readAsDataURL(file);
			const fileSize = file.size / 1000000;
			if (fileSize > 2) {
				this.bannerMessageService.errorNotifications.push({
					message: 'FILE_SIZE_2MB',
				});
				return;
			}
			// (file.size <= 1024000) ? this.fileSizeError = false : this.fileSizeError = true;
			reader.onload = () => {
				const data: any = reader.result;
				this.image = data;
				// need to run CD since file load runs outside of zone
				this.cd.markForCheck();
			};
		}
	}

	public displayWorkflowName(workflow?: any): string | undefined {
		return workflow ? workflow['NAME'] : undefined;
	}

	public initializeWorkflow() {
		this.workflowSubjects
			.pipe(
				debounceTime(400),
				distinctUntilChanged(),
				switchMap(([value]) => {
					let page = 0;
					if (this.workflows) {
						page = Math.ceil(this.workflows.length / 10);
					}
					return this.workflowApiService
						.getWorkflows(this.moduleId, page, 10, ['NAME', 'Asc'])
						.pipe(
							map((results: any) => {
								this.workflows = this.workflows.concat(results.content);
								return results.content;
							})
						);
				})
			)
			.subscribe();
	}

	public onScrollWorkflow() {
		this.workflowSubjects.next(['', false]);
	}

	public disableSelectedValues(item) {
		if (this.selectedValues.length > 0) {
			if (this.selectedValues.includes(item.id)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public createNewWorkflow() {
		this.router.navigate([`modules/${this.moduleId}/workflows`]);
	}

	public setSelectedValues(catalogue) {
		if (catalogue) {
			catalogue.visibleTo.forEach((data) => {
				this.selectedValues.push(data._id);
			});
		}
	}
}
