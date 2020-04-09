package com.fedex.accs.file.convert.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

import org.ghost4j.converter.ConverterException;
import org.ghost4j.converter.PDFConverter;
import org.ghost4j.document.DocumentException;
import org.ghost4j.document.PSDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiParam;

@RestController
public class FileConverterController {

	@Value("${file.upload.dir}")
	private String uploadDir;

	@Value("${file.from.dir}")
	private String fromDir;
	
	//, produces = { "multipart/form-data" }
	@PostMapping(value = "/fileUpload", consumes = { "multipart/form-data" })
	public void uploadConvertPSToPDF(
			@ApiParam(name = "file", value = "Select the file to Upload", required = true)
			@RequestParam("file") MultipartFile file, HttpServletResponse response) {

		if(file != null) {
			try {
				String fileName = StringUtils.cleanPath(file.getOriginalFilename());
				fileName = fileName.replaceAll(".ps", ".pdf");

				PSDocument document = new PSDocument();
				document.load(file.getInputStream());
				PDFConverter converter = new PDFConverter();
				converter.setPDFSettings(PDFConverter.OPTION_PDFSETTINGS_PREPRESS);

				response.setContentType("application/pdf");
				response.setHeader("Content-Description", " File Transfer");
//				response.setHeader("Content-Transfer-Encoding", " binary");
				response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

				converter.convert(document, response.getOutputStream());
				response.getOutputStream().flush();

			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ConverterException e) {
				e.printStackTrace();
			} catch (DocumentException e) {
				e.printStackTrace();
			} 
		}
	}

	@GetMapping("/download/{fileName}")
	public void downloadFile( @PathVariable("fileName") String fileName, HttpServletResponse resonse)  throws IOException {

		File file = new File(fromDir+fileName);
		// Content-Type
		// application/pdf
		resonse.setContentType(MediaType.APPLICATION_PDF_VALUE);
		resonse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=Umesh.pdf");
		resonse.setContentLength((int) file.length());

		BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
		BufferedOutputStream outStream = new BufferedOutputStream(resonse.getOutputStream());

		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		while ((bytesRead = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}
		outStream.flush();
		inStream.close();
	}
	
	
	

//	@PostMapping("/multi-fileUpload")
//	public void multiUpload(@RequestParam("file") MultipartFile[] files) {
//		List<MultipartFile> fileDownloadUrls = new LinkedList<>(Arrays.asList(files));
//		fileDownloadUrls.stream().forEach(file -> uploadFile(file));
//	}

}
