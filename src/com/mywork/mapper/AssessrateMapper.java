package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.Assessrate;

/**
 * yonghu 
 * @author 
 *
 */
public interface AssessrateMapper extends SqlMapper{

	
	public List<Assessrate> getList(Map<String, Object> map);
	
	
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public Assessrate getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(Assessrate Assessrate);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(Assessrate Assessrate);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
	public void deleteByLesson(Map<String, Object> map);
}
