package com.ngdesk.sam.swidtag;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.SwidtagRepository;
import com.ngdesk.sam.software.SoftwareInstallation;
import com.ngdesk.sam.software.SoftwareInstallations;

@Component
@RabbitListener(queues = "post-swidtag-and-software", concurrency = "5")
public class SwidtagSoftwareListener {

	private final Logger log = LoggerFactory.getLogger(SwidtagSoftwareListener.class);

	@Autowired
	SwidtagRepository swidtagRepository;

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	DataProxy dataProxy;

	@RabbitHandler
	public void postSwidtagAndSoftwareEntry(SwidtagPayload swidtagPayload) {
		Swidtag swidtag = swidtagPayload.getSwidtag();
		postSoftware(swidtagPayload);
		swidtag.setStatus("Processed");
		swidtagRepository.save(swidtag, "swidtag_files");
	}

	public void postSoftware(SwidtagPayload swidtagPayload) {
		try {
			SoftwareInstallation softwareInstallation = getSoftwareDetails(
					swidtagPayload.getSwidtag().getFileContent());
			softwareInstallation.setAsset(swidtagPayload.getSwidtag().getAssetId());
			String moduleId = moduleRepository.findModuleIdByModuleName("Software Installation",
					"modules_" + swidtagPayload.getSwidtag().getCompanyId()).get().getModuleId();
			List<SoftwareInstallation> softwares = new ArrayList<SoftwareInstallation>();
			softwares.add(softwareInstallation);
			SoftwareInstallations softwareInstallations = new SoftwareInstallations(softwares);
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, Object> payload = mapper.readValue(mapper.writeValueAsString(softwareInstallations),
					HashMap.class);
			dataProxy.postModuleEntry(payload, moduleId, swidtagPayload.getUser().getCompanyId(),
					swidtagPayload.getUser().getUserUuid());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SoftwareInstallation getSoftwareDetails(String fileContent) {
		try {
			SoftwareInstallation softwareInstallation = new SoftwareInstallation();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder doucumentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = doucumentBuilder.parse(new InputSource(new StringReader(fileContent)));
			document.getDocumentElement().normalize();
			softwareInstallation.setSoftwareName(getSoftwareName(document));
			softwareInstallation.setPublisher(getPublisher(document));
			softwareInstallation.setVersion(getVersion(document));
			return softwareInstallation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getSoftwareName(Document document) throws Exception {
		NodeList nodeList = document.getElementsByTagName("swid:product_title");
		if (nodeList.item(0).getTextContent() != null) {
			return nodeList.item(0).getTextContent();
		}
		return null;
	}

	public String getPublisher(Document document) throws Exception {
		NodeList nodeList = document.getElementsByTagName("swid:software_creator");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				return element.getElementsByTagName("swid:name").item(0).getTextContent();
			}
		}
		return null;
	}

	public String getVersion(Document document) throws Exception {
		NodeList nodeList = document.getElementsByTagName("swid:product_version");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;

				return element.getElementsByTagName("swid:name").item(0).getTextContent();
			}
		}
		return null;
	}

}
