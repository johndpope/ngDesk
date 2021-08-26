package com.ngdesk.repositories;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;

@NoRepositoryBean
public interface CustomNgdeskRepository<T, ID extends Serializable> extends MongoRepository<T, ID> {

	public Page<T> findAll(Pageable pageable, String collectionName);

	public <S extends T> S save(S entity, String collectionName);

	public Optional<T> findById(ID id, String collectionName);

	public void deleteById(ID id, String collectionName);

	public Page<T> findAllByCompanyId(Pageable pageable, String collectionName, String companyId);

	public Optional<T> findByIdAndCompanyId(ID id, String companyId, String collectionName);

	public List<T> findAll(@Nullable Query query, String collectionName);

}
