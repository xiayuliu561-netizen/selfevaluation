package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.News;

/**
 * yonghu 
 * @author 
 *
 */
public interface NewsMapper extends SqlMapper{

	
	public List<News> getList(Map<String, Object> map);
	
	
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public News getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(News News);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(News News);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
}
