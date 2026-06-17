package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.AnalysisTarget;

public interface AnalysisTargetMapper extends SqlMapper{
	public List<AnalysisTarget> getList(Map<String, Object> map);
	public void insert(AnalysisTarget analysisTarget);
	public void deleteByLesson(Map<String, Object> map);
}
