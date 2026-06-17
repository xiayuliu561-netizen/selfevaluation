package com.mywork.bean;

import java.math.BigDecimal;

public class Assessrate {
	private Integer id;
	private Integer userid;
	private String targetname;
	private Integer rate1;
	private Integer rate2;
	private Integer rate3;
	private Integer rate4;
	private Integer rate5;
	private Integer rate6;
	private String remarks;
	private BigDecimal targetrate;
	private Integer lessonid;
	private Lesson lesson;
	
	
	
	
	public Integer getLessonid() {
		return lessonid;
	}
	public void setLessonid(Integer lessonid) {
		this.lessonid = lessonid;
	}
	public Lesson getLesson() {
		return lesson;
	}
	public void setLesson(Lesson lesson) {
		this.lesson = lesson;
	}
	public BigDecimal getTargetrate() {
		return targetrate;
	}
	public void setTargetrate(BigDecimal targetrate) {
		this.targetrate = targetrate;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public Integer getRate5() {
		return rate5;
	}
	public void setRate5(Integer rate5) {
		this.rate5 = rate5;
	}
	public Integer getRate6() {
		return rate6;
	}
	public void setRate6(Integer rate6) {
		this.rate6 = rate6;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public String getTargetname() {
		return targetname;
	}
	public void setTargetname(String targetname) {
		this.targetname = targetname;
	}
	public Integer getRate1() {
		return rate1;
	}
	public void setRate1(Integer rate1) {
		this.rate1 = rate1;
	}
	public Integer getRate2() {
		return rate2;
	}
	public void setRate2(Integer rate2) {
		this.rate2 = rate2;
	}
	public Integer getRate3() {
		return rate3;
	}
	public void setRate3(Integer rate3) {
		this.rate3 = rate3;
	}
	public Integer getRate4() {
		return rate4;
	}
	public void setRate4(Integer rate4) {
		this.rate4 = rate4;
	}
	
	
}
