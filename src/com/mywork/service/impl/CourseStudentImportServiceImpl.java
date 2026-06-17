package com.mywork.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.mywork.bean.College;
import com.mywork.bean.CourseStudentImportItem;
import com.mywork.bean.CourseStudentImportResult;
import com.mywork.bean.Dept;
import com.mywork.bean.Lesson;
import com.mywork.bean.LessonStudent;
import com.mywork.bean.User;
import com.mywork.service.CollegeService;
import com.mywork.service.CourseStudentImportService;
import com.mywork.service.DeptService;
import com.mywork.service.LessonService;
import com.mywork.service.LessonStudentService;
import com.mywork.service.UserService;
import com.mywork.util.CommonUtil;
import com.mywork.util.DateUtil;

@Service("CourseStudentImportService")
public class CourseStudentImportServiceImpl implements CourseStudentImportService{
	@Inject
	private LessonService lessonService;
	@Inject
	private CollegeService collegeService;
	@Inject
	private DeptService deptService;
	@Inject
	private UserService userService;
	@Inject
	private LessonStudentService lessonStudentService;

	public CourseStudentImportResult importStudents(String lessonid, Integer teacherid, List<CourseStudentImportItem> items) throws Exception {
		if(isBlank(lessonid)){
			throw new Exception("请选择课程");
		}
		if(teacherid == null){
			throw new Exception("登录已过期，请重新登录");
		}
		Lesson lesson = lessonService.getById(lessonid);
		if(lesson == null || lesson.getUserid() == null || !lesson.getUserid().equals(teacherid)){
			throw new Exception("课程不存在或无权操作");
		}
		if(items == null || items.size() == 0){
			throw new Exception("没有可导入的学生数据");
		}
		CourseStudentImportResult result = new CourseStudentImportResult();
		LinkedHashSet<Integer> studentIds = new LinkedHashSet<Integer>();
		for(CourseStudentImportItem item : items){
			if(item == null || item.getValid() == null || !item.getValid().booleanValue()){
				result.setSkippedRows(result.getSkippedRows() + 1);
				continue;
			}
			validateItem(item);
			College college = getOrCreateCollege(item.getCollegeName(), result);
			Dept dept = getOrCreateDept(college, item.getDeptName(), result);
			User student = getStudentByNo(item.getNo());
			if(student == null){
				ensureUsernameAvailable(item.getNo(), null);
				student = new User();
				student.setNo(item.getNo());
				student.setUsername(item.getNo());
				student.setPassword(CommonUtil.md5(item.getNo(), "123456"));
				student.setIsadmin("2");
				applyStudent(student, item, college, dept);
				userService.insert(student);
				student = getStudentByNo(item.getNo());
				result.setCreatedStudents(result.getCreatedStudents() + 1);
			}else{
				if(!"2".equals(student.getIsadmin())){
					throw new Exception("学号已被非学生用户占用：" + item.getNo());
				}
				if(isBlank(student.getUsername())){
					ensureUsernameAvailable(item.getNo(), student.getId());
					student.setUsername(item.getNo());
				}
				applyStudent(student, item, college, dept);
				userService.update(student);
				result.setUpdatedStudents(result.getUpdatedStudents() + 1);
			}
			if(student == null || student.getId() == null){
				throw new Exception("学生保存失败：" + item.getNo());
			}
			if(item.getExistingStudent() != null && item.getExistingStudent().booleanValue()){
				result.setExistingStudents(result.getExistingStudents() + 1);
			}
			studentIds.add(student.getId());
		}
		if(studentIds.size() == 0){
			throw new Exception("没有通过校验的学生数据，未执行导入");
		}
		Map<String,Object> deleteQuery = new HashMap<String,Object>();
		deleteQuery.put("lessonid", lessonid);
		deleteQuery.put("teacherid", teacherid);
		lessonStudentService.deleteByLesson(deleteQuery);
		for(Integer studentId : studentIds){
			LessonStudent lessonStudent = new LessonStudent();
			lessonStudent.setLessonid(Integer.valueOf(lessonid));
			lessonStudent.setTeacherid(teacherid);
			lessonStudent.setUserid(studentId);
			lessonStudent.setCreatetime(DateUtil.formatHMS(new Date()));
			lessonStudentService.insert(lessonStudent);
		}
		result.setBoundStudents(studentIds.size());
		return result;
	}

