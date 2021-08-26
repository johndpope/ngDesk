import {
	Directive,
	ElementRef,
	HostListener,
	Input,
	OnInit
} from '@angular/core';

@Directive({
	selector: '[appHighlight]'
})
// TODO: support angular material theme colros so we can remove the CSS varibles to contorl color
// this can be done by getting material color in Typescript
export class HighlightDirective implements OnInit {
	constructor(private el: ElementRef) {}

	@Input('appHighlight') private highlightColor: {
		BACKGROUND: string;
		TEXT?: string;
	};
	@HostListener('mouseenter') private onMouseEnter() {
		this.highlight(
			this.highlightColor.BACKGROUND,
			this.highlightColor.TEXT ? this.highlightColor.TEXT : null
		);
	}

	@HostListener('mouseleave') private onMouseLeave() {
		this.highlight(null, null);
	}

	private highlight(backgroundColor: string, textColor?: string) {
		this.el.nativeElement.style.backgroundColor = backgroundColor;
		this.el.nativeElement.style.color = textColor;
	}

	public ngOnInit() {
		this.onMouseEnter();
		this.onMouseLeave();
	}
}
