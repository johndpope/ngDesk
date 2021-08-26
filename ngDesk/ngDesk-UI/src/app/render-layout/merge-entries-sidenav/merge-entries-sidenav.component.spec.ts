import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MergeEntriesSidenavComponent } from '@src/app/render-layout/merge-entries-sidenav/merge-entries-sidenav.component';

describe('MergeEntriesSidenavComponent', () => {
  let component: MergeEntriesSidenavComponent;
  let fixture: ComponentFixture<MergeEntriesSidenavComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MergeEntriesSidenavComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MergeEntriesSidenavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
