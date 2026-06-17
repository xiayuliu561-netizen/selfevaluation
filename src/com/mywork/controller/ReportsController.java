package com.mywork.controller;


import java.math.BigDecimal;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.mywork.bean.AnalysisComponent;
import com.mywork.bean.AnalysisTarget;
import com.mywork.bean.AnalysisTargetItem;
import com.mywork.bean.Assessrate;
import com.mywork.bean.LessonStudent;
import com.mywork.bean.Question;
import com.mywork.bean.Questionscore;
import com.mywork.bean.Lesson;
import com.mywork.bean.Rate;
import com.mywork.bean.Reports;
import com.mywork.bean.ScoreDetail;
import com.mywork.bean.Sumscore;
import com.mywork.bean.User;
import com.mywork.service.AiModelService;
import com.mywork.service.AnalysisComponentService;
import com.mywork.service.AnalysisTargetItemService;
import com.mywork.service.AnalysisTargetService;
import com.mywork.service.AssessrateService;
import com.mywork.service.LessonService;
import com.mywork.service.LessonStudentService;
import com.mywork.service.QuestionService;
import com.mywork.service.QuestionscoreService;
import com.mywork.service.RateService;
import com.mywork.service.ReportsService;
import com.mywork.service.ScoreDetailService;
import com.mywork.service.SumscoreService;
import com.mywork.service.UserService;
import com.mywork.util.AnalysisJsonUtil;
import com.mywork.util.CommonUtil;
import com.mywork.util.DateUtil;
import com.mywork.util.ScoreComputeUtil;
/**
 * 报告
 * @author 
 *
 */
@Controller
@RequestMapping(value="reports")
public class ReportsController extends BaseController{
	private static final String REPORT_CHARTS_START = "<!--REPORT_AUTO_CHARTS_START-->";
	private static final String REPORT_CHARTS_END = "<!--REPORT_AUTO_CHARTS_END-->";
	private static final int REPORT_AI_MAX_TOKENS = 14000;
	private static final int REPORT_AI_TIMEOUT_MILLIS = 300000;
	private static final int REPORT_EXPAND_MAX_TOKENS = 10000;
	private static final int REPORT_EXPAND_TIMEOUT_MILLIS = 240000;
	private static final int REPORT_MIN_BODY_CHARS = 5000;
	private static long reportPictureId = 1L;

	private static class ClassStat{
		String deptName;
		int count;
		int scoredCount;
		int passCount;
		BigDecimal total = BigDecimal.ZERO;
	}

	@Inject
	private ReportsService reportsService;
	@Inject
	private RateService rateService;
	@Inject
	private AssessrateService assessrateService;
	@Inject
	private SumscoreService sumscoreService;
	@Inject
	private LessonService lessonService;
	@Inject
	private LessonStudentService lessonStudentService;
	@Inject
	private UserService userService;
	@Inject
	private AnalysisComponentService analysisComponentService;
	@Inject
	private AnalysisTargetService analysisTargetService;
	@Inject
	private AnalysisTargetItemService analysisTargetItemService;
	@Inject
	private ScoreDetailService scoreDetailService;
	@Inject
	private QuestionService questionService;
	@Inject
	private QuestionscoreService questionscoreService;
	@Inject
	private AiModelService aiModelService;

