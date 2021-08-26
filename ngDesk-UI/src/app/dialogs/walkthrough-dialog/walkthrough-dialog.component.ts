import { Component, EventEmitter, Inject, Output } from '@angular/core';

import { FilePreviewOverlayRef } from '../../shared/file-preview-overlay/file-preview-overlay-ref';
import { FILE_PREVIEW_DIALOG_DATA } from '../../shared/file-preview-overlay/file-preview-overlay.tokens';
@Component({
	selector: 'app-walkthrough-dialog',
	templateUrl: './walkthrough-dialog.component.html',
	styleUrls: ['./walkthrough-dialog.component.scss'],
})
export class WalkthroughDialogComponent {
	@Output() public closeButton = new EventEmitter<any>();
	
	constructor(
		public dialogRef: FilePreviewOverlayRef,
		@Inject(FILE_PREVIEW_DIALOG_DATA) public data: any
	) {
	}

	public next() {
		this.closeButton.emit(this.data.progress);
	}
}
