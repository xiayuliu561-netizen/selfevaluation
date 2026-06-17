package com.mywork.controller;


import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.mywork.bean.College;
import com.mywork.bean.Dept;
import com.mywork.bean.User;
import com.mywork.common.SessionKeys;
import com.mywork.service.CollegeService;
import com.mywork.service.DeptService;
import com.mywork.service.UserService;
import com.mywork.util.CommonUtil;
import com.mywork.util.ExcelUtil;
import com.mywork.util.ImageUtil;
import com.mywork.util.SysModel;
/**
 * 用户
 * @author 
 *
 */
@Controller
@RequestMapping(value="user")
public class UserController extends BaseController{
	private static final String LOGIN_ERROR_MESSAGE = "loginErrorMessage";
	private static final String ROLE_ADMIN = "0";
	private static final String ROLE_TEACHER = "1";

	@Inject
	private UserService userService;
	@Inject
	private CollegeService collegeService;
	@Inject
	private DeptService deptService;

	private boolean isBlank(String value){
		return value == null || "".equals(value.trim());
	}

	private String cellValue(ArrayList<Object> linevalue, int index){
		if(linevalue == null || linevalue.size() <= index || linevalue.get(index) == null){
			return "";
		}
		return (linevalue.get(index) + "").trim();
	}

	private boolean isRowBlank(ArrayList<Object> linevalue){
		if(linevalue == null){
			return true;
		}
		for(Object value : linevalue){
			if(value != null && !"".equals((value + "").trim())){
				return false;
			}
		}
		return true;
	}

	private College findImportCollege(List<College> collegeList, String collegeName){
		for(College college : collegeList){
			if(!isBlank(collegeName) && collegeName.equals(college.getCollegeName())){
				return college;
			}
		}
		return null;
	}

	private Dept findImportDept(List<Dept> deptList, College college, String deptName){
		if(college == null){
			return null;
		}
		String collegeId = college.getId() == null ? "" : college.getId().toString();
		for(Dept dept : deptList){
			if(!collegeId.equals(dept.getCollegeId())){
				continue;
			}
			if(!isBlank(deptName) && deptName.equals(dept.getDeptName())){
				return dept;
			}
		}
		return null;
	}

	private User getLoginUser(HttpServletRequest request){
		return (User)request.getSession().getAttribute(SessionKeys.LOGIN_USER);
	}

	private boolean requireAdmin(HttpServletRequest request, HttpServletResponse response){
		return requireAdminRole(request, response);
	}

	private String validateImportHeader(ArrayList<Object> header){
		String[] expectedHeaders = {"学院", "班级", "学号", "姓名", "用户名", "邮箱", "性别", "电话"};
		if(header == null || header.size() < expectedHeaders.length){
			return "ERROR:学生导入模板列不正确，请下载并使用最新模板。";
		}
		for(int i=0; i<expectedHeaders.length; i++){
			if(!expectedHeaders[i].equals(cellValue(header, i))){
				return "ERROR:学生导入模板列不正确，第" + (i + 1) + "列应为“" + expectedHeaders[i] + "”，请下载并使用最新模板。";
			}
		}
		return null;
	}

	private String validateTeacherImportHeader(ArrayList<Object> header){
		String[] expectedHeaders = {"学院", "工号", "姓名", "用户名", "密码", "邮箱", "性别", "电话"};
		if(header == null || header.size() < expectedHeaders.length){
			return "ERROR:教师导入模板列不正确，请下载并使用最新模板。";
		}
		for(int i=0; i<expectedHeaders.length; i++){
			if(!expectedHeaders[i].equals(cellValue(header, i))){
				return "ERROR:教师导入模板列不正确，第" + (i + 1) + "列应为“" + expectedHeaders[i] + "”，请下载并使用最新模板。";
			}
		}
		return null;
	}

	private String importErrorMessage(String message){
		if(message == null || "".equals(message.trim())){
			return "ERROR:导入失败";
		}
		if(message.startsWith("ERROR:")){
			return message;
		}
		return "ERROR:导入失败，" + message;
	}

