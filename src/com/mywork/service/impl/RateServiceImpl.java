package com.mywork.service.impl;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.Rate;
import com.mywork.mapper.RateMapper;
import com.mywork.service.RateService;

@Service("RateService")
public class RateServiceImpl implements RateService{
	
	@Autowired
	private RateMapper RateMapper;

	/**
	 * 根据ID查找用户
	 * @param id
	 * @return
	 */
	public Rate getRateById(String id) {
		return RateMapper.getById(id);
	}
	/**
	 * 删除用户
	 */
	public boolean delete(String id) {
		RateMapper.delete(id);
		return true;
	}
	public boolean deleteByLesson(Map<String, Object> map) {
		RateMapper.deleteByLesson(map);
		return true;
	}
	/**
	 * 增加用户
	 */
	public boolean insert(Rate Rate) {
		RateMapper.insert(Rate);
		return true;
	}
	/**
	 * 修改用户
	 */
	public boolean update(Rate Rate) {
		RateMapper.update(Rate);
		return true;
	}
	public List<Rate> getList(Map<String, Object> map) {
		return RateMapper.getList(map);
	}
	public Rate getByTeacherId(String teacherid) {
		// TODO Auto-generated method stub
		return RateMapper.getByTeacherId(teacherid);
	}
	
	
}

