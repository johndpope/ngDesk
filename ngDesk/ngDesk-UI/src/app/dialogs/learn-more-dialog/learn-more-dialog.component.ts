import { OverlayContainer } from '@angular/cdk/overlay';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ThemeService } from '../../theme.service';

export interface DialogData {
	title: string;
	description: string;
	buttonText: string;
	linkText: string;
}

@Component({
	selector: 'app-learn-more-dialog',
	templateUrl: './learn-more-dialog.component.html',
	styleUrls: ['./learn-more-dialog.component.scss'],
})
export class LearnMoreDialogComponent {
	private themeColors = [
		'blue-theme',
		'green-theme',
		'red-theme',
		'red1-theme',
		'yellow-theme',
		'purple-theme',
		'black-theme',
	];
	constructor(
		public themeService: ThemeService,
		public overlay: OverlayContainer,
		public dialogRef: MatDialogRef<LearnMoreDialogComponent>,
		@Inject(MAT_DIALOG_DATA) public data: any
	) {
		this.themeService.getThemeColor().subscribe((response: any) => {
			overlay.getContainerElement().classList.forEach((element) => {
				if (this.themeColors.includes(element)) {
					overlay.getContainerElement().classList.remove(element);
				}
			});
			switch (response.PRIMARY_COLOR) {
				case '#3f51b5':
					overlay.getContainerElement().classList.add('blue-theme');
					break;
				case '#43a047':
					overlay.getContainerElement().classList.add('green-theme');
					break;
				case '#f44336':
					overlay.getContainerElement().classList.add('red-theme');
					break;
				case '#f90200':
					overlay.getContainerElement().classList.add('red1-theme');
					break;
				case '#ffea00':
					overlay.getContainerElement().classList.add('yellow-theme');
					break;
				case '#9c27b0':
					overlay.getContainerElement().classList.add('purple-theme');
					break;
				case '#000000':
					overlay.getContainerElement().classList.add('black-theme');
					break;
			}
		});

		dialogRef.disableClose = true;
	}

	public onNoClick(): void {
		this.dialogRef.close();
	}
}
