package com.mywork.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.Dept;
import com.mywork.mapper.DeptMapper;
import com.mywork.service.DeptService;

@Service("deptService")
public class DeptServiceImpl implements DeptService{
	
	@Autowired
	private DeptMapper deptMapper;

	/**
	 * 根据ID查找
	 * @param id
	 * @return
	 */
	public Dept getById(String id) {
		return deptMapper.getById(id);
	}
	/**
	 * 删除
	 */
	public boolean delete(String id) {
		deptMapper.delete(id);
		return true;
	}
	/**
	 * 增加
	 */
	public boolean insert(Dept dept) {
		deptMapper.insert(dept);
		return true;
		
	}
	/**
	 * 修改
	 */
	public boolean update(Dept dept) {
		deptMapper.update(dept);
		return true;
	}

	public List<Dept> getList(Map<String, Object> map) {
		return deptMapper.getList(map);
	}
	
}