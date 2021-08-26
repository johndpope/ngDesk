import { TestBed } from '@angular/core/testing';

import { DiscoveryMapsService } from './discovery-maps.service';

describe('DiscoveryMapsService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: DiscoveryMapsService = TestBed.get(DiscoveryMapsService);
    expect(service).toBeTruthy();
  });
});
