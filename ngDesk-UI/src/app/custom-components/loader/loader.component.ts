import {
	Component,
	EventEmitter,
	Input,
	Output,
	TemplateRef,
} from '@angular/core';
import { LoaderService } from './loader.service';

@Component({
	selector: 'app-loader',
	templateUrl: 'loader.component.html',
	styleUrls: ['loader.component.scss'],
})
export class LoaderComponent {
	@Output() public buttonRightOnClick = new EventEmitter<string>();
	@Output() public buttonRightOnClick2 = new EventEmitter<string>();
	@Output() public buttonRightOnClick3 = new EventEmitter<string>();
	@Output() public layoutOnClick = new EventEmitter<string>();
	@Output() public navOnClick = new EventEmitter<string>();

	@Input() public templateRef: TemplateRef<any>;
	@Input() public title: string;
	@Input() public buttonText: string;
	@Input() public showSpinner: boolean;
	@Input() public buttonColor = 'primary';
	@Input() public disabledButton = false;
	@Input() public enableButton2 = true;
	@Input() public customButtonShow;
	@Input() public buttonColor2 = 'primary';
	@Input() public buttonText2: string;
	@Input() public disabledButton2 = false;
	@Input() public buttonText3: string;
	@Input() public disabledButton3 = false;
	@Input() public enableButton3 = true;

	constructor(public loaderService: LoaderService) {
		console.log(typeof this.enableButton2);
		console.log(typeof this.enableButton3);
	}
	
	public customEmit() {
		this.loaderService.isLoading = true;
		this.buttonRightOnClick.emit('custom');
	}
	public customEmit2() {
		this.loaderService.isLoading2 = true;
		this.buttonRightOnClick2.emit('custom');
	}

	public customEmit3() {
		this.loaderService.isLoading3 = true;
		this.buttonRightOnClick3.emit('custom');
	}
}
