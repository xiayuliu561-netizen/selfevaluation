package com.mywork.service;


import java.util.List;
import java.util.Map;

import com.mywork.bean.Questionscore;


public interface QuestionscoreService {

	/**
	 * 查询列表
	 * @param map
	 * @return
	 */
	public List<Questionscore> getList(Map<String, Object> map);
	public List<Questionscore> getAvgList(Map<String, Object> map);
	
	/**
	 * 根据ID查询
	 * @param username
	 * @param password
	 * @return
	 */
	public Questionscore getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public boolean insert(Questionscore Questionscore);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public boolean update(Questionscore Questionscore);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public boolean delete(String id);
	public boolean deleteBySurvey(Map<String, Object> map);
	
}
