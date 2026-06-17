package com.mywork.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.AnalysisTargetItem;
import com.mywork.mapper.AnalysisTargetItemMapper;
import com.mywork.service.AnalysisTargetItemService;

@Service("AnalysisTargetItemService")
public class AnalysisTargetItemServiceImpl implements AnalysisTargetItemService{
	@Autowired
	private AnalysisTargetItemMapper analysisTargetItemMapper;

	public List<AnalysisTargetItem> getList(Map<String, Object> map) {
		return analysisTargetItemMapper.getList(map);
	}

	public boolean insert(AnalysisTargetItem analysisTargetItem) {
		analysisTargetItemMapper.insert(analysisTargetItem);
		return true;
	}

	public boolean deleteByLesson(Map<String, Object> map) {
		analysisTargetItemMapper.deleteByLesson(map);
		return true;
	}
}
