import { animate, state, style, transition, trigger } from '@angular/animations';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'app-sliding-view-panel',
  templateUrl: './sliding-view-panel.component.html',
  styleUrls: ['./sliding-view-panel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('slide', [
      state('paneOne', style({ transform: 'translateX(0)' })), // slides view a third over (pane 1 of 3)
      state('paneTwo', style({ transform: 'translateX(-33.33%)' })), // slides view two thirds over (pane 2 of 3)
      state('paneThree', style({ transform: 'translateX(-66.66%)' })), // slides view fully over (pane 3 of 3)
      transition('* => *', animate(300))
    ])]
})
export class SlidingViewPanelComponent {
  @Input() public activePane: PaneType = 'paneOne';
  constructor() { }

}

type PaneType = 'paneOne' | 'paneTwo' | 'paneThree';
