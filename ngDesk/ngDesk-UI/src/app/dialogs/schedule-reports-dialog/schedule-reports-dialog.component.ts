import { COMMA, ENTER, SPACE } from '@angular/cdk/keycodes';
import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatChipInputEvent } from '@angular/material/chips';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ScheduleEmailValidator } from './schedule-email-validator';

@Component({
	selector: 'app-schedule-reports-dialog',
	templateUrl: './schedule-reports-dialog.component.html',
	styleUrls: ['./schedule-reports-dialog.component.scss'],
})
export class ScheduleReportsDialogComponent implements OnInit {
	public options = ['Daily', 'Weekly'];
	public weekdays = [
		{ name: 'Sunday', day: 'SUN' },
		{ name: 'Monday', day: 'MON' },
		{ name: 'Tuesday', day: 'TUE' },
		{ name: 'Wednesday', day: 'WED' },
		{ name: 'Thursday', day: 'THU' },
		{ name: 'Friday', day: 'FRI' },
		{ name: 'Saturday', day: 'SAT' },
	];
	public schedules = { CRON: '', EMAILS: [] };
	public schedulesForm: FormGroup;
	public emails = [];
	public readonly separatorKeysCodes: number[] = [ENTER, COMMA, SPACE];
	public weeklySchedule = false;
	public times = [];
	public errorMessage = '';

	constructor(
		private formBuilder: FormBuilder,
		public dialogRef: MatDialogRef<ScheduleReportsDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {}

	public ngOnInit(): void {
		for (let i = 0; i < 24; i++) {
			let time = i + ':00';
			if (i < 10) {
				time = '0' + time;
			}
			this.times.push(time);
		}

		// initializes an empty form
		this.emails = JSON.parse(JSON.stringify(this.data.EMAILS));
		this.schedulesForm = this.formBuilder.group({
			schedule: ['Daily', [Validators.required]],
			weekday: ['SUN'],
			time: ['00:00', [Validators.required]],
			emails: [this.emails, [ScheduleEmailValidator.validateEmails]],
		});

		if (this.data.CRON !== '') {
			let schedule: string;
			let weekday: number;
			let hour = this.data.CRON.substring(4, this.data.CRON.indexOf('?') - 1);
			if (hour.length < 2) {
				hour = '0' + hour;
			}
			const time = hour + ':00';
			if (this.data.CRON.slice(-1) === '*') {
				schedule = 'Daily';
			} else {
				schedule = 'Weekly';
				weekday = this.data.CRON.slice(-3);
				this.weeklySchedule = true;
			}
			this.schedulesForm.controls['emails'].setValue(this.emails);
			this.schedulesForm.controls['schedule'].setValue(schedule);
			this.schedulesForm.controls['weekday'].setValue(weekday);
			this.schedulesForm.controls['time'].setValue(time);
		}
	}

	public addEmail(event: MatChipInputEvent) {
		const input = event.input;
		const value = event.value;
		if (value.trim() !== '') {
			this.schedulesForm.controls['emails'].setErrors(null); // 1
			if (this.schedulesForm.controls['emails'].value.length > 9) {
				this.errorMessage = 'Cannot enter more than 10 email';
			} else {
				this.errorMessage = '';
				const tempEmails = this.schedulesForm.controls['emails'].value; // 2
				tempEmails.push(value.trim());
				this.schedulesForm.controls['emails'].setValue(tempEmails); // 3
				if (this.schedulesForm.controls['emails'].valid) {
					// 4
					this.schedulesForm.controls['emails'].markAsDirty();
					input.value = ''; // 5
				} else {
					const index = this.emails.findIndex(
						(value1) => value1 === value.trim()
					);
					if (index !== -1) {
						this.emails.splice(index, 1); // 6
					}
				}
			}
		} else {
			this.schedulesForm.controls['emails'].updateValueAndValidity(); // 7
		}
	}

	public onRemoveEmail(email: any) {
		const controller = this.schedulesForm.controls['emails'];
		const index = this.emails.indexOf(email, 0);
		if (index > -1) {
			this.emails.splice(index, 1);
		}
		controller.updateValueAndValidity();
		controller.markAsDirty();
	}

	public change(event: any) {
		if (event.value === 'Weekly') {
			this.weeklySchedule = true;
			if (this.schedulesForm.controls['weekday'].value === undefined) {
				this.schedulesForm.controls['weekday'].setValue('SUN');
			}
		} else {
			this.weeklySchedule = false;
		}
	}

	public save(): void {
		if (this.schedulesForm.valid) {
			const time: string = this.schedulesForm.value.time;
			let weekday = '';
			const hour = parseInt(time.substring(0, 2), 10);
			if (this.schedulesForm.value.schedule === 'Daily') {
				weekday = '*';
			} else {
				weekday = this.schedulesForm.value.weekday;
			}
			this.schedules.CRON = '0 0 ' + hour + ' ? * ' + weekday;
			const emails = this.schedulesForm.value.emails.slice(0);
			this.schedules.EMAILS = emails;
			this.dialogRef.close(this.schedules);
		}
	}
}
