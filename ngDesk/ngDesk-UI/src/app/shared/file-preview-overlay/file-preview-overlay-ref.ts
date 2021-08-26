import { OverlayRef } from '@angular/cdk/overlay';

export class FilePreviewOverlayRef {

  constructor(private overlayRef: OverlayRef) { }

  public close(): void {
    this.overlayRef.dispose();
  }
}
