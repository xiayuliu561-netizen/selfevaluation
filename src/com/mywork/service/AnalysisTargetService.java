package com.mywork.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mywork.bean.AnalysisTarget;

@Transactional(rollbackFor = { Exception.class })
public interface AnalysisTargetService {
	public List<AnalysisTarget> getList(Map<String, Object> map);
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean insert(AnalysisTarget analysisTarget);
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean deleteByLesson(Map<String, Object> map);
}
