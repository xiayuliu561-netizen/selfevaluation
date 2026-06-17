package com.mywork.service;


import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mywork.bean.Score;

/**
 * 用户
 * @author gaozq
 *
 */
@Transactional(rollbackFor = { Exception.class })
public interface ScoreService {
	
	
	public List<Score> getList(Map<String, Object> map);
	public List<Score> getDictinctList(Map<String, Object> map);
	
	/**
	 * 根据ID查询用户
	 * @param Scorename
	 * @param password
	 * @return
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
	public Score getScoreById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean insert(Score Score);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean update(Score Score);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean delete(String id);
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean deleteByLesson(Map<String, Object> map);
	
}
