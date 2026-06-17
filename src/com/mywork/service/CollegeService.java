package com.mywork.service;


import java.util.List;
import java.util.Map;

import com.mywork.bean.College;


public interface CollegeService {

	/**
	 * 查询列表
	 * @param map
	 * @return
	 */
	public List<College> getList(Map<String, Object> map);
	
	/**
	 * 根据ID查询
	 * @param username
	 * @param password
	 * @return
	 */
	public College getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public boolean insert(College college);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public boolean update(College college);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public boolean delete(String id);
	
}
