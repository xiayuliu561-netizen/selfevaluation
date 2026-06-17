package com.mywork.bean;

import java.util.ArrayList;
import java.util.List;

public class ScoreImportResult {
	private Integer createdRecords = 0;
	private Integer insertedDetails = 0;
	private Integer updatedDetails = 0;
	private Integer filledBlankDetails = 0;
	private Integer overwrittenDetails = 0;
	private Integer skippedConflicts = 0;
	private Integer skippedDuplicates = 0;
	private Integer skippedInvalid = 0;
	private Integer affectedStudents = 0;
	private Integer recalculatedStudents = 0;
	private List<String> errors = new ArrayList<String>();

	public Integer getCreatedRecords() {
		return createdRecords;
	}
	public void setCreatedRecords(Integer createdRecords) {
		this.createdRecords = createdRecords;
	}
	public Integer getInsertedDetails() {
		return insertedDetails;
	}
	public void setInsertedDetails(Integer insertedDetails) {
		this.insertedDetails = insertedDetails;
	}
	public Integer getUpdatedDetails() {
		return updatedDetails;
	}
	public void setUpdatedDetails(Integer updatedDetails) {
		this.updatedDetails = updatedDetails;
	}
	public Integer getFilledBlankDetails() {
		return filledBlankDetails;
	}
	public void setFilledBlankDetails(Integer filledBlankDetails) {
		this.filledBlankDetails = filledBlankDetails;
	}
	public Integer getOverwrittenDetails() {
		return overwrittenDetails;
	}
	public void setOverwrittenDetails(Integer overwrittenDetails) {
		this.overwrittenDetails = overwrittenDetails;
	}
	public Integer getSkippedConflicts() {
		return skippedConflicts;
	}
	public void setSkippedConflicts(Integer skippedConflicts) {
		this.skippedConflicts = skippedConflicts;
	}
	public Integer getSkippedDuplicates() {
		return skippedDuplicates;
	}
	public void setSkippedDuplicates(Integer skippedDuplicates) {
		this.skippedDuplicates = skippedDuplicates;
	}
	public Integer getSkippedInvalid() {
		return skippedInvalid;
	}
	public void setSkippedInvalid(Integer skippedInvalid) {
		this.skippedInvalid = skippedInvalid;
	}
	public Integer getAffectedStudents() {
		return affectedStudents;
	}
	public void setAffectedStudents(Integer affectedStudents) {
		this.affectedStudents = affectedStudents;
	}
	public Integer getRecalculatedStudents() {
		return recalculatedStudents;
	}
	public void setRecalculatedStudents(Integer recalculatedStudents) {
		this.recalculatedStudents = recalculatedStudents;
	}
	public List<String> getErrors() {
		return errors;
	}
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
}
