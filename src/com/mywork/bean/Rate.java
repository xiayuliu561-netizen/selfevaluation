package com.mywork.bean;

public class Rate {
	private Integer id;
	private Integer teacherid;
	private Integer showrate;
	private Integer homeworkrate;
	private Integer testrate;
	private Integer designrate;
	private Integer middlerate;
	private Integer endrate;
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
	public Integer getMiddlerate() {
		return middlerate;
	}
	public void setMiddlerate(Integer middlerate) {
		this.middlerate = middlerate;
	}
	public Integer getEndrate() {
		return endrate;
	}
	public void setEndrate(Integer endrate) {
		this.endrate = endrate;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getTeacherid() {
		return teacherid;
	}
	public void setTeacherid(Integer teacherid) {
		this.teacherid = teacherid;
	}
	public Integer getShowrate() {
		return showrate;
	}
	public void setShowrate(Integer showrate) {
		this.showrate = showrate;
	}
	public Integer getHomeworkrate() {
		return homeworkrate;
	}
	public void setHomeworkrate(Integer homeworkrate) {
		this.homeworkrate = homeworkrate;
	}
	public Integer getTestrate() {
		return testrate;
	}
	public void setTestrate(Integer testrate) {
		this.testrate = testrate;
	}
	public Integer getDesignrate() {
		return designrate;
	}
	public void setDesignrate(Integer designrate) {
		this.designrate = designrate;
	}

	
}
