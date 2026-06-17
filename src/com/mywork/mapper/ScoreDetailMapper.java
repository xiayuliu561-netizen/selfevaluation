package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.ScoreDetail;

public interface ScoreDetailMapper extends SqlMapper{
	public List<ScoreDetail> getList(Map<String, Object> map);
	public void insert(ScoreDetail scoreDetail);
	public void updateScore(ScoreDetail scoreDetail);
	public void deleteByLesson(Map<String, Object> map);
	public void deleteByLessonUser(Map<String, Object> map);
}