	private List<Lesson> getTeacherLessonList(HttpServletRequest request){
		if(getSessionUser(request) == null || !"1".equals(getSessionUser(request).getIsadmin())){
			return new ArrayList<Lesson>();
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", getSessionUser(request).getId());
		return lessonService.getList(map);
	}

	private boolean isReportOperator(HttpServletRequest request){
		User user = getSessionUser(request);
		return user != null && "1".equals(user.getIsadmin());
	}

	private boolean requireReportOperator(HttpServletRequest request, HttpServletResponse response){
		if(isReportOperator(request)){
			return true;
		}
		if(getSessionUser(request) == null){
			ajax(response, "登录已过期，请重新登录");
		}else{
			ajax(response, "无权操作课程质量报告");
		}
		return false;
	}

	private String getCurrentLessonId(HttpServletRequest request, List<Lesson> lessonlist){
		String lessonid = request.getParameter("lessonid");
		if(lessonid == null || "".equals(lessonid)){
			lessonid = request.getParameter("querylessonid");
		}
		if((lessonid == null || "".equals(lessonid)) && lessonlist != null && lessonlist.size() > 0){
			lessonid = lessonlist.get(0).getId()+"";
		}
		return lessonid;
	}

	private Map<String,Object> getDefaultScoreMap(){
		Map<String,Object> scoremap = new HashMap<String, Object>();
		scoremap.put("dy90", 0);
		scoremap.put("c8090", 0);
		scoremap.put("c7080", 0);
		scoremap.put("c6070", 0);
		scoremap.put("xy60", 0);
		scoremap.put("dy90rate", "0.00%");
		scoremap.put("c8090rate", "0.00%");
		scoremap.put("c7080rate", "0.00%");
		scoremap.put("c6070rate", "0.00%");
		scoremap.put("xy60rate", "0.00%");
		return scoremap;
	}

	private Map<String,Object> getScoreMap(List<Sumscore> scorelist){
		Map<String,Object> scoremap = getDefaultScoreMap();
		if(scorelist == null || scorelist.size() == 0){
			return scoremap;
		}
		for(Sumscore score : scorelist){
			double value = Double.parseDouble(score.getSumscore());
			if(value >= 90d){
				scoremap.put("dy90", Integer.parseInt(scoremap.get("dy90")+"")+1);
			}else if(value >= 80d){
				scoremap.put("c8090", Integer.parseInt(scoremap.get("c8090")+"")+1);
			}else if(value >= 70d){
				scoremap.put("c7080", Integer.parseInt(scoremap.get("c7080")+"")+1);
			}else if(value >= 60d){
				scoremap.put("c6070", Integer.parseInt(scoremap.get("c6070")+"")+1);
			}else{
				scoremap.put("xy60", Integer.parseInt(scoremap.get("xy60")+"")+1);
			}
		}
		DecimalFormat df=new DecimalFormat("0.00");
		scoremap.put("dy90rate", df.format(((float)Integer.parseInt(scoremap.get("dy90")+"")/scorelist.size()*100))+"%");
		scoremap.put("c8090rate", df.format(((float)Integer.parseInt(scoremap.get("c8090")+"")/scorelist.size()*100))+"%");
		scoremap.put("c7080rate", df.format(((float)Integer.parseInt(scoremap.get("c7080")+"")/scorelist.size()*100))+"%");
		scoremap.put("c6070rate", df.format(((float)Integer.parseInt(scoremap.get("c6070")+"")/scorelist.size()*100))+"%");
		scoremap.put("xy60rate", df.format(((float)Integer.parseInt(scoremap.get("xy60")+"")/scorelist.size()*100))+"%");
		return scoremap;
	}

	private Rate getEmptyRate(){
		Rate rate = new Rate();
		rate.setShowrate(0);
		rate.setHomeworkrate(0);
		rate.setTestrate(0);
		rate.setDesignrate(0);
		rate.setMiddlerate(0);
		rate.setEndrate(0);
		return rate;
	}

	private void putReportCommonData(Map<String,Object> map, HttpServletRequest request, List<Lesson> lessonlist, String lessonid){
		map.put("lessonlist", lessonlist);
		map.put("lessonid", lessonid);
		map.put("lessonname", "");
		map.put("reports", new Reports());
		map.put("rate", getEmptyRate());
		map.put("scoremap", getDefaultScoreMap());
		map.put("assessratelist", Collections.emptyList());
		map.put("analysisComponents", Collections.emptyList());
		map.put("analysisTargets", Collections.emptyList());
		map.put("teacher", getSessionUser(request));
		map.put("month", DateUtil.format(new Date(),"yyyy年MM月"));
		map.put("day", DateUtil.format(new Date(),"yyyy年MM月dd日"));
	}

	private ModelAndView showReportPage(HttpServletRequest request, String viewName){
		Map<String,Object> map = new HashMap<String,Object>();
		List<Lesson> lessonlist = getTeacherLessonList(request);
		String lessonid = getCurrentLessonId(request, lessonlist);
		putReportCommonData(map, request, lessonlist, lessonid);
		if(lessonid == null || "".equals(lessonid)){
			map.put("msg", "请先维护课程信息");
			return jsp(viewName, map, request);
		}

		Lesson lesson = lessonService.getById(lessonid);
		if(lesson == null || !canOperateReportLesson(request, lesson.getId())){
			map.put("msg", "课程不存在或无权操作");
			return jsp(viewName, map, request);
		}
		Integer dataTeacherId = getReportDataTeacherId(request, lesson);
		if(lesson != null){
			map.put("lessonname", lesson.getName());
		}
		map.put("userid", getSessionUser(request).getId());
		List<Reports> reportlist = reportsService.getList(map);
		if(reportlist.size() > 0){
			map.put("reports", reportlist.get(0));
		}

		Map<String,Object> query = new HashMap<String,Object>();
		query.put("teacherid", dataTeacherId);
		query.put("lessonid", lessonid);
		List<Rate> ratelist = rateService.getList(query);
		if(ratelist.size() == 0){
			map.put("msg", "总评系数未设置");
		}else{
			map.put("rate", ratelist.get(0));
		}

		query.clear();
		query.put("lessonid", lessonid);
		query.put("teacherid", dataTeacherId);
		map.put("analysisComponents", analysisComponentService.getList(query));
		map.put("analysisTargets", getTargets(lessonid, dataTeacherId));

		query.clear();
		query.put("lesson", lessonid);
		query.put("teacherid", dataTeacherId);
		List<Sumscore> scorelist = sumscoreService.getList(query);
		map.put("scoremap", getScoreMap(scorelist));

		query.clear();
		query.put("userid", dataTeacherId);
		query.put("lessonid", lessonid);
		List<Assessrate> assessratelist = assessrateService.getList(query);
		map.put("assessratelist", assessratelist);
		return jsp(viewName, map, request);
	}
	/**
	 * 列表
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="list")
	public ModelAndView list(HttpServletRequest request){
		if(!isReportOperator(request)){
			return jsp("login", new HashMap<String,Object>(), request);
		}
		Map<String,Object> map = new HashMap<String,Object>();
		return jsp("reports/reports", map, request);
	}
	/**
	 * 
	 * get list data
	 * @param request
	 * @return
	 */
	@RequestMapping(value="listdata")
	public void listdata(HttpServletRequest request, HttpServletResponse response){
		if(!requireReportOperator(request, response)){
			return;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("reportsName", CommonUtil.changeEncoding(request.getParameter("queryname")));
		if("1".equals(getSessionUser(request).getIsadmin())){
			map.put("userid", getSessionUser(request).getId());
		}
		List<Reports> list = reportsService.getList(map);
		JSONArray jsonarray = JSONArray.fromObject(list);
		String json = "{\"data\":"+jsonarray.toString()+"}";
		ajax(response, json);
	}
	/**
	 * form
	 * @param request
	 * @return
	 */
	@RequestMapping(value="form")
	public ModelAndView form(HttpServletRequest request){
		if(!isReportOperator(request)){
			return jsp("login", new HashMap<String,Object>(), request);
		}
		return showReportPage(request, "reports/reports");
	}
	/**
	 * form
	 * @param request
	 * @return
	 */
	@RequestMapping(value="check")
	public ModelAndView check(HttpServletRequest request){
		if(!isReportOperator(request)){
			return jsp("login", new HashMap<String,Object>(), request);
		}
		return showReportPage(request, "reports/check");
	}

	@RequestMapping(value="aigenerate")
	public void aigenerate(HttpServletRequest request, HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		if(!isReportOperator(request)){
			ajax(response, reportError(getSessionUser(request) == null ? "登录已过期，请重新登录" : "无权操作课程质量报告"));
			return;
		}
		try{
			List<Lesson> lessonlist = getTeacherLessonList(request);
			String lessonid = getCurrentLessonId(request, lessonlist);
			if(lessonid == null || "".equals(lessonid)){
				ajax(response, reportError("请选择课程"));
				return;
			}
			Lesson lesson = lessonService.getById(lessonid);
			if(lesson == null || !canOperateReportLesson(request, lesson.getId())){
				ajax(response, reportError("课程不存在或无权操作"));
				return;
			}
			boolean adminOperator = "0".equals(getSessionUser(request).getIsadmin());
			String nextStepMessage = adminOperator ? "请核对后打印预览或下载Word报告。" : "请核对后保存。";
			JSONObject reportData = buildReportData(request, lesson, lessonid);
			Reports reports = buildLocalReport(reportData);
			String message = "AI 报告已生成，" + nextStepMessage;
			String raw = "";
			try{
				raw = aiModelService.callPrompt(buildReportPrompt(reportData), REPORT_AI_MAX_TOKENS, REPORT_AI_TIMEOUT_MILLIS);
				Reports aiReports = parseAiReport(raw, reports, reportData);
				if(reportBodyTextLength(aiReports) < REPORT_MIN_BODY_CHARS){
					aiReports = expandShortAiReport(reportData, aiReports);
				}
				reports = aiReports;
				if(reportBodyTextLength(reports) < REPORT_MIN_BODY_CHARS){
					message = "AI 报告已生成。当前报告篇幅仍偏短，建议结合课程实际补充教学过程记录后再" + (adminOperator ? "打印预览或下载Word报告。" : "保存。");
				}
			}catch(Exception e){
				message = "AI 暂时未能完成生成。系统已根据现有数据生成基础报告，" + nextStepMessage;
			}
			insertReportCharts(reports, reportData);
				JSONObject result = new JSONObject();
				result.put("success", true);
				result.put("message", message);
				result.put("data", reportsToJson(reports));
				ajax(response, result.toString());
			}catch(Exception e){
				ajax(response, reportError("报告生成失败，请稍后重试；如多次失败，请联系管理员。"));
			}
		}
	/**
	 * 添加
	 * @param request
	 * @return
	 */
	@RequestMapping(value="add")
	public ModelAndView add(HttpServletRequest request, HttpServletResponse response, Reports reports){
		if(!isReportOperator(request)){
			return jsp("login", new HashMap<String,Object>(), request);
		}
		if("0".equals(getSessionUser(request).getIsadmin())){
			ajax(response, "管理员可生成、预览和下载课程质量报告，不能保存覆盖教师报告");
			return null;
		}
		if(!canOperateReportLesson(request, reports.getLessonid())){
			ajax(response, "课程不存在或无权操作");
			return null;
		}
		reports.setUserid(getSessionUser(request).getId());
		Reports reports1 = null;
		if(reports.getId() != null){
			reports1 = reportsService.getById(reports.getId()+"");
			if(reports1 != null && (!getSessionUser(request).getId().equals(reports1.getUserid()) || !canOperateReportLesson(request, reports1.getLessonid()))){
				ajax(response, "报告不存在或无权操作");
				return null;
			}
		}
		if(reports1 != null){
			reportsService.update(reports);
		}else{
			reportsService.insert(reports);
		}
		return showReportPage(request, "reports/check");
	}
	/**
	 * 修改
	 * @param request
	 * @return
	 */
	@RequestMapping(value="update")
	public void update(HttpServletRequest request, HttpServletResponse response, Reports reports){
		if(!requireReportOperator(request, response)){
			return;
		}
		if("0".equals(getSessionUser(request).getIsadmin())){
			ajax(response, "管理员不能保存覆盖教师报告");
			return;
		}
		if(!canOperateReportLesson(request, reports.getLessonid())){
			ajax(response, "课程不存在或无权操作");
			return;
		}
		reportsService.update(reports);
		ajax(response, "修改成功");
	}

	@RequestMapping(value="download")
	public void download(HttpServletRequest request, HttpServletResponse response){
		if(!requireReportOperator(request, response)){
			return;
		}
		try{
			List<Lesson> lessonlist = getTeacherLessonList(request);
			String lessonid = getCurrentLessonId(request, lessonlist);
			if(lessonid == null || "".equals(lessonid)){
				ajax(response, "请选择课程");
				return;
			}
			Lesson lesson = lessonService.getById(lessonid);
			if(lesson == null || !canOperateReportLesson(request, lesson.getId())){
				ajax(response, "课程不存在或无权操作");
				return;
			}
			Reports reports = getReportForDownload(request, lessonid);
			JSONObject reportData = buildReportData(request, lesson, lessonid);
			XWPFDocument document = buildReportDocx(lesson, reports, reportData);
			String fileName = URLEncoder.encode(lesson.getName() + "课程教学质量分析报告.docx", "UTF-8");
			response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
			ServletOutputStream outputStream = response.getOutputStream();
			document.write(outputStream);
			outputStream.flush();
			outputStream.close();
		}catch(Exception e){
			response.setContentType("text/plain;charset=UTF-8");
			ajax(response, "报告下载失败：" + e.getMessage());
		}
	}

	private JSONObject buildReportData(HttpServletRequest request, Lesson lesson, String lessonid){
		Integer teacherid = getReportDataTeacherId(request, lesson);
		User teacherUser = teacherid == null ? getSessionUser(request) : userService.getUserById(teacherid+"");
		if(teacherUser == null){
			teacherUser = getSessionUser(request);
		}
		JSONObject data = new JSONObject();
		JSONObject teacher = new JSONObject();
		teacher.put("name", teacherUser.getName());
		teacher.put("collegeName", teacherUser.getCollegeName());
		teacher.put("deptName", teacherUser.getDeptName());
		data.put("teacher", teacher);

		JSONObject lessonJson = new JSONObject();
		lessonJson.put("id", lesson.getId());
		lessonJson.put("name", lesson.getName());
		lessonJson.put("time", lesson.getTime());
		lessonJson.put("room", lesson.getRoom());
		lessonJson.put("beginend", lesson.getBeginend());
		data.put("lesson", lessonJson);

		Map<String,Object> query = new HashMap<String,Object>();
		query.put("lessonid", lessonid);
		query.put("teacherid", teacherid);
		List<LessonStudent> lessonStudents = lessonStudentService.getList(query);
		data.put("studentCount", lessonStudents.size());

		query.clear();
		query.put("lessonid", lessonid);
		query.put("teacherid", teacherid);
		List<AnalysisComponent> components = analysisComponentService.getList(query);
		data.put("components", componentsToJson(components));

		List<AnalysisTarget> targets = getTargets(lessonid, teacherid);
		data.put("targets", targetsToSummaryJson(targets, components, lessonStudents, lessonid, teacherid));
		data.put("studentTargetData", studentTargetDataToJson(targets, lessonStudents, lessonid, teacherid));

		query.clear();
		query.put("lesson", lessonid);
		query.put("teacherid", teacherid);
		List<Sumscore> scorelist = sumscoreService.getList(query);
		data.put("scoreSummary", scoreSummaryToJson(scorelist));
		data.put("scoreDistribution", JSONObject.fromObject(getScoreMap(scorelist)));
		data.put("classStats", classStatsToJson(lessonStudents, scorelist));
		data.put("componentStats", componentStatsToJson(components, lessonid, teacherid, lessonStudents.size()));
		data.put("questionnaire", questionnaireToJson(lessonid, teacherid));
		return data;
	}

	private JSONArray componentsToJson(List<AnalysisComponent> components){
		JSONArray array = new JSONArray();
		for(AnalysisComponent component : components){
			JSONObject item = new JSONObject();
			item.put("name", component.getComponentName());
			item.put("rate", formatDecimal(component.getRate()));
			array.add(item);
		}
		return array;
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

	private JSONArray targetsToSummaryJson(List<AnalysisTarget> targets, List<AnalysisComponent> components, List<LessonStudent> students, String lessonid, Integer teacherid){
		JSONArray array = new JSONArray();
		for(AnalysisTarget target : targets){
			JSONObject item = new JSONObject();
			item.put("name", target.getTargetName());
			item.put("content", target.getTargetContent() == null ? "" : target.getTargetContent());
			item.put("targetrate", formatDecimal(target.getTargetrate()));
			item.put("items", targetItemsToJson(target));
			BigDecimal scoreSum = BigDecimal.ZERO;
			BigDecimal achievementSum = BigDecimal.ZERO;
			for(LessonStudent student : students){
				Map<String,BigDecimal> scoreMap = getUserScoreMap(lessonid, student.getUserid(), teacherid);
				scoreSum = scoreSum.add(ScoreComputeUtil.calculateTargetScore(target, scoreMap));
				achievementSum = achievementSum.add(ScoreComputeUtil.calculateAchievement(target, scoreMap));
			}
			if(students.size() > 0){
				item.put("avgScore", ScoreComputeUtil.format(scoreSum.divide(new BigDecimal(students.size()), 2, BigDecimal.ROUND_HALF_UP)));
				item.put("achievement", ScoreComputeUtil.format(achievementSum.divide(new BigDecimal(students.size()), 4, BigDecimal.ROUND_HALF_UP)));
			}else{
				item.put("avgScore", "0.00");
				item.put("achievement", "0.00");
			}
			item.put("fullScore", ScoreComputeUtil.format(ScoreComputeUtil.calculateTargetFullScore(target)));
			array.add(item);
		}
		return array;
	}

	private JSONArray targetItemsToJson(AnalysisTarget target){
		JSONArray array = new JSONArray();
		if(target.getItemlist() == null){
			return array;
		}
		for(AnalysisTargetItem detail : target.getItemlist()){
			JSONObject item = new JSONObject();
			item.put("method", detail.getMethodName());
			item.put("weight", formatDecimal(detail.getWeightRate()));
			item.put("coefficient", formatDecimal(detail.getCoefficient()));
			array.add(item);
		}
		return array;
	}

	private JSONArray studentTargetDataToJson(List<AnalysisTarget> targets, List<LessonStudent> students, String lessonid, Integer teacherid){
		JSONArray array = new JSONArray();
		for(LessonStudent student : students){
			Map<String,BigDecimal> scoreMap = getUserScoreMap(lessonid, student.getUserid(), teacherid);
			JSONObject row = new JSONObject();
			User user = student.getUser();
			if(user == null){
				user = getLessonStudentUser(student);
			}
			row.put("no", user == null || user.getNo() == null ? "" : user.getNo());
			row.put("name", user == null || user.getName() == null ? "学生" + student.getUserid() : user.getName());
			row.put("deptName", user == null || user.getDeptName() == null ? "" : user.getDeptName());
			JSONArray achievements = new JSONArray();
			for(AnalysisTarget target : targets){
				achievements.add(ScoreComputeUtil.format(ScoreComputeUtil.calculateAchievement(target, scoreMap)));
			}
			row.put("achievements", achievements);
			array.add(row);
		}
		return array;
	}

	private JSONArray classStatsToJson(List<LessonStudent> students, List<Sumscore> scorelist){
		JSONArray array = new JSONArray();
		if(students == null || students.size() == 0 || scorelist == null || scorelist.size() == 0){
			return array;
		}
		Map<Integer,BigDecimal> scoreMap = new HashMap<Integer,BigDecimal>();
		for(Sumscore sumscore : scorelist){
			if(sumscore.getUserid() != null){
				scoreMap.put(sumscore.getUserid(), parseDecimal(sumscore.getSumscore()));
			}
		}
		LinkedHashMap<String,ClassStat> grouped = new LinkedHashMap<String,ClassStat>();
		for(LessonStudent lessonStudent : students){
			User user = getLessonStudentUser(lessonStudent);
			String deptName = user == null || isBlank(user.getDeptName()) ? "未维护班级" : user.getDeptName();
			ClassStat stat = grouped.get(deptName);
			if(stat == null){
				stat = new ClassStat();
				stat.deptName = deptName;
				grouped.put(deptName, stat);
			}
			stat.count++;
			BigDecimal score = scoreMap.get(lessonStudent.getUserid());
			if(score != null){
				stat.scoredCount++;
				stat.total = stat.total.add(score);
				if(score.compareTo(new BigDecimal("60")) >= 0){
					stat.passCount++;
				}
			}
		}
		for(String deptName : grouped.keySet()){
			ClassStat stat = grouped.get(deptName);
			JSONObject item = new JSONObject();
			item.put("deptName", deptName);
			item.put("studentCount", stat.count);
			item.put("scoreCount", stat.scoredCount);
			item.put("avgScore", stat.scoredCount == 0 ? "0.00" : ScoreComputeUtil.format(stat.total.divide(new BigDecimal(stat.scoredCount), 2, BigDecimal.ROUND_HALF_UP)));
			item.put("passRate", stat.scoredCount == 0 ? "0.00%" : new DecimalFormat("0.00").format(((float)stat.passCount / stat.scoredCount * 100)) + "%");
			array.add(item);
		}
		return array;
	}

	private User getLessonStudentUser(LessonStudent lessonStudent){
		if(lessonStudent == null){
			return null;
		}
		if(lessonStudent.getUser() != null){
			return lessonStudent.getUser();
		}
		if(lessonStudent.getUserid() == null){
			return null;
		}
		return userService.getUserById(lessonStudent.getUserid()+"");
	}

	private JSONObject scoreSummaryToJson(List<Sumscore> scorelist){
		JSONObject json = new JSONObject();
		json.put("count", scorelist == null ? 0 : scorelist.size());
		if(scorelist == null || scorelist.size() == 0){
			json.put("avg", "0.00");
			json.put("max", "0.00");
			json.put("min", "0.00");
			json.put("passRate", "0.00%");
			json.put("failCount", 0);
			return json;
		}
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal max = null;
		BigDecimal min = null;
		int failCount = 0;
		for(Sumscore score : scorelist){
			BigDecimal value = parseDecimal(score.getSumscore());
			total = total.add(value);
			if(max == null || value.compareTo(max) > 0){
				max = value;
			}
			if(min == null || value.compareTo(min) < 0){
				min = value;
			}
			if(value.compareTo(new BigDecimal("60")) < 0){
				failCount++;
			}
		}
		DecimalFormat df = new DecimalFormat("0.00");
		json.put("avg", df.format(total.divide(new BigDecimal(scorelist.size()), 2, BigDecimal.ROUND_HALF_UP)));
		json.put("max", df.format(max));
		json.put("min", df.format(min));
		json.put("failCount", failCount);
		json.put("passRate", df.format(((float)(scorelist.size() - failCount) / scorelist.size() * 100)) + "%");
		return json;
	}

	private JSONArray componentStatsToJson(List<AnalysisComponent> components, String lessonid, Integer teacherid, int studentCount){
		JSONArray array = new JSONArray();
		for(AnalysisComponent component : components){
			Map<String,Object> query = new HashMap<String,Object>();
			query.put("lessonid", lessonid);
			query.put("teacherid", teacherid);
			query.put("componentName", component.getComponentName());
			List<ScoreDetail> details = scoreDetailService.getList(query);
			BigDecimal total = BigDecimal.ZERO;
			BigDecimal max = null;
			BigDecimal min = null;
			int zeroCount = 0;
			for(ScoreDetail detail : details){
				BigDecimal value = detail.getScore() == null ? BigDecimal.ZERO : detail.getScore();
				total = total.add(value);
				if(max == null || value.compareTo(max) > 0){
					max = value;
				}
				if(min == null || value.compareTo(min) < 0){
					min = value;
				}
				if(value.compareTo(BigDecimal.ZERO) == 0){
					zeroCount++;
				}
			}
			int denominator = studentCount > 0 ? studentCount : details.size();
			JSONObject item = new JSONObject();
			item.put("name", component.getComponentName());
			item.put("rate", formatDecimal(component.getRate()));
			item.put("avg", denominator == 0 ? "0.00" : ScoreComputeUtil.format(total.divide(new BigDecimal(denominator), 2, BigDecimal.ROUND_HALF_UP)));
			item.put("max", max == null ? "0.00" : ScoreComputeUtil.format(max));
			item.put("min", min == null ? "0.00" : ScoreComputeUtil.format(min));
			item.put("zeroCount", zeroCount);
			array.add(item);
		}
		return array;
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

	private JSONObject questionnaireToJson(String lessonid, Integer teacherid){
		JSONObject json = new JSONObject();
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("type", teacherid+"");
		query.put("secondtype", lessonid);
		List<Question> questions = questionService.getList(query);
		query.clear();
		query.put("teacherid", teacherid);
		query.put("lesson", lessonid);
		List<Questionscore> scores = questionscoreService.getList(query);
		json.put("questionCount", questions.size());
		json.put("answerRecordCount", scores.size());
		Set<Integer> students = new HashSet<Integer>();
		BigDecimal total = BigDecimal.ZERO;
		for(Questionscore score : scores){
			students.add(score.getUserid());
			total = total.add(parseDecimal(score.getAvgscore()));
		}
		json.put("responseStudentCount", students.size());
		json.put("overallAvg", scores.size() == 0 ? "0.00" : ScoreComputeUtil.format(total.divide(new BigDecimal(scores.size()), 2, BigDecimal.ROUND_HALF_UP)));
		json.put("surveyCount", groupQuestionsBySurvey(questions).size());
		json.put("surveys", surveyStatsToJson(questions, scores));
		json.put("questions", questionStatsToJson(questions, scores));
		return json;
	}

	private JSONArray surveyStatsToJson(List<Question> questions, List<Questionscore> scores){
		JSONArray array = new JSONArray();
		LinkedHashMap<String,List<Question>> grouped = groupQuestionsBySurvey(questions);
		for(String key : grouped.keySet()){
			List<Question> surveyQuestions = grouped.get(key);
			if(surveyQuestions == null || surveyQuestions.size() == 0){
				continue;
			}
			Question first = surveyQuestions.get(0);
			Set<Integer> questionIds = new HashSet<Integer>();
			for(Question question : surveyQuestions){
				questionIds.add(question.getId());
			}
			Set<Integer> students = new HashSet<Integer>();
			List<Questionscore> surveyScores = new java.util.ArrayList<Questionscore>();
			BigDecimal total = BigDecimal.ZERO;
			for(Questionscore score : scores){
				if(score.getQuestionid() != null && questionIds.contains(score.getQuestionid())){
					students.add(score.getUserid());
					surveyScores.add(score);
					total = total.add(parseDecimal(score.getAvgscore()));
				}
			}
			JSONObject item = new JSONObject();
			item.put("surveyId", first.getSurveyId() == null ? "" : first.getSurveyId()+"");
			item.put("surveyName", isBlank(first.getSurveyName()) ? "默认问卷" : first.getSurveyName());
			item.put("surveyDesc", first.getSurveyDesc() == null ? "" : first.getSurveyDesc());
			item.put("questionCount", surveyQuestions.size());
			item.put("answerRecordCount", surveyScores.size());
			item.put("responseStudentCount", students.size());
			item.put("overallAvg", surveyScores.size() == 0 ? "0.00" : ScoreComputeUtil.format(total.divide(new BigDecimal(surveyScores.size()), 2, BigDecimal.ROUND_HALF_UP)));
			item.put("questions", questionStatsToJson(surveyQuestions, surveyScores));
			array.add(item);
		}
		return array;
	}

	private LinkedHashMap<String,List<Question>> groupQuestionsBySurvey(List<Question> questions){
		LinkedHashMap<String,List<Question>> grouped = new LinkedHashMap<String,List<Question>>();
		for(Question question : questions){
			String key = question.getSurveyId() == null ? "legacy_" + question.getSecondtype() : "survey_" + question.getSurveyId();
			List<Question> surveyQuestions = grouped.get(key);
			if(surveyQuestions == null){
				surveyQuestions = new java.util.ArrayList<Question>();
				grouped.put(key, surveyQuestions);
			}
			surveyQuestions.add(question);
		}
		return grouped;
	}

	private JSONArray questionStatsToJson(List<Question> questions, List<Questionscore> scores){
		JSONArray array = new JSONArray();
		for(Question question : questions){
			BigDecimal total = BigDecimal.ZERO;
			int count = 0;
			for(Questionscore score : scores){
				if(score.getQuestionid() != null && score.getQuestionid().equals(question.getId())){
					total = total.add(parseDecimal(score.getAvgscore()));
					count++;
				}
			}
			JSONObject item = new JSONObject();
			item.put("title", question.getTitle());
			item.put("count", count);
			item.put("avg", count == 0 ? "0.00" : ScoreComputeUtil.format(total.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP)));
			array.add(item);
		}
		return array;
	}

	private String buildReportPrompt(JSONObject reportData){
		return "你是一名熟悉高校课程质量评价、工程教育认证和OBE持续改进的课程教学质量分析专家。"
			+ "请基于系统统计数据生成一篇内容充实、结构完整、可直接放入课程教学质量分析报告的正文。"
			+ "报告要像真实教师完成的课程质量报告，不要只写几句概括性套话；除reports字段外，其余字段合计必须不少于5500个中文字符，低于该篇幅视为不合格。"
			+ "如果某些数据为空，也要围绕“数据缺失说明、可能影响、后续补充采集与改进安排”写出完整分析，不要用“略”“待补充”等占位词。"
			+ "只返回 JSON，不要 Markdown，不要代码块，不要解释。JSON 字段必须且只能为："
			+ "{\"reports\":\"课程目标内容HTML\",\"content1\":\"支撑课程目标的试题或考核依据HTML\","
			+ "\"content2\":\"课程目标达成情况分析HTML\",\"content3\":\"课程教学自我评价及改进措施HTML\","
			+ "\"content4\":\"课程教学第三方评价及持续改进建议HTML\",\"content5\":\"附件HTML\"}。"
			+ "所有字段内容使用简单HTML片段，可使用<h4>、<h5>、<p>、<ul>、<li>、<table>、<thead>、<tbody>、<tr>、<th>、<td>，"
			+ "不要使用<script>、<style>、SVG、图片、Markdown表格或外链；系统会另外插入真实图表。"
			+ "必须严格遵守以下写作要求："
			+ "1. reports字段只整理课程目标。必须原样使用系统统计数据 targets.content 中的课程目标正文，不得改写、扩写或自行生成课程目标；没有正文时说明需在融合数据分析中补充。"
			+ "2. content1写“课程目标与考核依据”，不少于1000个中文字符。先说明课程采用过程性评价与终结性评价相结合的评价体系，再逐项说明components中的成绩组成、权重及其对课程目标的支撑逻辑；"
			+ "必须结合targets.items中的method、weight、coefficient说明每个课程目标由哪些考核方式支撑；可以生成一张支撑关系表，并在表后写出文字解释。"
			+ "3. content2写“课程目标达成情况分析”，不少于1800个中文字符。必须引用scoreSummary、scoreDistribution、componentStats、targets、classStats中的具体数值，"
			+ "包括有效成绩人数、平均分、最高分、最低分、通过率、不及格人数、各分数段比例、各考核环节均分和0分人数、各课程目标满分折算、平均得分和达成度。"
			+ "分析时要包含总体判断、成绩分布解释、课程目标逐项分析、薄弱环节定位、班级差异或学生个体差异提示、数据可靠性说明；"
			+ "不得凭空编造系统没有提供的数据，不要把图表写成SVG或脚本。"
			+ "4. content3写“课程教学自我评价及持续改进措施”，不少于1500个中文字符。必须分成教学成效、存在问题、原因分析、改进措施、下一轮跟踪验证五部分；"
			+ "改进措施要具体到教学设计、课堂组织、作业/实验/阶段测验反馈、学业预警、低分学生帮扶、课程目标达成度复盘、数据闭环等，不要只写空泛口号。"
			+ "5. content4写“第三方评价及持续改进建议”，不少于900个中文字符。必须只使用questionnaire中的问卷数据；若answerRecordCount为0，明确说明尚未形成有效第三方评价数据，"
			+ "并提出下轮问卷设计、回收、题项维度、结果反馈、整改闭环的安排；若有问卷，则引用问卷数量、问题数量、答题人数、总体均分和各问卷/题项均分进行分析。"
			+ "6. content5写“附件及支撑材料说明”，不少于500个中文字符。列出系统可支撑的附件材料，例如课程学生名单、成绩组成与权重、成绩明细、总评成绩、课程目标达成度统计、班级对比、问卷统计、图表数据等；"
			+ "说明这些材料如何支撑报告结论和持续改进，不得写“略”。"
			+ "7. 全文语气要正式、客观、可审阅；允许指出问题，但要与数据相对应；不要出现“根据常识”“可能大概”“平台显示”等无依据表达。"
			+ "8. JSON 字符串中的双引号必须正确转义，确保后端可以解析；不要在JSON外输出任何字符。"
			+ "系统统计数据如下：\n" + reportData.toString();
	}

	private Reports expandShortAiReport(JSONObject reportData, Reports draft){
		try{
			String expandedRaw = aiModelService.callPrompt(buildReportExpandPrompt(reportData, draft), REPORT_EXPAND_MAX_TOKENS, REPORT_EXPAND_TIMEOUT_MILLIS);
			Reports expanded = parseAiReport(expandedRaw, draft, reportData);
			return reportBodyTextLength(expanded) > reportBodyTextLength(draft) ? expanded : draft;
		}catch(Exception e){
			return draft;
		}
	}

	private String buildReportExpandPrompt(JSONObject reportData, Reports draft){
		return "你是一名高校课程质量报告审稿与扩写专家。下面的课程教学质量分析报告草稿篇幅偏短，请在不改变事实、不编造系统没有统计值的前提下扩写。"
			+ "扩写后的reports字段仍只保留课程目标原文，不要改写；content1到content5合计必须不少于5500个中文字符。"
			+ "各字段最低篇幅要求：content1不少于1000字，content2不少于1800字，content3不少于1500字，content4不少于900字，content5不少于500字。"
			+ "扩写重点：把数据含义、形成原因、教学影响、薄弱环节、持续改进措施、跟踪验证方式写完整；数据为空时写清数据缺失对评价的影响和下一轮采集安排。"
			+ "只返回 JSON，不要 Markdown，不要代码块，不要解释。JSON 字段必须且只能为："
			+ "{\"reports\":\"课程目标内容HTML\",\"content1\":\"支撑课程目标的试题或考核依据HTML\","
			+ "\"content2\":\"课程目标达成情况分析HTML\",\"content3\":\"课程教学自我评价及改进措施HTML\","
			+ "\"content4\":\"课程教学第三方评价及持续改进建议HTML\",\"content5\":\"附件HTML\"}。"
			+ "所有字段内容使用简单HTML片段，不要使用<script>、<style>、SVG、图片、Markdown表格或外链。"
			+ "当前草稿JSON如下：\n" + reportsToJson(draft).toString()
			+ "\n系统统计数据如下：\n" + reportData.toString();
	}

	private Reports parseAiReport(String raw, Reports fallback, JSONObject reportData){
		try{
			JSONObject json = JSONObject.fromObject(AnalysisJsonUtil.extractJson(raw));
			Reports reports = new Reports();
			reports.setReports(fallback.getReports());
			reports.setContent1(getJsonText(json, "content1", fallback.getContent1()));
			reports.setContent2(getJsonText(json, "content2", fallback.getContent2()));
			reports.setContent3(getJsonText(json, "content3", fallback.getContent3()));
			String content4 = getJsonText(json, "content4", fallback.getContent4());
			if(shouldUseFallbackQuestionnaire(reportData) && htmlTextLength(content4) < 240){
				content4 = fallback.getContent4();
			}
			reports.setContent4(content4);
			String content5 = getJsonText(json, "content5", fallback.getContent5());
			reports.setContent5(isWeakAttachmentContent(content5) ? fallback.getContent5() : content5);
			return reports;
		}catch(Exception e){
			return fallback;
		}
	}

	private boolean shouldUseFallbackQuestionnaire(JSONObject reportData){
		if(reportData == null || !reportData.containsKey("questionnaire")){
			return true;
		}
		JSONObject questionnaire = reportData.getJSONObject("questionnaire");
		return !questionnaire.containsKey("answerRecordCount") || questionnaire.getInt("answerRecordCount") == 0;
	}

	private boolean isWeakAttachmentContent(String content){
		if(content == null){
			return true;
		}
		String text = htmlPlainText(content);
		return text.length() < 80 || text.indexOf("略") >= 0 || text.indexOf("待补充") >= 0;
	}

	private int reportBodyTextLength(Reports reports){
		if(reports == null){
			return 0;
		}
		return htmlTextLength(reports.getContent1())
			+ htmlTextLength(reports.getContent2())
			+ htmlTextLength(reports.getContent3())
			+ htmlTextLength(reports.getContent4())
			+ htmlTextLength(reports.getContent5());
	}

	private int htmlTextLength(String html){
		return htmlPlainText(html).length();
	}

	private String htmlPlainText(String html){
		if(html == null){
			return "";
		}
		String text = stripAutoCharts(html)
			.replaceAll("(?is)<script[^>]*>.*?</script>", "")
			.replaceAll("(?is)<style[^>]*>.*?</style>", "")
			.replaceAll("(?is)<svg[^>]*>.*?</svg>", "")
			.replaceAll("(?is)<[^>]+>", "");
		return decodeHtml(text).replaceAll("\\s+", "").trim();
	}

	private Reports buildLocalReport(JSONObject data){
		Reports reports = new Reports();
		JSONObject lesson = data.getJSONObject("lesson");
		JSONObject score = data.getJSONObject("scoreSummary");
		JSONObject questionnaire = data.getJSONObject("questionnaire");
		reports.setReports(buildTargetsHtml(data.getJSONArray("targets")));
		reports.setContent1(buildContent1Html(data.getJSONArray("components"), data.getJSONArray("targets")));
		reports.setContent2(buildAchievementHtml(data.getJSONArray("targets"), score, data.getJSONArray("componentStats")));
		reports.setContent3(buildSelfEvaluationHtml(lesson, score, data.getJSONObject("scoreDistribution")));
		reports.setContent4(buildQuestionnaireHtml(questionnaire));
		reports.setContent5(buildAttachmentHtml(data));
		return reports;
	}

	private void insertReportCharts(Reports reports, JSONObject reportData){
		String scoreChart = buildScoreDistributionChartSection(reportData.getJSONObject("scoreDistribution"));
		String targetChart = buildTargetChartSection(reportData.getJSONArray("targets"));
		String studentChart = buildStudentTargetChartSection(reportData.getJSONArray("targets"), reportData.getJSONArray("studentTargetData"));
		String classChart = buildClassComparisonChartSection(reportData.getJSONArray("classStats"));
		String content = stripAutoCharts(reports.getContent2());
		if(isBlank(content)){
			content = buildAchievementHtml(reportData.getJSONArray("targets"), reportData.getJSONObject("scoreSummary"), reportData.getJSONArray("componentStats"));
		}
		if(scoreChart.length() > 0){
			content = insertAfterFirst(content, "</p>", scoreChart);
		}
		if(targetChart.length() > 0){
			content = insertAfterText(content, "课程目标", targetChart);
		}
		if(studentChart.length() > 0){
			content = content + studentChart;
		}
		if(classChart.length() > 0){
			content = content + classChart;
		}
		if((scoreChart + targetChart + studentChart + classChart).length() == 0){
			return;
		}
		reports.setContent2(REPORT_CHARTS_START + content + REPORT_CHARTS_END);
	}

	private String insertAfterFirst(String content, String marker, String insert){
		int index = content.indexOf(marker);
		if(index < 0){
			return content + insert;
		}
		return content.substring(0, index + marker.length()) + insert + content.substring(index + marker.length());
	}

	private String insertAfterText(String content, String keyword, String insert){
		int keywordIndex = content.indexOf(keyword);
		if(keywordIndex < 0){
			return content + insert;
		}
		int paragraphEnd = content.indexOf("</p>", keywordIndex);
		if(paragraphEnd < 0){
			return content + insert;
		}
		return content.substring(0, paragraphEnd + 4) + insert + content.substring(paragraphEnd + 4);
	}

	private String stripAutoCharts(String content){
		if(content == null){
			return "";
		}
		int start = content.indexOf(REPORT_CHARTS_START);
		int end = content.indexOf(REPORT_CHARTS_END);
		if(start >= 0 && end > start){
			return content.substring(0, start) + content.substring(end + REPORT_CHARTS_END.length());
		}
		return content;
	}

	private String buildTargetsHtml(JSONArray targets){
		if(targets.size() == 0){
			return "<p>课程目标尚未从教学大纲中完成结构化维护，请先在融合数据分析中确认课程目标。</p>";
		}
		StringBuilder html = new StringBuilder();
		boolean hasContent = false;
		for(int i=0; i<targets.size(); i++){
			JSONObject target = targets.getJSONObject(i);
			String content = target.containsKey("content") ? AnalysisJsonUtil.cleanText(target.getString("content")) : "";
			String name = getReportTargetName(target, i);
			if(content.length() > 0){
				hasContent = true;
				html.append("<p><strong>").append(escapeHtml(name)).append("：</strong>")
					.append(escapeHtml(content)).append("</p>");
			}else{
				html.append("<p><strong>").append(escapeHtml(name)).append("：</strong>")
					.append("课程目标正文尚未保存，请在融合数据分析中重新解析并确认教学大纲课程目标原文。</p>");
			}
		}
		if(!hasContent){
			html.insert(0, "<p>当前报告未读取到教学大纲中的课程目标正文，以下仅显示已保存的课程目标名称。</p>");
		}
		return html.toString();
	}

	private String getReportTargetName(JSONObject target, int index){
		String name = target.containsKey("name") ? target.getString("name") : "";
		if(name.matches("^课程目标\\d+\\.\\d+$")){
			return "课程目标" + (index + 1);
		}
		return name.length() == 0 ? "课程目标" + (index + 1) : name;
	}

	private String buildContent1Html(JSONArray components, JSONArray targets){
		StringBuilder html = new StringBuilder();
		html.append("<p>本课程采用过程性评价与终结性评价相结合的方式开展课程目标达成评价。各考核环节由融合数据分析根据教学大纲解析后统一维护，成绩管理与课程质量分析均调用同一套权重数据。</p>");
		html.append("<table class=\"table table-bordered\"><thead><tr><th>序号</th><th>考核方式</th><th>总评占比</th></tr></thead><tbody>");
		for(int i=0; i<components.size(); i++){
			JSONObject component = components.getJSONObject(i);
			html.append("<tr><td>").append(i + 1).append("</td><td>").append(escapeHtml(component.getString("name")))
				.append("</td><td>").append(escapeHtml(component.getString("rate"))).append("%</td></tr>");
		}
		html.append("</tbody></table>");
		if(targets.size() > 0){
			html.append("<p>各课程目标与考核方式的支撑关系如下，表中“占比”为该考核方式在总评成绩中的权重，“系数”为该考核方式对相应课程目标的支撑比例。</p>");
			html.append("<table class=\"table table-bordered\"><thead><tr><th>课程目标</th><th>考核方式</th><th>占比</th><th>系数</th></tr></thead><tbody>");
			for(int i=0; i<targets.size(); i++){
				JSONObject target = targets.getJSONObject(i);
				JSONArray items = target.getJSONArray("items");
				if(items.size() == 0){
					html.append("<tr><td>").append(escapeHtml(target.getString("name"))).append("</td><td colspan=\"3\">尚未维护考核方式</td></tr>");
				}
				for(int j=0; j<items.size(); j++){
					JSONObject item = items.getJSONObject(j);
					html.append("<tr><td>").append(escapeHtml(target.getString("name"))).append("</td><td>")
						.append(escapeHtml(item.getString("method"))).append("</td><td>")
						.append(escapeHtml(item.getString("weight"))).append("%</td><td>")
						.append(escapeHtml(item.getString("coefficient"))).append("%</td></tr>");
				}
			}
			html.append("</tbody></table>");
		}
		return html.toString();
	}

	private String buildAchievementHtml(JSONArray targets, JSONObject score, JSONArray componentStats){
		StringBuilder html = new StringBuilder();
		html.append("<p>本课程共有 ").append(score.getString("count")).append(" 名学生形成有效总评成绩，平均分 ")
			.append(score.getString("avg")).append("，最高分 ").append(score.getString("max")).append("，最低分 ")
			.append(score.getString("min")).append("，通过率 ").append(score.getString("passRate")).append("。</p>");
		if(targets.size() > 0){
			html.append("<p>课程目标达成情况如下：</p><table class=\"table table-bordered\"><thead><tr><th>课程目标</th><th>满分折算</th><th>平均得分</th><th>达成度</th></tr></thead><tbody>");
			for(int i=0; i<targets.size(); i++){
				JSONObject target = targets.getJSONObject(i);
				html.append("<tr><td>").append(escapeHtml(target.getString("name"))).append("</td><td>")
					.append(escapeHtml(target.getString("fullScore"))).append("</td><td>")
					.append(escapeHtml(target.getString("avgScore"))).append("</td><td>")
					.append(escapeHtml(target.getString("achievement"))).append("</td></tr>");
			}
			html.append("</tbody></table>");
		}
		html.append("<p>各考核环节成绩统计如下：</p><table class=\"table table-bordered\"><thead><tr><th>考核方式</th><th>权重</th><th>平均分</th><th>最高分</th><th>最低分</th><th>0分人数</th></tr></thead><tbody>");
		for(int i=0; i<componentStats.size(); i++){
			JSONObject item = componentStats.getJSONObject(i);
			html.append("<tr><td>").append(escapeHtml(item.getString("name"))).append("</td><td>")
				.append(escapeHtml(item.getString("rate"))).append("%</td><td>")
				.append(escapeHtml(item.getString("avg"))).append("</td><td>")
				.append(escapeHtml(item.getString("max"))).append("</td><td>")
				.append(escapeHtml(item.getString("min"))).append("</td><td>")
				.append(escapeHtml(item.getString("zeroCount"))).append("</td></tr>");
		}
		html.append("</tbody></table>");
		html.append("<p>从课程目标和单项成绩的统计结果看，教师应重点核查达成度偏低、平均分偏低或0分人数较多的环节，结合课堂表现、作业完成质量、阶段测验和实验任务完成情况定位学生学习困难原因。</p>");
		return html.toString();
	}

	private String buildChartsHtml(JSONObject data){
		if(data == null){
			return "";
		}
		StringBuilder html = new StringBuilder();
		html.append(REPORT_CHARTS_START);
		html.append("<div class=\"report-chart-section\" style=\"margin-top:18px;\">");
		html.append("<h4 style=\"margin:12px 0 8px;font-size:16px;\">课程质量分析图表</h4>");
		html.append(buildChartDataTablesHtml(data));
		html.append(buildScoreDistributionChartSection(data.getJSONObject("scoreDistribution")));
		html.append(buildTargetChartSection(data.getJSONArray("targets")));
		html.append(buildStudentTargetChartSection(data.getJSONArray("targets"), data.getJSONArray("studentTargetData")));
		html.append(buildClassComparisonChartSection(data.getJSONArray("classStats")));
		html.append("</div>");
		html.append(REPORT_CHARTS_END);
		return html.toString();
	}

	private String buildChartDataTablesHtml(JSONObject data){
		StringBuilder html = new StringBuilder();
		JSONObject distribution = data.getJSONObject("scoreDistribution");
		html.append("<p><strong>总评成绩分布统计表</strong></p>");
		html.append("<table class=\"table table-bordered\"><thead><tr><th>分段</th><th>90-100</th><th>80-89</th><th>70-79</th><th>60-69</th><th>小于60</th></tr></thead><tbody>");
		html.append("<tr><td>人数</td><td>").append(distribution.getString("dy90")).append("</td><td>")
			.append(distribution.getString("c8090")).append("</td><td>").append(distribution.getString("c7080"))
			.append("</td><td>").append(distribution.getString("c6070")).append("</td><td>")
			.append(distribution.getString("xy60")).append("</td></tr>");
		html.append("<tr><td>比例</td><td>").append(distribution.getString("dy90rate")).append("</td><td>")
			.append(distribution.getString("c8090rate")).append("</td><td>").append(distribution.getString("c7080rate"))
			.append("</td><td>").append(distribution.getString("c6070rate")).append("</td><td>")
			.append(distribution.getString("xy60rate")).append("</td></tr>");
		html.append("</tbody></table>");

		JSONArray targets = data.getJSONArray("targets");
		if(targets.size() > 0){
			html.append("<p><strong>课程目标总体达成度统计表</strong></p>");
			html.append("<table class=\"table table-bordered\"><thead><tr><th>课程目标</th><th>满分折算</th><th>平均得分</th><th>达成度</th></tr></thead><tbody>");
			for(int i=0; i<targets.size(); i++){
				JSONObject target = targets.getJSONObject(i);
				html.append("<tr><td>").append(escapeHtml(getReportTargetName(target, i))).append("</td><td>")
					.append(escapeHtml(target.getString("fullScore"))).append("</td><td>")
					.append(escapeHtml(target.getString("avgScore"))).append("</td><td>")
					.append(escapeHtml(target.getString("achievement"))).append("</td></tr>");
			}
			html.append("</tbody></table>");
		}
		return html.toString();
	}

	private String buildScoreDistributionChartSection(JSONObject distribution){
		if(distribution == null){
			return "";
		}
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"report-chart-section report-score-chart\" style=\"margin:14px 0;\">");
		html.append("<p><strong>图：总评成绩分布</strong></p>");
		html.append(buildScoreDistributionChart(distribution));
		html.append("</div>");
		return html.toString();
	}

	private String buildTargetChartSection(JSONArray targets){
		if(targets == null || targets.size() == 0){
			return "";
		}
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"report-chart-section report-target-chart\" style=\"margin:14px 0;\">");
		html.append("<p><strong>图：课程目标总体达成情况</strong></p>");
		html.append(buildTargetBarChart(targets));
		html.append("</div>");
		return html.toString();
	}

	private String buildStudentTargetChartSection(JSONArray targets, JSONArray students){
		if(targets == null || students == null || targets.size() == 0 || students.size() == 0){
			return "";
		}
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"report-chart-section report-student-chart\" style=\"margin:14px 0;\">");
		html.append("<p><strong>图：学生个体课程目标达成情况</strong></p>");
		html.append(buildTargetScatterCharts(targets, students));
		html.append("</div>");
		return html.toString();
	}

