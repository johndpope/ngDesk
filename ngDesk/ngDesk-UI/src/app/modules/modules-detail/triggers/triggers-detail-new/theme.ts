/*! Rappid v3.2.0 - HTML5 Diagramming Framework - TRIAL VERSION

Copyright (c) 2015 client IO

 2020-07-08


This Source Code Form is subject to the terms of the Rappid Trial License
, v. 2.0. If a copy of the Rappid License was not distributed with this
file, You can obtain one at http://jointjs.com/license/rappid_v2.txt
 or from the Rappid archive as was distributed by client IO. See the LICENSE file.*/

export const MAX_PORT_COUNT = 3;
export const FONT_FAMILY = 'realist, Helvetica, Arial, sans-serif';
export const OUT_PORT_HEIGHT = 28;
export const OUT_PORT_WIDTH = 96;
export const OUT_PORT_LABEL = 'out';
export const PORT_BORDER_RADIUS = 16;
export const GRID_SIZE = 8;
export const PADDING_S = GRID_SIZE;
export const PADDING_L = GRID_SIZE * 2;
export const ADD_PORT_SIZE = 20;
export const REMOVE_PORT_SIZE = 16;
export const BACKGROUND_COLOR = '#F9F9F9';
export const SECONDARY_BACKGROUND_COLOR = '#FCFCFC';
export const LIGHT_COLOR = '#FFFFFF';
export const DARK_COLOR = '#212121';
export const MAIN_COLOR = '#0057FF';
export const LINE_WIDTH = 2;
export const STENCIL_WIDTH = 200;
export const ZOOM_MAX = 3;
export const ZOOM_MIN = 0.4;
export const ZOOM_STEP = 0.2;

// icons
export const MESSAGE_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0iYmxhY2siIHdpZHRoPSIxOHB4IiBoZWlnaHQ9IjE4cHgiPjxwYXRoIGQ9Ik0yMCAySDRjLTEuMSAwLTEuOTkuOS0xLjk5IDJMMiAyMmw0LTRoMTRjMS4xIDAgMi0uOSAyLTJWNGMwLTEuMS0uOS0yLTItMnptLTIgMTJINnYtMmgxMnYyem0wLTNINlY5aDEydjJ6bTAtM0g2VjZoMTJ2MnoiLz48cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIi8+PC9zdmc+';
export const MAKE_PHONE_CALL_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0Ij48cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIi8+PHBhdGggZD0iTTIyIDNIMkMuOSAzIDAgMy45IDAgNXYxNGMwIDEuMS45IDIgMiAyaDIwYzEuMSAwIDEuOTktLjkgMS45OS0yTDI0IDVjMC0xLjEtLjktMi0yLTJ6TTggNmMxLjY2IDAgMyAxLjM0IDMgM3MtMS4zNCAzLTMgMy0zLTEuMzQtMy0zIDEuMzQtMyAzLTN6bTYgMTJIMnYtMWMwLTIgNC0zLjEgNi0zLjFzNiAxLjEgNiAzLjF2MXptMy44NS00aDEuNjRMMjEgMTZsLTEuOTkgMS45OWMtMS4zMS0uOTgtMi4yOC0yLjM4LTIuNzMtMy45OS0uMTgtLjY0LS4yOC0xLjMxLS4yOC0ycy4xLTEuMzYuMjgtMmMuNDUtMS42MiAxLjQyLTMuMDEgMi43My0zLjk5TDIxIDhsLTEuNTEgMmgtMS42NGMtLjIyLjYzLS4zNSAxLjMtLjM1IDJzLjEzIDEuMzcuMzUgMnoiLz48L3N2Zz4=';

export const SEND_SMS_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0Ij48cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIi8+PHBhdGggZD0iTTIuMDEgMjFMMjMgMTIgMi4wMSAzIDIgMTBsMTUgMi0xNSAyeiIvPjwvc3ZnPg==';

export const SEND_EMAIL_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGVuYWJsZS1iYWNrZ3JvdW5kPSJuZXcgMCAwIDI0IDI0IiBoZWlnaHQ9IjI0IiB2aWV3Qm94PSIwIDAgMjQgMjQiIHdpZHRoPSIyNCI+PGc+PHJlY3QgZmlsbD0ibm9uZSIgaGVpZ2h0PSIyNCIgd2lkdGg9IjI0IiB4PSIwIi8+PHBhdGggZD0iTTEyLDE5YzAtMy44NywzLjEzLTcsNy03YzEuMDgsMCwyLjA5LDAuMjUsMywwLjY4VjZjMC0xLjEtMC45LTItMi0ySDRDMi45LDQsMiw0LjksMiw2djEyYzAsMS4xLDAuOSwyLDIsMmg4LjA4IEMxMi4wMywxOS42NywxMiwxOS4zNCwxMiwxOXogTTQsNmw4LDVsOC01djJsLTgsNUw0LDhWNnogTTE3LjM0LDIybC0zLjU0LTMuNTRsMS40MS0xLjQxbDIuMTIsMi4xMmw0LjI0LTQuMjRMMjMsMTYuMzRMMTcuMzQsMjJ6Ii8+PC9nPjwvc3ZnPg==';
export const START_ESCALATION_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0Ij48cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIi8+PHBhdGggZD0iTTggNXYxNGwxMS03eiIvPjwvc3ZnPg==';
export const STOP_ESCALATION_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0Ij48cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIi8+PHBhdGggZD0iTTYgNmgxMnYxMkg2eiIvPjwvc3ZnPg==';