	private String joinMessages(List<String> messages){
		StringBuilder result = new StringBuilder();
		for(int i=0; i<messages.size(); i++){
			if(i > 0){
				result.append("；");
			}
			result.append(messages.get(i));
		}
		return result.toString();
	}

	private void addImportError(List<String> errors, int rowNumber, String message){
		if(errors.size() < 20){
			errors.add("第" + rowNumber + "行：" + message);
		}
	}

	private String buildImportErrorMessage(List<String> errors, int totalErrorCount){
		StringBuilder message = new StringBuilder("ERROR:上传失败，发现" + totalErrorCount + "处数据问题：");
		for(String error : errors){
			message.append("<br/>").append(error);
		}
		if(totalErrorCount > errors.size()){
			message.append("<br/>还有").append(totalErrorCount - errors.size()).append("处问题未展示，请修正后重新上传。");
		}
		return message.toString();
	}

	private String validateUserUnique(User user){
		Map<String,Object> map = new HashMap<String,Object>();
		if(user.getId() != null){
			map.put("excludeId", user.getId());
		}
		if(!isBlank(user.getUsername())){
			map.put("username", user.getUsername().trim());
			if(userService.getList(map).size() > 0){
				return "用户名已存在，请修改";
			}
			map.remove("username");
		}
		if(!isBlank(user.getNo())){
			map.put("no", user.getNo().trim());
			if(userService.getList(map).size() > 0){
				if("1".equals(user.getIsadmin())){
					return "工号已存在，请修改";
				}
				if("2".equals(user.getIsadmin())){
					return "学号已存在，请修改";
				}
				return "工号/学号已存在，请修改";
			}
		}
		return null;
	}

	private User getUserByNo(String no){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("no", no);
		List<User> users = userService.getList(map);
		return users.size() == 0 ? null : users.get(0);
	}

	private List<TeacherImportRow> parseTeacherImportRows(ArrayList<ArrayList<Object>> rows) throws Exception{
		List<TeacherImportRow> result = new ArrayList<TeacherImportRow>();
		List<College> collegeList = collegeService.getList(new HashMap<String,Object>());
		Set<String> uploadNos = new HashSet<String>();
		Set<String> uploadUsernames = new HashSet<String>();
		List<String> errors = new ArrayList<String>();
		int totalErrorCount = 0;
		for(int i=1; i<rows.size(); i++){
			ArrayList<Object> row = rows.get(i);
			if(isRowBlank(row)){
				continue;
			}
			int rowNumber = i + 1;
			List<String> rowErrors = new ArrayList<String>();
			String collegeName = cellValue(row, 0);
			String no = cellValue(row, 1);
			String name = cellValue(row, 2);
			String username = cellValue(row, 3);
			String password = cellValue(row, 4);
			String email = cellValue(row, 5);
			String sex = cellValue(row, 6);
			String tel = cellValue(row, 7);

			College college = findImportCollege(collegeList, collegeName);
			if(isBlank(collegeName)){
				rowErrors.add("学院不能为空");
			}else if(college == null){
				rowErrors.add("学院不存在：" + collegeName);
			}
			if(isBlank(no)){
				rowErrors.add("工号不能为空");
			}else if(!uploadNos.add(no)){
				rowErrors.add("工号在本次上传文件中重复：" + no);
			}
			if(isBlank(name)){
				rowErrors.add("姓名不能为空");
			}
			if(isBlank(username)){
				rowErrors.add("用户名不能为空");
			}else if(!uploadUsernames.add(username)){
				rowErrors.add("用户名在本次上传文件中重复：" + username);
			}
			if(isBlank(password)){
				rowErrors.add("密码不能为空");
			}
			if(isBlank(tel)){
				rowErrors.add("电话不能为空");
			}

			if(rowErrors.size() > 0){
				totalErrorCount += rowErrors.size();
				addImportError(errors, rowNumber, joinMessages(rowErrors));
				continue;
			}

			TeacherImportRow item = new TeacherImportRow();
			item.rowNumber = rowNumber;
			item.college = college;
			item.no = no;
			item.name = name;
			item.username = username;
			item.password = password;
			item.email = email;
			item.sex = sex;
			item.tel = tel;
			result.add(item);
		}
		totalErrorCount += validateTeacherRowsUnique(result, errors);
		if(totalErrorCount > 0){
			throw new Exception(buildImportErrorMessage(errors, totalErrorCount));
		}
		return result;
	}

