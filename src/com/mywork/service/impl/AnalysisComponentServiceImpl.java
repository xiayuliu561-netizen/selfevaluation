package com.mywork.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.AnalysisComponent;
import com.mywork.mapper.AnalysisComponentMapper;
import com.mywork.service.AnalysisComponentService;

@Service("AnalysisComponentService")
public class AnalysisComponentServiceImpl implements AnalysisComponentService{
	@Autowired
	private AnalysisComponentMapper analysisComponentMapper;

	public List<AnalysisComponent> getList(Map<String, Object> map) {
		return analysisComponentMapper.getList(map);
	}

	public boolean insert(AnalysisComponent analysisComponent) {
		analysisComponentMapper.insert(analysisComponent);
		return true;
	}

	public boolean deleteByLesson(Map<String, Object> map) {
		analysisComponentMapper.deleteByLesson(map);
		return true;
	}
}
