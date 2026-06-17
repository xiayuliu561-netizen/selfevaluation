package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.AnalysisComponent;

public interface AnalysisComponentMapper extends SqlMapper{
	public List<AnalysisComponent> getList(Map<String, Object> map);
	public void insert(AnalysisComponent analysisComponent);
	public void deleteByLesson(Map<String, Object> map);
}
