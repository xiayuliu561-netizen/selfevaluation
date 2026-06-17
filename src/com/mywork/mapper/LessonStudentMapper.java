package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.LessonStudent;

public interface LessonStudentMapper extends SqlMapper{
	public List<LessonStudent> getList(Map<String, Object> map);
	public void insert(LessonStudent lessonStudent);
	public void deleteByLesson(Map<String, Object> map);
}
