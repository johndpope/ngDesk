import { Component, OnInit } from '@angular/core';
import { IComponent, StoryboardService } from '../storyboard.service';

@Component({
	selector: 'app-bar-chart',
	templateUrl: './bar-chart.component.html',
	styleUrls: ['./bar-chart.component.scss']
})
export class BarChartComponent implements OnInit {
	constructor(private layoutService: StoryboardService) {}

	get component(): IComponent {
		return this.layoutService.components.find(f => f.id === this.id);
	}
	public id: string;
	public barChartOptions = {
		// barStrokeWidth: 0,
		cornerRadius: 10,
		point: {
			radius: 25,
			hoverRadius: 35,
			pointStyle: 'rectRounded'
		},
		scales: {
			xAxes: [
				{
					display: false,
					ticks: {
						beginAtZero: true
					},
					gridLines: {
						color: 'rgba(0, 0, 0, 0)'
					}
				}
			],
			yAxes: [
				{
					gridLines: {
						display: false,
						color: 'rgba(0, 0, 0, 0)'
					},
					ticks: {
						beginAtZero: true
						// max:99999,
						// min:0
					}
				}
			]
		},
		scaleShowVerticalLines: false,
		responsive: true
	};

	public chartColors: any[] = [
		{
			backgroundColor: '#3F51B5'
		}
	];

	public barChartType = 'horizontalBar';
	public barChartLegend = false;

	public ngOnInit() {
		console.log(this.layoutService.components);
	}
	public onSelect(event) {
		console.log(event);
	}
}
