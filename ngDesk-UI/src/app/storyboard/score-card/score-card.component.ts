import { Component, OnInit } from '@angular/core';
import { IComponent, StoryboardService } from '../storyboard.service';

@Component({
	selector: 'app-score-card',
	templateUrl: './score-card.component.html',
	styleUrls: ['./score-card.component.scss']
})
export class ScoreCardComponent implements OnInit {
	constructor(private layoutService: StoryboardService) {}

	get component(): IComponent {
		return this.layoutService.components.find(f => f.id === this.id);
	}

	public id: string;

	public ngOnInit() {}
}
