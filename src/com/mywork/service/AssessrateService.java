package com.mywork.service;


import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mywork.bean.Assessrate;

/**
 * 用户
 * @author gaozq
 *
 */
@Transactional(rollbackFor = { Exception.class })
public interface AssessrateService {
	
	
	public List<Assessrate> getList(Map<String, Object> map);
	
	
	/**
	 * 根据ID查询用户
	 * @param Assessratename
	 * @param password
	 * @return
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
	public Assessrate getAssessrateById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean insert(Assessrate Assessrate);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean update(Assessrate Assessrate);
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
