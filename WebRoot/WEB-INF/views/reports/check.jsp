<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>

<!DOCTYPE HTML>
<html>
<head>
	<title>打印预览</title>
	<!-- bootstrap -->
	<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap/css/bootstrap.min.css">
	<script type="text/javascript" src="<%=basePath %>resources/js/jquery.min.js"></script>
	<script type="text/javascript">
		window.SELF_EVAL_SESSION_ROLE = '${sessionRole}';
		function appendSessionRole(url){
			var role = window.SELF_EVAL_SESSION_ROLE || '';
			if(!role || !url || url.indexOf('sessionRole=') >= 0){ return url; }
			return url + (url.indexOf('?') >= 0 ? '&' : '?') + 'sessionRole=' + encodeURIComponent(role);
		}
		$(function(){
			var role = window.SELF_EVAL_SESSION_ROLE || '';
			if(role){
				$('form').append('<input type="hidden" name="sessionRole" value="'+role+'">');
				$('form[action]').each(function(){ $(this).attr('action', appendSessionRole($(this).attr('action'))); });
			}
		});
	</script>
	<script src="<%=basePath %>resources/lib/bootstrap/js/bootstrap.min.js"></script>
	<!-- bootstrap-table -->
	<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap-table/bootstrap-table.css">
	<script src="<%=basePath %>resources/lib/bootstrap-table/bootstrap-table.js"></script>
	<!-- bootstrap-table汉化包 -->
	<script src="<%=basePath %>resources/lib/bootstrap-table/bootstrap-table-zh-CN.js"></script>
	<!-- 图标 CSS-->
	<link rel="stylesheet" href="<%=basePath %>resources/css/fonts/font-awesome/font-awesome.css">
	<link rel="stylesheet" href="<%=basePath %>resources/css/fonts/web-icons/web-icons.css">
	
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/lib/H-ui/css/H-ui.old.min.css" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/lib/H-ui/css/H-ui.admin.css" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/lib/iconfont/iconfont.css" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/css/skin.css"/>
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/css/style.css" />
	<link href="<%=basePath %>resources/kindeditor/themes/default/default.css" type="text/css" rel="stylesheet">
    <script type="text/javascript" src="<%=basePath %>resources/kindeditor/kindeditor-all-min.js"></script>
    <script type="text/javascript" src="<%=basePath %>resources/kindeditor/lang/zh-CN.js"></script>
	<script>
		
    </script>
