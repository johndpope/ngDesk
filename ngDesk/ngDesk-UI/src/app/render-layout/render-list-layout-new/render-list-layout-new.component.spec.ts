import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RenderListLayoutNewComponent } from '@src/app/render-layout/render-list-layout-new/render-list-layout-new.component';

describe('RenderListLayoutNewComponent', () => {
  let component: RenderListLayoutNewComponent;
  let fixture: ComponentFixture<RenderListLayoutNewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RenderListLayoutNewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RenderListLayoutNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
