package com.mywork.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.mywork.bean.AnalysisComponent;
import com.mywork.bean.AnalysisTarget;
import com.mywork.bean.AnalysisTargetItem;
import com.mywork.bean.Lesson;
import com.mywork.bean.LessonStudent;
import com.mywork.bean.Score;
import com.mywork.bean.ScoreDetail;
import com.mywork.bean.ScoreImportItem;
import com.mywork.bean.ScoreImportPreview;
import com.mywork.bean.ScoreImportResult;
import com.mywork.bean.Sumscore;
import com.mywork.bean.User;
import com.mywork.service.AiModelService;
import com.mywork.service.AnalysisComponentService;
import com.mywork.service.AnalysisTargetItemService;
import com.mywork.service.AnalysisTargetService;
import com.mywork.service.LessonService;
import com.mywork.service.LessonStudentService;
import com.mywork.service.ScoreDetailService;
import com.mywork.service.ScoreService;
import com.mywork.service.StatisticsService;
import com.mywork.service.SumscoreService;
import com.mywork.service.UserService;
import com.mywork.util.DateUtil;
import com.mywork.util.ExcelUtil;
import com.mywork.util.ImageUtil;
import com.mywork.util.ScoreComputeUtil;
import com.mywork.util.ScoreExcelParser;
import com.mywork.util.SysModel;

@Controller
@RequestMapping(value="score")
public class ScoreController extends BaseController{
	@Inject
	private ScoreService scoreService;
	@Inject
	private UserService userService;
	@Inject
	private SumscoreService sumscoreService;
	@Inject
	private LessonService lessonService;
	@Inject
	private LessonStudentService lessonStudentService;
	@Inject
	private AnalysisComponentService analysisComponentService;
	@Inject
	private AnalysisTargetService analysisTargetService;
	@Inject
	private AnalysisTargetItemService analysisTargetItemService;
	@Inject
	private ScoreDetailService scoreDetailService;
	@Inject
	private AiModelService aiModelService;
	@Inject
	private StatisticsService statisticsService;

	private static final String STATUS_IMPORTED = "IMPORTED";
	private static final String STATUS_MISSING = "MISSING";
	private static final String STRATEGY_FILL_BLANK = "fill_blank";
	private static final String STRATEGY_SKIP = "skip";
	private static final String STRATEGY_OVERWRITE = "overwrite";
	private static final long PREVIEW_EXPIRE_MILLIS = 30 * 60 * 1000L;
	private static final Map<String, ScorePreviewCache> SCORE_PREVIEW_CACHE = new ConcurrentHashMap<String, ScorePreviewCache>();

