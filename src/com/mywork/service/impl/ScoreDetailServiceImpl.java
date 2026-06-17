package com.mywork.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.ScoreDetail;
import com.mywork.mapper.ScoreDetailMapper;
import com.mywork.service.ScoreDetailService;

@Service("ScoreDetailService")
public class ScoreDetailServiceImpl implements ScoreDetailService{
	@Autowired
	private ScoreDetailMapper scoreDetailMapper;

	public List<ScoreDetail> getList(Map<String, Object> map) {
		return scoreDetailMapper.getList(map);
	}

	public boolean insert(ScoreDetail scoreDetail) {
		scoreDetailMapper.insert(scoreDetail);
		return true;
	}

	public boolean updateScore(ScoreDetail scoreDetail) {
		scoreDetailMapper.updateScore(scoreDetail);
		return true;
	}

	public boolean deleteByLesson(Map<String, Object> map) {
		scoreDetailMapper.deleteByLesson(map);
		return true;
	}

	public boolean deleteByLessonUser(Map<String, Object> map) {
		scoreDetailMapper.deleteByLessonUser(map);
		return true;
	}
}
