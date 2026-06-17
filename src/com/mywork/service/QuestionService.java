package com.mywork.service;


import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mywork.bean.Question;

/**
 * 用户
 * @author gaozq
 *
 */
@Transactional(rollbackFor = { Exception.class })
public interface QuestionService {
	
	public List<Question> getList(Map<String, Object> map);

	
	/**
	 * 根据ID查询
	 * @param Questionname
	 * @param password
	 * @return
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
	public Question getQuestionById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean insert(Question Question);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean update(Question Question);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean delete(String id);
	
}
