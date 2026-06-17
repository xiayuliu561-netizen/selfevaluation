package com.mywork.mapper;



import java.util.List;
import java.util.Map;

import com.mywork.bean.Lesson;


public interface LessonMapper extends SqlMapper{
	public List<Lesson> getList(Map<String, Object> map);
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public Lesson getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(Lesson Lesson);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(Lesson Lesson);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
}
