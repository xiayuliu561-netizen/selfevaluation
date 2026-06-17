package com.mywork.bean;

public class Questionscore {
	private Integer id;
	private Integer userid;
	private Integer teacherid;
	private String lesson;
	private Integer surveyId;
	private String surveyName;
	private Integer questionid;
	private Integer option1score;
	private Integer option2score;
	private Integer option3score;
	private Integer option4score;
	private Integer option5score;
	private Integer option6score;
	private String avgscore;
	private User user;
	private Lesson lessonentity;
	
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
	public Integer getSurveyId() {
		return surveyId;
	}
	public void setSurveyId(Integer surveyId) {
		this.surveyId = surveyId;
	}
	public String getSurveyName() {
		return surveyName;
	}
	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}
	public Integer getQuestionid() {
		return questionid;
	}
	public void setQuestionid(Integer questionid) {
		this.questionid = questionid;
	}
	public Integer getOption1score() {
		return option1score;
	}
	public void setOption1score(Integer option1score) {
		this.option1score = option1score;
	}
	public Integer getOption2score() {
		return option2score;
	}
	public void setOption2score(Integer option2score) {
		this.option2score = option2score;
	}
	public Integer getOption3score() {
		return option3score;
	}
	public void setOption3score(Integer option3score) {
		this.option3score = option3score;
	}
	public Integer getOption4score() {
		return option4score;
	}
	public void setOption4score(Integer option4score) {
		this.option4score = option4score;
	}
	public Integer getOption5score() {
		return option5score;
	}
	public void setOption5score(Integer option5score) {
		this.option5score = option5score;
	}
	public Integer getOption6score() {
		return option6score;
	}
	public void setOption6score(Integer option6score) {
		this.option6score = option6score;
	}
	public String getAvgscore() {
		return avgscore;
	}
	public void setAvgscore(String avgscore) {
		this.avgscore = avgscore;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	
	
	
}
