package com.mywork.service;


import java.util.List;
import java.util.Map;

import com.mywork.bean.Lesson;


public interface LessonService {

	/**
	 * 查询列表
	 * @param map
	 * @return
	 */
	public List<Lesson> getList(Map<String, Object> map);
	
	/**
	 * 根据ID查询
	 * @param username
	 * @param password
	 * @return
	 */
	public Lesson getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public boolean insert(Lesson Lesson);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public boolean update(Lesson Lesson);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public boolean delete(String id);
	
}
