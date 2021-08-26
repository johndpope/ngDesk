import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MobileRenderListLayoutComponent } from '@src/app/render-layout/mobile-render-list-layout/mobile-render-list-layout.component';

describe('MobileRenderListLayoutComponent', () => {
  let component: MobileRenderListLayoutComponent;
  let fixture: ComponentFixture<MobileRenderListLayoutComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MobileRenderListLayoutComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MobileRenderListLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
