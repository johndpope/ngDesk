import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FileRuleMasterComponent } from '@src/app/company-settings/file-rule/file-rule-master/file-rule-master.component';

describe('FileRuleMasterComponent', () => {
  let component: FileRuleMasterComponent;
  let fixture: ComponentFixture<FileRuleMasterComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ FileRuleMasterComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FileRuleMasterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
