package com.mywork.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.LessonStudent;
import com.mywork.mapper.LessonStudentMapper;
import com.mywork.service.LessonStudentService;

@Service("LessonStudentService")
public class LessonStudentServiceImpl implements LessonStudentService{
	@Autowired
	private LessonStudentMapper lessonStudentMapper;

	public List<LessonStudent> getList(Map<String, Object> map) {
		return lessonStudentMapper.getList(map);
	}

	public boolean insert(LessonStudent lessonStudent) {
		lessonStudentMapper.insert(lessonStudent);
		return true;
	}

	public boolean deleteByLesson(Map<String, Object> map) {
		lessonStudentMapper.deleteByLesson(map);
		return true;
	}
}
