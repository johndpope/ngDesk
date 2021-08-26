package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;
import com.ngdesk.workflow.executor.dao.BlackListWhiteList;

@Repository
public interface BlackListWhiteListRepository extends CustomNgdeskRepository<BlackListWhiteList,String>, CustomBlackListWhiteListRepository{

}
