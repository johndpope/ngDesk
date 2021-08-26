import { TestBed } from '@angular/core/testing';

import { NormalizationRulesService } from './normalization-rules.service';

describe('NormalizationRulesService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: NormalizationRulesService = TestBed.get(NormalizationRulesService);
    expect(service).toBeTruthy();
  });
});
