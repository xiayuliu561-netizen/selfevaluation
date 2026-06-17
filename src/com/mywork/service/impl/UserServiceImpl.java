package com.mywork.service.impl;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.User;
import com.mywork.mapper.UserMapper;
import com.mywork.service.UserService;

@Service("loginService")
public class UserServiceImpl implements UserService{
	
	@Autowired
	private UserMapper userMapper;

	/**
	 * 登录验证
	 * @param username
	 * @param password
	 * @return
	 */
	public User getUserByNameAndPassword(User user){
		return userMapper.getUserByNameAndPassword(user);
	}
	/**
	 * 根据ID查找用户
	 * @param id
	 * @return
	 */
	public User getUserById(String id) {
		return userMapper.getById(id);
	}
	/**
	 * 删除用户
	 */
	public boolean delete(String id) {
		userMapper.delete(id);
		return true;
	}
	/**
	 * 增加用户
	 */
	public boolean insert(User user) {
		userMapper.insert(user);
		return true;
	}
	/**
	 * 修改用户
	 */
	public boolean update(User user) {
		userMapper.update(user);
		return true;
	}
	
	public List<User> getList(Map<String, Object> map) {
		return userMapper.getList(map);
	}

	
	
}

