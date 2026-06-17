package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.AnalysisTargetItem;

public interface AnalysisTargetItemMapper extends SqlMapper{
	public List<AnalysisTargetItem> getList(Map<String, Object> map);
	public void insert(AnalysisTargetItem analysisTargetItem);
	public void deleteByLesson(Map<String, Object> map);
}
