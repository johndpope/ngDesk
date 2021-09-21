import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CsvLogsService } from './csv-logs-detail.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
@Component({
	selector: 'app-csv-logs-detail',
	templateUrl: './csv-logs-detail.component.html',
	styleUrls: ['./csv-logs-detail.component.scss'],
})
export class CsvLogsDetailComponent implements OnInit {
	public logs = [];
	public fileName = '';

	constructor(
		private route: ActivatedRoute,
		private csvLogsService: CsvLogsService,
		private bannerMessageService: BannerMessageService
	) {}

	public ngOnInit() {
		const logId = this.route.snapshot.params['dataId'];
		this.csvLogsService.getCsvImport(logId).subscribe(
			(response: any) => {
				this.logs = response.DATA.logs;
				this.fileName = response.DATA.csvImportData.fileName;
			},
			(error: any) => {
				this.bannerMessageService.errorNotifications.push({
					message: error.error.ERROR,
				});
			}
		);
	}
}
