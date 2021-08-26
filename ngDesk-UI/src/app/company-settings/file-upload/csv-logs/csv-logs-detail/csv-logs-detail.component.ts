import { Component, OnInit } from '@angular/core';
import { ModulesService } from 'src/app/modules/modules.service';
import { ActivatedRoute } from '@angular/router';

@Component({
	selector: 'app-csv-logs-detail',
	templateUrl: './csv-logs-detail.component.html',
	styleUrls: ['./csv-logs-detail.component.scss']
})
export class CsvLogsDetailComponent implements OnInit {
	public logs: any;
	public fileName = '';

	constructor(
		private modulesService: ModulesService,
		private route: ActivatedRoute
	) {}

	public ngOnInit() {
		const logId = this.route.snapshot.params['dataId'];
		this.modulesService.getCsvLog(logId).subscribe(
			(response: any) => {
				this.logs = response.LOGS;
				this.fileName = response.NAME;
			},
			error => {
				console.error(error);
			}
		);
	}
}
