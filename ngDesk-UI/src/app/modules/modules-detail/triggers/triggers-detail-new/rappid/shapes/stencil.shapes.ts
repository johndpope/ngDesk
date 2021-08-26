/*! Rappid v3.2.0 - HTML5 Diagramming Framework - TRIAL VERSION

Copyright (c) 2015 client IO

 2020-07-08


This Source Code Form is subject to the terms of the Rappid Trial License
, v. 2.0. If a copy of the Rappid License was not distributed with this
file, You can obtain one at http://jointjs.com/license/rappid_v2.txt
 or from the Rappid archive as was distributed by client IO. See the LICENSE file.*/

import { dia, shapes } from '@clientio/rappid';
import { ObjectHash } from 'backbone';

import {
	FONT_FAMILY,
	PADDING_L,
	LIGHT_COLOR,
	MAIN_COLOR,
	MESSAGE_ICON,
} from '../../theme';

export enum ShapeTypesEnum {
	SEND_EMAIL = 'stencil.SendEmail',
	START_ESCALATION = 'stencil.StartEscalation',
	FLOWCHART_START = 'stencil.FlowchartStart',
	FLOWCHART_END = 'stencil.FlowchartEnd',
	STOP_ESCALATION = 'stencil.StopEscalation',
	SEND_SMS = 'stencil.SendSms',
	MAKE_PHONE_CALL = 'stencil.MakePhoneCall',
	UPDATE_ENTRY = 'stencil.UpdateEntry',
	CREATE_ENTRY = 'stencil.CreateEntry',
	DELETE_ENTRY = 'stencil.DeleteEntry',
	APPROVAL = 'stencil.Approval',
	GENERATE_PDF = 'stencil.GeneratePdf',
	TEAMS_ENTRY = 'stencil.MicrosoftTeamsNotification',
	SIGN_NODE = 'stencil.SignatureDocument',
}

const SHAPE_SIZE = 48;

const FlowchartStart = dia.Element.define(
	ShapeTypesEnum.FLOWCHART_START,
	{
		name: 'FlowchartStart',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: {
			body: {
				fill: MAIN_COLOR,
				stroke: 'none',
				refCx: '50%',
				refCy: '50%',
				refR: '50%',
			},
			icon: {
				d:
					'M 2 8 L 4.29 5.71 L 1.41 2.83 L 2.83 1.41 L 5.71 4.29 L 8 2 L 8 8 Z M -2 8 L -8 8 L -8 2 L -5.71 4.29 L -1 -0.41 L -1 -8 L 1 -8 L 1 0.41 L -4.29 5.71 Z',
				fill: '#FFFFFF',
				refX: '50%',
				refY: '50%',
			},
			label: {
				text: 'Start',
				refDx: PADDING_L,
				refY: '50%',
				textAnchor: 'start',
				textVerticalAnchor: 'middle',
				fill: '#242424',
				fontFamily: FONT_FAMILY,
				fontSize: 13,
			},
		},
	} as ObjectHash,
	{
		markup: [
			{
				tagName: 'circle',
				selector: 'body',
			},
			{
				tagName: 'path',
				selector: 'icon',
			},
			{
				tagName: 'text',
				selector: 'label',
			},
		],
	}
);

const FlowchartEnd = dia.Element.define(
	ShapeTypesEnum.FLOWCHART_END,
	{
		name: 'FlowchartEnd',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: {
			body: {
				fill: MAIN_COLOR,
				stroke: 'none',
				refCx: '50%',
				refCy: '50%',
				refR: '50%',
			},
			icon: {
				d:
					'M 5 -8.45 L 6.41 -7.04 L 3 -3.635 L 1.59 -5.04 Z M -4.5 3.95 L -1 3.95 L -1 -1.63 L -6.41 -7.04 L -5 -8.45 L 1 -2.45 L 1 3.95 L 4.5 3.95 L 0 8.45 Z',
				fill: '#FFFFFF',
				refX: '50%',
				refY: '50%',
			},
			label: {
				text: 'End',
				refDx: PADDING_L,
				refY: '50%',
				textAnchor: 'start',
				textVerticalAnchor: 'middle',
				fill: '#242424',
				fontFamily: FONT_FAMILY,
				fontSize: 13,
			},
		},
	} as ObjectHash,
	{
		markup: [
			{
				tagName: 'circle',
				selector: 'body',
			},
			{
				tagName: 'path',
				selector: 'icon',
			},
			{
				tagName: 'text',
				selector: 'label',
			},
		],
	}
);

const attributes = {
	body: {
		fill: LIGHT_COLOR,
		stroke: '#E8E8E8',
		refCx: '50%',
		refCy: '50%',
		refR: '50%',
	},
	icon: {
		width: 20,
		height: 20,
		refX: '50%',
		refY: '50%',
		x: -10,
		y: -10,
		xlinkHref: MESSAGE_ICON,
	},
	label: {
		text: 'Component',
		refDx: PADDING_L,
		refY: '50%',
		textAnchor: 'start',
		textVerticalAnchor: 'middle',
		fill: '#242424',
		fontFamily: FONT_FAMILY,
		fontSize: 13,
	},
};

const markup = {
	markup: [
		{
			tagName: 'circle',
			selector: 'body',
		},
		{
			tagName: 'image',
			selector: 'icon',
		},
		{
			tagName: 'text',
			selector: 'label',
		},
	],
};

const SendEmail = dia.Element.define(
	ShapeTypesEnum.SEND_EMAIL,
	{
		name: 'SendEmail',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const StartEscalation = dia.Element.define(
	ShapeTypesEnum.START_ESCALATION,
	{
		name: 'StartEscalation',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const StopEscalation = dia.Element.define(
	ShapeTypesEnum.STOP_ESCALATION,
	{
		name: 'StopEscalation',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const SendSms = dia.Element.define(
	ShapeTypesEnum.SEND_SMS,
	{
		name: 'SendSms',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const MakePhoneCall = dia.Element.define(
	ShapeTypesEnum.MAKE_PHONE_CALL,
	{
		name: 'MakePhoneCall',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const CreateEntry = dia.Element.define(
	ShapeTypesEnum.CREATE_ENTRY,
	{
		name: 'CreateEntry',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const UpdateEntry = dia.Element.define(
	ShapeTypesEnum.UPDATE_ENTRY,
	{
		name: 'UpdateEntry',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const DeleteEntry = dia.Element.define(
	ShapeTypesEnum.DELETE_ENTRY,
	{
		name: 'DeleteEntry',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const Approval = dia.Element.define(
	ShapeTypesEnum.APPROVAL,
	{
		name: 'Approval',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const GeneratePdf  = dia.Element.define(
	ShapeTypesEnum.GENERATE_PDF,
	{
		name: 'GeneratePdf',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const MicrosoftTeamsNotification = dia.Element.define(
	ShapeTypesEnum.TEAMS_ENTRY,
	{
		name: 'MicrosoftTeamsNotification',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);

const SignatureDocument = dia.Element.define(
	ShapeTypesEnum.SIGN_NODE,
	{
		name: 'Signature Document',
		size: { width: SHAPE_SIZE, height: SHAPE_SIZE },
		attrs: attributes,
	} as ObjectHash,
	markup
);



Object.assign(shapes, {
	stencil: {
		SendEmail,
		StopEscalation,
		SendSms,
		MakePhoneCall,
		StartEscalation,
		FlowchartStart,
		FlowchartEnd,
		CreateEntry,
		UpdateEntry,
		DeleteEntry,
		Approval,
		GeneratePdf,
		MicrosoftTeamsNotification,
		SignatureDocument
	},
});
