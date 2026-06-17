package com.mywork.bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScoreImportPreview {
	private String token;
	private String source;
	private String tableType;
	private String message;
	private String questionScoreMessage;
	private Integer totalCount = 0;
	private Integer validCount = 0;
	private Integer invalidCount = 0;
	private Integer conflictCount = 0;
	private Integer duplicateCount = 0;
	private Integer createCount = 0;
	private Integer fillCount = 0;
	private Integer updateRiskCount = 0;
	private List<String> affectedComponents = new ArrayList<String>();
	private Map<String,String> fieldMappings = new LinkedHashMap<String,String>();
	private List<ScoreImportItem> items = new ArrayList<ScoreImportItem>();

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
	public String getTableType() {
		return tableType;
	}
	public void setTableType(String tableType) {
		this.tableType = tableType;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getQuestionScoreMessage() {
		return questionScoreMessage;
	}
	public void setQuestionScoreMessage(String questionScoreMessage) {
		this.questionScoreMessage = questionScoreMessage;
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
	public Integer getConflictCount() {
		return conflictCount;
	}
	public void setConflictCount(Integer conflictCount) {
		this.conflictCount = conflictCount;
	}
	public Integer getDuplicateCount() {
		return duplicateCount;
	}
	public void setDuplicateCount(Integer duplicateCount) {
		this.duplicateCount = duplicateCount;
	}
	public Integer getCreateCount() {
		return createCount;
	}
	public void setCreateCount(Integer createCount) {
		this.createCount = createCount;
	}
	public Integer getFillCount() {
		return fillCount;
	}
	public void setFillCount(Integer fillCount) {
		this.fillCount = fillCount;
	}
	public Integer getUpdateRiskCount() {
		return updateRiskCount;
	}
	public void setUpdateRiskCount(Integer updateRiskCount) {
		this.updateRiskCount = updateRiskCount;
	}
	public List<String> getAffectedComponents() {
		return affectedComponents;
	}
	public void setAffectedComponents(List<String> affectedComponents) {
		this.affectedComponents = affectedComponents;
	}
	public Map<String, String> getFieldMappings() {
		return fieldMappings;
	}
	public void setFieldMappings(Map<String, String> fieldMappings) {
		this.fieldMappings = fieldMappings;
	}
	public List<ScoreImportItem> getItems() {
		return items;
	}
	public void setItems(List<ScoreImportItem> items) {
		this.items = items;
	}
}
