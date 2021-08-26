import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RoleLayoutMasterComponent } from '@src/app/company-settings/role-layout/role-layout-master/role-layout-master.component';

describe('RoleLayoutMasterComponent', () => {
  let component: RoleLayoutMasterComponent;
  let fixture: ComponentFixture<RoleLayoutMasterComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RoleLayoutMasterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RoleLayoutMasterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
