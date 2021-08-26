package com.ngdesk.repositories;


import org.springframework.stereotype.Repository;

import com.ngdesk.company.dao.Gallery;

@Repository
public interface GalleryRepository extends CustomGalleryRepository, CustomNgdeskRepository<Gallery, String> {

}
