package com.mywork.mapper;



import java.util.List;
import java.util.Map;

import com.mywork.bean.College;


public interface CollegeMapper extends SqlMapper{
	public List<College> getList(Map<String, Object> map);
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public College getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(College college);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(College college);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
}
