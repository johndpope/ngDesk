import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { Injectable } from '@angular/core';

@Injectable()
export class DomSanitizerHelper{

    
    constructor (
        private matIconRegistry: MatIconRegistry,
		private domSanitizer: DomSanitizer,
    ){
    }

    public sanitizer(){
        this.matIconRegistry.addSvgIcon(
			'facebook',
			this.domSanitizer.bypassSecurityTrustResourceUrl(
				'../../../assets/images/facebook.svg'
			)
		);
		this.matIconRegistry.addSvgIcon(
			'google',
			this.domSanitizer.bypassSecurityTrustResourceUrl(
				'../../../assets/images/google.svg'
			)
		);
		this.matIconRegistry.addSvgIcon(
			'twitter',
			this.domSanitizer.bypassSecurityTrustResourceUrl(
				'../../../assets/images/twitter.svg'
			)
		);
		this.matIconRegistry.addSvgIcon(
			'microsoft',
			this.domSanitizer.bypassSecurityTrustResourceUrl(
				'../../../assets/images/microsoft.svg'
			)
		);

	}
	

}