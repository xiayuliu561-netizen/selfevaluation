package com.mywork.controller;


import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.mywork.bean.College;
import com.mywork.bean.CourseStudentImportItem;
import com.mywork.bean.CourseStudentImportPreview;
import com.mywork.bean.CourseStudentImportResult;
import com.mywork.bean.Dept;
import com.mywork.bean.Lesson;
import com.mywork.bean.LessonStudent;
import com.mywork.bean.User;
import com.mywork.service.AiModelService;
import com.mywork.service.CollegeService;
import com.mywork.service.CourseStudentImportService;
import com.mywork.service.DeptService;
import com.mywork.service.LessonService;
import com.mywork.service.LessonStudentService;
import com.mywork.service.UserService;
import com.mywork.util.CommonUtil;
import com.mywork.util.DateUtil;
import com.mywork.util.ExcelUtil;
import com.mywork.util.ImageUtil;
import com.mywork.util.StudentExcelParser;
import com.mywork.util.SysModel;
/**
 * 课程
 * @author 
 *
 */
@Controller
@RequestMapping(value="lesson")
public class LessonController extends BaseController{
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
	@Inject
	private CourseStudentImportService courseStudentImportService;
	@Inject
	private AiModelService aiModelService;

	private static final long PREVIEW_EXPIRE_MILLIS = 30 * 60 * 1000L;
	private static final Map<String, PreviewCache> PREVIEW_CACHE = new ConcurrentHashMap<String, PreviewCache>();
	
