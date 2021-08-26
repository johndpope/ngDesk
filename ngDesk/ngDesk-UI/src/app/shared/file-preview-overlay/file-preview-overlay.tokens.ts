import { InjectionToken } from '@angular/core';

import { PopupData } from './file-preview-overlay.service';

export const FILE_PREVIEW_DIALOG_DATA = new InjectionToken<PopupData>('FILE_PREVIEW_DIALOG_DATA');
