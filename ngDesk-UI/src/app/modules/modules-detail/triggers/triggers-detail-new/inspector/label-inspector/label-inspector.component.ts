/*! Rappid v3.2.0 - HTML5 Diagramming Framework - TRIAL VERSION

Copyright (c) 2015 client IO

 2020-07-08 


This Source Code Form is subject to the terms of the Rappid Trial License
, v. 2.0. If a copy of the Rappid License was not distributed with this
file, You can obtain one at http://jointjs.com/license/rappid_v2.txt
 or from the Rappid archive as was distributed by client IO. See the LICENSE file.*/

import { Component } from '@angular/core';

import { BaseInspectorComponent } from '../base-inspector/base-inspector.component';

@Component({
	selector: 'app-label-inspector',
	templateUrl: './label-inspector.component.html',
	styleUrls: ['../inspector.component.scss'],
})
export class LabelInspectorComponent extends BaseInspectorComponent {
	public label: string;

	public props = {
		label: ['attrs', 'label', 'text'],
	};

	protected assignFormFields(): void {
		const { cell, props } = this;
		this.label = cell.prop(props.label);
	}
}