	/**
	 * 列表
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="list")
	public ModelAndView list(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		User user = getSessionUser(request);
		if(user == null || !"1".equals(user.getIsadmin())){
			return jsp("login", map, request);
		}
		map.put("collegelist", collegeService.getList(new HashMap<String,Object>()));
		return jsp("lesson", map, request);
	}
	/**
	 * 
	 * get list data
	 * @param request
	 * @return
	 */
	@RequestMapping(value="listdata")
	public void listdata(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("name", CommonUtil.changeEncoding(request.getParameter("queryname")));
		map.put("userid", getSessionUser(request).getId());
		List<Lesson> list = lessonService.getList(map);
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
	public void add(HttpServletRequest request, HttpServletResponse response, Lesson lesson){
		if(!requireTeacherRole(request, response)){
			return;
		}
		lesson.setUserid(getSessionUser(request).getId());
		lessonService.insert(lesson);
		ajax(response, "新增成功");
	}
	/**
	 * 修改
	 * @param request
	 * @return
	 */
	@RequestMapping(value="update")
	public void update(HttpServletRequest request, HttpServletResponse response, Lesson lesson){
		if(!requireTeacherRole(request, response)){
			return;
		}
		Lesson oldLesson = lessonService.getById(lesson.getId() == null ? null : lesson.getId().toString());
		if(oldLesson == null || !getSessionUser(request).getId().equals(oldLesson.getUserid())){
			ajax(response, "课程不存在或无权操作");
			return;
		}
		lesson.setUserid(getSessionUser(request).getId());
		lessonService.update(lesson);
		ajax(response, "修改成功");
	}
	
	/**
	 * delete
	 * @param request
	 * @return
	 */
	@RequestMapping(value="del")
	public void del(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		String id = request.getParameter("id");
		Lesson lesson = lessonService.getById(id);
		if(lesson == null || !getSessionUser(request).getId().equals(lesson.getUserid())){
			ajax(response, "课程不存在或无权操作");
			return;
		}
		Map<String,Object> deleteQuery = new HashMap<String,Object>();
		deleteQuery.put("lessonid", id);
		deleteQuery.put("teacherid", getSessionUser(request).getId());
		lessonStudentService.deleteByLesson(deleteQuery);
		lessonService.delete(id);
		ajax(response, "删除成功");
		
	}

	@RequestMapping(value="depts")
	public void depts(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("collegeId", request.getParameter("collegeId"));
		JSONArray jsonarray = JSONArray.fromObject(deptService.getList(map));
		ajax(response, "{\"data\":"+jsonarray.toString()+"}");
	}

	@RequestMapping(value="students")
	public void students(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		if(getTeacherOwnedLesson(request.getParameter("lessonid"), getSessionUser(request).getId()) == null){
			ajax(response, "{\"data\":[]}");
			return;
		}
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("isadmin", "2");
		query.put("collegeId", request.getParameter("collegeId"));
		query.put("deptId", request.getParameter("deptId"));
		List<User> students = new ArrayList<User>();
		if((request.getParameter("collegeId") != null && !"".equals(request.getParameter("collegeId"))) || (request.getParameter("deptId") != null && !"".equals(request.getParameter("deptId")))){
			students = userService.getList(query);
		}

		Map<String,Object> bindQuery = new HashMap<String,Object>();
		bindQuery.put("lessonid", request.getParameter("lessonid"));
		bindQuery.put("teacherid", getSessionUser(request).getId());
		List<LessonStudent> selected = lessonStudentService.getList(bindQuery);
		Set<Integer> selectedIds = new HashSet<Integer>();
		for(LessonStudent item : selected){
			selectedIds.add(item.getUserid());
		}
			if(students.size() == 0 && (request.getParameter("collegeId") == null || "".equals(request.getParameter("collegeId"))) && (request.getParameter("deptId") == null || "".equals(request.getParameter("deptId")))){
				for(LessonStudent item : selected){
					User user = userService.getUserById(item.getUserid()+"");
					if(user != null && !selectedIds.contains(-user.getId())){
						students.add(user);
						selectedIds.add(-user.getId());
					}
				}
			}
		for(User student : students){
			student.setChecked(selectedIds.contains(student.getId()));
		}
		JSONArray jsonarray = JSONArray.fromObject(students);
		ajax(response, "{\"data\":"+jsonarray.toString()+"}");
	}

	@RequestMapping(value="savestudents")
	public void savestudents(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		String lessonid = request.getParameter("lessonid");
		if(lessonid == null || "".equals(lessonid)){
			ajax(response, "请选择课程");
			return;
		}
		Lesson lesson = lessonService.getById(lessonid);
		if(lesson == null || !getSessionUser(request).getId().equals(lesson.getUserid())){
			ajax(response, "课程不存在或无权操作");
			return;
		}
		String studentids = request.getParameter("studentids");
		List<String> validIds = new ArrayList<String>();
		if(studentids != null && !"".equals(studentids.trim())){
			String[] ids = studentids.split(",");
			Set<String> exists = new HashSet<String>();
			for(String id : ids){
				if(id == null || "".equals(id.trim())){
					continue;
				}
				if(exists.contains(id.trim())){
					continue;
				}
				User student = userService.getUserById(id.trim());
				if(student == null || !"2".equals(student.getIsadmin())){
					ajax(response, "存在无效学生，已取消保存");
					return;
				}
				exists.add(id.trim());
				validIds.add(id.trim());
			}
		}

		Map<String,Object> deleteQuery = new HashMap<String,Object>();
		deleteQuery.put("lessonid", lessonid);
		deleteQuery.put("teacherid", getSessionUser(request).getId());
		lessonStudentService.deleteByLesson(deleteQuery);
		for(String id : validIds){
			LessonStudent lessonStudent = new LessonStudent();
			lessonStudent.setLessonid(Integer.valueOf(lessonid));
			lessonStudent.setTeacherid(getSessionUser(request).getId());
			lessonStudent.setUserid(Integer.valueOf(id));
			lessonStudent.setCreatetime(DateUtil.formatHMS(new Date()));
			lessonStudentService.insert(lessonStudent);
		}
		ajax(response, "保存成功");
	}

	@RequestMapping(value="studenttemplate")
	public void studenttemplate(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		try{
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("课程学生名单");
			HSSFRow header = sheet.createRow(0);
			header.createCell(0).setCellValue("姓名");
			header.createCell(1).setCellValue("学号");
			header.createCell(2).setCellValue("学院");
			header.createCell(3).setCellValue("班级");
			HSSFRow sample = sheet.createRow(1);
			sample.createCell(0).setCellValue("张三");
			sample.createCell(1).setCellValue("2024010101");
			sample.createCell(2).setCellValue("计算机学院");
			sample.createCell(3).setCellValue("计算机科学与技术1班");
			for(int i=0; i<4; i++){
				sheet.setColumnWidth(i, 5000);
			}
			String fileName = URLEncoder.encode("课程学生名单模板.xls", "UTF-8");
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			ServletOutputStream outputStream = response.getOutputStream();
			workbook.write(outputStream);
			outputStream.flush();
			outputStream.close();
		}catch(Exception e){
			response.setContentType("text/plain;charset=UTF-8");
			ajax(response, "模板生成失败：" + e.getMessage());
		}
	}

	@RequestMapping(value="importstudents")
	public void importstudents(HttpServletRequest request, HttpServletResponse response){
		try{
			if(!requireTeacherRole(request, response)){
				return;
			}
			CourseStudentImportPreview preview = buildImportPreview(request);
			List<CourseStudentImportItem> validItems = getValidPreviewItems(preview.getItems());
			if(validItems.size() == 0){
				ajax(response, "ERROR:没有通过校验的学生数据，未执行导入");
				return;
			}
			CourseStudentImportResult result = courseStudentImportService.importStudents(
				request.getParameter("lessonid"), getSessionUser(request).getId(), validItems);
			ajax(response, buildImportSuccessMessage(result, preview));
		}catch(Exception e){
			e.printStackTrace();
			ajax(response, importErrorMessage(e.getMessage()));
		}
	}

	@RequestMapping(value="previewstudents")
	public void previewstudents(HttpServletRequest request, HttpServletResponse response){
		try{
			if(!requireTeacherRole(request, response)){
				return;
			}
			CourseStudentImportPreview preview = buildImportPreview(request);
			cleanupPreviewCache();
			PREVIEW_CACHE.put(preview.getToken(), new PreviewCache(request.getParameter("lessonid"),
				getSessionUser(request).getId(), preview.getItems()));
			ajax(response, "{\"success\":true,\"data\":" + JSONObject.fromObject(preview).toString() + "}");
		}catch(Exception e){
			e.printStackTrace();
			ajax(response, "{\"success\":false,\"message\":\"" + jsonEscape(importErrorMessage(e.getMessage())) + "\"}");
		}
	}

	@RequestMapping(value="confirmstudents")
	public void confirmstudents(HttpServletRequest request, HttpServletResponse response){
		try{
			if(!requireTeacherRole(request, response)){
				return;
			}
			String token = request.getParameter("token");
			if(isBlank(token)){
				ajax(response, "{\"success\":false,\"message\":\"缺少预览确认标识，请重新识别名单\"}");
				return;
			}
			cleanupPreviewCache();
			PreviewCache cache = PREVIEW_CACHE.get(token);
			Integer teacherid = getSessionUser(request).getId();
			String lessonid = request.getParameter("lessonid");
			if(cache == null || !teacherid.equals(cache.teacherid) || !lessonid.equals(cache.lessonid)){
				ajax(response, "{\"success\":false,\"message\":\"预览结果已失效，请重新上传识别\"}");
				return;
			}
			List<CourseStudentImportItem> validItems = getValidPreviewItems(cache.items);
			if(validItems.size() == 0){
				ajax(response, "{\"success\":false,\"message\":\"没有通过校验的学生数据，未执行导入\"}");
				return;
			}
			CourseStudentImportResult result = courseStudentImportService.importStudents(lessonid, teacherid, validItems);
			PREVIEW_CACHE.remove(token);
			JSONObject data = JSONObject.fromObject(result);
			ajax(response, "{\"success\":true,\"message\":\"" + jsonEscape(buildConfirmSuccessMessage(result))
				+ "\",\"data\":" + data.toString() + "}");
		}catch(Exception e){
			e.printStackTrace();
			ajax(response, "{\"success\":false,\"message\":\"" + jsonEscape(importErrorMessage(e.getMessage())) + "\"}");
		}
	}

	private CourseStudentImportPreview buildImportPreview(final HttpServletRequest request) throws Exception{
		String lessonid = request.getParameter("lessonid");
		Integer teacherid = getSessionUser(request).getId();
		if(isBlank(lessonid)){
			throw new Exception("请选择课程");
		}
		if(getTeacherOwnedLesson(lessonid, teacherid) == null){
			throw new Exception("课程不存在或无权操作");
		}
		MultipartFile file = getUploadFile(request);
		String savedPath = saveImportFile(file);
		ArrayList<ArrayList<Object>> rows = ExcelUtil.readExcel(new File(savedPath));
		if(rows == null || rows.size() == 0){
			throw new Exception("Excel文件读取失败或内容为空");
		}
		StudentExcelParser.ParseResult localResult = StudentExcelParser.parseRows(rows);
		StudentExcelParser.ParseResult parseResult = localResult;
		boolean needAi = localResult == null || localResult.items.size() == 0 || hasIncompleteCandidate(localResult.items);
		if(needAi){
			try{
				String aiContent = aiModelService.callPrompt(StudentExcelParser.buildAiPrompt(rows), 2048, 45000);
				StudentExcelParser.ParseResult aiResult = StudentExcelParser.parseAiContent(aiContent);
				if(aiResult.items.size() > 0){
					parseResult = aiResult;
				}
			}catch(Exception e){
				if(localResult == null || localResult.items.size() == 0){
					throw new Exception("AI识别暂时未完成，请稍后重试；如多次失败，请联系管理员");
				}
				localResult.message = localResult.message + "；AI识别暂时未完成，已使用系统规则识别结果";
				parseResult = localResult;
			}
		}
		if(parseResult == null || parseResult.items.size() == 0){
			throw new Exception("未识别到有效学生数据，请确认表格中包含姓名、学号、学院、班级");
		}
		StudentExcelParser.validateItems(parseResult, new StudentExcelParser.StudentLookup(){
			public boolean existsStudentNo(String no) {
				User user = getStudentByNo(no);
				return user != null && "2".equals(user.getIsadmin());
			}
		});
		markOccupiedNonStudentNos(parseResult.items);
		markUnavailableUsernames(parseResult.items);
		CourseStudentImportPreview preview = new CourseStudentImportPreview();
		preview.setToken(UUID.randomUUID().toString().replace("-", ""));
		preview.setSource(parseResult.source);
		preview.setMessage(parseResult.message);
		preview.setItems(parseResult.items);
		fillPreviewCounts(preview);
		return preview;
	}

	private void markOccupiedNonStudentNos(List<CourseStudentImportItem> items){
		if(items == null){
			return;
		}
		for(CourseStudentImportItem item : items){
			if(item == null || isBlank(item.getNo())){
				continue;
			}
			User user = getStudentByNo(item.getNo());
			if(user != null && !"2".equals(user.getIsadmin())){
				item.setValid(Boolean.FALSE);
				item.setMessage(appendMessage(item.getMessage(), "学号已被非学生用户占用"));
			}
		}
	}

	private void markUnavailableUsernames(List<CourseStudentImportItem> items){
		if(items == null){
			return;
		}
		for(CourseStudentImportItem item : items){
			if(item == null || isBlank(item.getNo())){
				continue;
			}
			User studentByNo = getStudentByNo(item.getNo());
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("username", item.getNo());
			List<User> users = userService.getList(map);
			for(User user : users){
				if(studentByNo != null && studentByNo.getId() != null && studentByNo.getId().equals(user.getId())){
					continue;
				}
				item.setValid(Boolean.FALSE);
				item.setMessage(appendMessage(item.getMessage(), "学号作为用户名已被其他账号占用"));
				break;
			}
		}
	}

	private String appendMessage(String message, String append){
		if(isBlank(message)){
			return append;
		}
		return message + "；" + append;
	}

	private MultipartFile getUploadFile(HttpServletRequest request) throws Exception{
		if(!(request instanceof MultipartHttpServletRequest)){
			throw new Exception("未选择上传文件");
		}
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile file = multipartRequest.getFile("file");
		if(file == null || file.isEmpty()){
			throw new Exception("未选择上传文件");
		}
		if(file.getSize() > 10 * 1024 * 1024){
			throw new Exception("上传文件不能超过10MB");
		}
		String fileName = file.getOriginalFilename();
		if(isBlank(fileName) || fileName.lastIndexOf(".") < 0){
			throw new Exception("文件名不正确");
		}
		String filetype = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		if(!"xls".equals(filetype) && !"xlsx".equals(filetype)){
			throw new Exception("只支持上传xls或xlsx文件");
		}
		return file;
	}

	private String saveImportFile(MultipartFile file) throws Exception{
		String fileName = file.getOriginalFilename();
		String filetype = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		String uploadPath = SysModel.get("uploadRoot").toString();
		if(!uploadPath.endsWith("/") && !uploadPath.endsWith(File.separator)){
			uploadPath = uploadPath + File.separator;
		}
		File uploadDir = new File(uploadPath);
		if(!uploadDir.exists()){
			uploadDir.mkdirs();
		}
		String savedName = new SimpleDateFormat("yyyyMMddHHmmssSS").format(Calendar.getInstance().getTime())
			+ "_" + UUID.randomUUID().toString().replace("-", "") + "." + filetype;
		String savedPath = uploadPath + savedName;
		if(!ImageUtil.uploadfile(file, savedPath, savedName)){
			throw new Exception("文件保存失败");
		}
		return savedPath;
	}

	private boolean hasIncompleteCandidate(List<CourseStudentImportItem> items){
		if(items == null || items.size() == 0){
			return true;
		}
		for(CourseStudentImportItem item : items){
			if(item == null || isBlank(item.getName()) || isBlank(item.getNo())
				|| isBlank(item.getCollegeName()) || isBlank(item.getDeptName())){
				return true;
			}
		}
		return false;
	}

	private void fillPreviewCounts(CourseStudentImportPreview preview){
		int total = 0;
		int valid = 0;
		int invalid = 0;
		int duplicate = 0;
		int existing = 0;
		if(preview.getItems() != null){
			total = preview.getItems().size();
			for(CourseStudentImportItem item : preview.getItems()){
				if(item.getValid() != null && item.getValid().booleanValue()){
					valid++;
				}else{
					invalid++;
				}
				if(item.getDuplicate() != null && item.getDuplicate().booleanValue()){
					duplicate++;
				}
				if(item.getExistingStudent() != null && item.getExistingStudent().booleanValue()){
					existing++;
				}
			}
		}
		preview.setTotalCount(Integer.valueOf(total));
		preview.setValidCount(Integer.valueOf(valid));
		preview.setInvalidCount(Integer.valueOf(invalid));
		preview.setDuplicateCount(Integer.valueOf(duplicate));
		preview.setExistingCount(Integer.valueOf(existing));
	}

	private List<CourseStudentImportItem> getValidPreviewItems(List<CourseStudentImportItem> items){
		List<CourseStudentImportItem> validItems = new ArrayList<CourseStudentImportItem>();
		if(items == null){
			return validItems;
		}
		for(CourseStudentImportItem item : items){
			if(item != null && item.getValid() != null && item.getValid().booleanValue()){
				validItems.add(item);
			}
		}
		return validItems;
	}

	private Lesson getTeacherOwnedLesson(String lessonid, Integer teacherid){
		if(isBlank(lessonid) || teacherid == null){
			return null;
		}
		Lesson lesson = lessonService.getById(lessonid);
		if(lesson == null || lesson.getUserid() == null || !teacherid.equals(lesson.getUserid())){
			return null;
		}
		return lesson;
	}

	private User getStudentByNo(String no){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("no", no);
		List<User> list = userService.getList(map);
		return list.size() == 0 ? null : list.get(0);
	}

	private void cleanupPreviewCache(){
		long now = System.currentTimeMillis();
		Iterator<Map.Entry<String, PreviewCache>> iterator = PREVIEW_CACHE.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, PreviewCache> entry = iterator.next();
			if(entry.getValue() == null || now - entry.getValue().createTime > PREVIEW_EXPIRE_MILLIS){
				iterator.remove();
			}
		}
	}

