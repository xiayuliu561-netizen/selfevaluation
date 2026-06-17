package com.mywork.bean;

import java.math.BigDecimal;

public class ScoreImportItem {
	private Integer rowNumber;
	private String no;
	private String name;
	private Integer userid;
	private String systemName;
	private String deptName;
	private String componentName;
	private BigDecimal score;
	private String rawScore;
	private BigDecimal existingScore;
	private String action;
	private Boolean valid;
	private Boolean conflict;
	private Boolean duplicate;
	private String message;
	private String scoreSource;
	private String totalScoreSource;
	private BigDecimal confidence;

	public Integer getRowNumber() {
		return rowNumber;
	}
	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public String getSystemName() {
		return systemName;
	}
	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	public BigDecimal getScore() {
		return score;
	}
	public void setScore(BigDecimal score) {
		this.score = score;
	}
	public String getRawScore() {
		return rawScore;
	}
	public void setRawScore(String rawScore) {
		this.rawScore = rawScore;
	}
	public BigDecimal getExistingScore() {
		return existingScore;
	}
	public void setExistingScore(BigDecimal existingScore) {
		this.existingScore = existingScore;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Boolean getValid() {
		return valid;
	}
	public void setValid(Boolean valid) {
		this.valid = valid;
	}
	public Boolean getConflict() {
		return conflict;
	}
	public void setConflict(Boolean conflict) {
		this.conflict = conflict;
	}
	public Boolean getDuplicate() {
		return duplicate;
	}
	public void setDuplicate(Boolean duplicate) {
		this.duplicate = duplicate;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getScoreSource() {
		return scoreSource;
	}
	public void setScoreSource(String scoreSource) {
		this.scoreSource = scoreSource;
	}
	public String getTotalScoreSource() {
		return totalScoreSource;
	}
	public void setTotalScoreSource(String totalScoreSource) {
		this.totalScoreSource = totalScoreSource;
	}
	public BigDecimal getConfidence() {
		return confidence;
	}
	public void setConfidence(BigDecimal confidence) {
		this.confidence = confidence;
	}
}