	private int validateTeacherRowsUnique(List<TeacherImportRow> rows, List<String> errors){
		int totalErrorCount = 0;
		for(int i=0; i<rows.size(); i++){
			TeacherImportRow row = rows.get(i);
			User existing = getUserByNo(row.no);
			User user = new User();
			if(existing != null){
				user.setId(existing.getId());
			}
			user.setNo(row.no);
			user.setUsername(row.username);
			user.setIsadmin(ROLE_TEACHER);
			List<String> rowErrors = new ArrayList<String>();
			if(existing != null && !ROLE_TEACHER.equals(existing.getIsadmin())){
				rowErrors.add("工号已被非教师用户占用：" + row.no);
			}
			String uniqueMessage = validateUserUnique(user);
			if(uniqueMessage != null){
				rowErrors.add(uniqueMessage);
			}
			if(rowErrors.size() > 0){
				totalErrorCount += rowErrors.size();
				addImportError(errors, row.rowNumber, joinMessages(rowErrors));
			}
		}
		return totalErrorCount;
	}

	private ImportResult saveTeacherRows(List<TeacherImportRow> rows){
		ImportResult result = new ImportResult();
		for(TeacherImportRow row : rows){
			User teacher = getUserByNo(row.no);
			if(teacher == null){
				teacher = new User();
				teacher.setNo(row.no);
				teacher.setIsadmin(ROLE_TEACHER);
				applyTeacherImportData(teacher, row);
				userService.insert(teacher);
				result.createdTeachers++;
			}else{
				applyTeacherImportData(teacher, row);
				userService.update(teacher);
				result.updatedTeachers++;
			}
		}
		return result;
	}

