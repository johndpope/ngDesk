package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

public interface CustomContactsRepository {

	Optional<Map<String, Object>> findContactsByContactId(String contactId, String collectionName);

}