export const UPDATE_ENTRY_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0Ij48cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIi8+PHBhdGggZD0iTTMgMTcuMjVWMjFoMy43NUwxNy44MSA5Ljk0bC0zLjc1LTMuNzVMMyAxNy4yNXpNMjAuNzEgNy4wNGMuMzktLjM5LjM5LTEuMDIgMC0xLjQxbC0yLjM0LTIuMzRjLS4zOS0uMzktMS4wMi0uMzktMS40MSAwbC0xLjgzIDEuODMgMy43NSAzLjc1IDEuODMtMS44M3oiLz48L3N2Zz4=';
export const CREATE_ENTRY_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0Ij48cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIi8+PHBhdGggZD0iTTE5IDNINWMtMS4xMSAwLTIgLjktMiAydjE0YzAgMS4xLjg5IDIgMiAyaDE0YzEuMSAwIDItLjkgMi0yVjVjMC0xLjEtLjktMi0yLTJ6bS0yIDEwaC00djRoLTJ2LTRIN3YtMmg0VjdoMnY0aDR2MnoiLz48L3N2Zz4=';
export const DELETE_ENTRY_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0Ij48cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIi8+PHBhdGggZD0iTTAgMGgyNHYyNEgwVjB6IiBmaWxsPSJub25lIi8+PHBhdGggZD0iTTYgMTljMCAxLjEuOSAyIDIgMmg4YzEuMSAwIDItLjkgMi0yVjdINnYxMnptMi40Ni03LjEybDEuNDEtMS40MUwxMiAxMi41OWwyLjEyLTIuMTIgMS40MSAxLjQxTDEzLjQxIDE0bDIuMTIgMi4xMi0xLjQxIDEuNDFMMTIgMTUuNDFsLTIuMTIgMi4xMi0xLjQxLTEuNDFMMTAuNTkgMTRsLTIuMTMtMi4xMnpNMTUuNSA0bC0xLTFoLTVsLTEgMUg1djJoMTRWNHoiLz48L3N2Zz4=';
export const APPROVAL_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGVuYWJsZS1iYWNrZ3JvdW5kPSJuZXcgMCAwIDI0IDI0IiBoZWlnaHQ9IjI0IiB2aWV3Qm94PSIwIDAgMjQgMjQiIHdpZHRoPSIyNCI+PGc+PHJlY3QgZmlsbD0ibm9uZSIgaGVpZ2h0PSIyNCIgd2lkdGg9IjI0Ii8+PC9nPjxnPjxnPjxnPjxwYXRoIGQ9Ik0xNCwxMEgydjJoMTJWMTB6IE0xNCw2SDJ2MmgxMlY2eiBNMiwxNmg4di0ySDJWMTZ6IE0yMS41LDExLjVMMjMsMTNsLTYuOTksN2wtNC41MS00LjVMMTMsMTRsMy4wMSwzTDIxLjUsMTEuNXoiLz48L2c+PC9nPjwvZz48L3N2Zz4=';
export const GENERATE_PDF_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGhlaWdodD0iMjQiIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0Ij48cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIi8+PHBhdGggZD0iTTIwIDJIOGMtMS4xIDAtMiAuOS0yIDJ2MTJjMCAxLjEuOSAyIDIgMmgxMmMxLjEgMCAyLS45IDItMlY0YzAtMS4xLS45LTItMi0yem0tOC41IDcuNWMwIC44My0uNjcgMS41LTEuNSAxLjVIOXYySDcuNVY3SDEwYy44MyAwIDEuNS42NyAxLjUgMS41djF6bTUgMmMwIC44My0uNjcgMS41LTEuNSAxLjVoLTIuNVY3SDE1Yy44MyAwIDEuNS42NyAxLjUgMS41djN6bTQtM0gxOXYxaDEuNVYxMUgxOXYyaC0xLjVWN2gzdjEuNXpNOSA5LjVoMXYtMUg5djF6TTQgNkgydjE0YzAgMS4xLjkgMiAyIDJoMTR2LTJINFY2em0xMCA1LjVoMXYtM2gtMXYzeiIvPjwvc3ZnPg==';
export const TEAMS_ICON =
	'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0iYmxhY2siIHdpZHRoPSIxOHB4IiBoZWlnaHQ9IjE4cHgiPjxwYXRoIGQ9Ik0yMCAySDRjLTEuMSAwLTEuOTkuOS0xLjk5IDJMMiAyMmw0LTRoMTRjMS4xIDAgMi0uOSAyLTJWNGMwLTEuMS0uOS0yLTItMnptLTIgMTJINnYtMmgxMnYyem0wLTNINlY5aDEydjJ6bTAtM0g2VjZoMTJ2MnoiLz48cGF0aCBkPSJNMCAwaDI0djI0SDB6IiBmaWxsPSJub25lIi8+PC9zdmc+';
