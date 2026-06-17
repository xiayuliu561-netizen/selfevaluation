package com.mywork.bean;

public class Sumscore {
	private Integer id;
	private Integer userid;
	private Integer teacherid;
	private String lesson;
	private String sumscore;
	private String remarks;
	private Lesson lessonentity;
	private User user;

	public Lesson getLessonentity() {
		return lessonentity;
	}

	public void setLessonentity(Lesson lessonentity) {
		this.lessonentity = lessonentity;
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

	public String getSumscore() {
		return sumscore;
	}

	public void setSumscore(String sumscore) {
		this.sumscore = sumscore;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
