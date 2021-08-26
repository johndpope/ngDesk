import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Stage } from '@ngdesk/workflow-api';
import { AppGlobals } from '@src/app/app.globals';

@Component({
	selector: 'app-stages',
	templateUrl: './stages.component.html',
	styleUrls: ['./stages.component.scss'],
})
export class StagesComponent implements OnInit {
	constructor(
		private dialogRef: MatDialogRef<StagesComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any,
		private global: AppGlobals
	) {}

	public ngOnInit() {
		if (!this.data.STAGES) {
			this.data.STAGES = [];
		}
	}

	public addStages() {
		const stage: Stage = {
			CONDITIONS: [],
			NAME: 'Stage' + this.data.STAGES.length + 1,
			NODES: [],
			STAGE_ID: this.global.guid(),
		};
		this.data.STAGES.push(stage);
	}

	public saveData() {
		this.dialogRef.close(this.data.STAGES);
	}
}
