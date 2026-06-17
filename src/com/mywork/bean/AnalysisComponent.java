package com.mywork.bean;

import java.math.BigDecimal;

public class AnalysisComponent {
	private Integer id;
	private Integer lessonid;
	private Integer teacherid;
	private String componentName;
	private BigDecimal rate;
	private Integer sortno;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	public BigDecimal getRate() {
		return rate;
	}
	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}
	public Integer getSortno() {
		return sortno;
	}
	public void setSortno(Integer sortno) {
		this.sortno = sortno;
	}
}
