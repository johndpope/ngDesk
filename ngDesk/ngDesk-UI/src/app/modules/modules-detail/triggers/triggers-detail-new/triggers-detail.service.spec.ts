import { TestBed } from '@angular/core/testing';

import { TriggersDetailService } from './triggers-detail.service';

describe('TriggersDetailService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: TriggersDetailService = TestBed.get(TriggersDetailService);
    expect(service).toBeTruthy();
  });
});
