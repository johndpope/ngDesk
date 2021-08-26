package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.sam.swidtag.Swidtag;

public interface CustomSwidtagRepository {

	Optional<Swidtag> findExistingSwidtag(String fileName, String companyId, String assetId, String collectionName);

}
