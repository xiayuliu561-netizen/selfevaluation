package com.mywork.service.impl;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.Sumscore;
import com.mywork.mapper.SumscoreMapper;
import com.mywork.service.SumscoreService;

@Service("SumscoreService")
public class SumscoreServiceImpl implements SumscoreService{
	
	@Autowired
	private SumscoreMapper SumscoreMapper;

	/**
	 * 根据ID查找用户
	 * @param id
	 * @return
	 */
	public Sumscore getSumscoreById(String id) {
		return SumscoreMapper.getById(id);
	}
	/**
	 * 删除用户
	 */
	public boolean delete(String id) {
		SumscoreMapper.delete(id);
		return true;
	}
	public boolean deleteByLesson(Map<String, Object> map) {
		SumscoreMapper.deleteByLesson(map);
		return true;
	}
	/**
	 * 增加用户
	 */
	public boolean insert(Sumscore Sumscore) {
		SumscoreMapper.insert(Sumscore);
		return true;
	}
	/**
	 * 修改用户
	 */
	public boolean update(Sumscore Sumscore) {
		SumscoreMapper.update(Sumscore);
		return true;
	}
	public List<Sumscore> getList(Map<String, Object> map) {
		return SumscoreMapper.getList(map);
	}
	
	
}

