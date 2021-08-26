package com.ngdesk.repositories;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class CustomNgdeskRepositoryImpl<T, ID extends Serializable> extends SimpleMongoRepository<T, ID>
		implements CustomNgdeskRepository<T, ID> {

	private final MongoEntityInformation<T, ID> metadata;
	private final MongoOperations mongoOperations;

	public CustomNgdeskRepositoryImpl(MongoEntityInformation<T, ID> metadata, MongoOperations mongoOperations) {
		super(metadata, mongoOperations);
		this.metadata = metadata;
		this.mongoOperations = mongoOperations;
	}

	/*
	 * FIND ALL
	 */
	public Page<T> findAll(Pageable pageable, String collectionName) {
		Long count = count(collectionName);
		List<T> list = findAll(new Query().with(pageable), collectionName);
		return new PageImpl<>(list, pageable, count);
	}

	public List<T> findAll(@Nullable Query query, String collectionName) {
		if (query == null) {
			return Collections.emptyList();
		}
		return mongoOperations.find(query, this.metadata.getJavaType(), collectionName);
	}

	public long count(String collectionName) {
		return mongoOperations.count(new Query(), collectionName);
	}
	/* END FIND ALL */

	/* FIND ALL BY COMPANY ID */
	public Page<T> findAllByCompanyId(Pageable pageable, String collectionName, String companyId) {
		Long count = mongoOperations.count(new Query(Criteria.where("COMPANY_ID").is(companyId)), collectionName);
		List<T> list = findAll(new Query(Criteria.where("COMPANY_ID").is(companyId)).with(pageable), collectionName);
		return new PageImpl<>(list, pageable, count);
	}
	/* END FIND ALL BY COMPANY ID */

	/* FIND BY ID */
	public Optional<T> findById(ID id, String collectionName) {
		Assert.notNull(id, "The given id must not be null!");
		return Optional.ofNullable(mongoOperations.findById(id, this.metadata.getJavaType(), collectionName));
	}
	/* END FIND BY ID */

	/* FIND BY ID AND COMPANY ID */
	public Optional<T> findByIdAndCompanyId(ID id, String companyId, String collectionName) {
		Assert.notNull(id, "The given id must not be null!");
		Assert.notNull(companyId, "The given companyId must not be null!");
		Assert.notNull(collectionName, "The given collectionName must not be null!");

		Query query = new Query();
		query.addCriteria(Criteria.where("COMPANY_ID").is(companyId));
		query.addCriteria(Criteria.where("_id").is(id));

		return Optional.ofNullable(mongoOperations.findOne(query, this.metadata.getJavaType(), collectionName));
	}
	/* END FIND BY ID AND COMPANY ID */

	/* SAVE */
	public <S extends T> S save(S entity, String collectionName) {
		Assert.notNull(entity, "Entity must not be null!");

		if (this.metadata.isNew(entity)) {
			return mongoOperations.insert(entity, collectionName);
		}

		return mongoOperations.save(entity, collectionName);
	}
	/* END SAVE */

	/* DELETE BY ID */
	public void deleteById(ID id, String collectionName) {
		Assert.notNull(id, "The given id must not be null!");
		mongoOperations.remove(getIdQuery(id), this.metadata.getJavaType(), collectionName);
	}

	private Query getIdQuery(Object id) {
		return new Query(getIdCriteria(id));
	}

	private Criteria getIdCriteria(Object id) {
		return where(this.metadata.getIdAttribute()).is(id);
	}
	/* END DELETE BY ID */

}
