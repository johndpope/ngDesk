package com.ngdesk.repositories;

import java.util.List;

public interface CustomModuleEntryRepository {

	public void updateOCRToMetadata(String dataId, String receipt, String fieldName, String collectionName);

}
