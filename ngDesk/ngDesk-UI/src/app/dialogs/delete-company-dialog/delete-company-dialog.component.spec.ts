import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DeleteCompanyDialogComponent } from '@src/app/dialogs/delete-company-dialog/delete-company-dialog.component';

describe('DeleteCompanyDialogComponent', () => {
	let component: DeleteCompanyDialogComponent;
	let fixture: ComponentFixture<DeleteCompanyDialogComponent>;

	beforeEach(waitForAsync(() => {
		TestBed.configureTestingModule({
			declarations: [DeleteCompanyDialogComponent],
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(DeleteCompanyDialogComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
