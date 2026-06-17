package com.mywork.mapper;



import java.util.List;
import java.util.Map;

import com.mywork.bean.Questionscore;


public interface QuestionscoreMapper extends SqlMapper{
	public List<Questionscore> getList(Map<String, Object> map);
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public Questionscore getById(String id);
	public List<Questionscore> getAvgList(Map<String, Object> map);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(Questionscore Questionscore);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(Questionscore Questionscore);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
	public void deleteBySurvey(Map<String, Object> map);
}
