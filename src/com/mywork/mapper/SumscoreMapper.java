package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.Sumscore;

/**
 * yonghu 
 * @author 
 *
 */
public interface SumscoreMapper extends SqlMapper{

	
	public List<Sumscore> getList(Map<String, Object> map);
	
	
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public Sumscore getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(Sumscore Sumscore);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(Sumscore Sumscore);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
	public void deleteByLesson(Map<String, Object> map);
}
