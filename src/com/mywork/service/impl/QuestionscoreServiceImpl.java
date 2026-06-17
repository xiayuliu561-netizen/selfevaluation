package com.mywork.service.impl;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mywork.bean.Questionscore;
import com.mywork.service.QuestionscoreService;

@Service("QuestionscoreService")
public class QuestionscoreServiceImpl implements QuestionscoreService{
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private boolean surveyColumnChecked = false;

	/**
	 * 根据ID查找
	 * @param id
	 * @return
	 */
	public Questionscore getById(String id) {
		ensureSurveyColumn();
		return (Questionscore)sqlSession.selectOne("com.mywork.mapper.QuestionscoreMapper.getById", id);
	}
	/**
	 * 删除
	 */
	public boolean delete(String id) {
		ensureSurveyColumn();
		sqlSession.delete("com.mywork.mapper.QuestionscoreMapper.delete", id);
		return true;
	}
	/**
	 * 增加
	 */
	public boolean insert(Questionscore Questionscore) {
		ensureSurveyColumn();
		sqlSession.insert("com.mywork.mapper.QuestionscoreMapper.insert", Questionscore);
		return true;
		
	}
	/**
	 * 修改
	 */
	public boolean update(Questionscore Questionscore) {
		ensureSurveyColumn();
		sqlSession.update("com.mywork.mapper.QuestionscoreMapper.update", Questionscore);
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<Questionscore> getList(Map<String, Object> map) {
		ensureSurveyColumn();
		return (List<Questionscore>) (List<?>) sqlSession.selectList("com.mywork.mapper.QuestionscoreMapper.getList", map);
	}
	@SuppressWarnings("unchecked")
	public List<Questionscore> getAvgList(Map<String, Object> map) {
		ensureSurveyColumn();
		return (List<Questionscore>) (List<?>) sqlSession.selectList("com.mywork.mapper.QuestionscoreMapper.getAvgList", map);
	}

	public boolean deleteBySurvey(Map<String, Object> map) {
		ensureSurveyColumn();
		sqlSession.delete("com.mywork.mapper.QuestionscoreMapper.deleteBySurvey", map);
		return true;
	}

	private synchronized void ensureSurveyColumn(){
		if(surveyColumnChecked){
			return;
		}
		try{
			jdbcTemplate.execute("ALTER TABLE questionscore ADD COLUMN survey_id INT NULL AFTER lesson");
		}catch(Exception e){
		}
		surveyColumnChecked = true;
	}
	
}
