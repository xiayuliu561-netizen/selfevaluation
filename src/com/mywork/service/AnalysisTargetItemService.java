package com.mywork.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mywork.bean.AnalysisTargetItem;

@Transactional(rollbackFor = { Exception.class })
public interface AnalysisTargetItemService {
	public List<AnalysisTargetItem> getList(Map<String, Object> map);
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean insert(AnalysisTargetItem analysisTargetItem);
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public boolean deleteByLesson(Map<String, Object> map);
}
