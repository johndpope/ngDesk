/*! Rappid v3.2.0 - HTML5 Diagramming Framework - TRIAL VERSION

Copyright (c) 2015 client IO

 2020-07-08


This Source Code Form is subject to the terms of the Rappid Trial License
, v. 2.0. If a copy of the Rappid License was not distributed with this
file, You can obtain one at http://jointjs.com/license/rappid_v2.txt
 or from the Rappid archive as was distributed by client IO. See the LICENSE file.*/

import {
	START_ESCALATION_ICON,
	STOP_ESCALATION_ICON,
	MAKE_PHONE_CALL_ICON,
	SEND_EMAIL_ICON,
	SEND_SMS_ICON,
	DELETE_ENTRY_ICON,
	UPDATE_ENTRY_ICON,
	APPROVAL_ICON,
	CREATE_ENTRY_ICON,
	GENERATE_PDF_ICON,
	TEAMS_ICON,
	SIGN_NODE_ICON,
} from '../../theme';

export const stencilConfig = {
	shapes: [
		{
			name: 'SendEmail',
			attrs: {
				label: { text: 'Send Email' },
				icon: { xlinkHref: SEND_EMAIL_ICON },
			},
		},
		{
			name: 'StartEscalation',
			attrs: {
				label: { text: 'Start Escalation' },
				icon: { xlinkHref: START_ESCALATION_ICON },
			},
		},
		{
			name: 'StopEscalation',
			attrs: {
				label: { text: 'Stop Escalation' },
				icon: { xlinkHref: STOP_ESCALATION_ICON },
			},
		},
		{
			name: 'SendSms',
			attrs: {
				label: { text: 'Send SMS' },
				icon: { xlinkHref: SEND_SMS_ICON },
			},
		},
		{
			name: 'MakePhoneCall',
			attrs: {
				label: { text: 'Make Phone' },
				icon: { xlinkHref: MAKE_PHONE_CALL_ICON },
			},
		},
		{
			name: 'CreateEntry',
			attrs: {
				label: { text: 'Create Entry' },
				icon: { xlinkHref: CREATE_ENTRY_ICON },
			},
		},
		{
			name: 'DeleteEntry',
			attrs: {
				label: { text: 'Delete Entry' },
				icon: { xlinkHref: DELETE_ENTRY_ICON },
			},
		},
		{
			name: 'UpdateEntry',
			attrs: {
				label: { text: 'Update Entry' },
				icon: { xlinkHref: UPDATE_ENTRY_ICON },
			},
		},
		{
			name: 'Approval',
			attrs: {
				label: { text: 'Approval' },
				icon: { xlinkHref: APPROVAL_ICON },
			},
		},
		{
			name: 'GeneratePdf',
			attrs: {
				label: { text: 'Generate PDF' },
				icon: { xlinkHref: GENERATE_PDF_ICON },
			},
		},
		{
			name: 'MicrosoftTeamsNotification',
			attrs: {
				label: { text: 'Microsoft Teams Notification' },
				icon: { xlinkHref: TEAMS_ICON },
			},
		},
		{
			name: 'SignatureDocument',
			attrs: {
				label: { text: 'Signature Document' },
				icon: { xlinkHref: SIGN_NODE_ICON },
			},
		},

	],
};
