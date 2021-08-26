import {
	Component,
	EventEmitter,
	Input,
	Output,
	TemplateRef,
} from '@angular/core';

@Component({
	selector: 'app-title-bar',
	templateUrl: './title-bar.component.html',
	styleUrls: ['./title-bar.component.scss'],
})
export class TitleBarComponent {
	@Output() public buttonRightOnClick = new EventEmitter<string>();
	@Output() public buttonRightOnClick2 = new EventEmitter<string>();
	@Output() public buttonRightOnClick3 = new EventEmitter<string>();
	@Output() public layoutOnClick = new EventEmitter<string>();
	@Output() public navOnClick = new EventEmitter<string>();

	@Input() public templateRef: TemplateRef<any>;
	@Input() public buttonsTemplateRef: TemplateRef<any>;
	@Input() public menuButtonTemplateRef: TemplateRef<any>;
	@Input() public labelTemplateRef: TemplateRef<any>;
	@Input() public title: string;
	@Input() public buttonText: string;
	@Input() public customButtonShow: boolean;
	@Input() public disable = false;
	@Input() public buttonColor = 'primary';

	@Input() public buttonText2: string;
	@Input() public enableButton2 = false;
	@Input() public buttonColor2 = 'primary';

	@Input() public buttonText3: string;
	@Input() public enableButton3 = false;
	
	constructor() {}
	
	public customEmit() {
		this.buttonRightOnClick.emit('custom');
	}
	public customEmit2() {
		this.buttonRightOnClick2.emit('custom');
	}
	public customEmit3() {
		this.buttonRightOnClick3.emit('custom');
	}
}
