package com.mywork.service.impl;


import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.News;
import com.mywork.mapper.NewsMapper;
import com.mywork.service.NewsService;

@Service("newsService")
public class NewsServiceImpl implements NewsService{
	
	@Autowired
	private NewsMapper NewsMapper;

	/**
	 * 根据ID查找用户
	 * @param id
	 * @return
	 */
	public News getNewsById(String id) {
		return NewsMapper.getById(id);
	}
	/**
	 * 删除用户
	 */
	public boolean delete(String id) {
		NewsMapper.delete(id);
		return true;
	}
	/**
	 * 增加用户
	 */
	public boolean insert(News News) {
		NewsMapper.insert(News);
		return true;
	}
	/**
	 * 修改用户
	 */
	public boolean update(News News) {
		NewsMapper.update(News);
		return true;
	}
	public List<News> getList(Map<String, Object> map) {
		return NewsMapper.getList(map);
	}
	
	
}

