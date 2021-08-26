import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

@Component({
	selector: 'app-signature-pad',
	templateUrl: './signature-pad.component.html',
	styleUrls: ['./signature-pad.component.scss'],
})
export class SignaturePadComponent implements OnInit, OnDestroy {
	sigPadElement;
	context;
	isDrawing = false;
	img;
	isSigned: boolean = false;

	constructor(
		private dialog: MatDialog,
		public dialogRef: MatDialogRef<SignaturePadComponent>
	) {}

	ngOnInit() {}

	ngAfterContentInit() {
		this.sigPadElement = document.getElementById('htmlElemId');
		this.context = this.sigPadElement.getContext('2d');
		this.context.strokeStyle = 'red';
	}

	@HostListener('document:mouseup', ['$event'])
	onMouseUp(e) {
		this.isDrawing = false;
	}

	onMouseDown(e) {
		this.isDrawing = true;
		this.isSigned = true;
		const coords = this.relativeCoords(e);
		this.context.moveTo(coords.x, coords.y);
	}

	onMouseMove(e) {
		if (this.isDrawing) {
			const coords = this.relativeCoords(e);
			this.context.lineTo(coords.x, coords.y);
			this.context.stroke();
		}
	}

	private relativeCoords(event) {
		const bounds = event.target.getBoundingClientRect();
		const x = event.clientX - bounds.left;
		const y = event.clientY - bounds.top;
		return { x: x, y: y };
	}

	clear() {
		this.context.clearRect(
			0,
			0,
			this.sigPadElement.width,
			this.sigPadElement.height
		);
		this.context.beginPath();
		this.isSigned = false;
	}
	save() {
		this.img = this.sigPadElement.toDataURL('image/png');
		this.dialogRef.close(this.img);
	}

	touchStart(e) {
		this.isDrawing = true;
		this.isSigned = true;
		const coords = this.getRelativeCoordsForMobile(e);
		this.context.moveTo(coords.x, coords.y);
	}

	touchMoving(e) {
		if (this.isDrawing) {
			const coords = this.getRelativeCoordsForMobile(e);
			this.context.lineTo(coords.x, coords.y);
			this.context.stroke();
		}
	}

	@HostListener('document:touchend', ['$event'])
	ontouchend(e) {
		this.isDrawing = false;
	}

	getRelativeCoordsForMobile(e) {
		const limits: any = document.getElementById('htmlElemId');
		const x = e.touches[0].clientX - limits.offsetLeft;
		const y = e.touches[0].clientY - limits.offsetTop;
		return { x: x, y: y };
	}

	ngOnDestroy() {
		this.isSigned = false;
	}
}
