package com.ngdesk.repositories;

import com.ngdesk.company.dao.CustomLogin;

public interface LoginRepository extends CustomLoginRepository, CustomNgdeskRepository<CustomLogin, String> {

}
