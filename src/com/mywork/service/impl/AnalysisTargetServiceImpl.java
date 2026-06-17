package com.mywork.service.impl;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.mywork.bean.AnalysisTarget;
import com.mywork.service.AnalysisTargetService;

@Service("AnalysisTargetService")
public class AnalysisTargetServiceImpl implements AnalysisTargetService{
	@Autowired
	private SqlSessionTemplate sqlSession;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private boolean targetContentColumnChecked = false;

	@SuppressWarnings("unchecked")
	public List<AnalysisTarget> getList(Map<String, Object> map) {
		ensureTargetContentColumn();
		return (List<AnalysisTarget>) (List<?>) sqlSession.selectList("com.mywork.mapper.AnalysisTargetMapper.getList", map);
	}

	public boolean insert(AnalysisTarget analysisTarget) {
		ensureTargetContentColumn();
		sqlSession.insert("com.mywork.mapper.AnalysisTargetMapper.insert", analysisTarget);
		return true;
	}

	public boolean deleteByLesson(Map<String, Object> map) {
		ensureTargetContentColumn();
		sqlSession.delete("com.mywork.mapper.AnalysisTargetMapper.deleteByLesson", map);
		return true;
	}

	private synchronized void ensureTargetContentColumn(){
		if(targetContentColumnChecked){
			return;
		}
		try{
			jdbcTemplate.execute("ALTER TABLE analysis_target ADD COLUMN target_content TEXT NULL AFTER target_name");
		}catch(Exception e){
		}
		targetContentColumnChecked = true;
	}
}
