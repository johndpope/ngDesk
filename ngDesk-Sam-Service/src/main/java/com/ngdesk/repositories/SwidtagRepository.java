package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.sam.swidtag.Swidtag;

@Repository
public interface SwidtagRepository extends CustomSwidtagRepository, CustomNgdeskRepository<Swidtag, String> {

}
