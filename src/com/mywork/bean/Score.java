package com.mywork.bean;

import java.math.BigDecimal;
import java.util.Map;

public class Score {
	private Integer id;
	private Integer userid;
	private Integer teacherid;
	private String lesson;
	private BigDecimal show;
	private BigDecimal homework;
	private BigDecimal test;
	private BigDecimal design;
	private BigDecimal middle;
	private BigDecimal end;
	private String createdate;
	private Sumscore sumscore;
	private Map<String,Object> datamap;
	private Lesson lessonentity;
	private User teacher;
	private String queryid;
	
	
	public String getQueryid() {
		return queryid;
	}
	public void setQueryid(String queryid) {
		this.queryid = queryid;
	}
	public User getTeacher() {
		return teacher;
	}
	public void setTeacher(User teacher) {
		this.teacher = teacher;
	}
	public Lesson getLessonentity() {
		return lessonentity;
	}
	public void setLessonentity(Lesson lessonentity) {
		this.lessonentity = lessonentity;
	}
	public Map<String, Object> getDatamap() {
		return datamap;
	}
	public void setDatamap(Map<String, Object> datamap) {
		this.datamap = datamap;
	}
	public Sumscore getSumscore() {
		return sumscore;
	}
	public void setSumscore(Sumscore sumscore) {
		this.sumscore = sumscore;
	}
	private User user;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
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
	public Integer getTeacherid() {
		return teacherid;
	}
	public void setTeacherid(Integer teacherid) {
		this.teacherid = teacherid;
	}
	public String getLesson() {
		return lesson;
	}
	public void setLesson(String lesson) {
		this.lesson = lesson;
	}
	
	public BigDecimal getShow() {
		return show;
	}
	public void setShow(BigDecimal show) {
		this.show = show;
	}
	public BigDecimal getHomework() {
		return homework;
	}
	public void setHomework(BigDecimal homework) {
		this.homework = homework;
	}
	public BigDecimal getTest() {
		return test;
	}
	public void setTest(BigDecimal test) {
		this.test = test;
	}
	public BigDecimal getDesign() {
		return design;
	}
	public void setDesign(BigDecimal design) {
		this.design = design;
	}
	public BigDecimal getMiddle() {
		return middle;
	}
	public void setMiddle(BigDecimal middle) {
		this.middle = middle;
	}
	public BigDecimal getEnd() {
		return end;
	}
	public void setEnd(BigDecimal end) {
		this.end = end;
	}
	public String getCreatedate() {
		return createdate;
	}
	public void setCreatedate(String createdate) {
		this.createdate = createdate;
	}
	
	
}
