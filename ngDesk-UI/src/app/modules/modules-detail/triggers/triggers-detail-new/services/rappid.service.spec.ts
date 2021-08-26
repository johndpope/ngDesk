import { TestBed } from '@angular/core/testing';

import { RappidService } from './rappid.service';

describe('RappidService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: RappidService = TestBed.get(RappidService);
    expect(service).toBeTruthy();
  });
});
