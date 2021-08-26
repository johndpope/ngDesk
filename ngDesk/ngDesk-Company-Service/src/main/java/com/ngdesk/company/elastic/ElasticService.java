package com.ngdesk.company.elastic;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.company.module.dao.Module;
import com.ngdesk.company.module.dao.ModuleField;
import com.ngdesk.company.module.dao.ModuleService;
import com.ngdesk.repositories.ModuleRepository;

import net.minidev.json.JSONObject;

@Component
public class ElasticService {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	RestHighLevelClient elasticClient;

	public void loadModuleDataIntoFieldLookUp(String companyId) {
		try {
			Optional<List<Module>> optionalModules = moduleRepository.findAllModules("modules_" + companyId);

			BulkRequest request = new BulkRequest();
			for (Module module : optionalModules.get()) {
				String moduleId = module.getModuleId();
				JSONObject body = new JSONObject();
				body.put("COMPANY_ID", companyId);
				body.put("MODULE_ID", moduleId);

				List<ModuleField> fields = moduleService.getAllFields(module, optionalModules.get());
				
				int i = 0;
				int index = 85;
				for (ModuleField field : fields) {
					i++;
					String fieldName = field.getName();
					String displayDataType = field.getDataType().getDisplay();

					if (!fieldName.equals("TEAMS")) {
						if (displayDataType.equals("Date/Time") || displayDataType.equals("Date")
								|| displayDataType.equals("Time")) {
							body.put("field" + index, fieldName);
							index++;
						} else {
							body.put("field" + i, fieldName);
						}
					}

				}

				String textToHash = moduleId + companyId;
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] messageDigest = md.digest(textToHash.toString().getBytes());
				BigInteger number = new BigInteger(1, messageDigest);
				String hashText = number.toString(16);

				IndexRequest requestIn = new IndexRequest("field_lookup");
				requestIn.source(body.toString(), XContentType.JSON);
				requestIn.id(hashText);
				request.add(requestIn);
			}
			request.setRefreshPolicy("wait_for");
			elasticClient.bulk(request, RequestOptions.DEFAULT);
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
