package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.Score;

/**
 * yonghu 
 * @author 
 *
 */
public interface ScoreMapper extends SqlMapper{

	
	public List<Score> getList(Map<String, Object> map);
	public List<Score> getDictinctList(Map<String, Object> map);
	
	/**
	 * 根据Id查找
	 * @param org
	 * @return
	 */
	public Score getById(String id);
	/**
	 * add 
	 * @param org
	 * @return
	 */
	public void insert(Score Score);
	/**
	 * 修改
	 * @param org
	 * @return
	 */
	public void update(Score Score);
	/**
	 * 删除
	 * @param dwbm
	 * @return
	 */
	public void delete(String id);
	public void deleteByLesson(Map<String, Object> map);
}
