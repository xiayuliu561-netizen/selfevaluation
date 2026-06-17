package com.mywork.mapper;



import java.util.List;
import java.util.Map;

import com.mywork.bean.Reports;


public interface ReportsMapper extends SqlMapper{
	public List<Reports> getList(Map<String, Object> map);
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public Reports getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(Reports Reports);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(Reports Reports);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
}
