import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DiscoveryMapsDetailComponent } from '@src/app/company-settings/software-asset-management/discovery-maps/discovery-maps-detail/discovery-maps-detail.component';

describe('DiscoveryMapsDetailComponent', () => {
	let component: DiscoveryMapsDetailComponent;
	let fixture: ComponentFixture<DiscoveryMapsDetailComponent>;

	beforeEach(waitForAsync(() => {
		TestBed.configureTestingModule({
			declarations: [DiscoveryMapsDetailComponent],
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(DiscoveryMapsDetailComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