	@RequestMapping(value="list")
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return null;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		List<Lesson> lessonlist = getTeacherLessonList(request);
		map.put("lessonlist", lessonlist);
		map.put("lessonid", getCurrentLessonId(request, lessonlist));
		return jsp("score", map, request);
	}

	@RequestMapping(value="components")
	public void components(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		String lessonid = getCurrentLessonId(request, getTeacherLessonList(request));
		JSONObject result = new JSONObject();
		result.put("success", true);
		result.put("components", JSONArray.fromObject(getComponents(lessonid, getSessionUser(request).getId())));
		ajax(response, result.toString());
	}

	@RequestMapping(value="listdata")
	public void listdata(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		String lessonid = getCurrentLessonId(request, getTeacherLessonList(request));
		if(!isNotBlank(lessonid)){
			writeRows(response, new ArrayList<Score>());
			return;
		}
		List<Score> rows = buildScoreRows(lessonid, request.getParameter("queryname"), getSessionUser(request).getId());
		writeRows(response, rows);
	}

	@RequestMapping(value="downloadtemplate")
	public void downloadtemplate(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		String lessonid = request.getParameter("lessonid");
		try{
			if(getTeacherOwnedLesson(lessonid, getSessionUser(request).getId()) == null){
				response.setContentType("text/plain;charset=UTF-8");
				ajax(response, "课程不存在或无权操作");
				return;
			}
			List<AnalysisComponent> components = getComponents(lessonid, getSessionUser(request).getId());
			if(components.size() == 0){
				response.setContentType("text/plain;charset=UTF-8");
				ajax(response, "请先在融合数据分析页面保存成绩组成部分");
				return;
			}
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("成绩导入模板");
			HSSFRow header = sheet.createRow(0);
			header.createCell(0).setCellValue("学号");
			header.createCell(1).setCellValue("姓名");
			for(int i=0; i<components.size(); i++){
				header.createCell(i+2).setCellValue(components.get(i).getComponentName());
			}
			int rowIndex = 1;
			for(User student : getBoundStudents(lessonid, getSessionUser(request).getId(), null)){
				HSSFRow row = sheet.createRow(rowIndex++);
				row.createCell(0).setCellValue(student.getNo() == null ? "" : student.getNo());
				row.createCell(1).setCellValue(student.getName() == null ? "" : student.getName());
				for(int i=0; i<components.size(); i++){
					row.createCell(i+2).setCellValue(0);
				}
			}
			if(rowIndex == 1){
				HSSFRow row = sheet.createRow(rowIndex);
				row.createCell(0).setCellValue("");
				row.createCell(1).setCellValue("");
				for(int i=0; i<components.size(); i++){
					row.createCell(i+2).setCellValue(0);
				}
			}
			for(int i=0; i<components.size()+2; i++){
				sheet.setColumnWidth(i, 5000);
			}
			String fileName = URLEncoder.encode("成绩导入模板.xls", "UTF-8");
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

	@RequestMapping(value="importfile")
	@Transactional(rollbackFor = { Exception.class })
	public void importfile(HttpServletRequest request, HttpServletResponse response) {
		if(!requireTeacherRole(request, response)){
			return;
		}
		try{
			ScoreImportPreview preview = buildScoreImportPreview(request);
			ScoreImportResult result = applyScoreImport(request.getParameter("lessonid"), preview, STRATEGY_FILL_BLANK, getSessionUser(request).getId());
			ajax(response, buildScoreImportResultMessage(result, preview, STRATEGY_FILL_BLANK));
		}catch(Exception e){
			try{
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			}catch(Exception tx){
			}
			ajax(response, "上传失败：" + e.getMessage());
		}
	}

	@RequestMapping(value="previewimport")
	public void previewimport(HttpServletRequest request, HttpServletResponse response) {
		if(!requireTeacherRole(request, response)){
			return;
		}
		try{
			ScoreImportPreview preview = buildScoreImportPreview(request);
			cleanupScorePreviewCache();
			String lessonid = request.getParameter("lessonid");
			SCORE_PREVIEW_CACHE.put(preview.getToken(), new ScorePreviewCache(lessonid, getSessionUser(request).getId(), preview));
			JSONObject result = new JSONObject();
			result.put("success", true);
			result.put("data", JSONObject.fromObject(preview));
			ajax(response, result.toString());
		}catch(Exception e){
			JSONObject result = new JSONObject();
			result.put("success", false);
			result.put("message", e.getMessage());
			ajax(response, result.toString());
		}
	}

	@RequestMapping(value="confirmimport")
	@Transactional(rollbackFor = { Exception.class })
	public void confirmimport(HttpServletRequest request, HttpServletResponse response) {
		if(!requireTeacherRole(request, response)){
			return;
		}
		try{
			String token = request.getParameter("token");
			String lessonid = request.getParameter("lessonid");
			String strategy = normalizeConflictStrategy(request.getParameter("conflictStrategy"));
			if(!isNotBlank(lessonid)){
				throw new Exception("请选择课程");
			}
			if(!isNotBlank(token)){
				throw new Exception("缺少预览确认标识，请重新上传成绩表");
			}
			cleanupScorePreviewCache();
			ScorePreviewCache cache = SCORE_PREVIEW_CACHE.get(token);
			if(cache == null || !lessonid.equals(cache.lessonid) || !getSessionUser(request).getId().equals(cache.teacherid)){
				throw new Exception("预览结果已失效，请重新上传成绩表");
			}
			ScoreImportResult result = applyScoreImport(cache.lessonid, cache.preview, strategy, getSessionUser(request).getId());
			SCORE_PREVIEW_CACHE.remove(token);
			JSONObject json = new JSONObject();
			json.put("success", true);
			json.put("message", buildScoreImportResultMessage(result, cache.preview, strategy));
			json.put("data", JSONObject.fromObject(result));
			ajax(response, json.toString());
		}catch(Exception e){
			try{
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			}catch(Exception tx){
			}
			JSONObject result = new JSONObject();
			result.put("success", false);
			result.put("message", e.getMessage());
			ajax(response, result.toString());
		}
	}

	private ScoreImportPreview buildScoreImportPreview(HttpServletRequest request) throws Exception{
		String lessonid = request.getParameter("lessonid");
		Integer teacherid = getSessionUser(request).getId();
		if(!isNotBlank(lessonid)){
			throw new Exception("请选择课程");
		}
		Lesson lesson = getTeacherOwnedLesson(lessonid, teacherid);
		if(lesson == null){
			throw new Exception("课程不存在或无权操作");
		}
		List<AnalysisComponent> components = getComponents(lessonid, teacherid);
		if(components.size() == 0){
			throw new Exception("请先在融合数据分析页面保存成绩组成部分");
		}
		List<User> boundStudents = getBoundStudents(lessonid, teacherid, null);
		if(boundStudents.size() == 0){
			throw new Exception("请先在课程管理中为该课程指定学生");
		}
		MultipartFile file = getScoreUploadFile(request);
		File savedFile = null;
		try{
			savedFile = new File(saveScoreImportFile(file));
			ArrayList<ArrayList<Object>> rows = ExcelUtil.readExcel(savedFile);
			if(rows == null || rows.size() == 0){
				throw new Exception("Excel文件读取失败或内容为空");
			}
			ScoreExcelParser.ParseResult parseResult = ScoreExcelParser.parseRows(rows, components);
			List<ScoreImportItem> validatedItems = null;
			if(parseResult != null && parseResult.items.size() > 0){
				validatedItems = validateScoreImportItems(lessonid, teacherid, components, boundStudents, parseResult.items);
			}
			if(parseResult == null || parseResult.items.size() == 0 || !hasValidImportItems(validatedItems)){
				try{
					String aiContent = aiModelService.callPrompt(ScoreExcelParser.buildAiPrompt(rows, components, lesson.getName()), 4096, 90000);
					ScoreExcelParser.ParseResult aiResult = ScoreExcelParser.parseAiContent(aiContent);
					if(aiResult != null && aiResult.items.size() > 0){
						List<ScoreImportItem> aiValidatedItems = validateScoreImportItems(lessonid, teacherid, components, boundStudents, aiResult.items);
						if(hasValidImportItems(aiValidatedItems) || !hasValidImportItems(validatedItems)){
							parseResult = aiResult;
							validatedItems = aiValidatedItems;
						}
					}
				}catch(Exception e){
					throw new Exception("AI识别暂时未完成，请稍后重试；如多次失败，请联系管理员");
				}
			}
			if(parseResult == null || parseResult.items.size() == 0){
				throw new Exception("未识别到可导入的成绩数据");
			}
			ScoreImportPreview preview = new ScoreImportPreview();
			preview.setToken(UUID.randomUUID().toString().replace("-", ""));
			preview.setSource(parseResult.source);
			preview.setTableType(isNotBlank(parseResult.tableType) ? parseResult.tableType : "成绩表");
			preview.setMessage(parseResult.message);
			preview.setQuestionScoreMessage(parseResult.questionScoreMessage);
			preview.setFieldMappings(parseResult.fieldMappings);
			if(validatedItems == null){
				validatedItems = validateScoreImportItems(lessonid, teacherid, components, boundStudents, parseResult.items);
			}
			preview.setItems(validatedItems);
			fillScoreImportPreviewCounts(preview);
			if(preview.getTotalCount().intValue() == 0){
				throw new Exception("没有通过校验的成绩数据，未生成导入预览");
			}
			return preview;
		}finally{
			deleteQuietly(savedFile);
		}
	}

	private boolean hasValidImportItems(List<ScoreImportItem> items){
		if(items == null || items.size() == 0){
			return false;
		}
		for(ScoreImportItem item : items){
			if(item != null && Boolean.TRUE.equals(item.getValid())){
				return true;
			}
		}
		return false;
	}

	private List<ScoreImportItem> validateScoreImportItems(String lessonid, Integer teacherid, List<AnalysisComponent> components, List<User> boundStudents, List<ScoreImportItem> candidates){
		List<ScoreImportItem> items = new ArrayList<ScoreImportItem>();
		Map<String,User> studentsByNo = new HashMap<String,User>();
		Map<String,List<User>> studentsByName = new HashMap<String,List<User>>();
		for(User student : boundStudents){
			if(student.getNo() != null){
				studentsByNo.put(normalizeImportNo(student.getNo()), student);
			}
			String name = normalizeImportText(student.getName());
			if(isNotBlank(name)){
				List<User> sameName = studentsByName.get(name);
				if(sameName == null){
					sameName = new ArrayList<User>();
					studentsByName.put(name, sameName);
				}
				sameName.add(student);
			}
		}
		ExistingScoreIndex existingIndex = buildExistingScoreIndex(lessonid, teacherid);
		Set<String> seen = new HashSet<String>();
		for(ScoreImportItem source : candidates){
			ScoreImportItem item = copyScoreImportItem(source);
			List<String> errors = new ArrayList<String>();
			item.setNo(normalizeImportNo(item.getNo()));
			item.setName(normalizeImportText(item.getName()));
			String componentName = matchCourseComponentName(item.getComponentName(), components);
			if(!isNotBlank(componentName)){
				errors.add("成绩项无法匹配当前课程成绩组成：" + safeText(item.getComponentName()));
			}else{
				item.setComponentName(componentName);
			}
			User student = null;
			if(isNotBlank(item.getNo())){
				student = studentsByNo.get(item.getNo());
				if(student == null){
					errors.add("学号未绑定当前课程或系统中不存在：" + item.getNo());
				}
			}else if(isNotBlank(item.getName())){
				List<User> sameName = studentsByName.get(item.getName());
				if(sameName != null && sameName.size() == 1){
					student = sameName.get(0);
					item.setNo(normalizeImportNo(student.getNo()));
				}else if(sameName != null && sameName.size() > 1){
					errors.add("姓名对应多名课程学生，无法确定学号：" + item.getName());
				}else{
					errors.add("缺少学号且姓名未匹配课程学生：" + item.getName());
				}
			}else{
				errors.add("缺少学号和姓名");
			}
			if(student != null){
				item.setUserid(student.getId());
				item.setSystemName(student.getName());
				item.setDeptName(student.getDeptName());
				if(isNotBlank(item.getName()) && isNotBlank(student.getName()) && !normalizeImportText(student.getName()).equals(item.getName())){
					errors.add("姓名与系统学生不一致，系统姓名为：" + student.getName());
				}
			}
			if(item.getScore() == null){
				errors.add("成绩不是合法数字：" + safeText(item.getRawScore()));
			}else if(item.getScore().compareTo(BigDecimal.ZERO) < 0 || item.getScore().compareTo(new BigDecimal("100")) > 0){
				errors.add("成绩超出允许范围0-100：" + item.getScore());
			}
			if(hasManualConfirmRisk(item)){
				errors.add("成绩来源存在歧义，需人工确认：" + safeText(item.getMessage()));
			}
			if(errors.size() == 0){
				String key = item.getUserid() + "|" + ScoreComputeUtil.normalize(item.getComponentName());
				if(seen.contains(key)){
					item.setDuplicate(Boolean.TRUE);
					item.setValid(Boolean.FALSE);
					item.setAction("DUPLICATE");
					item.setMessage(appendMessage(item.getMessage(), "同一学生同一成绩项在本次文件中重复，已跳过"));
					items.add(item);
					continue;
				}
				seen.add(key);
				ScoreDetail existing = existingIndex.detailMap.get(key);
				if(existing != null){
					item.setExistingScore(existing.getScore());
				}
				if(existing == null){
					item.setAction(existingIndex.userDetailCount.containsKey(item.getUserid()) ? "FILL" : "CREATE");
					item.setValid(Boolean.TRUE);
					item.setConflict(Boolean.FALSE);
				}else if(isMissingDetail(existing)){
					item.setAction("FILL");
					item.setValid(Boolean.TRUE);
					item.setConflict(Boolean.FALSE);
				}else if(compareScore(existing.getScore(), item.getScore()) == 0){
					item.setAction("SKIP");
					item.setValid(Boolean.TRUE);
					item.setConflict(Boolean.FALSE);
					item.setMessage(appendMessage(item.getMessage(), "已有相同成绩，确认时不重复写入"));
				}else{
					item.setAction("CONFLICT");
					item.setValid(Boolean.TRUE);
					item.setConflict(Boolean.TRUE);
					item.setMessage(appendMessage(item.getMessage(), "已有成绩为" + ScoreComputeUtil.format(existing.getScore()) + "，新成绩为" + ScoreComputeUtil.format(item.getScore()) + "，需选择冲突处理策略"));
				}
			}else{
				item.setValid(Boolean.FALSE);
				item.setConflict(Boolean.FALSE);
				item.setAction("INVALID");
				item.setMessage(appendMessage(item.getMessage(), joinMessages(errors)));
			}
			items.add(item);
		}
		return items;
	}

	private void fillScoreImportPreviewCounts(ScoreImportPreview preview){
		int total = 0;
		int valid = 0;
		int invalid = 0;
		int conflict = 0;
		int duplicate = 0;
		int create = 0;
		int fill = 0;
		int updateRisk = 0;
		Map<String,String> affected = new LinkedHashMap<String,String>();
		if(preview.getItems() != null){
			total = preview.getItems().size();
			for(ScoreImportItem item : preview.getItems()){
				if(Boolean.TRUE.equals(item.getDuplicate()) || "DUPLICATE".equals(item.getAction()) || "SKIP".equals(item.getAction())){
					duplicate++;
				}
				if(Boolean.TRUE.equals(item.getValid())){
					valid++;
					if("CREATE".equals(item.getAction())){
						create++;
					}else if("FILL".equals(item.getAction())){
						fill++;
					}else if("CONFLICT".equals(item.getAction())){
						conflict++;
						updateRisk++;
					}
					if(isNotBlank(item.getComponentName())){
						affected.put(item.getComponentName(), item.getComponentName());
					}
				}else{
					invalid++;
				}
			}
		}
		preview.setTotalCount(Integer.valueOf(total));
		preview.setValidCount(Integer.valueOf(valid));
		preview.setInvalidCount(Integer.valueOf(invalid));
		preview.setConflictCount(Integer.valueOf(conflict));
		preview.setDuplicateCount(Integer.valueOf(duplicate));
		preview.setCreateCount(Integer.valueOf(create));
		preview.setFillCount(Integer.valueOf(fill));
		preview.setUpdateRiskCount(Integer.valueOf(updateRisk));
		preview.setAffectedComponents(new ArrayList<String>(affected.keySet()));
	}

	private ScoreImportResult applyScoreImport(String lessonid, ScoreImportPreview preview, String strategy, Integer teacherid) throws Exception{
		Lesson lesson = getTeacherOwnedLesson(lessonid, teacherid);
		if(lesson == null){
			throw new Exception("课程不存在或无权操作");
		}
		List<AnalysisComponent> components = getComponents(lessonid, teacherid);
		ScoreImportResult result = new ScoreImportResult();
		Set<Integer> affectedStudents = new HashSet<Integer>();
		Set<Integer> recalculatedStudents = new HashSet<Integer>();
		if(preview.getItems() == null || preview.getItems().size() == 0){
			throw new Exception("预览结果为空，请重新上传成绩表");
		}
		for(ScoreImportItem item : preview.getItems()){
			if(!Boolean.TRUE.equals(item.getValid())){
				result.setSkippedInvalid(Integer.valueOf(result.getSkippedInvalid().intValue() + 1));
				continue;
			}
			if(item.getUserid() == null || !isNotBlank(item.getComponentName()) || item.getScore() == null){
				result.setSkippedInvalid(Integer.valueOf(result.getSkippedInvalid().intValue() + 1));
				continue;
			}
			if("DUPLICATE".equals(item.getAction()) || "SKIP".equals(item.getAction())){
				result.setSkippedDuplicates(Integer.valueOf(result.getSkippedDuplicates().intValue() + 1));
				continue;
			}
			ScoreDetail current = getUserComponentDetail(lessonid, teacherid, item.getUserid(), item.getComponentName());
			if(current == null){
				insertScoreDetail(lessonid, teacherid, item.getUserid(), item.getComponentName(), item.getScore(), findComponentSort(components, item.getComponentName()), STATUS_IMPORTED);
				result.setInsertedDetails(Integer.valueOf(result.getInsertedDetails().intValue() + 1));
				result.setCreatedRecords(Integer.valueOf(result.getCreatedRecords().intValue() + 1));
				affectedStudents.add(item.getUserid());
			}else if(isMissingDetail(current)){
				upsertScoreDetail(lessonid, teacherid, item.getUserid(), item.getComponentName(), item.getScore(), findComponentSort(components, item.getComponentName()), STATUS_IMPORTED);
				result.setFilledBlankDetails(Integer.valueOf(result.getFilledBlankDetails().intValue() + 1));
				result.setUpdatedDetails(Integer.valueOf(result.getUpdatedDetails().intValue() + 1));
				affectedStudents.add(item.getUserid());
			}else if(compareScore(current.getScore(), item.getScore()) == 0){
				result.setSkippedDuplicates(Integer.valueOf(result.getSkippedDuplicates().intValue() + 1));
			}else if(STRATEGY_OVERWRITE.equals(strategy)){
				upsertScoreDetail(lessonid, teacherid, item.getUserid(), item.getComponentName(), item.getScore(), findComponentSort(components, item.getComponentName()), STATUS_IMPORTED);
				result.setOverwrittenDetails(Integer.valueOf(result.getOverwrittenDetails().intValue() + 1));
				result.setUpdatedDetails(Integer.valueOf(result.getUpdatedDetails().intValue() + 1));
				affectedStudents.add(item.getUserid());
			}else{
				result.setSkippedConflicts(Integer.valueOf(result.getSkippedConflicts().intValue() + 1));
			}
		}
		for(Integer userid : affectedStudents){
			Map<String,BigDecimal> scoreMap = getUserScoreMap(lessonid, userid, teacherid);
			syncLegacyScore(lessonid, teacherid, userid, components, scoreMap);
			String status = (isMissingScore(lessonid, userid, teacherid) || scoreMap.size() < components.size()) ? STATUS_MISSING : STATUS_IMPORTED;
			saveSumscore(lessonid, teacherid, userid, components, scoreMap, status);
			recalculatedStudents.add(userid);
		}
		result.setAffectedStudents(Integer.valueOf(affectedStudents.size()));
		result.setRecalculatedStudents(Integer.valueOf(recalculatedStudents.size()));
		statisticsService.incrementGradeServiceCount(affectedStudents.size());
		return result;
	}

	private String buildScoreImportResultMessage(ScoreImportResult result, ScoreImportPreview preview, String strategy){
		StringBuilder builder = new StringBuilder();
		builder.append("导入完成：识别").append(preview.getTotalCount()).append("项，新增")
			.append(result.getInsertedDetails()).append("项，补充空缺")
			.append(result.getFilledBlankDetails()).append("项，覆盖更新")
			.append(result.getOverwrittenDetails()).append("项，跳过冲突")
			.append(result.getSkippedConflicts()).append("项，跳过重复")
			.append(result.getSkippedDuplicates()).append("项，异常")
			.append(result.getSkippedInvalid()).append("项，重算")
			.append(result.getRecalculatedStudents()).append("名学生总评。");
		if(STRATEGY_FILL_BLANK.equals(strategy) && result.getSkippedConflicts().intValue() > 0){
			builder.append(" 当前策略为仅补充空缺项，已有成绩未覆盖。");
		}
		return builder.toString();
	}

	private MultipartFile getScoreUploadFile(HttpServletRequest request) throws Exception{
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
		if(!isNotBlank(fileName) || fileName.lastIndexOf(".") < 0){
			throw new Exception("文件名不正确");
		}
		String filetype = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		if(!"xls".equals(filetype) && !"xlsx".equals(filetype)){
			throw new Exception("只支持上传xls或xlsx文件");
		}
		return file;
	}

	private String saveScoreImportFile(MultipartFile file) throws Exception{
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

	private String normalizeConflictStrategy(String strategy){
		if(STRATEGY_OVERWRITE.equals(strategy)){
			return STRATEGY_OVERWRITE;
		}
		if(STRATEGY_SKIP.equals(strategy)){
			return STRATEGY_SKIP;
		}
		return STRATEGY_FILL_BLANK;
	}

	private ExistingScoreIndex buildExistingScoreIndex(String lessonid, Integer teacherid){
		ExistingScoreIndex index = new ExistingScoreIndex();
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("lessonid", lessonid);
		query.put("teacherid", teacherid);
		List<ScoreDetail> details = scoreDetailService.getList(query);
		for(ScoreDetail detail : details){
			String key = detailKey(detail.getUserid(), detail.getComponentName());
			index.detailMap.put(key, detail);
			Integer count = index.userDetailCount.get(detail.getUserid());
			index.userDetailCount.put(detail.getUserid(), Integer.valueOf(count == null ? 1 : count.intValue() + 1));
		}
		return index;
	}

	private ScoreDetail getUserComponentDetail(String lessonid, Integer teacherid, Integer userid, String componentName){
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("lessonid", lessonid);
		query.put("teacherid", teacherid);
		query.put("userid", userid);
		query.put("componentName", componentName);
		List<ScoreDetail> details = scoreDetailService.getList(query);
		return details.size() > 0 ? details.get(0) : null;
	}

	private String detailKey(Integer userid, String componentName){
		return userid + "|" + ScoreComputeUtil.normalize(componentName);
	}

	private ScoreImportItem copyScoreImportItem(ScoreImportItem source){
		ScoreImportItem item = new ScoreImportItem();
		if(source == null){
			return item;
		}
		item.setRowNumber(source.getRowNumber());
		item.setNo(source.getNo());
		item.setName(source.getName());
		item.setUserid(source.getUserid());
		item.setSystemName(source.getSystemName());
		item.setDeptName(source.getDeptName());
		item.setComponentName(source.getComponentName());
		item.setScore(source.getScore());
		item.setRawScore(source.getRawScore());
		item.setExistingScore(source.getExistingScore());
		item.setAction(source.getAction());
		item.setValid(source.getValid());
		item.setConflict(source.getConflict());
		item.setDuplicate(source.getDuplicate());
		item.setMessage(source.getMessage());
		item.setScoreSource(source.getScoreSource());
		item.setTotalScoreSource(source.getTotalScoreSource());
		item.setConfidence(source.getConfidence());
		return item;
	}

	private String matchCourseComponentName(String incomingName, List<AnalysisComponent> components){
		if(components == null || components.size() == 0){
			return "";
		}
		String incoming = normalizeScoreName(incomingName);
		if(!isNotBlank(incoming)){
			return components.size() == 1 ? components.get(0).getComponentName() : "";
		}
		String bestName = "";
		int bestScore = 0;
		for(AnalysisComponent component : components){
			String componentName = component.getComponentName();
			String normalizedComponent = normalizeScoreName(componentName);
			int score = matchComponentScore(incoming, normalizedComponent, componentName);
			if(score > bestScore){
				bestScore = score;
				bestName = componentName;
			}
		}
		return bestScore >= 45 ? bestName : "";
	}

	private int matchComponentScore(String incoming, String component, String componentName){
		if(!isNotBlank(incoming) || !isNotBlank(component)){
			return 0;
		}
		if(incoming.equals(component)){
			return 100;
		}
		if(incoming.indexOf(component) >= 0 || component.indexOf(incoming) >= 0){
			return 80;
		}
		if(isFinalScoreName(incoming) && isFinalScoreName(componentName)){
			return 88;
		}
		if(isMiddleScoreName(incoming) && isMiddleScoreName(componentName)){
			return 88;
		}
		if(isHomeworkScoreName(incoming) && isHomeworkScoreName(componentName)){
			return 88;
		}
		if(isExperimentScoreName(incoming) && isExperimentScoreName(componentName)){
			return 88;
		}
		if(isUsualScoreName(incoming) && isUsualScoreName(componentName)){
			return 88;
		}
		if(isTotalScoreName(incoming) && isTotalScoreName(componentName)){
			return 88;
		}
		return 0;
	}

	private String normalizeScoreName(String value){
		if(value == null){
			return "";
		}
		return value.replace("成绩", "").replace("考核", "").replace("方式", "").replace("得分", "")
			.replace("分数", "").replace("原始分", "").replace("折算分", "")
			.replace("：", "").replace(":", "").replace(" ", "").trim().toLowerCase();
	}

	private boolean isUsualScoreName(String value){
		String text = normalizeScoreName(value);
		return text.indexOf("平时") >= 0 || text.indexOf("过程") >= 0 || text.indexOf("课堂") >= 0 || text.indexOf("表现") >= 0;
	}

	private boolean isHomeworkScoreName(String value){
		String text = normalizeScoreName(value);
		return text.indexOf("作业") >= 0 || text.indexOf("homework") >= 0;
	}

	private boolean isExperimentScoreName(String value){
		String text = normalizeScoreName(value);
		return text.indexOf("实验") >= 0 || text.indexOf("实训") >= 0 || text.indexOf("实践") >= 0 || text.indexOf("lab") >= 0;
	}

	private boolean isMiddleScoreName(String value){
		String text = normalizeScoreName(value);
		return text.indexOf("期中") >= 0 || text.indexOf("mid") >= 0;
	}

	private boolean isFinalScoreName(String value){
		String text = normalizeScoreName(value);
		return text.indexOf("期末") >= 0 || text.indexOf("final") >= 0 || text.indexOf("终考") >= 0;
	}

	private boolean isTotalScoreName(String value){
		String text = normalizeScoreName(value);
		return text.indexOf("总评") >= 0 || text.indexOf("总成绩") >= 0 || text.indexOf("总分") >= 0 || text.indexOf("最终") >= 0 || text.indexOf("综合") >= 0 || text.indexOf("total") >= 0;
	}

	private boolean isMissingDetail(ScoreDetail detail){
		if(detail == null){
			return true;
		}
		return detail.getScore() == null || STATUS_MISSING.equals(detail.getSourceStatus());
	}

	private int compareScore(BigDecimal left, BigDecimal right){
		BigDecimal l = left == null ? BigDecimal.ZERO : left.setScale(2, BigDecimal.ROUND_HALF_UP);
		BigDecimal r = right == null ? BigDecimal.ZERO : right.setScale(2, BigDecimal.ROUND_HALF_UP);
		return l.compareTo(r);
	}

	private boolean hasManualConfirmRisk(ScoreImportItem item){
		String text = safeText(item.getMessage()) + safeText(item.getScoreSource()) + safeText(item.getTotalScoreSource()) + safeText(item.getRawScore());
		return text.indexOf("缺考") >= 0 || text.indexOf("作弊") >= 0 || text.indexOf("缓考") >= 0
			|| text.indexOf("补考") >= 0 || text.indexOf("免考") >= 0 || text.indexOf("旷考") >= 0
			|| text.indexOf("加权") >= 0 || text.indexOf("折算") >= 0 || text.indexOf("人工确认") >= 0
			|| text.indexOf("不一致") >= 0 || text.indexOf("歧义") >= 0;
	}

	private String normalizeImportNo(String value){
		String text = value == null ? "" : value.trim();
		if(text.endsWith(".0")){
			text = text.substring(0, text.length() - 2);
		}
		if(text.endsWith(".00")){
			text = text.substring(0, text.length() - 3);
		}
		return text.replace(" ", "");
	}

	private String normalizeImportText(String value){
		if(value == null){
			return "";
		}
		return value.replace("\r", " ").replace("\n", " ").replace("\t", " ").replaceAll("\\s+", " ").trim();
	}

	private String appendMessage(String message, String append){
		if(!isNotBlank(append)){
			return message;
		}
		if(!isNotBlank(message)){
			return append;
		}
		return message + "；" + append;
	}

	private String safeText(String value){
		return value == null ? "" : value;
	}

	private void cleanupScorePreviewCache(){
		long now = System.currentTimeMillis();
		Iterator<Map.Entry<String, ScorePreviewCache>> iterator = SCORE_PREVIEW_CACHE.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String, ScorePreviewCache> entry = iterator.next();
			if(entry.getValue() == null || now - entry.getValue().createTime > PREVIEW_EXPIRE_MILLIS){
				iterator.remove();
			}
		}
	}

	private void deleteQuietly(File file){
		if(file != null && file.exists()){
			try{
				file.delete();
			}catch(Exception e){
			}
		}
	}

	private Lesson getTeacherOwnedLesson(String lessonid, Integer teacherid){
		if(!isNotBlank(lessonid) || teacherid == null){
			return null;
		}
		Lesson lesson = lessonService.getById(lessonid);
		if(lesson == null || lesson.getUserid() == null || !teacherid.equals(lesson.getUserid())){
			return null;
		}
		return lesson;
	}

	private boolean isBoundStudent(String lessonid, Integer teacherid, Integer userid){
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("lessonid", lessonid);
		query.put("teacherid", teacherid);
		query.put("userid", userid);
		return lessonStudentService.getList(query).size() > 0;
	}

	private BigDecimal parseScoreValue(String value){
		if(value == null || "".equals(value.trim())){
			return null;
		}
		try{
			BigDecimal decimal = new BigDecimal(value.replace("%", "").replace("％", "").trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
			if(decimal.compareTo(BigDecimal.ZERO) < 0 || decimal.compareTo(new BigDecimal("100")) > 0){
				return null;
			}
			return decimal;
		}catch(Exception e){
			return null;
		}
	}

	@RequestMapping(value="updatescore")
	@Transactional(rollbackFor = { Exception.class })
	public void updatescore(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		try{
			String lessonid = request.getParameter("lessonid");
			String userid = request.getParameter("userid");
			String componentName = request.getParameter("componentName");
			String scoreValue = request.getParameter("score");
			if(!isNotBlank(lessonid) || !isNotBlank(userid) || !isNotBlank(componentName)){
				ajax(response, "参数错误");
				return;
			}
			if(getTeacherOwnedLesson(lessonid, getSessionUser(request).getId()) == null){
				ajax(response, "课程不存在或无权操作");
				return;
			}
			if(!isBoundStudent(lessonid, getSessionUser(request).getId(), Integer.valueOf(userid))){
				ajax(response, "学生未绑定当前课程，不能修改成绩");
				return;
			}
			List<AnalysisComponent> components = getComponents(lessonid, getSessionUser(request).getId());
			componentName = matchCourseComponentName(componentName, components);
			if(!isNotBlank(componentName)){
				ajax(response, "成绩项不存在于当前课程成绩结构中");
				return;
			}
			BigDecimal scoreDecimal = parseScoreValue(scoreValue);
			if(scoreDecimal == null){
				ajax(response, "成绩必须是0到100之间的数字");
				return;
			}
			int sortno = findComponentSort(components, componentName);
			upsertScoreDetail(lessonid, getSessionUser(request).getId(), Integer.valueOf(userid), componentName, scoreDecimal, sortno, STATUS_IMPORTED);
			Map<String,BigDecimal> scoreMap = getUserScoreMap(lessonid, Integer.valueOf(userid), getSessionUser(request).getId());
			syncLegacyScore(lessonid, getSessionUser(request).getId(), Integer.valueOf(userid), components, scoreMap);
			String status = scoreMap.size() < components.size() ? STATUS_MISSING : STATUS_IMPORTED;
			Sumscore sumscore = saveSumscore(lessonid, getSessionUser(request).getId(), Integer.valueOf(userid), components, scoreMap, status);
			statisticsService.incrementGradeServiceCount(1);
			JSONObject result = new JSONObject();
			result.put("success", true);
			result.put("sumscore", sumscore.getSumscore());
			result.put("remarks", sumscore.getRemarks());
			ajax(response, result.toString());
		}catch(Exception e){
			JSONObject result = new JSONObject();
			result.put("success", false);
			result.put("message", e.getMessage());
			try{
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			}catch(Exception tx){
			}
			ajax(response, result.toString());
		}
	}

	@RequestMapping(value="createsumscore")
	public void createsumscore(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		String lessonid = request.getParameter("lessonid");
		List<Lesson> lessons = getTeacherLessonList(request);
		if(isNotBlank(lessonid)){
			if(getTeacherOwnedLesson(lessonid, getSessionUser(request).getId()) == null){
				ajax(response, "课程不存在或无权操作");
				return;
			}
			recalculateLesson(lessonid, getSessionUser(request).getId());
		}else{
			for(Lesson lesson : lessons){
				recalculateLesson(lesson.getId()+"", getSessionUser(request).getId());
			}
		}
		ajax(response, "总评成绩生成成功");
	}

	@RequestMapping(value="selfevalution")
	public ModelAndView selfevalution(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return null;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		List<Lesson> lessonlist = getTeacherLessonList(request);
		String lessonid = getCurrentLessonId(request, lessonlist);
		map.put("lessonlist", lessonlist);
		map.put("lessonid", lessonid);
		map.put("tablehead", getSelfEvaluationTablehead(lessonid, getSessionUser(request).getId()));
		if(!isNotBlank(lessonid)){
			map.put("msg", "请先维护课程信息");
		}else if(getComponents(lessonid, getSessionUser(request).getId()).size() == 0){
			map.put("msg", "请先在融合数据分析页面保存考核标准");
		}
		return jsp("selfevalution", map, request);
	}

	@RequestMapping(value="selfevalutionlistdata")
	public void selfevalutionlistdata(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		String lessonid = getCurrentLessonId(request, getTeacherLessonList(request));
		List<Score> rows = buildEvaluationRows(lessonid, getSessionUser(request).getId());
		writeRows(response, rows);
	}

	@RequestMapping(value="charts")
	public ModelAndView charts(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return null;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		List<Lesson> lessonlist = getTeacherLessonList(request);
		String lessonid = getCurrentLessonId(request, lessonlist);
		map.put("lessonlist", lessonlist);
		map.put("lessonid", lessonid);
		map.put("targetlist", new ArrayList<AnalysisTarget>());
		map.put("targetid", "");
		map.put("scatterData", "[]");
		map.put("scatterMap", "{}");
		map.put("targetRateMap", "{}");
		map.put("targetNameMap", "{}");
		map.put("radarIndicators", "[]");
		map.put("radarData", "[]");
		map.put("barNames", "[]");
		map.put("barData", "[]");
		map.put("targetrate", "0");
		if(!isNotBlank(lessonid)){
			map.put("msg", "请先维护课程信息");
			return jsp("charts", map, request);
		}
		List<AnalysisComponent> chartComponents = getComponents(lessonid, getSessionUser(request).getId());
		List<AnalysisTarget> targets = getTargets(lessonid, getSessionUser(request).getId(), chartComponents);
		map.put("targetlist", targets);
		if(targets.size() == 0){
			map.put("msg", "请先在融合数据分析页面保存课程目标考核比例");
			return jsp("charts", map, request);
		}
		String targetid = request.getParameter("targetid");
		AnalysisTarget selected = targets.get(0);
		if(isNotBlank(targetid)){
			for(AnalysisTarget target : targets){
				if(target.getId().toString().equals(targetid)){
					selected = target;
					break;
				}
			}
		}
		map.put("targetid", selected.getId());
		map.put("targetrate", selected.getTargetrate() == null ? "0" : selected.getTargetrate());

		List<Score> rows = buildEvaluationRows(lessonid, getSessionUser(request).getId());
		Score summary = buildEvaluationSummary(lessonid, getSessionUser(request).getId(), rows);
		JSONArray scatterData = new JSONArray();
		JSONObject scatterMap = new JSONObject();
		JSONObject targetRateMap = new JSONObject();
		JSONObject targetNameMap = new JSONObject();
		JSONArray barNames = new JSONArray();
		JSONArray barData = new JSONArray();
		JSONArray radarIndicators = new JSONArray();
		JSONArray radarData = new JSONArray();
		if(summary != null){
			BigDecimal maxAchievement = new BigDecimal("1");
			BigDecimal[] achievements = new BigDecimal[targets.size()];
			for(int i=0; i<targets.size(); i++){
				achievements[i] = toDecimal(summary.getDatamap().get("targetAchievement"+i)+"");
				if(achievements[i].compareTo(maxAchievement) > 0){
					maxAchievement = achievements[i];
				}
			}
			for(int i=0; i<targets.size(); i++){
				AnalysisTarget target = targets.get(i);
				BigDecimal achievement = achievements[i];
				barNames.add(target.getTargetName());
				barData.add(achievement);
				targetRateMap.put(target.getId().toString(), target.getTargetrate() == null ? "0" : target.getTargetrate());
				targetNameMap.put(target.getId().toString(), target.getTargetName());
				JSONObject indicator = new JSONObject();
				indicator.put("name", target.getTargetName());
				indicator.put("max", maxAchievement.setScale(2, BigDecimal.ROUND_UP));
				radarIndicators.add(indicator);
				radarData.add(achievement);
			}
			int selectedIndex = 0;
			for(int i=0; i<targets.size(); i++){
				if(targets.get(i).getId().equals(selected.getId())){
					selectedIndex = i;
					break;
				}
			}
			for(int i=0; i<targets.size(); i++){
				JSONArray targetScatterData = new JSONArray();
				for(int j=0; j<rows.size(); j++){
					BigDecimal achievement = toDecimal(rows.get(j).getDatamap().get("targetAchievement"+i)+"");
					JSONArray point = new JSONArray();
					point.add(j + 1);
					point.add(achievement);
					targetScatterData.add(point);
				}
				scatterMap.put(targets.get(i).getId().toString(), targetScatterData);
			}
			for(int i=0; i<rows.size(); i++){
				BigDecimal achievement = toDecimal(rows.get(i).getDatamap().get("targetAchievement"+selectedIndex)+"");
				JSONArray point = new JSONArray();
				point.add(i + 1);
				point.add(achievement);
				scatterData.add(point);
			}
			if(!scatterMap.containsKey(selected.getId().toString())){
				scatterMap.put(selected.getId().toString(), scatterData);
			}
		}
		map.put("scatterData", scatterData.toString());
		map.put("scatterMap", scatterMap.toString());
		map.put("targetRateMap", targetRateMap.toString());
		map.put("targetNameMap", targetNameMap.toString());
		map.put("radarIndicators", radarIndicators.toString());
		map.put("radarData", radarData.toString());
		map.put("barNames", barNames.toString());
		map.put("barData", barData.toString());
		return jsp("charts", map, request);
	}

	@RequestMapping(value="ownlist")
	public ModelAndView ownlist(HttpServletRequest request, HttpServletResponse response){
		if(!requireLogin(request, response)){
			return null;
		}
		return jsp("scoreown", new HashMap<String,Object>(), request);
	}

	@RequestMapping(value="ownlistdata")
	public void ownlistdata(HttpServletRequest request, HttpServletResponse response){
		if(!requireLogin(request, response)){
			return;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", getSessionUser(request).getId());
		List<Sumscore> list = buildOwnSumscoreList(getSessionUser(request).getId());
		JSONArray jsonarray = JSONArray.fromObject(list);
		ajax(response, "{\"data\":"+jsonarray.toString()+"}");
	}

	@RequestMapping(value="sumlist")
	public ModelAndView sumlist(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return null;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("deptId", getSessionUser(request).getDeptId());
		map.put("userlist", userService.getList(map));
		return jsp("scoresum", map, request);
	}

	@RequestMapping(value="sumlistdata")
	public void sumlistdata(HttpServletRequest request, HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", request.getParameter("queryname"));
		map.put("teacherid", getSessionUser(request).getId());
		List<Sumscore> list = sumscoreService.getList(map);
		for(Sumscore sumscore : list){
			sumscore.setUser(userService.getUserById(sumscore.getUserid()+""));
		}
		JSONArray jsonarray = JSONArray.fromObject(list);
		ajax(response, "{\"data\":"+jsonarray.toString()+"}");
	}

	@RequestMapping(value="add")
	@Transactional(rollbackFor = { Exception.class })
	public void add(HttpServletRequest request, HttpServletResponse response, Score score){
		if(!requireTeacherRole(request, response)){
			return;
		}
		if(score == null || !isNotBlank(score.getLesson()) || getTeacherOwnedLesson(score.getLesson(), getSessionUser(request).getId()) == null){
			ajax(response, "课程不存在或无权操作");
			return;
		}
		score.setTeacherid(getSessionUser(request).getId());
		scoreService.insert(score);
		if(score.getUserid() != null){
			statisticsService.incrementGradeServiceCount(1);
		}
		ajax(response, "新增成功");
	}

	@RequestMapping(value="update")
	@Transactional(rollbackFor = { Exception.class })
	public void update(HttpServletRequest request, HttpServletResponse response, Score score){
		if(!requireTeacherRole(request, response)){
			return;
		}
		if(score == null || !isNotBlank(score.getLesson()) || getTeacherOwnedLesson(score.getLesson(), getSessionUser(request).getId()) == null){
			ajax(response, "课程不存在或无权操作");
			return;
		}
		score.setTeacherid(getSessionUser(request).getId());
		scoreService.update(score);
		if(score.getUserid() != null){
			statisticsService.incrementGradeServiceCount(1);
		}
		ajax(response, "修改成功");
	}

	@RequestMapping(value="del")
	public void del(HttpServletRequest request,HttpServletResponse response){
		if(!requireTeacherRole(request, response)){
			return;
		}
		Score score = scoreService.getScoreById(request.getParameter("id"));
		if(score == null || score.getTeacherid() == null || !score.getTeacherid().equals(getSessionUser(request).getId())){
			ajax(response, "成绩不存在或无权操作");
			return;
		}
		scoreService.delete(request.getParameter("id"));
		ajax(response, "删除成功");
	}

	private List<Score> buildScoreRows(String lessonid, String queryUserId, Integer teacherid){
		List<AnalysisComponent> components = getComponents(lessonid, teacherid);
		List<User> students = getBoundStudents(lessonid, teacherid, queryUserId);
		Lesson lesson = lessonService.getById(lessonid);
		List<Score> rows = new ArrayList<Score>();
			for(User student : students){
				Map<String,BigDecimal> scoreMap = getUserScoreMap(lessonid, student.getId(), teacherid);
				String status = (isMissingScore(lessonid, student.getId(), teacherid) || scoreMap.size() < components.size()) ? STATUS_MISSING : STATUS_IMPORTED;
				Score score = new Score();
				score.setUserid(student.getId());
				score.setTeacherid(teacherid);
			score.setLesson(lessonid);
			score.setUser(student);
				score.setLessonentity(lesson);
				Map<String,Object> datamap = new HashMap<String,Object>();
				for(int i=0; i<components.size(); i++){
					BigDecimal componentScore = ScoreComputeUtil.getScore(scoreMap, components.get(i).getComponentName());
					datamap.put("c"+i, ScoreComputeUtil.format(componentScore));
					datamap.put("zero"+i, componentScore.compareTo(BigDecimal.ZERO) == 0 ? "1" : "0");
				}
				datamap.put("rowStatus", STATUS_MISSING.equals(status) ? STATUS_MISSING : STATUS_IMPORTED);
				datamap.put("warning", "");
				score.setDatamap(datamap);
				Sumscore sumscore = getUserSumscore(lessonid, student.getId(), teacherid);
				if(sumscore == null){
					sumscore = buildSumscore(lessonid, student.getId(), teacherid, components, scoreMap, status);
				}else{
					refreshSumscore(sumscore, components, scoreMap, status);
				}
				score.setSumscore(sumscore);
				rows.add(score);
		}
		return rows;
	}

	private List<Score> buildEvaluationRows(String lessonid, Integer teacherid){
		List<Score> rows = new ArrayList<Score>();
		if(!isNotBlank(lessonid)){
			return rows;
		}
		List<AnalysisComponent> components = getComponents(lessonid, teacherid);
		List<AnalysisTarget> targets = getTargets(lessonid, teacherid, components);
		List<User> students = getBoundStudents(lessonid, teacherid, null);
		if(components.size() == 0 || targets.size() == 0){
			return rows;
		}
		Lesson lesson = lessonService.getById(lessonid);
		for(User student : students){
			Map<String,BigDecimal> scoreMap = getUserScoreMap(lessonid, student.getId(), teacherid);
			Score row = new Score();
			row.setUserid(student.getId());
			row.setTeacherid(teacherid);
			row.setLesson(lessonid);
			row.setUser(student);
			row.setLessonentity(lesson);
			Map<String,Object> datamap = new HashMap<String,Object>();
			for(int i=0; i<components.size(); i++){
				datamap.put("c"+i, ScoreComputeUtil.format(ScoreComputeUtil.getScore(scoreMap, components.get(i).getComponentName())));
			}
			for(int i=0; i<targets.size(); i++){
				BigDecimal targetScore = ScoreComputeUtil.calculateTargetScore(targets.get(i), scoreMap);
				BigDecimal targetAchievement = ScoreComputeUtil.calculateAchievement(targets.get(i), scoreMap);
				datamap.put("targetScore"+i, ScoreComputeUtil.format(targetScore));
				datamap.put("targetAchievement"+i, ScoreComputeUtil.format(targetAchievement));
			}
			row.setDatamap(datamap);
				Sumscore sumscore = getUserSumscore(lessonid, student.getId(), teacherid);
				String status = (isMissingScore(lessonid, student.getId(), teacherid) || scoreMap.size() < components.size()) ? STATUS_MISSING : STATUS_IMPORTED;
				if(sumscore == null){
					sumscore = buildSumscore(lessonid, student.getId(), teacherid, components, scoreMap, status);
				}else{
					refreshSumscore(sumscore, components, scoreMap, status);
				}
			row.setSumscore(sumscore);
			rows.add(row);
		}
		return rows;
	}

	private Score buildEvaluationSummary(String lessonid, Integer teacherid, List<Score> rows){
		if(!isNotBlank(lessonid)){
			return null;
		}
		List<AnalysisComponent> components = getComponents(lessonid, teacherid);
		List<AnalysisTarget> targets = getTargets(lessonid, teacherid, components);
		if(components.size() == 0 || targets.size() == 0){
			return null;
		}
		Score summary = new Score();
		User summaryUser = new User();
		summaryUser.setNo("-");
		summaryUser.setName("总体平均");
		summaryUser.setDeptName("-");
		summary.setUser(summaryUser);
		summary.setLesson(lessonid);
		summary.setLessonentity(lessonService.getById(lessonid));
		summary.setDatamap(new HashMap<String,Object>());
		Sumscore summarySum = new Sumscore();
		summarySum.setSumscore("0.00");
		summary.setSumscore(summarySum);

		BigDecimal totalSum = BigDecimal.ZERO;
		BigDecimal[] targetScoreSums = new BigDecimal[targets.size()];
		BigDecimal[] targetAchieveSums = new BigDecimal[targets.size()];
		for(int i=0; i<targets.size(); i++){
			targetScoreSums[i] = BigDecimal.ZERO;
			targetAchieveSums[i] = BigDecimal.ZERO;
		}
		if(rows != null){
			for(Score row : rows){
				if(row == null){
					continue;
				}
				if(row.getSumscore() != null){
					totalSum = totalSum.add(toDecimal(row.getSumscore().getSumscore()));
				}
				for(int i=0; i<targets.size(); i++){
					targetScoreSums[i] = targetScoreSums[i].add(toDecimal(row.getDatamap().get("targetScore"+i)+""));
					targetAchieveSums[i] = targetAchieveSums[i].add(toDecimal(row.getDatamap().get("targetAchievement"+i)+""));
				}
			}
		}
		int count = rows == null ? 0 : rows.size();
		if(count > 0){
			summary.getSumscore().setSumscore(ScoreComputeUtil.format(totalSum.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP)));
			for(int i=0; i<targets.size(); i++){
				summary.getDatamap().put("targetScore"+i, ScoreComputeUtil.format(targetScoreSums[i].divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP)));
				summary.getDatamap().put("targetAchievement"+i, ScoreComputeUtil.format(targetAchieveSums[i].divide(new BigDecimal(count), 4, BigDecimal.ROUND_HALF_UP)));
			}
		}else{
			for(int i=0; i<targets.size(); i++){
				summary.getDatamap().put("targetScore"+i, "0.00");
				summary.getDatamap().put("targetAchievement"+i, "0.00");
			}
		}
		return summary;
	}

	private String getSelfEvaluationTablehead(String lessonid, Integer teacherid){
		List<AnalysisComponent> components = getComponents(lessonid, teacherid);
		List<AnalysisTarget> targets = getTargets(lessonid, teacherid, components);
		String tablehead =
			"{title:'序号',width:'50px',align:'center',formatter:function(value,row,index){return index+1;}},"
			+ "{field:'user.no',title:'学号',sortable:true},"
			+ "{field:'user.name',title:'姓名',sortable:true},"
			+ "{field:'user.deptName',title:'班级',sortable:true},"
			+ "{field:'lessonentity.name',title:'课程',sortable:true}";
		for(int i=0; i<components.size(); i++){
			tablehead += ",{field:'datamap.c"+i+"',title:'"+jsEscape(components.get(i).getComponentName())+"<br>("+components.get(i).getRate()+"%)',sortable:true}";
		}
		for(int i=0; i<targets.size(); i++){
			tablehead += ",{field:'datamap.targetScore"+i+"',title:'"+jsEscape(targets.get(i).getTargetName())+"<br>得分',sortable:true}";
			tablehead += ",{field:'datamap.targetAchievement"+i+"',title:'"+jsEscape(targets.get(i).getTargetName())+"<br>达成度',sortable:true}";
		}
		tablehead += ",{field:'sumscore.sumscore',title:'总评',sortable:true}";
		return tablehead;
	}

	private void recalculateLesson(String lessonid, Integer teacherid){
		List<AnalysisComponent> components = getComponents(lessonid, teacherid);
		if(components.size() == 0){
			return;
		}
		for(User student : getBoundStudents(lessonid, teacherid, null)){
			Map<String,BigDecimal> scoreMap = getUserScoreMap(lessonid, student.getId(), teacherid);
			String status = (isMissingScore(lessonid, student.getId(), teacherid) || scoreMap.size() < components.size()) ? STATUS_MISSING : STATUS_IMPORTED;
			saveSumscore(lessonid, teacherid, student.getId(), components, scoreMap, status);
		}
	}

	private List<Lesson> getTeacherLessonList(HttpServletRequest request){
		User user = getSessionUser(request);
		if(user == null){
			return new ArrayList<Lesson>();
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", user.getId());
		return lessonService.getList(map);
	}

	private String getCurrentLessonId(HttpServletRequest request, List<Lesson> lessonlist){
		String lessonid = request.getParameter("lessonid");
		if(!isNotBlank(lessonid)){
			lessonid = request.getParameter("querylessonid");
		}
		if(!isNotBlank(lessonid) && lessonlist != null && lessonlist.size() > 0){
			lessonid = lessonlist.get(0).getId()+"";
		}
		return lessonid;
	}

	private List<AnalysisComponent> getComponents(String lessonid, Integer teacherid){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("lessonid", lessonid);
		map.put("teacherid", teacherid);
		return analysisComponentService.getList(map);
	}

	private List<AnalysisTarget> getTargets(String lessonid, Integer teacherid){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("lessonid", lessonid);
		map.put("teacherid", teacherid);
		List<AnalysisTarget> targets = analysisTargetService.getList(map);
		for(AnalysisTarget target : targets){
			Map<String,Object> itemMap = new HashMap<String,Object>();
			itemMap.put("targetid", target.getId());
			target.setItemlist(analysisTargetItemService.getList(itemMap));
		}
		return targets;
	}

	private List<AnalysisTarget> getTargets(String lessonid, Integer teacherid, List<AnalysisComponent> components){
		List<AnalysisTarget> targets = getTargets(lessonid, teacherid);
		if(components == null || components.size() == 0){
			return targets;
		}
		for(AnalysisTarget target : targets){
			if(target.getItemlist() == null){
				continue;
			}
			for(AnalysisTargetItem item : target.getItemlist()){
				String matchedName = findComponentName(components, item.getMethodName());
				if(isNotBlank(matchedName)){
					item.setMethodName(matchedName);
				}
			}
		}
		return targets;
	}

	private String findComponentName(List<AnalysisComponent> components, String methodName){
		String method = ScoreComputeUtil.normalize(methodName);
		if(!isNotBlank(method)){
			return methodName;
		}
		for(AnalysisComponent component : components){
			String componentName = ScoreComputeUtil.normalize(component.getComponentName());
			if(!isNotBlank(componentName)){
				continue;
			}
			if(componentName.equals(method) || componentName.indexOf(method) >= 0 || method.indexOf(componentName) >= 0){
				return component.getComponentName();
			}
		}
		return methodName;
	}

	private List<User> getBoundStudents(String lessonid, Integer teacherid, String queryUserId){
		List<User> students = new ArrayList<User>();
		if(!isNotBlank(lessonid)){
			return students;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("lessonid", lessonid);
		map.put("teacherid", teacherid);
		List<LessonStudent> list = lessonStudentService.getList(map);
		for(LessonStudent lessonStudent : list){
			if(isNotBlank(queryUserId) && !queryUserId.equals(lessonStudent.getUserid()+"")){
				continue;
			}
			User user = userService.getUserById(lessonStudent.getUserid()+"");
			if(user != null){
				students.add(user);
			}
		}
		return students;
	}

	private Map<String,BigDecimal> getUserScoreMap(String lessonid, Integer userid, Integer teacherid){
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("lessonid", lessonid);
		query.put("teacherid", teacherid);
		query.put("userid", userid);
		List<ScoreDetail> details = scoreDetailService.getList(query);
		Map<String,BigDecimal> scoreMap = new LinkedHashMap<String,BigDecimal>();
		for(ScoreDetail detail : details){
			scoreMap.put(detail.getComponentName(), detail.getScore() == null ? BigDecimal.ZERO : detail.getScore());
		}
		return scoreMap;
	}

	private boolean isMissingScore(String lessonid, Integer userid, Integer teacherid){
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("lessonid", lessonid);
		query.put("teacherid", teacherid);
		query.put("userid", userid);
		List<ScoreDetail> details = scoreDetailService.getList(query);
		if(details.size() == 0){
			return true;
		}
		for(ScoreDetail detail : details){
			if(STATUS_MISSING.equals(detail.getSourceStatus())){
				return true;
			}
		}
		return false;
	}

	private void insertScoreDetail(String lessonid, Integer teacherid, Integer userid, String componentName, BigDecimal score, int sortno, String sourceStatus){
		ScoreDetail detail = new ScoreDetail();
		detail.setLessonid(Integer.valueOf(lessonid));
		detail.setTeacherid(teacherid);
		detail.setUserid(userid);
		detail.setComponentName(componentName);
		detail.setScore(score);
		detail.setSortno(sortno);
		detail.setSourceStatus(sourceStatus);
		detail.setCreatedate(DateUtil.formatHMS(new java.util.Date()));
		scoreDetailService.insert(detail);
	}

	private void upsertScoreDetail(String lessonid, Integer teacherid, Integer userid, String componentName, BigDecimal score, int sortno, String sourceStatus){
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("lessonid", lessonid);
		query.put("teacherid", teacherid);
		query.put("userid", userid);
		query.put("componentName", componentName);
		List<ScoreDetail> details = scoreDetailService.getList(query);
		if(details.size() == 0){
			insertScoreDetail(lessonid, teacherid, userid, componentName, score, sortno, sourceStatus);
		}else{
			ScoreDetail detail = details.get(0);
			detail.setScore(score);
			detail.setSourceStatus(sourceStatus);
			detail.setCreatedate(DateUtil.formatHMS(new java.util.Date()));
			scoreDetailService.updateScore(detail);
		}
	}

	private void syncLegacyScore(String lessonid, Integer teacherid, Integer userid, List<AnalysisComponent> components, Map<String,BigDecimal> scoreMap){
		Map<String,Object> deleteQuery = new HashMap<String,Object>();
		deleteQuery.put("lesson", lessonid);
		deleteQuery.put("teacherid", teacherid);
		deleteQuery.put("userid", userid);
		scoreService.deleteByLesson(deleteQuery);
		Score score = new Score();
		score.setUserid(userid);
		score.setTeacherid(teacherid);
		score.setLesson(lessonid);
		score.setShow(getComponentScore(components, scoreMap, 0));
		score.setHomework(getComponentScore(components, scoreMap, 1));
		score.setTest(getComponentScore(components, scoreMap, 2));
		score.setDesign(getComponentScore(components, scoreMap, 3));
		score.setMiddle(getComponentScore(components, scoreMap, 4));
		score.setEnd(getComponentScore(components, scoreMap, 5));
		score.setCreatedate(DateUtil.formatHMS(new java.util.Date()));
		scoreService.insert(score);
	}

	private Sumscore saveSumscore(String lessonid, Integer teacherid, Integer userid, List<AnalysisComponent> components, Map<String,BigDecimal> scoreMap, String status){
		Sumscore sumscore = buildSumscore(lessonid, userid, teacherid, components, scoreMap, status);
		Map<String,Object> deleteQuery = new HashMap<String,Object>();
		deleteQuery.put("lesson", lessonid);
		deleteQuery.put("teacherid", teacherid);
		deleteQuery.put("userid", userid);
		sumscoreService.deleteByLesson(deleteQuery);
		sumscoreService.insert(sumscore);
		return sumscore;
	}

	private Sumscore buildSumscore(String lessonid, Integer userid, Integer teacherid, List<AnalysisComponent> components, Map<String,BigDecimal> scoreMap, String status){
		BigDecimal total = ScoreComputeUtil.calculateTotalScore(components, scoreMap);
		Sumscore sumscore = new Sumscore();
		sumscore.setUserid(userid);
		sumscore.setTeacherid(teacherid);
		sumscore.setLesson(lessonid);
		sumscore.setSumscore(ScoreComputeUtil.format(total));
		sumscore.setRemarks(buildScoreRemarks(total, components, scoreMap, status));
		return sumscore;
	}

	private void refreshSumscore(Sumscore sumscore, List<AnalysisComponent> components, Map<String,BigDecimal> scoreMap, String status){
		BigDecimal total = ScoreComputeUtil.calculateTotalScore(components, scoreMap);
		sumscore.setSumscore(ScoreComputeUtil.format(total));
		sumscore.setRemarks(buildScoreRemarks(total, components, scoreMap, status));
	}

	private String buildScoreRemarks(BigDecimal total, List<AnalysisComponent> components, Map<String,BigDecimal> scoreMap, String status){
		List<String> messages = new ArrayList<String>();
		if(STATUS_MISSING.equals(status)){
			messages.add("缺失成绩，请检查");
		}
		String zeroNames = getZeroScoreNames(components, scoreMap);
		if(isNotBlank(zeroNames)){
			messages.add("存在 0 分单项：" + zeroNames + "，请检查");
		}
		if(total.compareTo(new BigDecimal("60")) < 0){
			messages.add("不及格");
		}
		return joinMessages(messages);
	}

	private String getZeroScoreNames(List<AnalysisComponent> components, Map<String,BigDecimal> scoreMap){
		if(components == null || components.size() == 0){
			return "";
		}
		List<String> names = new ArrayList<String>();
		for(AnalysisComponent component : components){
			BigDecimal score = ScoreComputeUtil.getScore(scoreMap, component.getComponentName());
			if(score.compareTo(BigDecimal.ZERO) == 0){
				names.add(component.getComponentName());
			}
		}
		return joinNames(names);
	}

	private String joinNames(List<String> names){
		StringBuilder builder = new StringBuilder();
		for(String name : names){
			if(!isNotBlank(name)){
				continue;
			}
			if(builder.length() > 0){
				builder.append("、");
			}
			builder.append(name);
		}
		return builder.toString();
	}

	private String joinMessages(List<String> messages){
		StringBuilder builder = new StringBuilder();
		for(String message : messages){
			if(!isNotBlank(message)){
				continue;
			}
			if(builder.length() > 0){
				builder.append("；");
			}
			builder.append(message);
		}
		return builder.toString();
	}

	private Sumscore getUserSumscore(String lessonid, Integer userid, Integer teacherid){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("lesson", lessonid);
		map.put("teacherid", teacherid);
		map.put("userid", userid);
		List<Sumscore> list = sumscoreService.getList(map);
		return list.size() > 0 ? list.get(0) : null;
	}

	private List<Sumscore> buildOwnSumscoreList(Integer userid){
		List<LessonStudent> activeBindings = getActiveStudentBindings(userid);
		Map<String,LessonStudent> bindingMap = new LinkedHashMap<String,LessonStudent>();
		for(LessonStudent binding : activeBindings){
			bindingMap.put(binding.getTeacherid()+"_"+binding.getLessonid(), binding);
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", userid);
		List<Sumscore> rawList = sumscoreService.getList(map);
		List<Sumscore> list = new ArrayList<Sumscore>();
		Map<String,Sumscore> exists = new LinkedHashMap<String,Sumscore>();
		for(Sumscore sumscore : rawList){
			String lessonid = sumscore.getLesson();
			String key = sumscore.getTeacherid()+"_"+lessonid;
			if(!bindingMap.containsKey(key)){
				continue;
			}
			List<AnalysisComponent> components = getComponents(lessonid, sumscore.getTeacherid());
			if(components.size() > 0){
				Map<String,BigDecimal> scoreMap = getUserScoreMap(lessonid, userid, sumscore.getTeacherid());
				String status = (isMissingScore(lessonid, userid, sumscore.getTeacherid()) || scoreMap.size() < components.size()) ? STATUS_MISSING : STATUS_IMPORTED;
				refreshSumscore(sumscore, components, scoreMap, status);
			}
			fillSumscoreRelation(sumscore);
			if(sumscore.getLessonentity() == null){
				continue;
			}
			list.add(sumscore);
			exists.put(key, sumscore);
		}

		for(LessonStudent binding : activeBindings){
			String lessonid = binding.getLessonid() == null ? "" : binding.getLessonid()+"";
			String key = binding.getTeacherid()+"_"+lessonid;
			if(exists.containsKey(key)){
				continue;
			}
			List<AnalysisComponent> components = getComponents(lessonid, binding.getTeacherid());
			Map<String,BigDecimal> scoreMap = getUserScoreMap(lessonid, userid, binding.getTeacherid());
			Sumscore sumscore;
			if(components.size() == 0){
				sumscore = new Sumscore();
				sumscore.setUserid(userid);
				sumscore.setTeacherid(binding.getTeacherid());
				sumscore.setLesson(lessonid);
				sumscore.setSumscore("0.00");
				sumscore.setRemarks("教师尚未完成融合数据分析或总评计算");
			}else{
				String status = (isMissingScore(lessonid, userid, binding.getTeacherid()) || scoreMap.size() < components.size()) ? STATUS_MISSING : STATUS_IMPORTED;
				sumscore = buildSumscore(lessonid, userid, binding.getTeacherid(), components, scoreMap, status);
			}
			fillSumscoreRelation(sumscore);
			list.add(sumscore);
			exists.put(key, sumscore);
		}
		return list;
	}

	private List<LessonStudent> getActiveStudentBindings(Integer userid){
		List<LessonStudent> active = new ArrayList<LessonStudent>();
		Map<String,Object> bindQuery = new HashMap<String,Object>();
		bindQuery.put("userid", userid);
		List<LessonStudent> bindings = lessonStudentService.getList(bindQuery);
		for(LessonStudent binding : bindings){
			if(binding == null || binding.getTeacherid() == null || binding.getLessonid() == null){
				continue;
			}
			Lesson lesson = lessonService.getById(binding.getLessonid()+"");
			if(lesson == null || lesson.getUserid() == null || !lesson.getUserid().equals(binding.getTeacherid())){
				continue;
			}
			binding.setLesson(lesson);
			active.add(binding);
		}
		return active;
	}

	private void fillSumscoreRelation(Sumscore sumscore){
		if(sumscore == null){
			return;
		}
		sumscore.setUser(userService.getUserById(sumscore.getUserid()+""));
		if(sumscore.getLessonentity() == null && isNotBlank(sumscore.getLesson())){
			sumscore.setLessonentity(lessonService.getById(sumscore.getLesson()));
		}
	}

	private BigDecimal getComponentScore(List<AnalysisComponent> components, Map<String,BigDecimal> scoreMap, int index){
		if(index >= components.size()){
			return BigDecimal.ZERO;
		}
		return ScoreComputeUtil.getScore(scoreMap, components.get(index).getComponentName());
	}

	private int findComponentSort(List<AnalysisComponent> components, String componentName){
		for(int i=0; i<components.size(); i++){
			if(components.get(i).getComponentName().equals(componentName)){
				return i+1;
			}
		}
		return components.size()+1;
	}

	private Map<String,Integer> readHeaderMap(ArrayList<Object> header){
		Map<String,Integer> map = new HashMap<String,Integer>();
		if(header == null){
			return map;
		}
		for(int i=0; i<header.size(); i++){
			String value = getCell(header, i);
			if(isNotBlank(value)){
				map.put(value, i);
			}
		}
		return map;
	}

	private int findHeaderIndex(Map<String,Integer> headerMap, String componentName){
		Integer direct = headerMap.get(componentName);
		if(direct != null){
			return direct.intValue();
		}
		String component = ScoreComputeUtil.normalize(componentName);
		for(String key : headerMap.keySet()){
			String header = ScoreComputeUtil.normalize(key);
			if(header.equals(component) || header.indexOf(component) >= 0 || component.indexOf(header) >= 0){
				return headerMap.get(key).intValue();
			}
		}
		return -1;
	}

	private String getCell(ArrayList<Object> row, int index){
		if(row == null || index < 0 || index >= row.size() || row.get(index) == null){
			return "";
		}
		String value = row.get(index).toString().trim();
		if(value.endsWith(".00")){
			value = value.substring(0, value.length()-3);
		}
		return value;
	}

	private BigDecimal toDecimal(String value){
		if(value == null || "".equals(value.trim())){
			return BigDecimal.ZERO;
		}
		try{
			return new BigDecimal(value.replace("%", "").replace("％", "").trim()).setScale(2, BigDecimal.ROUND_HALF_UP);
		}catch(Exception e){
			return BigDecimal.ZERO;
		}
	}

	private void writeRows(HttpServletResponse response, List<Score> rows){
		JSONArray jsonarray = JSONArray.fromObject(rows);
		ajax(response, "{\"data\":"+jsonarray.toString()+"}");
	}

	private String jsEscape(String value){
		return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'");
	}

	private static class ExistingScoreIndex{
		Map<String,ScoreDetail> detailMap = new HashMap<String,ScoreDetail>();
		Map<Integer,Integer> userDetailCount = new HashMap<Integer,Integer>();
	}

	private static class ScorePreviewCache{
		String lessonid;
		Integer teacherid;
		ScoreImportPreview preview;
		long createTime;

		ScorePreviewCache(String lessonid, Integer teacherid, ScoreImportPreview preview){
			this.lessonid = lessonid;
			this.teacherid = teacherid;
			this.preview = preview;
			this.createTime = System.currentTimeMillis();
		}
	}

	private boolean isNotBlank(String value){
		return value != null && value.trim().length() > 0;
	}

}
