import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DragDropFieldsComponent } from '@src/app/custom-components/drag-drop-fields/drag-drop-fields.component';

describe('DragDropFieldsComponent', () => {
  let component: DragDropFieldsComponent;
  let fixture: ComponentFixture<DragDropFieldsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ DragDropFieldsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DragDropFieldsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
