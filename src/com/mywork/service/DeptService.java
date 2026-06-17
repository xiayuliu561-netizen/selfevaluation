package com.mywork.service;


import java.util.List;
import java.util.Map;

import com.mywork.bean.Dept;


public interface DeptService {

	/**
	 * 查询列表
	 * @param map
	 * @return
	 */
	public List<Dept> getList(Map<String, Object> map);
	
	/**
	 * 根据ID查询
	 * @param username
	 * @param password
	 * @return
	 */
	public Dept getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public boolean insert(Dept dept);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public boolean update(Dept dept);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public boolean delete(String id);
	
}
