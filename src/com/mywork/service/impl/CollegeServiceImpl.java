package com.mywork.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.College;
import com.mywork.mapper.CollegeMapper;
import com.mywork.service.CollegeService;

@Service("collegeService")
public class CollegeServiceImpl implements CollegeService{
	
	@Autowired
	private CollegeMapper collegeMapper;

	/**
	 * 根据ID查找
	 * @param id
	 * @return
	 */
	public College getById(String id) {
		return collegeMapper.getById(id);
	}
	/**
	 * 删除
	 */
	public boolean delete(String id) {
		collegeMapper.delete(id);
		return true;
	}
	/**
	 * 增加
	 */
	public boolean insert(College college) {
		collegeMapper.insert(college);
		return true;
		
	}
	/**
	 * 修改
	 */
	public boolean update(College college) {
		collegeMapper.update(college);
		return true;
	}

	public List<College> getList(Map<String, Object> map) {
		return collegeMapper.getList(map);
	}
	
}