</head>
<body>
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理 
		<span class="c-gray en">&gt;</span> 课程质量报告
		<button  class="btn btn-success btn-xs pull-right"  style="margin-top:6px; padding: 1px 5px; font-size: 12px; height: 23px;" onclick="location.replace(location.href)" title="刷新" ><i class="fa fa-undo"></i></button>
	</nav>
	<div class="page-container" id="maindiv" style="padding-top: 0px; margin-top: -10px;">
		<div id="tab-system" class="HuiTab">
			<jsp:include page="firstpage.jsp"></jsp:include>
			<div class="tabCon">
				<form class="form form-horizontal" id="reportsinfo" method="post" action="<%=basePath %>reports/add.html">
					<table border="1">
						<tr>
							<td style="background-color: #E5EEDA; font-size: 20px;">一、课程目标（依据课程教学大纲）</td>
						</tr>
						<tr>
							<td>
								${reports.reports}
							</td>
						</tr>
						<tr>
							<td style="background-color: #E5EEDA; font-size: 20px;">二、课程的成绩评定组成及分布</td>
						</tr>
						<tr>
							<td style="font-size: 18px;">
									1. 课程总评成绩构成（考核方式及比例）<br/>
									<c:choose>
										<c:when test="${not empty analysisComponents}">
											总评成绩 =
											<c:forEach var="component" items="${analysisComponents}" varStatus="status">
												<c:if test="${not status.first}"> + </c:if>${component.componentName}×<fmt:formatNumber value="${component.rate}" pattern="0.##"/>%
											</c:forEach>
										</c:when>
										<c:otherwise>
											总评成绩 =课堂表现×${rate.showrate}%+课程作业×${rate.homeworkrate}%+课程实验×${rate.testrate}%+课程设计×${rate.designrate}%+期中成绩×${rate.middlerate}%+期末成绩×${rate.endrate}%
										</c:otherwise>
									</c:choose>
									<br/>
								2. 总评成绩分布<br/>
								<table border="1" style="width:90%; margin: 20px;">
									<tr>
										<td style="text-align: center;">分段</td>
										<td style="text-align: center;">90-100</td>
										<td style="text-align: center;">80-89</td>
										<td style="text-align: center;">70-79</td>
										<td style="text-align: center;">60-69</td>
										<td style="text-align: center;">小于60</td>
									</tr>
									<tr>
										<td style="text-align: center;">人数</td>
										<td style="text-align: center;">${scoremap.dy90}</td>
										<td style="text-align: center;">${scoremap.c8090}</td>
										<td style="text-align: center;">${scoremap.c7080}</td>
										<td style="text-align: center;">${scoremap.c6070}</td>
										<td style="text-align: center;">${scoremap.xy60}</td>
									</tr>
									<tr>
										<td style="text-align: center;">比例</td>
										<td style="text-align: center;">${scoremap.dy90rate}</td>
										<td style="text-align: center;">${scoremap.c8090rate}</td>
										<td style="text-align: center;">${scoremap.c7080rate}</td>
										<td style="text-align: center;">${scoremap.c6070rate}</td>
										<td style="text-align: center;">${scoremap.xy60rate}</td>
									</tr>
								</table>
							</td>
						</tr>
						<tr>
							<td style="background-color: #E5EEDA; font-size: 20px;">三、课程目标达成情况评价</td>
						</tr>
						<tr>
								<td style="font-size: 18px;">
									&nbsp;&nbsp;&nbsp;&nbsp;1、 课程考核方式设置<br/>
									<table border="1" style="width:90%; margin: 20px;">
										<tr>
											<td style="text-align: center;">序号</td><td style="text-align: center;">考核方式</td>
										</tr>
										<c:forEach var="component" items="${analysisComponents}" varStatus="status">
											<tr>
												<td style="text-align: center;">${status.index + 1}</td><td style="text-align: center;">${component.componentName}</td>
											</tr>
										</c:forEach>
										<c:if test="${empty analysisComponents}">
											<tr><td style="text-align: center;">1</td><td style="text-align: center;">课堂表现</td></tr>
											<tr><td style="text-align: center;">2</td><td style="text-align: center;">课程作业</td></tr>
											<tr><td style="text-align: center;">3</td><td style="text-align: center;">课程实验</td></tr>
											<tr><td style="text-align: center;">4</td><td style="text-align: center;">课程设计</td></tr>
											<tr><td style="text-align: center;">5</td><td style="text-align: center;">期中成绩</td></tr>
											<tr><td style="text-align: center;">6</td><td style="text-align: center;">期末成绩</td></tr>
										</c:if>
									</table>
									&nbsp;&nbsp;&nbsp;&nbsp;2. 课程目标与课程考核方式对应关系<br/>
									<table border="1" style="width:90%; margin: 20px;">
										<c:choose>
											<c:when test="${not empty analysisTargets}">
												<tr>
													<td style="text-align: center; width: 120px;">课程目标</td>
													<td style="text-align: center;">考核方式</td>
													<td style="text-align: center;">占比</td>
													<td style="text-align: center;">系数</td>
												</tr>
												<c:forEach var="target" items="${analysisTargets}">
													<c:forEach var="item" items="${target.itemlist}" varStatus="itemStatus">
														<tr>
															<c:if test="${itemStatus.first}">
																<td style="text-align: center;" rowspan="${fn:length(target.itemlist)}">${target.targetName}</td>
															</c:if>
															<td style="text-align: center;">${item.methodName}</td>
															<td style="text-align: center;"><fmt:formatNumber value="${item.weightRate}" pattern="0.##"/>%</td>
															<td style="text-align: center;"><fmt:formatNumber value="${item.coefficient}" pattern="0.##"/>%</td>
														</tr>
													</c:forEach>
													<c:if test="${empty target.itemlist}">
														<tr>
															<td style="text-align: center;">${target.targetName}</td>
															<td style="text-align: center;" colspan="3">暂无考核方式数据</td>
														</tr>
													</c:if>
												</c:forEach>
											</c:when>
											<c:otherwise>
												<tr>
													<td style="text-align: center; width: 120px;">课程目标\考核方式</td><td style="text-align: center;">1</td><td style="text-align: center;">2</td><td style="text-align: center;">3</td><td style="text-align: center;">4</td><td style="text-align: center;">5</td><td style="text-align: center;">6</td>
												</tr>
												<c:forEach var="rate" items="${assessratelist}" varStatus="status">
													<tr>
														<td style="text-align: center;">${rate.targetname}</td>
														<td style="text-align: center;">${rate.rate1}</td>
														<td style="text-align: center;">${rate.rate2}</td>
														<td style="text-align: center;">${rate.rate3}</td>
														<td style="text-align: center;">${rate.rate4}</td>
														<td style="text-align: center;">${rate.rate5}</td>
														<td style="text-align: center;">${rate.rate6}</td>
													</tr>
												</c:forEach>
											</c:otherwise>
										</c:choose>
									</table>
								&nbsp;&nbsp;&nbsp;&nbsp;3、支撑课程目标的试题举例<br/>
								${reports.content1}
								&nbsp;&nbsp;&nbsp;&nbsp;4、课程目标达成情况分析<br/>
								${reports.content2}
							</td>
						</tr>
						<tr>
							<td style="background-color: #E5EEDA; font-size: 20px;">四、课程教学自我评价及改进措施</td>
						</tr>
						<tr>
							<td>
								${reports.content3}
							</td>
						</tr>
						<tr>
							<td style="background-color: #E5EEDA; font-size: 20px;">五、课程教学第三方评价及持续改进建议</td>
						</tr>
						<tr>
							<td>
								${reports.content4}
							</td>
						</tr>
						<tr>
							<td style="background-color: #E5EEDA; font-size: 20px;">九、附件</td>
						</tr>
						<tr>
							<td>
								${reports.content5}
							</td>
						</tr>
					</table>
				</form>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="<%=basePath %>resources/lib/layer/2.4/layer.js"></script>
	<script type="text/javascript" src="<%=basePath %>resources/lib/H-ui/js/H-ui.min.js"></script>
	<script type="text/javascript" src="<%=basePath %>resources/lib/H-ui/js/H-ui.admin.js"></script> 
	<script type="text/javascript">
		$(function(){
			$('.skin-minimal input').iCheck({
				checkboxClass: 'icheckbox-blue',
				radioClass: 'iradio-blue',
				increaseArea: '20%'
			});
			$("#tab-system").Huitab({
				index:0
				});
				var height = document.documentElement.clientHeight-175;
				var reportsNode = document.getElementById('reports');
				if(reportsNode){
					reportsNode.style.minHeight = height+"px";
				}
			
		});
		
	</script>
</body>
</html>
