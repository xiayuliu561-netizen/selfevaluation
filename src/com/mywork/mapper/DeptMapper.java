package com.mywork.mapper;



import java.util.List;
import java.util.Map;

import com.mywork.bean.Dept;


public interface DeptMapper extends SqlMapper{
	public List<Dept> getList(Map<String, Object> map);
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public Dept getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(Dept dept);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(Dept dept);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
}
