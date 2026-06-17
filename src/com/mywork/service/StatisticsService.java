package com.mywork.service;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = { Exception.class })
public interface StatisticsService {
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public long getGradeServiceCount();

	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public void incrementGradeServiceCount(int successCount);
}
