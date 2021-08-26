package com.ngdesk.tesseract.dao;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.tesseract.module.dao.Module;
import com.ngdesk.tesseract.module.dao.ModuleField;
import com.ngdesk.tesseract.module.dao.ModuleService;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

@Component
@RabbitListener(queues = "ocr-response", concurrency = "6")
public class OCRService {

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleService moduleService;

	@RabbitHandler
	public void ocrResponse(OCRPayload ocrPayload) {

		try {

			String base64String = ocrPayload.getAttachment().getFile();
			byte[] decodedBytes = Base64.getDecoder().decode(base64String);
			File file = File.createTempFile("temp", ".png");
			FileOutputStream outputStream = new FileOutputStream(file.getPath());
			outputStream.write(decodedBytes);
			ITesseract instance = new Tesseract();
			BufferedImage in = ImageIO.read(file);
			BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = newImage.createGraphics();
			g.drawImage(in, 0, 0, null);
			g.dispose();
			instance.setDatapath(ResourceUtils.getFile("classpath:").getPath());
			instance.setLanguage("eng");
			String result = instance.doOCR(newImage);
			outputStream.flush();
			outputStream.close();
			file.delete();

			Module module = modulesRepository.findById(ocrPayload.getModuleId(), "modules_" + ocrPayload.getCompanyId())
					.orElse(null);
			if (module != null) {

				ModuleField field = module.getFields().stream()
						.filter(currentField -> currentField.getFieldId().equals(ocrPayload.getFieldId())).findFirst()
						.orElse(null);
				if (field != null) {
					moduleEntryRepository.updateOCRToMetadata(ocrPayload.getDataId(), result,
							field.getName() + "_CAPTURED",
							moduleService.getCollectionName(module.getName(), ocrPayload.getCompanyId()));
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
