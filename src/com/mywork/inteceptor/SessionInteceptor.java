package com.mywork.inteceptor;

import java.util.Arrays;
import java.util.HashSet;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.mywork.bean.User;
import com.mywork.common.SessionKeys;

/**
 * springMVC拦截器
 * @author gaozq
 *
 */
public class SessionInteceptor implements HandlerInterceptor{
	private static final Set<String> PUBLIC_PATHS = new HashSet<String>(Arrays.asList(
			"/user/login.html",
			"/user/main.html",
			"/user/logout.html",
			"/index.html"
	));
	private static final Set<String> COMMON_LOGIN_PATHS = new HashSet<String>(Arrays.asList(
			"/user/personalinfo.html",
			"/user/changepassword.html",
			"/news/checklist.html",
			"/news/listdata.html",
			"/download.html"
	));
	private static final Set<String> ADMIN_PREFIXES = new HashSet<String>(Arrays.asList(
			"/aimodel/"
	));
	private static final Set<String> ADMIN_PATHS = new HashSet<String>(Arrays.asList(
			"/college/list.html",
			"/college/listdata.html",
			"/college/update.html",
			"/college/del.html",
			"/college/dels.html",
			"/dept/list.html",
			"/dept/listdata.html",
			"/dept/update.html",
			"/dept/del.html",
			"/dept/dels.html",
			"/dept/getByCollegeId.html",
			"/user/teacherlist.html",
			"/user/studentlist.html",
			"/user/listdata.html",
			"/user/add.html",
			"/user/update.html",
			"/user/del.html",
			"/user/dels.html",
			"/news/list.html",
			"/news/add.html",
			"/news/update.html",
			"/news/del.html",
			"/statistics/grade-service-count.html"
	));
	private static final Set<String> TEACHER_PREFIXES = new HashSet<String>(Arrays.asList(
			"/lesson/",
			"/fusion/",
			"/reports/",
			"/kindeditor/"
	));
	private static final Set<String> TEACHER_PATHS = new HashSet<String>(Arrays.asList(
			"/dept/getByCollegeId.html",
			"/score/list.html",
			"/score/components.html",
			"/score/listdata.html",
			"/score/downloadtemplate.html",
			"/score/importfile.html",
			"/score/previewimport.html",
			"/score/confirmimport.html",
			"/score/updatescore.html",
			"/score/createsumscore.html",
			"/score/selfevalution.html",
			"/score/selfevalutionlistdata.html",
			"/score/charts.html",
			"/score/sumlist.html",
			"/score/sumlistdata.html",
			"/question/questionanswerscorelist.html",
			"/question/questionanswerstatsdata.html",
			"/question/questionanswerscorelistdata.html",
			"/question/list.html",
			"/question/surveylistdata.html",
			"/question/savesurvey.html",
			"/question/delsurvey.html",
			"/question/listdata.html",
			"/question/add.html",
			"/question/update.html",
			"/question/del.html",
			"/question/delscore.html"
	));
	private static final Set<String> STUDENT_PATHS = new HashSet<String>(Arrays.asList(
			"/score/ownlist.html",
			"/score/ownlistdata.html",
			"/question/questionanswer.html",
			"/question/answer.html"
	));
	
	Log log=LogFactory.getLog(SessionInteceptor.class);
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object arg2, Exception arg3)
			throws Exception {
		//log.debug("================执行顺序：3 afterCompletion==============================");
		
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object arg2, ModelAndView arg3)
			throws Exception {
		//log.debug("==================执行顺序：2 postHandle================================================");
//		String url=request.getServletPath(); 
//		log.debug("===================请求路径为："+url);
//		if(session.getAttribute("loginOperatorUser")==null || session.getAttribute("loginSupplierUser")==null || session.getAttribute("loginUser")==null){
//					
//		}
//		if((!url.equals("/")) && (!url.equals("")) && !url.equals("/operator/login.html") && !url.equals("/supplier/login.html") && !url.equals("/commonuser/login1.html") && !url.equals("/middle.html") && !url.equals("/message/toDetail.html") && url.indexOf("supplier")!=-1){
//			HttpSession session = request.getSession();
//			if(null==session.getAttribute("loginSupplierUser")){
//				request.getRequestDispatcher("/supplier/login.html").forward(request, response);
//			}
//		}
//		if((!url.equals("/")) && (!url.equals("")) && !url.equals("/operator/login.html") && !url.equals("/commonuser/login1.html") && !url.equals("/middle.html") && !url.equals("/message/toDetail.html")){
//			if(null==session.getAttribute(SessionKeys.WZ_USER)){
//				request.getRequestDispatcher("/middle.html").forward(request, response);
//				
//			}
//		}
	}
	/**
	 * 在到达action之前执行
	 */
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2) throws Exception {
		String url = normalizePath(request.getServletPath());
		if(PUBLIC_PATHS.contains(url)){
			return true;
		}
		User sessionuser = getSessionUser(request);
		if(sessionuser == null){
			writeLoginTimeout(response);
			return false;
		}
		if(isAllowed(url, sessionuser.getIsadmin())){
			return true;
		}
		writeForbidden(response);
		return false;
	}

	private String normalizePath(String url){
		if(url == null || "".equals(url)){
			return "/";
		}
		return url.startsWith("/") ? url : "/" + url;
	}

	private User getSessionUser(HttpServletRequest request){
		String role = request.getParameter("sessionRole");
		if(role == null || "".equals(role.trim())){
			role = request.getParameter("isadmin");
		}
		User sessionuser = null;
		if(role != null && !"".equals(role.trim())){
			sessionuser = (User)request.getSession().getAttribute(SessionKeys.loginUserRoleKey(role.trim()));
		}
		if(sessionuser == null){
			sessionuser = (User)request.getSession().getAttribute(SessionKeys.LOGIN_USER);
		}
		return sessionuser;
	}

	private boolean isAllowed(String url, String role){
		if(COMMON_LOGIN_PATHS.contains(url)){
			return true;
		}
		if("0".equals(role)){
			return startsWithAny(url, ADMIN_PREFIXES) || ADMIN_PATHS.contains(url);
		}
		if("1".equals(role)){
			return startsWithAny(url, TEACHER_PREFIXES) || TEACHER_PATHS.contains(url);
		}
		if("2".equals(role)){
			return STUDENT_PATHS.contains(url);
		}
		return false;
	}

	private boolean startsWithAny(String url, Set<String> prefixes){
		for(String prefix : prefixes){
			if(url.startsWith(prefix)){
				return true;
			}
		}
		return false;
	}

	private void writeLoginTimeout(HttpServletResponse response) throws Exception{
		PrintWriter out = response.getWriter();
		StringBuilder builder = new StringBuilder();
		builder.append("<script type=\"text/javascript\" charset=\"GBK\">");
		builder.append("alert(\"Login Timeout, please login again\");");
		builder.append("window.top.location.href=\"");
		builder.append("/user/login.html\";</script>");
		out.print(builder.toString());
		out.close();
	}

	private void writeForbidden(HttpServletResponse response) throws Exception{
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("text/plain;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.print("无权访问该功能");
		out.close();
	}

}
