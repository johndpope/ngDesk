package com.ngdesk.repositories.emailList;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.emailList.dao.EmailList;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface EmailListRepository
		extends CustomEmailListRepository, CustomNgdeskRepository<EmailList, String> {

	

}
