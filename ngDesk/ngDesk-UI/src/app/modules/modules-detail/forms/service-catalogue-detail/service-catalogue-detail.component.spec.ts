import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceCatalogueDetailComponent } from './service-catalogue-detail.component';

describe('ServiceCatalogueDetailComponent', () => {
  let component: ServiceCatalogueDetailComponent;
  let fixture: ComponentFixture<ServiceCatalogueDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServiceCatalogueDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceCatalogueDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
