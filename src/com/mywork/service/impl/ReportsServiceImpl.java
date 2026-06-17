package com.mywork.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.Reports;
import com.mywork.mapper.ReportsMapper;
import com.mywork.service.ReportsService;

@Service("ReportsService")
public class ReportsServiceImpl implements ReportsService{
	
	@Autowired
	private ReportsMapper ReportsMapper;

	/**
	 * 根据ID查找
	 * @param id
	 * @return
	 */
	public Reports getById(String id) {
		return ReportsMapper.getById(id);
	}
	/**
	 * 删除
	 */
	public boolean delete(String id) {
		ReportsMapper.delete(id);
		return true;
	}
	/**
	 * 增加
	 */
	public boolean insert(Reports Reports) {
		ReportsMapper.insert(Reports);
		return true;
		
	}
	/**
	 * 修改
	 */
	public boolean update(Reports Reports) {
		ReportsMapper.update(Reports);
		return true;
	}

	public List<Reports> getList(Map<String, Object> map) {
		return ReportsMapper.getList(map);
	}
	
}