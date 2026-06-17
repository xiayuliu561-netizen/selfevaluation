package com.mywork.mapper;

import java.util.Map;

import com.mywork.bean.SystemStatistics;

public interface SystemStatisticsMapper extends SqlMapper{
	public void insertGradeServiceCountIfMissing();
	public Long getGradeServiceCount();
	public void incrementGradeServiceCount(Map<String, Object> map);
	public SystemStatistics getByKey(String statKey);
}
