package com.ngdesk.repositories.emailList;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.emailList.dao.EmailList;

public interface CustomEmailListRepository {
	public Optional<List<EmailList>> findAllEmailLists(Pageable pageable, String collectionName);

	public Optional<EmailList> findEmailListById(String id, String collectionName);

	public Integer findEmailListCount(String collectionName);
}
