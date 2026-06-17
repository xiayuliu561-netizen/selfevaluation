package com.mywork.service.impl;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.Score;
import com.mywork.mapper.ScoreMapper;
import com.mywork.service.ScoreService;

@Service("ScoreService")
public class ScoreServiceImpl implements ScoreService{
	
	@Autowired
	private ScoreMapper ScoreMapper;

	/**
	 * 根据ID查找用户
	 * @param id
	 * @return
	 */
	public Score getScoreById(String id) {
		return ScoreMapper.getById(id);
	}
	/**
	 * 删除用户
	 */
	public boolean delete(String id) {
		ScoreMapper.delete(id);
		return true;
	}
	public boolean deleteByLesson(Map<String, Object> map) {
		ScoreMapper.deleteByLesson(map);
		return true;
	}
	/**
	 * 增加用户
	 */
	public boolean insert(Score Score) {
		ScoreMapper.insert(Score);
		return true;
	}
	/**
	 * 修改用户
	 */
	public boolean update(Score Score) {
		ScoreMapper.update(Score);
		return true;
	}
	public List<Score> getList(Map<String, Object> map) {
		return ScoreMapper.getList(map);
	}
	public List<Score> getDictinctList(Map<String, Object> map) {
		return ScoreMapper.getDictinctList(map);
	}
	
	
}

