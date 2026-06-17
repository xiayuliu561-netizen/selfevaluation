package com.mywork.bean;

import java.util.ArrayList;
import java.util.List;

public class CourseStudentImportResult {
	private Integer createdStudents = 0;
	private Integer updatedStudents = 0;
	private Integer existingStudents = 0;
	private Integer createdColleges = 0;
	private Integer createdDepts = 0;
	private Integer boundStudents = 0;
	private Integer skippedRows = 0;
	private List<String> errors = new ArrayList<String>();

	public Integer getCreatedStudents() {
		return createdStudents;
	}
	public void setCreatedStudents(Integer createdStudents) {
		this.createdStudents = createdStudents;
	}
	public Integer getUpdatedStudents() {
		return updatedStudents;
	}
	public void setUpdatedStudents(Integer updatedStudents) {
		this.updatedStudents = updatedStudents;
	}
	public Integer getExistingStudents() {
		return existingStudents;
	}
	public void setExistingStudents(Integer existingStudents) {
		this.existingStudents = existingStudents;
	}
	public Integer getCreatedColleges() {
		return createdColleges;
	}
	public void setCreatedColleges(Integer createdColleges) {
		this.createdColleges = createdColleges;
	}
	public Integer getCreatedDepts() {
		return createdDepts;
	}
	public void setCreatedDepts(Integer createdDepts) {
		this.createdDepts = createdDepts;
	}
	public Integer getBoundStudents() {
		return boundStudents;
	}
	public void setBoundStudents(Integer boundStudents) {
		this.boundStudents = boundStudents;
	}
	public Integer getSkippedRows() {
		return skippedRows;
	}
	public void setSkippedRows(Integer skippedRows) {
		this.skippedRows = skippedRows;
	}
	public List<String> getErrors() {
		return errors;
	}
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
}