	private String buildImportSuccessMessage(CourseStudentImportResult result, CourseStudentImportPreview preview){
		return "导入成功，识别" + preview.getTotalCount() + "行，成功导入" + result.getBoundStudents()
			+ "名课程学生，新增学生" + result.getCreatedStudents() + "人，更新/复用学生"
			+ result.getUpdatedStudents() + "人，跳过无效行" + result.getSkippedRows()
			+ "行，自动创建学院" + result.getCreatedColleges() + "个、班级" + result.getCreatedDepts()
			+ "个。新建学生用户名默认为学号，初始密码为123456。";
	}

	private String buildConfirmSuccessMessage(CourseStudentImportResult result){
		return "导入成功：新增学生" + result.getCreatedStudents() + "人，更新/复用学生"
			+ result.getUpdatedStudents() + "人，绑定课程学生" + result.getBoundStudents()
			+ "人，跳过无效行" + result.getSkippedRows() + "行，创建学院"
			+ result.getCreatedColleges() + "个、班级" + result.getCreatedDepts() + "个。";
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

	private boolean isBlank(String value){
		return value == null || "".equals(value.trim());
	}

	private String jsonEscape(String value){
		if(value == null){
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
	}

	private static class PreviewCache{
		String lessonid;
		Integer teacherid;
		List<CourseStudentImportItem> items;
		long createTime;

		PreviewCache(String lessonid, Integer teacherid, List<CourseStudentImportItem> items){
			this.lessonid = lessonid;
			this.teacherid = teacherid;
			this.items = items;
			this.createTime = System.currentTimeMillis();
		}
	}

}