export const SIGN_NODE_ICON =
	'data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9Im5vIj8+CjxzdmcKICAgeG1sbnM6ZGM9Imh0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvIgogICB4bWxuczpjYz0iaHR0cDovL2NyZWF0aXZlY29tbW9ucy5vcmcvbnMjIgogICB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiCiAgIHhtbG5zOnN2Zz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciCiAgIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIKICAgaWQ9InN2ZzE2IgogICB2ZXJzaW9uPSIxLjEiCiAgIHdpZHRoPSIyNCIKICAgdmlld0JveD0iMCAwIDI0IDI0IgogICBoZWlnaHQ9IjI0IgogICBlbmFibGUtYmFja2dyb3VuZD0ibmV3IDAgMCAyNCAyNCI+CiAgPG1ldGFkYXRhCiAgICAgaWQ9Im1ldGFkYXRhMjIiPgogICAgPHJkZjpSREY+CiAgICAgIDxjYzpXb3JrCiAgICAgICAgIHJkZjphYm91dD0iIj4KICAgICAgICA8ZGM6Zm9ybWF0PmltYWdlL3N2Zyt4bWw8L2RjOmZvcm1hdD4KICAgICAgICA8ZGM6dHlwZQogICAgICAgICAgIHJkZjpyZXNvdXJjZT0iaHR0cDovL3B1cmwub3JnL2RjL2RjbWl0eXBlL1N0aWxsSW1hZ2UiIC8+CiAgICAgIDwvY2M6V29yaz4KICAgIDwvcmRmOlJERj4KICA8L21ldGFkYXRhPgogIDxkZWZzCiAgICAgaWQ9ImRlZnMyMCIgLz4KICA8ZwogICAgIGlkPSJnNCI+CiAgICA8cmVjdAogICAgICAgaWQ9InJlY3QyIgogICAgICAgd2lkdGg9IjI0IgogICAgICAgaGVpZ2h0PSIyNCIKICAgICAgIGZpbGw9Im5vbmUiIC8+CiAgPC9nPgogIDxnCiAgICAgaWQ9ImcxNCI+CiAgICA8ZwogICAgICAgaWQ9ImcxMCI+CiAgICAgIDxwYXRoCiAgICAgICAgIGlkPSJwYXRoNiIKICAgICAgICAgZD0iTTE2LjI0LDExLjUxbDEuNTctMS41N2wtMy43NS0zLjc1bC0xLjU3LDEuNTdMOC4zNSwzLjYzYy0wLjc4LTAuNzgtMi4wNS0wLjc4LTIuODMsMGwtMS45LDEuOSBjLTAuNzgsMC43OC0wLjc4LDIuMDUsMCwyLjgzbDQuMTMsNC4xM0wzLDE3LjI1VjIxaDMuNzVsNC43Ni00Ljc2bDQuMTMsNC4xM2MwLjk1LDAuOTUsMi4yMywwLjYsMi44MywwbDEuOS0xLjkgYzAuNzgtMC43OCwwLjc4LTIuMDUsMC0yLjgzTDE2LjI0LDExLjUxeiBNOS4xOCwxMS4wN0w1LjA0LDYuOTRsMS44OS0xLjljMCwwLDAsMCwwLDBsMS4yNywxLjI3TDcuMDIsNy41bDEuNDEsMS40MWwxLjE5LTEuMTkgbDEuNDUsMS40NUw5LjE4LDExLjA3eiBNMTcuMDYsMTguOTZsLTQuMTMtNC4xM2wxLjktMS45bDEuNDUsMS40NWwtMS4xOSwxLjE5bDEuNDEsMS40MWwxLjE5LTEuMTlsMS4yNywxLjI3TDE3LjA2LDE4Ljk2eiIgLz4KICAgICAgPHBhdGgKICAgICAgICAgaWQ9InBhdGg4IgogICAgICAgICBkPSJNMjAuNzEsNy4wNGMwLjM5LTAuMzksMC4zOS0xLjAyLDAtMS40MWwtMi4zNC0yLjM0Yy0wLjQ3LTAuNDctMS4xMi0wLjI5LTEuNDEsMGwtMS44MywxLjgzbDMuNzUsMy43NUwyMC43MSw3LjA0eiIgLz4KICAgIDwvZz4KICAgIDxnCiAgICAgICBpZD0iZzEyIiAvPgogIDwvZz4KPC9zdmc+Cg==';
export const FIND_AGENT_AND_ASSIGN_ICON =
	'';
