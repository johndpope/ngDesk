import { MissingTranslationHandler, MissingTranslationHandlerParams } from '@ngx-translate/core';

export class CustomMissingTranslationHandler implements MissingTranslationHandler {
  public handle(params: MissingTranslationHandlerParams) {
    return 'TRANSLATION IS MISSING!';
  }
}
