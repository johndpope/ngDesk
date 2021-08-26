import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MobileUserLogoutComponent } from '@src/app/mobile-user-logout/mobile-user-logout.component';

describe('MobileUserLogoutComponent', () => {
  let component: MobileUserLogoutComponent;
  let fixture: ComponentFixture<MobileUserLogoutComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MobileUserLogoutComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MobileUserLogoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
