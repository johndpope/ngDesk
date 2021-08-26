import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RoleLayoutDetailComponent } from '@src/app/company-settings/role-layout/role-layout-detail/role-layout-detail.component';

describe('RoleLayoutDetailComponent', () => {
  let component: RoleLayoutDetailComponent;
  let fixture: ComponentFixture<RoleLayoutDetailComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RoleLayoutDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RoleLayoutDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
