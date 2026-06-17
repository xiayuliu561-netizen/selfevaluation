package com.mywork.service;


import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mywork.bean.Sumscore;

/**
 * 用户
 * @author gaozq
 *
 */
@Transactional(rollbackFor = { Exception.class })
public interface SumscoreService {
	
	
	public List<Sumscore> getList(Map<String, Object> map);
	
	
	/**
	 * 根据ID查询用户
	 * @param Sumscorename
	 * @param password
	 * @return
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
	public Sumscore getSumscoreById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean insert(Sumscore Sumscore);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean update(Sumscore Sumscore);
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