	private void applyTeacherImportData(User teacher, TeacherImportRow row){
		teacher.setCollegeId(row.college.getId().toString());
		teacher.setCollegeName(row.college.getCollegeName());
		teacher.setDeptId(null);
		teacher.setDeptName(null);
		teacher.setNo(row.no);
		teacher.setName(row.name);
		teacher.setUsername(row.username);
		teacher.setPassword(row.password);
		teacher.setEmail(row.email);
		teacher.setSex(row.sex);
		teacher.setTel(row.tel);
		teacher.setIsadmin(ROLE_TEACHER);
	}
	
	
	/**
	 * 跳转到登录界面
	 * @param request
	 * @return
	 */
	@RequestMapping(value="login")
	public ModelAndView login(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		Object loginErrorMessage = request.getSession().getAttribute(LOGIN_ERROR_MESSAGE);
		if(loginErrorMessage != null){
			map.put("msg", loginErrorMessage.toString());
			request.getSession().removeAttribute(LOGIN_ERROR_MESSAGE);
		}
		map.put("yzm", CommonUtil.getYzm());
		return jsp("login", map, request);
	}
	/**
	 * 登录
	 * @param request
	 * @return
	 */
	@RequestMapping(value="main")
	public ModelAndView main(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if(isBlank(username) && isBlank(password)){
			User sessionUser = getSessionUser(request);
			if(sessionUser != null){
				map.put("user", sessionUser);
				map.put("sessionRole", sessionUser.getIsadmin());
				map.put("leaderid", sessionUser.getId());
				return jsp("main", map, request);
			}
			map.put("yzm", CommonUtil.getYzm());
			return jsp("login", map, request);
		}
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		user = userService.getUserByNameAndPassword(user);
		if(user == null && !isBlank(username) && !isBlank(password)){
			User encryptedUser = new User();
			encryptedUser.setUsername(username);
			encryptedUser.setPassword(CommonUtil.md5(username, password));
			user = userService.getUserByNameAndPassword(encryptedUser);
		}
		if(user==null){
			request.getSession().setAttribute(LOGIN_ERROR_MESSAGE, "用户名或密码错误!");
			return new ModelAndView("redirect:/user/login.html");
		}
		request.getSession().setAttribute(SessionKeys.LOGIN_USER, user);
		request.getSession().setAttribute(SessionKeys.ACTIVE_ROLE, user.getIsadmin());
		request.getSession().setAttribute(SessionKeys.loginUserRoleKey(user.getIsadmin()), user);
		map.put("user", user);
		map.put("sessionRole", user.getIsadmin());
		map.put("leaderid", user.getId());
		return jsp("main", map, request);
	}
	/**
	 * 登出
	 * @param request
	 * @return
	 */
	@RequestMapping(value="logout")
	public ModelAndView logout(HttpServletRequest request){
		String role = request.getParameter("sessionRole");
		if(role == null || "".equals(role.trim())){
			role = request.getParameter("isadmin");
		}
		if(role != null && !"".equals(role.trim())){
			request.getSession().setAttribute(SessionKeys.loginUserRoleKey(role.trim()), null);
			User activeUser = (User)request.getSession().getAttribute(SessionKeys.LOGIN_USER);
			if(activeUser != null && role.trim().equals(activeUser.getIsadmin())){
				request.getSession().setAttribute(SessionKeys.LOGIN_USER, null);
				request.getSession().setAttribute(SessionKeys.ACTIVE_ROLE, null);
			}
		}else{
			request.getSession().setAttribute(SessionKeys.LOGIN_USER, null);
			request.getSession().setAttribute(SessionKeys.ACTIVE_ROLE, null);
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("yzm", CommonUtil.getYzm());
		return jsp("login", map, request);
	}
	/**
	 * 跳转到个人信息页面（密码，个人信息，日志）
	 * @param request
	 * @return
	 */
	@RequestMapping(value="personalinfo")
	public ModelAndView password(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		User user = getSessionUser(request);
		map.put("user", user);
		map.put("userid", user.getId());
		//List<Logs> list = logsService.getList(map);
		map.put("list", null);
		map.put("logscount", 0);
		return jsp("personalinfo", map, request);
	}
	/**
	 * 修改密码
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value="changepassword")
	public void changepsd(HttpServletRequest request, HttpServletResponse response) throws IOException{
		User user = getSessionUser(request);
		String oldPwd = request.getParameter("oldPwd");
		String oldPwdMd5 = CommonUtil.md5(user.getUsername(), oldPwd);
		if(!user.getPassword().equals(oldPwd) && !user.getPassword().equals(oldPwdMd5)){
			ajax(response, "error old");
		}else{
			String newPwd = request.getParameter("newPwd");
			user.setPassword(CommonUtil.md5(user.getUsername(), newPwd));
			userService.update(user);
			ajax(response, "修改密码成功！");
		}
	}
	/**
	 * 教师列表
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="teacherlist")
	public ModelAndView teacherlist(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		if(getSessionUser(request) == null || !ROLE_ADMIN.equals(getSessionUser(request).getIsadmin())){
			return jsp("login", map, request);
		}
		map.put("collegelist", collegeService.getList(map));
		return jsp("user/teacher", map, request);
	}
	/**
	 * 学生列表
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="studentlist")
	public ModelAndView studentlist(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		if(getSessionUser(request) == null || !ROLE_ADMIN.equals(getSessionUser(request).getIsadmin())){
			return jsp("login", map, request);
		}
		map.put("collegelist", collegeService.getList(map));
		return jsp("user/student", map, request);
	}
	/**
	 * 我的学生列表
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="ownstudentlist")
	public ModelAndView ownstudentlist(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		if(getSessionUser(request) == null || !ROLE_TEACHER.equals(getSessionUser(request).getIsadmin())){
			return jsp("login", map, request);
		}
		map.put("collegelist", collegeService.getList(map));
		return jsp("user/ownstudent", map, request);
	}
	/**
	 * 
	 * get list data
	 * @param request
	 * @return
	 */
	@RequestMapping(value="ownlistdata")
	public void ownlistdata(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("name", CommonUtil.changeEncoding(request.getParameter("queryname")));
		map.put("isadmin", request.getParameter("isadmin"));
		map.put("deptId", getSessionUser(request).getDeptId());
		List<User> list = userService.getList(map);
		JSONArray jsonarray = JSONArray.fromObject(list);
		String json = "{\"data\":"+jsonarray.toString()+"}";
		ajax(response, json);
	}
	/**
	 * 
	 * get list data
	 * @param request
	 * @return
	 */
	@RequestMapping(value="listdata")
	public void listdata(HttpServletRequest request, HttpServletResponse response){
		if(!requireAdminRole(request, response)){
			return;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("queryname", CommonUtil.changeEncoding(request.getParameter("queryname")));
		map.put("isadmin", request.getParameter("isadmin"));
		List<User> list = userService.getList(map);
		JSONArray jsonarray = JSONArray.fromObject(list);
		String json = "{\"data\":"+jsonarray.toString()+"}";
		ajax(response, json);
	}
	
	/**
	 * 添加
	 * @param request
	 * @return
	 */
	@RequestMapping(value="add")
	public void add(HttpServletRequest request, HttpServletResponse response, User user){
		if(!requireAdminRole(request, response)){
			return;
		}
		if("2".equals(user.getIsadmin())){
			ajax(response, "学生名单请在课程管理中由任课教师按课程导入或维护");
			return;
		}
		String validateMessage = validateUserUnique(user);
		if(validateMessage != null){
			ajax(response, validateMessage);
			return;
		}
		if(user.getCollegeId() != null){
			user.setCollegeName(collegeService.getById(user.getCollegeId()).getCollegeName());
		}
		if(user.getDeptId() != null){
			user.setDeptName(deptService.getById(user.getDeptId()).getDeptName());
		}
		userService.insert(user);
		ajax(response, "新增成功");
	}
	/**
	 * 修改
	 * @param request
	 * @return
	 */
	@RequestMapping(value="update")
	public void update(HttpServletRequest request, HttpServletResponse response,User user ){
		if(!requireAdminRole(request, response)){
			return;
		}
		User oldUser = user.getId() == null ? null : userService.getUserById(user.getId().toString());
		if(oldUser == null){
			ajax(response, "用户不存在");
			return;
		}
		user.setIsadmin(oldUser.getIsadmin());
		if("2".equals(oldUser.getIsadmin())){
			user.setPassword(oldUser.getPassword());
		}else if(isBlank(user.getPassword())){
			user.setPassword(oldUser.getPassword());
		}
		String validateMessage = validateUserUnique(user);
		if(validateMessage != null){
			ajax(response, validateMessage);
			return;
		}
		if(user.getCollegeId() != null){
			user.setCollegeName(collegeService.getById(user.getCollegeId()).getCollegeName());
		}
		if(user.getDeptId() != null){
			user.setDeptName(deptService.getById(user.getDeptId()).getDeptName());
		}
//		user.setName(request.getParameter("name"));
//		user.setPassword(request.getParameter("password"));
//		user.setEmail(request.getParameter("email"));
//		user.setSex(request.getParameter("sex"));
//		user.setTel(request.getParameter("tel"));
//		user.setPost(request.getParameter("post"));
		userService.update(user);
		ajax(response, "修改成功");
	}
	/**
	 * delete
	 * @param request
	 * @return
	 */
	@RequestMapping(value="del")
	public void del(HttpServletRequest request, HttpServletResponse response){
		if(!requireAdminRole(request, response)){
			return;
		}
		String id = request.getParameter("id");
		userService.delete(id);
		ajax(response, "删除成功");
		
	}

	@RequestMapping(value="dels")
	public void dels(HttpServletRequest request, HttpServletResponse response){
		if(!requireAdminRole(request, response)){
			return;
		}
		String ids = request.getParameter("ids");
		if(ids == null || "".equals(ids.trim())){
			ajax(response, "请选择要删除的用户");
			return;
		}
		int count = 0;
		String[] idArray = ids.split(",");
		for(String id : idArray){
			if(id != null && !"".equals(id.trim())){
				userService.delete(id.trim());
				count++;
			}
		}
		ajax(response, count > 0 ? "删除成功" : "请选择要删除的用户");
	}

	@RequestMapping(value="teachertemplate")
	public void teachertemplate(HttpServletRequest request, HttpServletResponse response){
		if(!requireAdmin(request, response)){
			return;
		}
		try{
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("教师导入模板");
			String[] headers = {"学院", "工号", "姓名", "用户名", "密码", "邮箱", "性别", "电话"};
			HSSFRow header = sheet.createRow(0);
			for(int i=0; i<headers.length; i++){
				header.createCell(i).setCellValue(headers[i]);
				sheet.setColumnWidth(i, 5000);
			}
			HSSFRow sample = sheet.createRow(1);
			sample.createCell(0).setCellValue("计算机学院");
			sample.createCell(1).setCellValue("T001");
			sample.createCell(2).setCellValue("张老师");
			sample.createCell(3).setCellValue("teacher001");
			sample.createCell(4).setCellValue("123456");
			sample.createCell(5).setCellValue("teacher001@example.com");
			sample.createCell(6).setCellValue("男");
			sample.createCell(7).setCellValue("13800000000");
			String fileName = URLEncoder.encode("教师导入模板.xls", "UTF-8");
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			ServletOutputStream outputStream = response.getOutputStream();
			workbook.write(outputStream);
			outputStream.flush();
			outputStream.close();
		}catch(Exception e){
			response.setContentType("text/plain;charset=UTF-8");
			ajax(response, importErrorMessage("模板生成失败：" + e.getMessage()));
		}
	}

	@RequestMapping(value="import")
	public void importteacher(HttpServletRequest request, HttpServletResponse response) {
		if(!requireAdmin(request, response)){
			return;
		}
		try{
			if(!(request instanceof MultipartHttpServletRequest)){
				ajax(response, "ERROR:未选择上传文件");
				return;
			}
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multipartRequest.getFile("file");
			if(file == null || file.isEmpty()){
				ajax(response, "ERROR:未选择上传文件");
				return;
			}
			String fileName = file.getOriginalFilename();
			if(isBlank(fileName) || fileName.lastIndexOf(".") < 0){
				ajax(response, "ERROR:文件名不正确");
				return;
			}
			String filetype = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			if(!"xls".equals(filetype) && !"xlsx".equals(filetype)){
				ajax(response, "ERROR:只支持上传xls或xlsx文件");
				return;
			}
			String uploadPath = SysModel.get("uploadRoot").toString();
			if(!uploadPath.endsWith("/") && !uploadPath.endsWith(File.separator)){
				uploadPath = uploadPath + File.separator;
			}
			String savedName = new SimpleDateFormat("yyyyMMddHHmmssSS").format(Calendar.getInstance().getTime()) + "." + filetype;
			String savedPath = uploadPath + savedName;
			if(!ImageUtil.uploadfile(file, savedPath, savedName)){
				ajax(response, "ERROR:文件保存失败");
				return;
			}
			ArrayList<ArrayList<Object>> rows = ExcelUtil.readExcel(new File(savedPath));
			if(rows == null || rows.size() <= 1){
				ajax(response, "ERROR:Excel文件读取失败或内容为空");
				return;
			}
			String headerError = validateTeacherImportHeader(rows.get(0));
			if(headerError != null){
				ajax(response, headerError);
				return;
			}
			List<TeacherImportRow> importRows = parseTeacherImportRows(rows);
			if(importRows.size() == 0){
				ajax(response, "ERROR:未读取到有效教师数据");
				return;
			}
			ImportResult result = saveTeacherRows(importRows);
			ajax(response, "导入成功，新增教师" + result.createdTeachers + "人，更新教师" + result.updatedTeachers + "人。");
		}catch(Exception e){
			e.printStackTrace();
			ajax(response, importErrorMessage(e.getMessage()));
		}
	}
	
	/**
	 * 上传文件
	 * @param request
	 * @return
	 * @throws SQLException 
	 */
	@RequestMapping(value="importfile")
	public void importfile(HttpServletRequest request, HttpServletResponse response) {
		if(!requireAdminRole(request, response)){
			return;
		}
		ajax(response, "学生名单请在课程管理中由任课教师按课程导入或维护");
	}

	private static class TeacherImportRow{
		int rowNumber;
		College college;
		String no;
		String name;
		String username;
		String password;
		String email;
		String sex;
		String tel;
	}

	private static class ImportResult{
		int createdTeachers;
		int updatedTeachers;
	}

}
