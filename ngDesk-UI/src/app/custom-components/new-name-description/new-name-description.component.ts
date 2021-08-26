import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
	ControlContainer,
	FormControl,
	FormGroup,
	FormGroupDirective,
	NgForm,
	Validators,
} from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { TranslateService } from '@ngx-translate/core';

export class MyErrorStateMatcher implements ErrorStateMatcher {
	public isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
		return !!(control && control.invalid && (control.dirty || control.touched));
	}
}

@Component({
	selector: 'app-new-name-description',
	templateUrl: './new-name-description.component.html',
	styleUrls: ['./new-name-description.component.scss'],
	viewProviders: [
		{ provide: ControlContainer, useExisting: FormGroupDirective },
	],
})

export class NewNameDescriptionComponent implements OnInit {
	public params;
	public matcher = new MyErrorStateMatcher();
	@Input() public altButton: any;
	@Input() public altButtonTitle: string;
	@Input() public buttonText: string;
	@Input() public additionalFields = [];
	@Input() public additionalCheckboxFields = [];
	@Input() public schedulesButton: string;
	@Input() public schedulesButtonTitle: string;
	@Input() public disabledButton: boolean;
	@Output() public save: EventEmitter<FormGroup> = new EventEmitter<any>();
	@Output() public messageName: EventEmitter<FormGroup> = new EventEmitter<any>();
	@Output() public messageDesc: EventEmitter<FormGroup> = new EventEmitter<any>();
	@Output() public altButtonAction: EventEmitter<any> = new EventEmitter<any>();
	@Output() public schedulesButtonAction: EventEmitter<any> = new EventEmitter<
		any
	>();
	// tslint:disable-next-line: no-output-rename
	@Output('customSelectionChange') public customSelectionChange: EventEmitter<
		any
	> = new EventEmitter();

	constructor(
		public fgd: FormGroupDirective,
		private translateService: TranslateService
	) {
		// needs to subscribe here to get the translation once the actual file is loaded
		// if using instant outside it wont get the trasnlation.
		this.params = {
			triggerType: { field: this.translateService.instant('TRIGGER_TYPE') },
			order: { field: this.translateService.instant('ORDER') },
			role: { field: this.translateService.instant('ROLE') },
			name: { field: this.translateService.instant('NAME') },
		};
	}

	public ngOnInit() {
		this.fgd.control.addControl(
			'NAME',
			new FormControl('', [Validators.required])
		);
		this.fgd.control.addControl('DESCRIPTION', new FormControl(''));
		this.additionalCheckboxFields.forEach((data) => {
			this.fgd.control.addControl(data.control, new FormControl(false));
		});
		this.additionalFields.forEach((field) => {
			if (field.type === 'multipleList') {
				this.fgd.control.addControl(field.control, new FormControl(['']));
			} else {
				this.fgd.control.addControl(field.control, new FormControl(''));
			}
		});
	}

	public focusoutName(event) {

		this.messageName.emit(this.fgd.value.NAME);
	}
	public focusoutDesc(event) {
		this.messageDesc.emit(this.fgd.value.DESCRIPTION);
	}
	public customEmit() {
		this.save.emit();
	}

	public customSelectionChangeEmit() {
		this.customSelectionChange.emit();
	}

	public schedulesButtonEmit() {
		this.schedulesButtonAction.emit();
	}

	public altButtonEmit() {
		this.altButtonAction.emit();
	}
}
