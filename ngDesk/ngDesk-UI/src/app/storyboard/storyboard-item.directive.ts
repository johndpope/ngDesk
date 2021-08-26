import {
	ComponentFactoryResolver,
	ComponentRef,
	Directive,
	Input,
	OnChanges,
	ViewContainerRef
} from '@angular/core';
import { BarChartComponent } from './bar-chart/bar-chart.component';
import { ScoreCardComponent } from './score-card/score-card.component';

const components = {
	score: ScoreCardComponent,
	bar: BarChartComponent
};

@Directive({
	selector: '[appStoryboardItem]'
})
export class StoryboardItemDirective implements OnChanges {
	@Input() public componentRef: string;
	@Input() public id: string;
	public component: ComponentRef<any>;

	constructor(
		private container: ViewContainerRef,
		private resolver: ComponentFactoryResolver
	) {}

	public ngOnChanges(): void {
		const component = components[this.componentRef];

		if (component) {
			const factory = this.resolver.resolveComponentFactory<any>(component);
			this.component = this.container.createComponent(factory);
			this.component.instance.id = this.id;
		}
	}
}
