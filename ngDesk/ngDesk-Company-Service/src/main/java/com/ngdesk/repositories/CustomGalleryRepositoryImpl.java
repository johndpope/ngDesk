package com.ngdesk.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.company.dao.Gallery;

@Repository
public class CustomGalleryRepositoryImpl implements CustomGalleryRepository {
	
	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Gallery getFirstGallery(String collectionName) {
		Query query=new Query();
		return mongoOperations.findOne(query, Gallery.class, collectionName);
	}

}
