package com.mywork.bean;

public class CourseStudentImportItem {
	private Integer rowNumber;
	private String name;
	private String no;
	private String collegeName;
	private String deptName;
	private Boolean valid;
	private Boolean duplicate;
	private Boolean existingStudent;
	private String message;

	public Integer getRowNumber() {
		return rowNumber;
	}
	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getCollegeName() {
		return collegeName;
	}
	public void setCollegeName(String collegeName) {
		this.collegeName = collegeName;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public Boolean getValid() {
		return valid;
	}
	public void setValid(Boolean valid) {
		this.valid = valid;
	}
	public Boolean getDuplicate() {
		return duplicate;
	}
	public void setDuplicate(Boolean duplicate) {
		this.duplicate = duplicate;
	}
	public Boolean getExistingStudent() {
		return existingStudent;
	}
	public void setExistingStudent(Boolean existingStudent) {
		this.existingStudent = existingStudent;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
