package com.mywork.bean;

import java.math.BigDecimal;
import java.util.List;

public class AnalysisTarget {
	private Integer id;
	private Integer lessonid;
	private Integer teacherid;
	private String targetName;
	private String targetContent;
	private BigDecimal targetrate;
	private Integer sortno;
	private List<AnalysisTargetItem> itemlist;
	
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
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public String getTargetContent() {
		return targetContent;
	}
	public void setTargetContent(String targetContent) {
		this.targetContent = targetContent;
	}
	public BigDecimal getTargetrate() {
		return targetrate;
	}
	public void setTargetrate(BigDecimal targetrate) {
		this.targetrate = targetrate;
	}
	public Integer getSortno() {
		return sortno;
	}
	public void setSortno(Integer sortno) {
		this.sortno = sortno;
	}
	public List<AnalysisTargetItem> getItemlist() {
		return itemlist;
	}
	public void setItemlist(List<AnalysisTargetItem> itemlist) {
		this.itemlist = itemlist;
	}
}
