import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-custom-matcard',
  templateUrl: './custom-matcard.component.html',
  styleUrls: ['./custom-matcard.component.scss']
})
export class CustomMatcardComponent implements OnInit {


  @Input() public tittle: string;
  @Input() public description: string;
  @Input() public authentication: boolean;
  @Input() public date: string;
  @Output() public unsubscribeButton: EventEmitter<any> = new EventEmitter<
		any
	>();

  constructor() { }

  public ngOnInit() {

  }

  public unsubscribeButtonEmit() {
		this.unsubscribeButton.emit();
	}

}
