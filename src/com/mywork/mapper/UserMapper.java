package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.User;

/**
 * yonghu 
 * @author 
 *
 */
public interface UserMapper extends SqlMapper{
	
	
	public List<User> getList(Map<String, Object> map);
	
	/**
	 * 用户登录时验证用户名密码
	 * @param username
	 * @param password
	 * @return
	 */
	public User getUserByNameAndPassword(User user);
	
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public User getById(String id);
	/**
	 * add 用户
	 * @param org
	 * @return
	 */
	public void insert(User user);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(User user);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
}
