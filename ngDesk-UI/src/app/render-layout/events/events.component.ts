import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CacheService } from '@src/app/cache.service';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { EventsService } from './events.service';
@Component({
	selector: 'app-events',
	templateUrl: './events.component.html',
	styleUrls: ['./events.component.scss'],
})
export class EventsComponent implements OnInit {
	public moduleId: string;
	public entryId: string;
	public module: any;
	public metaData = {EVENTS:[]};
	
	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private bannerMessageService: BannerMessageService,
		private eventsService: EventsService,
	) {}

	public ngOnInit() {
		this.moduleId = this.route.snapshot.params.moduleId;
		this.entryId = this.route.snapshot.params.dataId;
		this.eventsService.
			getRequiredData(this.moduleId,this.entryId).
				subscribe((response: any) => {
					this.module = response[0];
					const metaDataResponse = response[1].entry;
					if(metaDataResponse.META_DATA && metaDataResponse.META_DATA != null){
						this.metaData = metaDataResponse.META_DATA;
					}
				},
				(error) => {
					this.bannerMessageService.errorNotifications.push({
						message: error.error.ERROR,
					}
				);
			}
		);			
    }

	public backButton(){
        this.router.navigate([
			`render/${this.route.snapshot.params.moduleId}/edit/${this.route.snapshot.params.dataId}`,
		]);
	}
}
