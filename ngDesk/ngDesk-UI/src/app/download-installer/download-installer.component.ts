import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AgentService } from '@src/app/company-settings/agents/agent.service';

@Component({
	selector: 'app-download-installer',
	templateUrl: './download-installer.component.html',
	styleUrls: ['./download-installer.component.scss']
})
export class DownloadInstallerComponent implements OnInit, OnDestroy {
	public platform: string;
	public isInstalled: boolean;
	public isInstallInProgress: boolean;
	public downloadTimeInterval = 5;
	private timerIntervalClear;
	private timeOutClear;
	constructor(
		private route: ActivatedRoute,
		private agentService: AgentService
	) {}

	public ngOnInit() {
		this.downloadTimeInterval = 5;
		this.platform = this.route.snapshot.queryParams['platform'];
		this.timerIntervalClear = setInterval(() => {
			if (this.downloadTimeInterval !== 0) {
				this.downloadTimeInterval--;
			} else {
				clearInterval(this.timerIntervalClear);
			}
		}, 1000);
		this.timeOutClear = setTimeout(() => {
			this.isInstallInProgress = true;
			this.downloadInstaller();
			clearTimeout(this.timeOutClear);
		}, 5000);
	}
	public ngOnDestroy(): void {
		if (this.timerIntervalClear) {
			clearInterval(this.timerIntervalClear);
		}
		if (this.timeOutClear) {
			clearTimeout(this.timeOutClear);
		}
	}
	public downloadInstaller() {
		this.agentService.getInstaller(this.platform).subscribe(
			value => {
				this.isInstalled = true;
			},
			(error: any) => {
				this.isInstalled = true;
			}
		);
	}

	public linkClicked() {
		this.isInstalled = false;
		this.isInstallInProgress = true;
		this.downloadInstaller();
	}
}
