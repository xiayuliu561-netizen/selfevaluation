package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.Rate;

/**
 * yonghu 
 * @author 
 *
 */
public interface RateMapper extends SqlMapper{

	
	public List<Rate> getList(Map<String, Object> map);
	
	
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public Rate getById(String id);
	
	public Rate getByTeacherId(String teacherid);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(Rate Rate);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(Rate Rate);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
	public void deleteByLesson(Map<String, Object> map);
}
