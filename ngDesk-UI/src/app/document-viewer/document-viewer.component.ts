import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { RenderDetailHelper } from '@src/app/render-layout/render-detail-helper/render-detail-helper';
import { SignaturePadComponent } from '@src/app/dialogs/signature-pad/signature-pad.component';
import { SignatureDocumentApiService } from '@ngdesk/integration-api';
import { BannerMessageService } from '@src/app/custom-components/banner-message/banner-message.service';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

@Component({
	selector: 'app-document-viewer',
	templateUrl: './document-viewer.component.html',
	styleUrls: ['./document-viewer.component.scss'],
})
export class DocumentViewerComponent implements OnInit {
	htmlConntent: string;
	SIGNATURE_REPLACE: any;
	dialogRef: any;
	templateId;
	isDocumentSigned: boolean = false;
	public dataMaterialModule: any = {};
	private signatureImage:any;

	constructor(
		public renderDetailHelper: RenderDetailHelper,
		private dialog: MatDialog,
		private signatureDocumentApiService: SignatureDocumentApiService,
		private bannerMessageService: BannerMessageService,
		private router: Router,
		private route: ActivatedRoute,
		private translateService: TranslateService,
	) {
		this.dataMaterialModule = this.renderDetailHelper.dataMaterialModule;
	}

	ngOnInit() {
		this.templateId = this.route.snapshot.params['templateId'];
		this.getTemplate();
	}

	public openSignaturePad() {
		if(this.signatureImage){
		this.htmlConntent=this.htmlConntent.replace(this.signatureImage,'SIGNATURE_REPLACE');
	}
		this.dialogRef = this.dialog.open(SignaturePadComponent, {
			width: '350px',
		});
		this.afterClosed();
	}

	// to get signature Image after Save
	afterClosed() {
		this.dialogRef.afterClosed().subscribe((signature: string) => {
			if (signature && signature != '') {
				this.signatureImage=signature;
				let re = /SIGNATURE_REPLACE/gi;
				this.htmlConntent = this.htmlConntent.replace(re, signature);
				this.isDocumentSigned = true;
			}
		});
	}

	// get template byId  from routing URL
	getTemplate() {
		this.signatureDocumentApiService
			.getSignatureDocument(this.templateId)
			.subscribe((data: any) => {
				if (data && data.htmlDocument) {
					let re = /{{SIGNATURE_REPLACE}}/gi;
					data.htmlDocument = data.htmlDocument.replace(
						re,
						'<img src = "SIGNATURE_REPLACE" />'
					);
					this.htmlConntent = data.htmlDocument;
				} else {
					this.bannerMessageService.errorNotifications.push({
						message: this.translateService.instant('DOCUMENT_NOT_AVAILABLE')
					});
					this.router.navigate(['/login']);
				}
			});
	}

	// To Do Update call for signature upload
	updateDocument() {
		let payload: any = {
			templateId: this.templateId,
			htmlDocument: this.htmlConntent,
		};

		this.signatureDocumentApiService
			.putSignatureDocument(payload)
			.subscribe(() => {
				this.bannerMessageService.successNotifications.push({
					message: this.translateService.instant('DOCUMENT_SAVED_SUCCESSFULLY')
					
				});
				this.router.navigate(['/login']);
			});
	}
}
