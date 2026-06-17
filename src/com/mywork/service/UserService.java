package com.mywork.service;


import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mywork.bean.User;

/**
 * 用户
 * @author gaozq
 *
 */
@Transactional(rollbackFor = { Exception.class })
public interface UserService {
	/**
	 * 登录验证
	 * @param username
	 * @param password
	 * @return
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
	public User getUserByNameAndPassword(User user);
	public List<User> getList(Map<String, Object> map);
	/**
	 * 根据ID查询用户
	 * @param username
	 * @param password
	 * @return
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
	public User getUserById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean insert(User user);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean update(User user);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean delete(String id);
	
}
