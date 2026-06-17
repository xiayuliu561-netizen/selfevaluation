package com.mywork.service;

import java.util.List;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mywork.bean.CourseStudentImportItem;
import com.mywork.bean.CourseStudentImportResult;

@Transactional(rollbackFor = { Exception.class })
public interface CourseStudentImportService {
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
	public CourseStudentImportResult importStudents(String lessonid, Integer teacherid, List<CourseStudentImportItem> items) throws Exception;
}
