package com.mywork.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mywork.bean.AnalysisComponent;

@Transactional(rollbackFor = { Exception.class })
public interface AnalysisComponentService {
	public List<AnalysisComponent> getList(Map<String, Object> map);
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean insert(AnalysisComponent analysisComponent);
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean deleteByLesson(Map<String, Object> map);
}