	private String buildClassComparisonChartSection(JSONArray classStats){
		if(classStats == null || classStats.size() <= 1){
			return "";
		}
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"report-chart-section report-class-chart\" style=\"margin:14px 0;\">");
		html.append("<p><strong>图：班级平均成绩对比</strong></p>");
		html.append(buildClassStatsTable(classStats));
		html.append(buildClassComparisonChart(classStats));
		html.append("</div>");
		return html.toString();
	}

	private String buildClassStatsTable(JSONArray classStats){
		StringBuilder html = new StringBuilder();
		html.append("<table class=\"table table-bordered\"><thead><tr><th>班级</th><th>课程学生数</th><th>有成绩人数</th><th>平均分</th><th>通过率</th></tr></thead><tbody>");
		for(int i=0; i<classStats.size(); i++){
			JSONObject item = classStats.getJSONObject(i);
			html.append("<tr><td>").append(escapeHtml(item.getString("deptName"))).append("</td><td>")
				.append(item.getString("studentCount")).append("</td><td>")
				.append(item.getString("scoreCount")).append("</td><td>")
				.append(item.getString("avgScore")).append("</td><td>")
				.append(item.getString("passRate")).append("</td></tr>");
		}
		html.append("</tbody></table>");
		return html.toString();
	}

	private String buildScoreDistributionChart(JSONObject distribution){
		JSONArray labels = new JSONArray();
		JSONArray values = new JSONArray();
		labels.add("90-100");
		labels.add("80-89");
		labels.add("70-79");
		labels.add("60-69");
		labels.add("小于60");
		values.add(distribution.getInt("dy90"));
		values.add(distribution.getInt("c8090"));
		values.add(distribution.getInt("c7080"));
		values.add(distribution.getInt("c6070"));
		values.add(distribution.getInt("xy60"));
		return buildBarSvg("总评成绩分布柱状图", labels, values, "人数", false);
	}

	private String buildTargetBarChart(JSONArray targets){
		JSONArray labels = new JSONArray();
		JSONArray values = new JSONArray();
		for(int i=0; i<targets.size(); i++){
			JSONObject target = targets.getJSONObject(i);
			labels.add(getShortTargetName(target, i));
			values.add(parseDecimal(target.getString("achievement")));
		}
		return buildBarSvg("课程目标总体达成度柱状图", labels, values, "达成度", true);
	}

	private String buildClassComparisonChart(JSONArray classStats){
		JSONArray labels = new JSONArray();
		JSONArray values = new JSONArray();
		for(int i=0; i<classStats.size(); i++){
			JSONObject item = classStats.getJSONObject(i);
			labels.add(item.getString("deptName"));
			values.add(parseDecimal(item.getString("avgScore")));
		}
		return buildBarSvg("各班级平均成绩对比图", labels, values, "平均分", false);
	}

	private String buildTargetScatterCharts(JSONArray targets, JSONArray students){
		if(targets.size() == 0 || students.size() == 0){
			return "<p>学生课程目标达成度散点图暂无数据。</p>";
		}
		StringBuilder html = new StringBuilder();
		for(int i=0; i<targets.size(); i++){
			html.append(buildScatterSvg(getShortTargetName(targets.getJSONObject(i), i) + "学生达成度散点图", students, i));
		}
		return html.toString();
	}

	private String buildBarSvg(String title, JSONArray labels, JSONArray values, String unit, boolean ratio){
		int width = 760;
		int height = 300;
		int left = 58;
		int top = 42;
		int chartWidth = 660;
		int chartHeight = 188;
		BigDecimal max = BigDecimal.ZERO;
		for(int i=0; i<values.size(); i++){
			BigDecimal value = toJsonDecimal(values.get(i));
			if(value.compareTo(max) > 0){
				max = value;
			}
		}
		BigDecimal axisMax = ratio ? max.max(new BigDecimal("1.00")) : max.max(new BigDecimal("1"));
		StringBuilder svg = new StringBuilder();
		svg.append("<div style=\"margin:14px 0;text-align:center;\">");
		svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width).append("\" height=\"").append(height).append("\" viewBox=\"0 0 ").append(width).append(" ").append(height).append("\" style=\"max-width:100%;height:auto;border:1px solid #d9e2ec;background:#fff;\">");
		svg.append("<text x=\"").append(width / 2).append("\" y=\"24\" text-anchor=\"middle\" font-size=\"16\" font-weight=\"600\" fill=\"#243b53\">").append(escapeHtml(title)).append("</text>");
		drawAxis(svg, left, top, chartWidth, chartHeight, unit);
		int count = Math.max(1, labels.size());
		int slot = chartWidth / count;
		int barWidth = Math.min(64, Math.max(26, slot - 24));
		for(int i=0; i<labels.size(); i++){
			BigDecimal value = toJsonDecimal(values.get(i));
			int barHeight = axisMax.compareTo(BigDecimal.ZERO) == 0 ? 0 : value.multiply(new BigDecimal(chartHeight)).divide(axisMax, 0, BigDecimal.ROUND_HALF_UP).intValue();
			int x = left + i * slot + (slot - barWidth) / 2;
			int y = top + chartHeight - barHeight;
			svg.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"").append(barWidth).append("\" height=\"").append(barHeight).append("\" fill=\"#2f80ed\"/>");
			svg.append("<text x=\"").append(x + barWidth / 2).append("\" y=\"").append(Math.max(top + 12, y - 6)).append("\" text-anchor=\"middle\" font-size=\"11\" fill=\"#1f2933\">").append(formatChartValue(value, ratio)).append("</text>");
			svg.append("<text x=\"").append(x + barWidth / 2).append("\" y=\"").append(top + chartHeight + 24).append("\" text-anchor=\"middle\" font-size=\"11\" fill=\"#334e68\">").append(escapeHtml(labels.getString(i))).append("</text>");
		}
		svg.append("</svg></div>");
		return svg.toString();
	}

	private String buildScatterSvg(String title, JSONArray students, int targetIndex){
		int width = 760;
		int height = 300;
		int left = 58;
		int top = 42;
		int chartWidth = 660;
		int chartHeight = 188;
		BigDecimal max = new BigDecimal("1.00");
		for(int i=0; i<students.size(); i++){
			BigDecimal value = getStudentAchievement(students.getJSONObject(i), targetIndex);
			if(value.compareTo(max) > 0){
				max = value;
			}
		}
		StringBuilder svg = new StringBuilder();
		svg.append("<div style=\"margin:14px 0;text-align:center;\">");
		svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width).append("\" height=\"").append(height).append("\" viewBox=\"0 0 ").append(width).append(" ").append(height).append("\" style=\"max-width:100%;height:auto;border:1px solid #d9e2ec;background:#fff;\">");
		svg.append("<text x=\"").append(width / 2).append("\" y=\"24\" text-anchor=\"middle\" font-size=\"16\" font-weight=\"600\" fill=\"#243b53\">").append(escapeHtml(title)).append("</text>");
		drawAxis(svg, left, top, chartWidth, chartHeight, "达成度");
		int count = Math.max(1, students.size());
		for(int i=0; i<students.size(); i++){
			BigDecimal value = getStudentAchievement(students.getJSONObject(i), targetIndex);
			int x = left + (count == 1 ? chartWidth / 2 : (int)Math.round(i * (chartWidth * 1.0d / (count - 1))));
			int y = top + chartHeight - value.multiply(new BigDecimal(chartHeight)).divide(max, 0, BigDecimal.ROUND_HALF_UP).intValue();
			svg.append("<circle cx=\"").append(x).append("\" cy=\"").append(y).append("\" r=\"4\" fill=\"#f2994a\"/>");
		}
		svg.append("<text x=\"").append(left + chartWidth / 2).append("\" y=\"").append(top + chartHeight + 28).append("\" text-anchor=\"middle\" font-size=\"12\" fill=\"#334e68\">学生序号</text>");
		svg.append("</svg></div>");
		return svg.toString();
	}

	private void drawAxis(StringBuilder svg, int left, int top, int chartWidth, int chartHeight, String unit){
		svg.append("<line x1=\"").append(left).append("\" y1=\"").append(top + chartHeight).append("\" x2=\"").append(left + chartWidth).append("\" y2=\"").append(top + chartHeight).append("\" stroke=\"#829ab1\"/>");
		svg.append("<line x1=\"").append(left).append("\" y1=\"").append(top).append("\" x2=\"").append(left).append("\" y2=\"").append(top + chartHeight).append("\" stroke=\"#829ab1\"/>");
		for(int i=0; i<=4; i++){
			int y = top + chartHeight - i * chartHeight / 4;
			svg.append("<line x1=\"").append(left).append("\" y1=\"").append(y).append("\" x2=\"").append(left + chartWidth).append("\" y2=\"").append(y).append("\" stroke=\"#edf2f7\"/>");
		}
		svg.append("<text x=\"18\" y=\"").append(top + 10).append("\" font-size=\"12\" fill=\"#52606d\">").append(escapeHtml(unit)).append("</text>");
	}

	private BigDecimal getStudentAchievement(JSONObject student, int targetIndex){
		if(student == null || !student.containsKey("achievements")){
			return BigDecimal.ZERO;
		}
		JSONArray achievements = student.getJSONArray("achievements");
		if(targetIndex >= achievements.size()){
			return BigDecimal.ZERO;
		}
		return toJsonDecimal(achievements.get(targetIndex));
	}

	private BigDecimal toJsonDecimal(Object value){
		if(value == null){
			return BigDecimal.ZERO;
		}
		return parseDecimal(value.toString());
	}

	private String formatChartValue(BigDecimal value, boolean ratio){
		if(value == null){
			return ratio ? "0.00" : "0";
		}
		return ratio ? ScoreComputeUtil.format(value) : value.setScale(0, BigDecimal.ROUND_HALF_UP).toString();
	}

	private String getShortTargetName(JSONObject target, int index){
		String name = target.containsKey("name") ? target.getString("name") : "";
		if(name.length() == 0 || name.matches("^课程目标\\d+\\.\\d+$")){
			return "目标" + (index + 1);
		}
		return name.replace("课程", "");
	}

	private String buildSelfEvaluationHtml(JSONObject lesson, JSONObject score, JSONObject distribution){
		StringBuilder html = new StringBuilder();
		html.append("<p>《").append(escapeHtml(lesson.getString("name"))).append("》课程总体成绩分布基本反映了学生对课程知识、能力与素养要求的掌握情况。")
			.append("90分及以上 ").append(distribution.getString("dy90rate")).append("，80-89分 ")
			.append(distribution.getString("c8090rate")).append("，70-79分 ").append(distribution.getString("c7080rate"))
			.append("，60-69分 ").append(distribution.getString("c6070rate")).append("，不及格 ")
			.append(distribution.getString("xy60rate")).append("。</p>")
			.append("<p>综合评价：课程已形成由课程学生名单、成绩组成、课程目标考核比例、单项成绩和总评成绩共同支撑的数据链路，能够较完整地反映学生在知识理解、工具应用和综合素养方面的学习结果。平均分为 ")
			.append(score.getString("avg")).append("，通过率为 ").append(score.getString("passRate"))
			.append("，说明多数学生能够完成课程基本要求；同时，低分段和不及格学生仍需作为后续教学帮扶的重点对象。</p>")
			.append("<p>持续改进措施：下一轮教学应围绕三个方面推进改进。第一，强化过程性评价反馈，在作业、在线学习和阶段测验后及时发布问题清单，帮助学生在期末前完成纠偏。第二，针对实验或操作类任务设置分层指导和样例讲解，提高学生对工具应用、数据处理和综合表达任务的完成质量。第三，建立课程目标达成度跟踪机制，对达成度偏低的目标开展专项复盘，将问题定位到具体考核方式和教学活动。</p>")
			.append("<p>跟踪要求：后续应继续使用同一套融合数据分析权重进行成绩计算和目标达成度计算，避免人工重复维护比例造成数据不一致；对缺考、缺交或单项成绩为0的学生，应保留过程记录并纳入教学预警与帮扶台账。</p>");
		return html.toString();
	}

	private String buildQuestionnaireHtml(JSONObject questionnaire){
		if(questionnaire.getInt("answerRecordCount") == 0){
			return "<p>当前课程尚未收集到有效调查问卷数据。建议后续在课程结束前组织学生完成课程评价问卷，问卷内容应覆盖教学内容适切性、教学组织、学习资源、实验指导、学习收获和持续改进建议，并将学生反馈纳入课程持续改进闭环。</p>";
		}
		StringBuilder html = new StringBuilder();
		html.append("<p>本课程共设置 ").append(questionnaire.getString("surveyCount")).append(" 份问卷、")
			.append(questionnaire.getString("questionCount")).append(" 个问卷问题，")
			.append(questionnaire.getString("responseStudentCount")).append(" 名学生提交问卷记录，问卷总体均分为 ")
			.append(questionnaire.getString("overallAvg")).append("。</p>");
		JSONArray surveys = questionnaire.containsKey("surveys") ? questionnaire.getJSONArray("surveys") : new JSONArray();
		html.append("<table class=\"table table-bordered\"><thead><tr><th>问卷名称</th><th>问题数</th><th>答题人数</th><th>平均分</th></tr></thead><tbody>");
		for(int i=0; i<surveys.size(); i++){
			JSONObject survey = surveys.getJSONObject(i);
			html.append("<tr><td>").append(escapeHtml(survey.getString("surveyName"))).append("</td><td>")
				.append(survey.getString("questionCount")).append("</td><td>")
				.append(survey.getString("responseStudentCount")).append("</td><td>")
				.append(survey.getString("overallAvg")).append("</td></tr>");
		}
		html.append("</tbody></table>");
		for(int i=0; i<surveys.size(); i++){
			JSONObject survey = surveys.getJSONObject(i);
			html.append("<p><strong>").append(escapeHtml(survey.getString("surveyName"))).append("题目统计：</strong></p>");
			html.append("<table class=\"table table-bordered\"><thead><tr><th>问卷题目</th><th>答题人数</th><th>平均分</th></tr></thead><tbody>");
			JSONArray questions = survey.getJSONArray("questions");
			for(int j=0; j<questions.size(); j++){
				JSONObject question = questions.getJSONObject(j);
				html.append("<tr><td>").append(escapeHtml(question.getString("title"))).append("</td><td>")
					.append(question.getString("count")).append("</td><td>").append(question.getString("avg")).append("</td></tr>");
			}
			html.append("</tbody></table>");
		}
		html.append("<p>后续应结合问卷中得分偏低的问题，持续改进教学组织、学习支持和课程资源建设；对学生集中反馈的问题，应在下一轮课程中形成可检查的改进措施并持续跟踪。</p>");
		return html.toString();
	}

	private String buildAttachmentHtml(JSONObject data){
		StringBuilder html = new StringBuilder();
		html.append("<p>附件数据由系统根据课程绑定学生、融合数据分析、成绩导入结果和调查问卷记录自动生成，可作为课程教学质量分析报告的数据支撑。</p>");
		html.append("<ul><li>课程学生名单：").append(data.getString("studentCount")).append(" 人。</li>");
		html.append("<li>成绩组成与总评比例：由融合数据分析保存的成绩组成部分提供。</li>");
		html.append("<li>课程目标考核比例与达成度：由课程目标下各考核方式的占比和系数自动计算。</li>");
		html.append("<li>成绩明细与总评成绩：由成绩管理模块导入和计算，缺失成绩或0分项应结合备注进行核查。</li>");
		html.append("<li>调查问卷统计：由系统问卷数据汇总，用于支撑第三方评价和持续改进。</li></ul>");
		return html.toString();
	}

	private Reports getReportForDownload(HttpServletRequest request, String lessonid){
		Reports reports = new Reports();
		reports.setReports(request.getParameter("reports"));
		reports.setContent1(request.getParameter("content1"));
		reports.setContent2(request.getParameter("content2"));
		reports.setContent3(request.getParameter("content3"));
		reports.setContent4(request.getParameter("content4"));
		reports.setContent5(request.getParameter("content5"));
		boolean hasRequestContent = !isBlank(reports.getReports()) || !isBlank(reports.getContent1())
			|| !isBlank(reports.getContent2()) || !isBlank(reports.getContent3())
			|| !isBlank(reports.getContent4()) || !isBlank(reports.getContent5());
		if(hasRequestContent){
			return reports;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", getSessionUser(request).getId());
		map.put("lessonid", lessonid);
		List<Reports> reportlist = reportsService.getList(map);
		return reportlist.size() > 0 ? reportlist.get(0) : reports;
	}

	private XWPFDocument buildReportDocx(Lesson lesson, Reports reports, JSONObject reportData) throws Exception{
		XWPFDocument document = new XWPFDocument();
		addTitle(document, lesson.getName() + "课程教学质量分析报告");
		addParagraph(document, "课程名称：" + safeText(lesson.getName()), false);
		JSONObject teacher = reportData.getJSONObject("teacher");
		addParagraph(document, "任课教师：" + safeText(teacher.getString("name")), false);
		addParagraph(document, "生成日期：" + DateUtil.format(new Date(),"yyyy年MM月dd日"), false);
		addBlank(document);

		addHeading(document, "一、课程目标（依据课程教学大纲）");
		addHtmlContent(document, reports.getReports());

		addHeading(document, "二、课程的成绩评定组成及分布");
		addScoreCompositionDoc(document, reportData.getJSONArray("components"));
		addScoreDistributionTableDoc(document, reportData.getJSONObject("scoreDistribution"));
		addScoreDistributionChartDoc(document, reportData);

		addHeading(document, "三、课程目标达成情况评价");
		addHtmlContent(document, reports.getContent1());
		addHtmlContent(document, stripAutoCharts(reports.getContent2()));

		addHeading(document, "课程质量分析图表");
		addTargetAchievementTableDoc(document, reportData.getJSONArray("targets"));
		addComponentStatsTableDoc(document, reportData.getJSONArray("componentStats"));
		addTargetChartsToDoc(document, reportData);
		addStudentChartsToDoc(document, reportData);
		addClassComparisonChartDoc(document, reportData);

		addHeading(document, "四、课程教学自我评价及改进措施");
		addHtmlContent(document, reports.getContent3());

		addHeading(document, "五、课程教学第三方评价及持续改进建议");
		addHtmlContent(document, reports.getContent4());

		addHeading(document, "九、附件");
		addHtmlContent(document, reports.getContent5());
		return document;
	}

	private void addScoreCompositionDoc(XWPFDocument document, JSONArray components){
		if(components == null || components.size() == 0){
			addParagraph(document, "总评成绩组成尚未维护。", false);
			return;
		}
		StringBuilder expression = new StringBuilder("总评成绩 = ");
		String[][] rows = new String[components.size() + 1][3];
		rows[0] = new String[]{"序号", "考核方式", "总评占比"};
		for(int i=0; i<components.size(); i++){
			JSONObject component = components.getJSONObject(i);
			if(i > 0){
				expression.append(" + ");
			}
			expression.append(component.getString("name")).append("×").append(component.getString("rate")).append("%");
			rows[i + 1] = new String[]{(i + 1)+"", component.getString("name"), component.getString("rate") + "%"};
		}
		addParagraph(document, expression.toString(), false);
		addSimpleTable(document, rows);
	}

	private void addScoreDistributionTableDoc(XWPFDocument document, JSONObject distribution){
		addParagraph(document, "总评成绩分布统计表", true);
		String[][] rows = new String[][]{
			{"分段", "90-100", "80-89", "70-79", "60-69", "小于60"},
			{"人数", distribution.getString("dy90"), distribution.getString("c8090"), distribution.getString("c7080"), distribution.getString("c6070"), distribution.getString("xy60")},
			{"比例", distribution.getString("dy90rate"), distribution.getString("c8090rate"), distribution.getString("c7080rate"), distribution.getString("c6070rate"), distribution.getString("xy60rate")}
		};
		addSimpleTable(document, rows);
	}

	private void addTargetAchievementTableDoc(XWPFDocument document, JSONArray targets){
		if(targets == null || targets.size() == 0){
			addParagraph(document, "课程目标达成度暂无数据。", false);
			return;
		}
		addParagraph(document, "课程目标总体达成度统计表", true);
		String[][] rows = new String[targets.size() + 1][4];
		rows[0] = new String[]{"课程目标", "满分折算", "平均得分", "达成度"};
		for(int i=0; i<targets.size(); i++){
			JSONObject target = targets.getJSONObject(i);
			rows[i + 1] = new String[]{getReportTargetName(target, i), target.getString("fullScore"), target.getString("avgScore"), target.getString("achievement")};
		}
		addSimpleTable(document, rows);
	}

	private void addComponentStatsTableDoc(XWPFDocument document, JSONArray stats){
		if(stats == null || stats.size() == 0){
			return;
		}
		addParagraph(document, "各考核环节成绩统计表", true);
		String[][] rows = new String[stats.size() + 1][6];
		rows[0] = new String[]{"考核方式", "权重", "平均分", "最高分", "最低分", "0分人数"};
		for(int i=0; i<stats.size(); i++){
			JSONObject item = stats.getJSONObject(i);
			rows[i + 1] = new String[]{item.getString("name"), item.getString("rate") + "%", item.getString("avg"), item.getString("max"), item.getString("min"), item.getString("zeroCount")};
		}
		addSimpleTable(document, rows);
	}

	private void addScoreDistributionChartDoc(XWPFDocument document, JSONObject data) throws Exception{
		JSONObject distribution = data.getJSONObject("scoreDistribution");
		addImage(document, drawBarChartPng("总评成绩分布柱状图",
			new String[]{"90-100", "80-89", "70-79", "60-69", "小于60"},
			new BigDecimal[]{parseDecimal(distribution.getString("dy90")), parseDecimal(distribution.getString("c8090")), parseDecimal(distribution.getString("c7080")), parseDecimal(distribution.getString("c6070")), parseDecimal(distribution.getString("xy60"))},
			"人数", false), "score-distribution.png");
	}

	private void addTargetChartsToDoc(XWPFDocument document, JSONObject data) throws Exception{
		JSONArray targets = data.getJSONArray("targets");
		if(targets.size() > 0){
			String[] labels = new String[targets.size()];
			BigDecimal[] values = new BigDecimal[targets.size()];
			for(int i=0; i<targets.size(); i++){
				JSONObject target = targets.getJSONObject(i);
				labels[i] = getShortTargetName(target, i);
				values[i] = parseDecimal(target.getString("achievement"));
			}
			addImage(document, drawBarChartPng("课程目标总体达成度柱状图", labels, values, "达成度", true), "target-achievement.png");
		}
	}

	private void addStudentChartsToDoc(XWPFDocument document, JSONObject data) throws Exception{
		JSONArray targets = data.getJSONArray("targets");
		JSONArray students = data.getJSONArray("studentTargetData");
		for(int i=0; i<targets.size(); i++){
			addImage(document, drawScatterChartPng(getShortTargetName(targets.getJSONObject(i), i) + "学生达成度散点图", students, i), "target-scatter-" + i + ".png");
		}
	}

	private void addClassComparisonChartDoc(XWPFDocument document, JSONObject data) throws Exception{
		JSONArray classStats = data.getJSONArray("classStats");
		if(classStats == null || classStats.size() <= 1){
			return;
		}
		String[][] rows = new String[classStats.size() + 1][5];
		rows[0] = new String[]{"班级", "课程学生数", "有成绩人数", "平均分", "通过率"};
		String[] labels = new String[classStats.size()];
		BigDecimal[] values = new BigDecimal[classStats.size()];
		for(int i=0; i<classStats.size(); i++){
			JSONObject item = classStats.getJSONObject(i);
			rows[i + 1] = new String[]{item.getString("deptName"), item.getString("studentCount"), item.getString("scoreCount"), item.getString("avgScore"), item.getString("passRate")};
			labels[i] = item.getString("deptName");
			values[i] = parseDecimal(item.getString("avgScore"));
		}
		addParagraph(document, "各班级平均成绩对比表", true);
		addSimpleTable(document, rows);
		addImage(document, drawBarChartPng("各班级平均成绩对比图", labels, values, "平均分", false), "class-comparison.png");
	}

	private void addImage(XWPFDocument document, byte[] imageBytes, String fileName) throws Exception{
		if(imageBytes == null || imageBytes.length == 0){
			return;
		}
		String relationId = document.addPictureData(imageBytes, Document.PICTURE_TYPE_PNG);
		XWPFParagraph paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun run = paragraph.createRun();
		CTInline inline = run.getCTR().addNewDrawing().addNewInline();
		long width = Units.toEMU(480);
		long height = Units.toEMU(190);
		long pictureId = nextReportPictureId();
		inline.addNewExtent().setCx(width);
		inline.getExtent().setCy(height);
		inline.addNewDocPr().setId(pictureId);
		inline.getDocPr().setName(fileName);
		inline.getDocPr().setDescr(fileName);
		inline.addNewGraphic().addNewGraphicData();
		inline.getGraphic().getGraphicData().setUri("http://schemas.openxmlformats.org/drawingml/2006/picture");
		String picXml =
			"<pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\" " +
			"xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" " +
			"xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
			"<pic:nvPicPr><pic:cNvPr id=\"" + pictureId + "\" name=\"" + escapeXml(fileName) + "\"/>" +
			"<pic:cNvPicPr><a:picLocks noChangeAspect=\"1\"/></pic:cNvPicPr></pic:nvPicPr>" +
			"<pic:blipFill><a:blip r:embed=\"" + relationId + "\"/>" +
			"<a:stretch><a:fillRect/></a:stretch></pic:blipFill>" +
			"<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"" + width + "\" cy=\"" + height + "\"/></a:xfrm>" +
			"<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic>";
		inline.getGraphic().getGraphicData().set(org.apache.xmlbeans.XmlToken.Factory.parse(picXml));
	}

	private synchronized long nextReportPictureId(){
		return reportPictureId++;
	}

	private byte[] drawBarChartPng(String title, String[] labels, BigDecimal[] values, String unit, boolean ratio) throws Exception{
		int width = 900;
		int height = 360;
		int left = 80;
		int top = 54;
		int chartWidth = 760;
		int chartHeight = 220;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		prepareGraphics(g, width, height);
		g.setFont(new Font("SansSerif", Font.BOLD, 20));
		drawCenteredText(g, title, width / 2, 32, new Color(36, 59, 83));
		drawPngAxis(g, left, top, chartWidth, chartHeight, unit);
		BigDecimal max = BigDecimal.ZERO;
		for(int i=0; i<values.length; i++){
			if(values[i] != null && values[i].compareTo(max) > 0){
				max = values[i];
			}
		}
		BigDecimal axisMax = ratio ? max.max(new BigDecimal("1.00")) : max.max(new BigDecimal("1"));
		int count = Math.max(1, labels.length);
		int slot = chartWidth / count;
		int barWidth = Math.min(76, Math.max(28, slot - 36));
		g.setFont(new Font("SansSerif", Font.PLAIN, 14));
		for(int i=0; i<labels.length; i++){
			BigDecimal value = values[i] == null ? BigDecimal.ZERO : values[i];
			int barHeight = axisMax.compareTo(BigDecimal.ZERO) == 0 ? 0 : value.multiply(new BigDecimal(chartHeight)).divide(axisMax, 0, BigDecimal.ROUND_HALF_UP).intValue();
			int x = left + i * slot + (slot - barWidth) / 2;
			int y = top + chartHeight - barHeight;
			g.setColor(new Color(47, 128, 237));
			g.fillRect(x, y, barWidth, barHeight);
			g.setColor(new Color(31, 41, 51));
			drawCenteredText(g, formatChartValue(value, ratio), x + barWidth / 2, Math.max(top + 16, y - 8), new Color(31, 41, 51));
			drawCenteredText(g, labels[i], x + barWidth / 2, top + chartHeight + 28, new Color(51, 78, 104));
		}
		g.dispose();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "png", output);
		return output.toByteArray();
	}

	private byte[] drawScatterChartPng(String title, JSONArray students, int targetIndex) throws Exception{
		int width = 900;
		int height = 360;
		int left = 80;
		int top = 54;
		int chartWidth = 760;
		int chartHeight = 220;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		prepareGraphics(g, width, height);
		g.setFont(new Font("SansSerif", Font.BOLD, 20));
		drawCenteredText(g, title, width / 2, 32, new Color(36, 59, 83));
		drawPngAxis(g, left, top, chartWidth, chartHeight, "达成度");
		BigDecimal max = new BigDecimal("1.00");
		for(int i=0; i<students.size(); i++){
			BigDecimal value = getStudentAchievement(students.getJSONObject(i), targetIndex);
			if(value.compareTo(max) > 0){
				max = value;
			}
		}
		int count = Math.max(1, students.size());
		g.setColor(new Color(242, 153, 74));
		for(int i=0; i<students.size(); i++){
			BigDecimal value = getStudentAchievement(students.getJSONObject(i), targetIndex);
			int x = left + (count == 1 ? chartWidth / 2 : (int)Math.round(i * (chartWidth * 1.0d / (count - 1))));
			int y = top + chartHeight - value.multiply(new BigDecimal(chartHeight)).divide(max, 0, BigDecimal.ROUND_HALF_UP).intValue();
			g.fillOval(x - 5, y - 5, 10, 10);
		}
		g.setFont(new Font("SansSerif", Font.PLAIN, 14));
		drawCenteredText(g, "学生序号", left + chartWidth / 2, top + chartHeight + 32, new Color(51, 78, 104));
		g.dispose();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "png", output);
		return output.toByteArray();
	}

	private void prepareGraphics(Graphics2D g, int width, int height){
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	private void drawPngAxis(Graphics2D g, int left, int top, int chartWidth, int chartHeight, String unit){
		g.setStroke(new BasicStroke(1.2f));
		g.setColor(new Color(130, 154, 177));
		g.drawLine(left, top + chartHeight, left + chartWidth, top + chartHeight);
		g.drawLine(left, top, left, top + chartHeight);
		g.setColor(new Color(237, 242, 247));
		for(int i=0; i<=4; i++){
			int y = top + chartHeight - i * chartHeight / 4;
			g.drawLine(left, y, left + chartWidth, y);
		}
		g.setFont(new Font("SansSerif", Font.PLAIN, 14));
		g.setColor(new Color(82, 96, 109));
		g.drawString(unit, 24, top + 14);
	}

	private void drawCenteredText(Graphics2D g, String text, int centerX, int y, Color color){
		if(text == null){
			text = "";
		}
		FontMetrics metrics = g.getFontMetrics();
		g.setColor(color);
		g.drawString(text, centerX - metrics.stringWidth(text) / 2, y);
	}

	private void addTitle(XWPFDocument document, String text){
		XWPFParagraph paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun run = paragraph.createRun();
		run.setBold(true);
		run.setFontSize(18);
		run.setText(safeText(text));
	}

	private void addHeading(XWPFDocument document, String text){
		XWPFParagraph paragraph = document.createParagraph();
		XWPFRun run = paragraph.createRun();
		run.setBold(true);
		run.setFontSize(14);
		run.setText(safeText(text));
	}

	private void addParagraph(XWPFDocument document, String text, boolean bold){
		if(isBlank(text)){
			return;
		}
		XWPFParagraph paragraph = document.createParagraph();
		XWPFRun run = paragraph.createRun();
		run.setFontSize(11);
		run.setBold(bold);
		run.setText(safeText(text));
	}

	private void addBlank(XWPFDocument document){
		document.createParagraph().createRun().setText("");
	}

	private void addHtmlContent(XWPFDocument document, String html){
		if(isBlank(html)){
			addParagraph(document, "暂无内容。", false);
			return;
		}
		String cleaned = stripAutoCharts(html);
		Pattern tablePattern = Pattern.compile("(?is)<table[^>]*>.*?</table>");
		Matcher matcher = tablePattern.matcher(cleaned);
		int last = 0;
		while(matcher.find()){
			addHtmlTextBlocks(document, cleaned.substring(last, matcher.start()));
			addHtmlTable(document, matcher.group());
			last = matcher.end();
		}
		addHtmlTextBlocks(document, cleaned.substring(last));
	}

	private void addHtmlTextBlocks(XWPFDocument document, String html){
		if(isBlank(html)){
			return;
		}
		String text = html.replaceAll("(?is)<script[^>]*>.*?</script>", "")
			.replaceAll("(?is)<style[^>]*>.*?</style>", "")
			.replaceAll("(?is)<svg[^>]*>.*?</svg>", "")
			.replaceAll("(?i)<br\\s*/?>", "\n")
			.replaceAll("(?i)</p>", "\n")
			.replaceAll("(?i)</div>", "\n")
			.replaceAll("(?i)<li[^>]*>", "\n- ")
			.replaceAll("(?i)</li>", "\n")
			.replaceAll("(?is)<[^>]+>", "");
		text = decodeHtml(text);
		String[] lines = text.split("\\n");
		for(int i=0; i<lines.length; i++){
			String line = lines[i].trim();
			if(line.length() > 0){
				addParagraph(document, line, false);
			}
		}
	}

	private void addHtmlTable(XWPFDocument document, String tableHtml){
		List<List<String>> rows = new ArrayList<List<String>>();
		Matcher rowMatcher = Pattern.compile("(?is)<tr[^>]*>(.*?)</tr>").matcher(tableHtml);
		while(rowMatcher.find()){
			List<String> cells = new ArrayList<String>();
			Matcher cellMatcher = Pattern.compile("(?is)<t[dh][^>]*>(.*?)</t[dh]>").matcher(rowMatcher.group(1));
			while(cellMatcher.find()){
				String cell = decodeHtml(cellMatcher.group(1).replaceAll("(?is)<[^>]+>", "").trim());
				cells.add(cell);
			}
			if(cells.size() > 0){
				rows.add(cells);
			}
		}
		if(rows.size() == 0){
			return;
		}
		int colCount = 1;
		for(List<String> row : rows){
			colCount = Math.max(colCount, row.size());
		}
		String[][] data = new String[rows.size()][colCount];
		for(int i=0; i<rows.size(); i++){
			for(int j=0; j<colCount; j++){
				data[i][j] = j < rows.get(i).size() ? rows.get(i).get(j) : "";
			}
		}
		addSimpleTable(document, data);
	}

	private void addSimpleTable(XWPFDocument document, String[][] data){
		if(data == null || data.length == 0 || data[0].length == 0){
			return;
		}
		XWPFTable table = document.createTable(data.length, data[0].length);
		for(int i=0; i<data.length; i++){
			XWPFTableRow row = table.getRow(i);
			for(int j=0; j<data[i].length; j++){
				XWPFTableCell cell = row.getCell(j);
				cell.setText(safeText(data[i][j]));
			}
		}
	}

	private String decodeHtml(String text){
		if(text == null){
			return "";
		}
		return text.replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<")
			.replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'");
	}

	private String safeText(String text){
		return text == null ? "" : decodeHtml(text).replaceAll("\\s+", " ").trim();
	}

	private JSONObject reportsToJson(Reports reports){
		JSONObject json = new JSONObject();
		json.put("reports", reports.getReports() == null ? "" : reports.getReports());
		json.put("content1", reports.getContent1() == null ? "" : reports.getContent1());
		json.put("content2", reports.getContent2() == null ? "" : reports.getContent2());
		json.put("content3", reports.getContent3() == null ? "" : reports.getContent3());
		json.put("content4", reports.getContent4() == null ? "" : reports.getContent4());
		json.put("content5", reports.getContent5() == null ? "" : reports.getContent5());
		return json;
	}

	private String reportError(String message){
		JSONObject result = new JSONObject();
		result.put("success", false);
		result.put("message", message);
		return result.toString();
	}

	private String getJsonText(JSONObject json, String key, String fallback){
		if(json != null && json.containsKey(key) && json.get(key) != null){
			String value = json.get(key).toString();
			if(value != null && value.trim().length() > 0 && !"null".equalsIgnoreCase(value.trim())){
				return value;
			}
		}
		return fallback == null ? "" : fallback;
	}

	private BigDecimal parseDecimal(String value){
		if(value == null || value.trim().length() == 0){
			return BigDecimal.ZERO;
		}
		try{
			return new BigDecimal(value.replace("%", "").replace("％", "").trim());
		}catch(Exception e){
			return BigDecimal.ZERO;
		}
	}

	private String formatDecimal(BigDecimal value){
		if(value == null){
			return "0.00";
		}
		return new DecimalFormat("0.##").format(value);
	}

	private String escapeHtml(String value){
		if(value == null){
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	private String escapeXml(String value){
		return escapeHtml(value).replace("'", "&apos;");
	}

	private boolean isBlank(String value){
		return value == null || value.trim().length() == 0;
	}
	
	/**
	 * delete
	 * @param request
	 * @return
	 */
	@RequestMapping(value="del")
	public void del(HttpServletRequest request, HttpServletResponse response){
		if(!requireReportOperator(request, response)){
			return;
		}
		if("0".equals(getSessionUser(request).getIsadmin())){
			ajax(response, "管理员不能删除教师课程质量报告");
			return;
		}
		String id = request.getParameter("id");
		Reports report = reportsService.getById(id);
		if(report == null || !getSessionUser(request).getId().equals(report.getUserid()) || !canOperateReportLesson(request, report.getLessonid())){
			ajax(response, "报告不存在或无权操作");
			return;
		}
		reportsService.delete(id);
		ajax(response, "删除成功");
		
	}

	private boolean canOperateReportLesson(HttpServletRequest request, Integer lessonid){
		if(lessonid == null || getSessionUser(request) == null){
			return false;
		}
		Lesson lesson = lessonService.getById(lessonid.toString());
		if(lesson == null){
			return false;
		}
		return getSessionUser(request).getId().equals(lesson.getUserid());
	}

	private Integer getReportDataTeacherId(HttpServletRequest request, Lesson lesson){
		if(lesson != null && lesson.getUserid() != null){
			return lesson.getUserid();
		}
		return getSessionUser(request) == null ? null : getSessionUser(request).getId();
	}

}
