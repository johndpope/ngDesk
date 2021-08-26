package com.ngdesk.repositories;

import java.util.Map;

import org.springframework.stereotype.Repository;

@Repository
public interface ContactsRepository extends CustomNgdeskRepository<Map<String, Object>, String>, CustomContactsRepository {

}
