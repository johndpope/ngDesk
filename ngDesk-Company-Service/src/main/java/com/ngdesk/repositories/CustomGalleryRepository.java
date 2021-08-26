package com.ngdesk.repositories;

import com.ngdesk.company.dao.Gallery;

public interface CustomGalleryRepository {
	
	public Gallery getFirstGallery(String collectionName);
}
