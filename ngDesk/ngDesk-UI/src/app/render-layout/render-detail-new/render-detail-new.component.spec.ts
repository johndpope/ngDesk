import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RenderDetailNewComponent } from '@src/app/render-layout/render-detail-new/render-detail-new.component';

describe('RenderDetailNewComponent', () => {
  let component: RenderDetailNewComponent;
  let fixture: ComponentFixture<RenderDetailNewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RenderDetailNewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RenderDetailNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
