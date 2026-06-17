package com.mywork.service;


import java.util.List;
import java.util.Map;

import com.mywork.bean.Reports;


public interface ReportsService {

	/**
	 * 查询列表
	 * @param map
	 * @return
	 */
	public List<Reports> getList(Map<String, Object> map);
	
	/**
	 * 根据ID查询
	 * @param username
	 * @param password
	 * @return
	 */
	public Reports getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public boolean insert(Reports Reports);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public boolean update(Reports Reports);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public boolean delete(String id);
	
}
