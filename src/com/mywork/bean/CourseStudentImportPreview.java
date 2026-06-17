package com.mywork.bean;

import java.util.ArrayList;
import java.util.List;

public class CourseStudentImportPreview {
	private String token;
	private String source;
	private String message;
	private Integer totalCount = 0;
	private Integer validCount = 0;
	private Integer invalidCount = 0;
	private Integer duplicateCount = 0;
	private Integer existingCount = 0;
	private List<CourseStudentImportItem> items = new ArrayList<CourseStudentImportItem>();

	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}
	public Integer getValidCount() {
		return validCount;
	}
	public void setValidCount(Integer validCount) {
		this.validCount = validCount;
	}
	public Integer getInvalidCount() {
		return invalidCount;
	}
	public void setInvalidCount(Integer invalidCount) {
		this.invalidCount = invalidCount;
	}
	public Integer getDuplicateCount() {
		return duplicateCount;
	}
	public void setDuplicateCount(Integer duplicateCount) {
		this.duplicateCount = duplicateCount;
	}
	public Integer getExistingCount() {
		return existingCount;
	}
	public void setExistingCount(Integer existingCount) {
		this.existingCount = existingCount;
	}
	public List<CourseStudentImportItem> getItems() {
		return items;
	}
	public void setItems(List<CourseStudentImportItem> items) {
		this.items = items;
	}
}
