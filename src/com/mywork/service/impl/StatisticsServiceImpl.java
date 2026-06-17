package com.mywork.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.mapper.SystemStatisticsMapper;
import com.mywork.service.StatisticsService;

@Service("StatisticsService")
public class StatisticsServiceImpl implements StatisticsService{
	@Autowired
	private SystemStatisticsMapper systemStatisticsMapper;

	public long getGradeServiceCount() {
		ensureGradeServiceCount();
		Long count = systemStatisticsMapper.getGradeServiceCount();
		return count == null ? 0L : count.longValue();
	}

	public void incrementGradeServiceCount(int successCount) {
		if(successCount <= 0){
			return;
		}
		ensureGradeServiceCount();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("amount", Integer.valueOf(successCount));
		systemStatisticsMapper.incrementGradeServiceCount(map);
	}

	private void ensureGradeServiceCount(){
		systemStatisticsMapper.insertGradeServiceCountIfMissing();
	}
}
