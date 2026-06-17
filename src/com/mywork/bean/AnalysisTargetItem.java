package com.mywork.bean;

import java.math.BigDecimal;

public class AnalysisTargetItem {
	private Integer id;
	private Integer targetid;
	private Integer lessonid;
	private Integer teacherid;
	private String methodName;
	private BigDecimal weightRate;
	private BigDecimal coefficient;
	private Integer sortno;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getTargetid() {
		return targetid;
	}
	public void setTargetid(Integer targetid) {
		this.targetid = targetid;
	}
	public Integer getLessonid() {
		return lessonid;
	}
	public void setLessonid(Integer lessonid) {
		this.lessonid = lessonid;
	}
	public Integer getTeacherid() {
		return teacherid;
	}
	public void setTeacherid(Integer teacherid) {
		this.teacherid = teacherid;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public BigDecimal getWeightRate() {
		return weightRate;
	}
	public void setWeightRate(BigDecimal weightRate) {
		this.weightRate = weightRate;
	}
	public BigDecimal getCoefficient() {
		return coefficient;
	}
	public void setCoefficient(BigDecimal coefficient) {
		this.coefficient = coefficient;
	}
	public Integer getSortno() {
		return sortno;
	}
	public void setSortno(Integer sortno) {
		this.sortno = sortno;
	}
}
