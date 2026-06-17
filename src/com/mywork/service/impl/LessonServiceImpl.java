package com.mywork.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;

import com.mywork.bean.Lesson;
import com.mywork.service.LessonService;

@Service("LessonService")
public class LessonServiceImpl implements LessonService{
	
	@Autowired
	private SqlSessionTemplate sqlSession;

	/**
	 * 根据ID查找
	 * @param id
	 * @return
	 */
	public Lesson getById(String id) {
		return (Lesson) sqlSession.selectOne("com.mywork.mapper.LessonMapper.getById", id);
	}
	/**
	 * 删除
	 */
	public boolean delete(String id) {
		sqlSession.delete("com.mywork.mapper.LessonMapper.delete", id);
		return true;
	}
	/**
	 * 增加
	 */
	public boolean insert(Lesson Lesson) {
		sqlSession.insert("com.mywork.mapper.LessonMapper.insert", Lesson);
		return true;
		
	}
	/**
	 * 修改
	 */
	public boolean update(Lesson Lesson) {
		sqlSession.update("com.mywork.mapper.LessonMapper.update", Lesson);
		return true;
	}

	@SuppressWarnings("unchecked")
	public List<Lesson> getList(Map<String, Object> map) {
		return (List<Lesson>) (List<?>) sqlSession.selectList("com.mywork.mapper.LessonMapper.getList", map);
	}
	
}
