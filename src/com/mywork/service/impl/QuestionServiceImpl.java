package com.mywork.service.impl;


import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mywork.bean.Question;
import com.mywork.service.QuestionService;

@Service("QuestionService")
public class QuestionServiceImpl implements QuestionService{
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private boolean surveyColumnsChecked = false;

	/**
	 * 根据ID查找
	 * @param id
	 * @return
	 */
	public Question getQuestionById(String id) {
		ensureSurveyColumns();
		return (Question)sqlSession.selectOne("com.mywork.mapper.QuestionMapper.getById", id);
	}
	/**
	 * 删除用户
	 */
	public boolean delete(String id) {
		ensureSurveyColumns();
		sqlSession.delete("com.mywork.mapper.QuestionMapper.delete", id);
		return true;
	}
	/**
	 * 增加用户
	 */
	public boolean insert(Question Question) {
		ensureSurveyColumns();
		sqlSession.insert("com.mywork.mapper.QuestionMapper.insert", Question);
		return true;
	}
	/**
	 * 修改用户
	 */
	public boolean update(Question Question) {
		ensureSurveyColumns();
		sqlSession.update("com.mywork.mapper.QuestionMapper.update", Question);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public List<Question> getList(Map<String, Object> map) {
		ensureSurveyColumns();
		return (List<Question>) (List<?>) sqlSession.selectList("com.mywork.mapper.QuestionMapper.getList", map);
	}

	private synchronized void ensureSurveyColumns(){
		if(surveyColumnsChecked){
			return;
		}
		try{
			jdbcTemplate.execute("ALTER TABLE question ADD COLUMN survey_id INT NULL AFTER id");
		}catch(Exception e){
		}
		try{
			jdbcTemplate.execute("ALTER TABLE question ADD COLUMN survey_name VARCHAR(200) NULL AFTER survey_id");
		}catch(Exception e){
		}
		try{
			jdbcTemplate.execute("ALTER TABLE question ADD COLUMN survey_desc TEXT NULL AFTER survey_name");
		}catch(Exception e){
		}
		try{
			jdbcTemplate.execute("ALTER TABLE question ADD COLUMN sortno INT NULL AFTER survey_desc");
		}catch(Exception e){
		}
		try{
			jdbcTemplate.execute("ALTER TABLE question ADD COLUMN option_count INT NULL AFTER sortno");
		}catch(Exception e){
		}
		surveyColumnsChecked = true;
	}

	
	
}

