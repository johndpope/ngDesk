import { Component,Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-feature-description-card',
  templateUrl: './feature-description-card.component.html',
  styleUrls: ['./feature-description-card.component.scss']
})
export class FeatureDescriptionCardComponent implements OnInit {

  @Input() public src: string;
  @Input() public name: string;
  @Input() public description: string;

  constructor() { }

 public ngOnInit() {
  }

}
