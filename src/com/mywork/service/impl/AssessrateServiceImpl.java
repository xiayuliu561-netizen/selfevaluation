package com.mywork.service.impl;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.Assessrate;
import com.mywork.mapper.AssessrateMapper;
import com.mywork.service.AssessrateService;

@Service("AssessrateService")
public class AssessrateServiceImpl implements AssessrateService{
	
	@Autowired
	private AssessrateMapper AssessrateMapper;

	/**
	 * 根据ID查找用户
	 * @param id
	 * @return
	 */
	public Assessrate getAssessrateById(String id) {
		return AssessrateMapper.getById(id);
	}
	/**
	 * 删除用户
	 */
	public boolean delete(String id) {
		AssessrateMapper.delete(id);
		return true;
	}
	public boolean deleteByLesson(Map<String, Object> map) {
		AssessrateMapper.deleteByLesson(map);
		return true;
	}
	/**
	 * 增加用户
	 */
	public boolean insert(Assessrate Assessrate) {
		AssessrateMapper.insert(Assessrate);
		return true;
	}
	/**
	 * 修改用户
	 */
	public boolean update(Assessrate Assessrate) {
		AssessrateMapper.update(Assessrate);
		return true;
	}
	public List<Assessrate> getList(Map<String, Object> map) {
		return AssessrateMapper.getList(map);
	}
	
	
}

