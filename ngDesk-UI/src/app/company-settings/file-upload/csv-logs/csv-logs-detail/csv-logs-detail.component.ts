import { Component, OnInit } from '@angular/core';
import { ModulesService } from 'src/app/modules/modules.service';
import { ActivatedRoute } from '@angular/router';
import { CsvLogsService } from './csv-logs-detail.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';

@Component({
	selector: 'app-csv-logs-detail',
	templateUrl: './csv-logs-detail.component.html',
	styleUrls: ['./csv-logs-detail.component.scss'],
})
export class CsvLogsDetailComponent implements OnInit {
	public logs: any;
	public fileName = '';

	constructor(
		private modulesService: ModulesService,
		private route: ActivatedRoute,
		private csvLogsService: CsvLogsService,
		private bannerMessageService: BannerMessageService
	) {}

	public ngOnInit() {
		const logId = this.route.snapshot.params['dataId'];
		this.csvLogsService.getCsvImport(logId).subscribe(
			(csvImportDataResponse: any) => {
				console.log('logs', csvImportDataResponse);
				// this.logs = csvImportDataResponse.logs;
				// this.fileName = csvImportDataResponse.fileName;
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
		// this.modulesService.getCsvLog(logId).subscribe(
		// 	(response: any) => {
		// 		console.log('logs', response);
		// 		this.logs = response.LOGS;
		// 		this.fileName = response.NAME;
		// 	},
		// 	(error) => {
		// 		console.error(error);
		// 	}
		// );
	}
}
