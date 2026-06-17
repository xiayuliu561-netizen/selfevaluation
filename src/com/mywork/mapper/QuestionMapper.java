package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.Question;

/**
 * yonghu 
 * @author 
 *
 */
public interface QuestionMapper extends SqlMapper{
	
	public List<Question> getList(Map<String, Object> map);

	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public Question getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(Question Question);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(Question Question);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
}
