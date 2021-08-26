package com.ngdesk.repositories;

import com.ngdesk.data.dao.Swidtag;

public interface CustomSwidtagRepository {
	
	
	public Swidtag findExistingSwidtag(String filename, String companyId, String assetId, String collectionname);
}
