import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DiscoveryMapsMasterComponent } from '@src/app/company-settings/software-asset-management/discovery-maps-master/discovery-maps-master.component';

describe('DiscoveryMapsMasterComponent', () => {
  let component: DiscoveryMapsMasterComponent;
  let fixture: ComponentFixture<DiscoveryMapsMasterComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ DiscoveryMapsMasterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DiscoveryMapsMasterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