	private void validateItem(CourseStudentImportItem item) throws Exception{
		if(isBlank(item.getName())){
			throw new Exception(rowPrefix(item) + "姓名不能为空");
		}
		if(isBlank(item.getNo())){
			throw new Exception(rowPrefix(item) + "学号不能为空");
		}
		if(isBlank(item.getCollegeName())){
			throw new Exception(rowPrefix(item) + "学院不能为空");
		}
		if(isBlank(item.getDeptName())){
			throw new Exception(rowPrefix(item) + "班级不能为空");
		}
		if(item.getNo().length() > 50 || item.getName().length() > 100 || item.getCollegeName().length() > 120 || item.getDeptName().length() > 120){
			throw new Exception(rowPrefix(item) + "字段长度过长");
		}
	}

	private String rowPrefix(CourseStudentImportItem item){
		return item.getRowNumber() == null ? "" : "第" + item.getRowNumber() + "行：";
	}

	private void applyStudent(User student, CourseStudentImportItem item, College college, Dept dept){
		student.setName(item.getName());
		student.setNo(item.getNo());
		student.setCollegeId(college.getId().toString());
		student.setCollegeName(college.getCollegeName());
		student.setDeptId(dept.getId().toString());
		student.setDeptName(dept.getDeptName());
		student.setIsadmin("2");
		if(isBlank(student.getPassword())){
			student.setPassword(CommonUtil.md5(student.getUsername(), "123456"));
		}
	}

	private College getOrCreateCollege(String collegeName, CourseStudentImportResult result) throws Exception{
		College college = findCollegeByName(collegeName);
		if(college != null){
			return college;
		}
		college = new College();
		college.setCollegeName(collegeName);
		collegeService.insert(college);
		result.setCreatedColleges(result.getCreatedColleges() + 1);
		college = findCollegeByName(collegeName);
		if(college == null){
			throw new Exception("学院创建失败：" + collegeName);
		}
		return college;
	}

	private Dept getOrCreateDept(College college, String deptName, CourseStudentImportResult result) throws Exception{
		Dept dept = findDeptByName(college, deptName);
		if(dept != null){
			return dept;
		}
		dept = new Dept();
		dept.setCollegeId(college.getId().toString());
		dept.setCollegeName(college.getCollegeName());
		dept.setDeptName(deptName);
		deptService.insert(dept);
		result.setCreatedDepts(result.getCreatedDepts() + 1);
		dept = findDeptByName(college, deptName);
		if(dept == null){
			throw new Exception("班级创建失败：" + deptName);
		}
		return dept;
	}

	private College findCollegeByName(String collegeName){
		Map<String,Object> map = new HashMap<String,Object>();
		List<College> list = collegeService.getList(map);
		for(College college : list){
			if(collegeName.equals(clean(college.getCollegeName()))){
				return college;
			}
		}
		return null;
	}

	private Dept findDeptByName(College college, String deptName){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("collegeId", college.getId().toString());
		List<Dept> list = deptService.getList(map);
		for(Dept dept : list){
			if(deptName.equals(clean(dept.getDeptName()))){
				return dept;
			}
		}
		return null;
	}

	private User getStudentByNo(String no){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("no", no);
		List<User> list = userService.getList(map);
		return list.size() == 0 ? null : list.get(0);
	}

	private void ensureUsernameAvailable(String username, Integer selfId) throws Exception{
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("username", username);
		List<User> list = userService.getList(map);
		for(User user : list){
			if(selfId == null || !selfId.equals(user.getId())){
				throw new Exception("用户名已存在，无法使用学号作为登录名：" + username);
			}
		}
	}

	private String clean(String value){
		return value == null ? "" : value.trim();
	}

	private boolean isBlank(String value){
		return value == null || "".equals(value.trim());
	}
}
