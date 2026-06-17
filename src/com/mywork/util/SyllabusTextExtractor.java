package com.mywork.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class SyllabusTextExtractor {
	private SyllabusTextExtractor(){
	}

	public static String extract(File file, String fileName) throws Exception{
		String lowerName = fileName == null ? "" : fileName.toLowerCase();
		if(lowerName.endsWith(".docx")){
			return readDocx(file);
		}
		if(lowerName.endsWith(".doc")){
			return readDoc(file);
		}
		if(lowerName.endsWith(".pdf")){
			return readPdf(file);
		}
		return readText(file);
	}

	private static String readDocx(File file) throws Exception{
		FileInputStream inputStream = new FileInputStream(file);
		try{
			XWPFDocument document = new XWPFDocument(inputStream);
			StringBuilder builder = new StringBuilder();
			for(XWPFParagraph paragraph : document.getParagraphs()){
				builder.append(paragraph.getText()).append("\n");
			}
			java.util.Iterator<org.apache.poi.xwpf.usermodel.XWPFTable> tableIterator = document.getTablesIterator();
			while(tableIterator.hasNext()){
				org.apache.poi.xwpf.usermodel.XWPFTable table = tableIterator.next();
				for(org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()){
					for(org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()){
						builder.append(cell.getText()).append(" ");
					}
					builder.append("\n");
				}
			}
			return builder.toString();
		}finally{
			inputStream.close();
		}
	}

	private static String readDoc(File file) throws Exception{
		FileInputStream inputStream = new FileInputStream(file);
		try{
			HWPFDocument document = new HWPFDocument(inputStream);
			WordExtractor extractor = new WordExtractor(document);
			return extractor.getText();
		}finally{
			inputStream.close();
		}
	}

	private static String readPdf(File file) throws Exception{
		File output = File.createTempFile("syllabus_pdf_", ".txt");
		Process process = new ProcessBuilder("pdftotext", "-layout", file.getAbsolutePath(), output.getAbsolutePath()).start();
		int code = process.waitFor();
		if(code != 0){
			throw new IllegalStateException("PDF 文本提取失败，请确认服务器已安装 pdftotext，或改用 docx 文件上传");
		}
		try{
			return readText(output);
		}finally{
			output.delete();
		}
	}

	private static String readText(File file) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		StringBuilder builder = new StringBuilder();
		try{
			String line;
			while((line = reader.readLine()) != null){
				builder.append(line).append("\n");
			}
		}finally{
			reader.close();
		}
		return builder.toString();
	}
}
