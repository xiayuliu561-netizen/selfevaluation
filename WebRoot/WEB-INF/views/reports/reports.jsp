<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>

<!DOCTYPE HTML>
<html>
<head>
	<title>课程质量报告</title>
	<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap/css/bootstrap.min.css">
	<script type="text/javascript" src="<%=basePath %>resources/js/jquery.min.js"></script>
	<script type="text/javascript">
		window.SELF_EVAL_SESSION_ROLE = '${sessionRole}';
		function appendSessionRole(url){
			var role = window.SELF_EVAL_SESSION_ROLE || '';
			if(!role || !url || url.indexOf('sessionRole=') >= 0){ return url; }
			return url + (url.indexOf('?') >= 0 ? '&' : '?') + 'sessionRole=' + encodeURIComponent(role);
		}
		$(document).ajaxSend(function(event, jqxhr, settings){
			var currentRole = window.SELF_EVAL_SESSION_ROLE || '';
			settings.url = appendSessionRole(settings.url);
			if(settings.type && settings.type.toUpperCase() !== 'GET' && currentRole){
				if(!settings.data){ settings.data = 'sessionRole=' + encodeURIComponent(currentRole); }
				else if(typeof settings.data === 'string' && settings.data.indexOf('sessionRole=') < 0){ settings.data += '&sessionRole=' + encodeURIComponent(currentRole); }
				else if(typeof settings.data === 'object' && settings.data.sessionRole == null){ settings.data.sessionRole = currentRole; }
			}
		});
		$(function(){
			var role = window.SELF_EVAL_SESSION_ROLE || '';
			if(role){
				$('form').append('<input type="hidden" name="sessionRole" value="'+role+'">');
				$('form[action]').each(function(){ $(this).attr('action', appendSessionRole($(this).attr('action'))); });
			}
		});
	</script>
	<script src="<%=basePath %>resources/lib/bootstrap/js/bootstrap.min.js"></script>
	<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap-table/bootstrap-table.css">
	<script src="<%=basePath %>resources/lib/bootstrap-table/bootstrap-table.js"></script>
	<script src="<%=basePath %>resources/lib/bootstrap-table/bootstrap-table-zh-CN.js"></script>
	<link rel="stylesheet" href="<%=basePath %>resources/css/fonts/font-awesome/font-awesome.css">
	<link rel="stylesheet" href="<%=basePath %>resources/css/fonts/web-icons/web-icons.css">
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/lib/H-ui/css/H-ui.old.min.css" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/lib/H-ui/css/H-ui.admin.css" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/lib/iconfont/iconfont.css" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/css/skin.css"/>
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/css/style.css" />
	<script type="text/javascript" src="<%=basePath %>resources/js/common.js"></script>
	<style type="text/css">
		body { background-color: #f1f4f5; }
		.report-toolbar {
			background: #fff;
			border: 1px solid #ddd;
			padding: 10px;
			margin-bottom: 10px;
		}
		.report-toolbar .form-inline {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			gap: 8px;
			margin: 0;
		}
		.report-toolbar .form-control {
			width: 220px;
			max-width: 100%;
		}
		.report-toolbar .btn {
			margin-left: 0 !important;
		}
		#btn_ai_generate {
			color: #fff !important;
			background-color: #5bc0de !important;
			border-color: #46b8da !important;
			min-width: 118px;
		}
		#btn_ai_generate .Hui-iconfont { color: #fff !important; }
		.report-empty {
			background: #fff;
			border: 1px solid #ddd;
			padding: 28px 16px;
			text-align: center;
			color: #666;
		}
		.report-empty-title {
			margin-bottom: 12px;
			font-size: 16px;
			font-weight: 600;
			color: #333;
		}
		.report-editor {
			background: #fff;
			border: 1px solid #ddd;
		}
		.report-header {
			padding: 12px 14px;
			border-bottom: 1px solid #e5e5e5;
			display: flex;
			align-items: center;
			justify-content: space-between;
			flex-wrap: wrap;
			gap: 8px;
		}
		.report-title {
			font-size: 18px;
			font-weight: 600;
			color: #333;
		}
		.report-meta {
			color: #666;
			font-size: 13px;
		}
		.report-section {
			border-bottom: 1px solid #eee;
		}
		.report-section:last-child {
			border-bottom: 0;
		}
		.report-section-title {
			margin: 0;
			padding: 10px 14px;
			background: #f7f9fb;
			font-size: 15px;
			font-weight: 600;
			color: #333;
		}
		.report-section-body {
			min-height: 56px;
			padding: 12px 14px;
			line-height: 1.8;
			outline: none;
			color: #333;
			cursor: text;
		}
		.report-section-body:focus {
			box-shadow: inset 0 0 0 2px #5bc0de;
		}
		.report-section-body:empty:before {
			content: "暂无内容";
			color: #999;
		}
		.report-section-body table {
			width: 100%;
			max-width: 100%;
			margin: 10px 0;
			border-collapse: collapse;
		}
		.report-section-body table td,
		.report-section-body table th {
			border: 1px solid #ddd;
			padding: 6px 8px;
		}
		.report-section-body svg {
			max-width: 100%;
			height: auto;
		}
		.hidden-report-fields {
			display: none;
		}
		#aiGenerateMsg {
			color: #666;
			margin-left: 4px;
		}
	</style>
	<script>
		function buildReportUrl(url){
			if(window.appendSessionRole){
				return window.appendSessionRole(url);
			}
			return url;
		}
		function refreshpage(){
			window.location = buildReportUrl("<%=basePath%>reports/form.html?lessonid="+encodeURIComponent($('#querylessonid').val() || ''));
		}
		function openPreview(){
			syncReportSections();
			window.open(buildReportUrl('<%=basePath %>reports/check.html?lessonid=' + encodeURIComponent($('#querylessonid').val() || '')));
		}
		function downloadReport(){
			syncReportSections();
			var oldAction = $('#reportsinfo').attr('action');
			$('#reportsinfo').attr('action', buildReportUrl('<%=basePath %>reports/download.html'));
			$('#reportsinfo').attr('target', '_blank');
			$('#reportsinfo')[0].submit();
			$('#reportsinfo').attr('action', oldAction);
			$('#reportsinfo').removeAttr('target');
		}
	</script>
</head>
<body>
	<c:if test="${msg!=null}">
		<script>alert('${msg}');</script>
	</c:if>
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理
		<span class="c-gray en">&gt;</span> 课程质量报告
		<button class="btn btn-success btn-xs pull-right" style="margin-top:6px; padding: 1px 5px; font-size: 12px; height: 23px;" onclick="location.replace(location.href)" title="刷新"><i class="fa fa-undo"></i></button>
	</nav>
	<div class="page-container" id="maindiv" style="padding-top: 0px; margin-top: -10px;">
		<div class="report-toolbar">
			<form id="formSearch" class="form-inline" method="post">
				<select class="form-control input-sm" id="querylessonid" name="querylessonid">
					<c:forEach var="lesson" items="${lessonlist}" varStatus="status">
						<option value="${lesson.id}" <c:if test="${lessonid == lesson.id}"> selected="selected"</c:if>>${lesson.name}</option>
					</c:forEach>
				</select>
				<button type="button" id="btn_query" onclick="refreshpage()" class="btn btn-primary btn-sm">查询</button>
				<button type="button" class="btn btn-info radius js-ai-generate" id="btn_ai_generate"><i class="Hui-iconfont">&#xe6df;</i> AI一键生成报告</button>
				<button type="submit" form="reportsinfo" class="btn btn-primary radius"><i class="Hui-iconfont">&#xe632;</i> 保存</button>
				<button type="button" onclick="openPreview()" class="btn btn-success radius"><i class="Hui-iconfont">&#xe632;</i> 打印预览</button>
				<button type="button" onclick="downloadReport()" class="btn btn-warning radius"><i class="Hui-iconfont">&#xe640;</i> 下载Word报告</button>
				<span id="aiGenerateMsg"></span>
			</form>
		</div>

		<form class="form form-horizontal" id="reportsinfo" method="post" action="<%=basePath %>reports/add.html">
			<input type="hidden" name="id" value="${reports.id}">
			<input type="hidden" name="lessonid" value="${lessonid}">
			<div class="hidden-report-fields">
				<textarea id="reports" name="reports">${reports.reports}</textarea>
				<textarea id="content1" name="content1">${reports.content1}</textarea>
				<textarea id="content2" name="content2">${reports.content2}</textarea>
				<textarea id="content3" name="content3">${reports.content3}</textarea>
				<textarea id="content4" name="content4">${reports.content4}</textarea>
				<textarea id="content5" name="content5">${reports.content5}</textarea>
			</div>
			<c:set var="hasReportContent" value="${not empty reports.reports or not empty reports.content1 or not empty reports.content2 or not empty reports.content3 or not empty reports.content4 or not empty reports.content5}" />
			<div id="reportEmpty" class="report-empty" <c:if test="${hasReportContent}">style="display:none;"</c:if>>
				<div class="report-empty-title">${lessonname}课程教学质量分析报告</div>
				<div>当前尚未生成报告。可使用上方 AI 一键生成报告，生成后在页面中核对并调整内容。</div>
			</div>
			<div id="reportEditor" class="report-editor" <c:if test="${not hasReportContent}">style="display:none;"</c:if>>
				<div class="report-header">
					<div>
						<div class="report-title">${lessonname}课程教学质量分析报告</div>
						<div class="report-meta">课程教师：${teacher.name}　评价日期：${day}</div>
					</div>
				</div>
				<div class="report-section">
					<h4 class="report-section-title">一、课程目标（依据课程教学大纲）</h4>
					<div class="report-section-body" contenteditable="true" data-field="reports">${reports.reports}</div>
				</div>
				<div class="report-section">
					<h4 class="report-section-title">二、支撑课程目标的试题或考核依据</h4>
					<div class="report-section-body" contenteditable="true" data-field="content1">${reports.content1}</div>
				</div>
				<div class="report-section">
					<h4 class="report-section-title">三、课程目标达成情况分析</h4>
					<div class="report-section-body" contenteditable="true" data-field="content2">${reports.content2}</div>
				</div>
				<div class="report-section">
					<h4 class="report-section-title">四、课程教学自我评价及改进措施</h4>
					<div class="report-section-body" contenteditable="true" data-field="content3">${reports.content3}</div>
				</div>
				<div class="report-section">
					<h4 class="report-section-title">五、课程教学第三方评价及持续改进建议</h4>
					<div class="report-section-body" contenteditable="true" data-field="content4">${reports.content4}</div>
				</div>
				<div class="report-section">
					<h4 class="report-section-title">六、附件</h4>
					<div class="report-section-body" contenteditable="true" data-field="content5">${reports.content5}</div>
				</div>
			</div>
		</form>
	</div>
	<script type="text/javascript" src="<%=basePath %>resources/lib/layer/2.4/layer.js"></script>
	<script type="text/javascript" src="<%=basePath %>resources/lib/H-ui/js/H-ui.min.js"></script>
	<script type="text/javascript" src="<%=basePath %>resources/lib/H-ui/js/H-ui.admin.js"></script>
	<script type="text/javascript">
		$(function(){
			var height = document.documentElement.clientHeight-85;
			if(height < 420){ height = 420; }
			$('#maindiv').css('min-height', height + 'px');
			$('#reportsinfo').on('submit', function(){
				syncReportSections();
			});
		});

		$('.js-ai-generate').on('click', function(){
			var lessonid = $('#querylessonid').val();
			if(lessonid == null || lessonid == ''){
				alert('请选择课程');
				return;
			}
			syncReportSections();
			$('.js-ai-generate').prop('disabled', true);
			$('#aiGenerateMsg').html('AI 正在生成报告，篇幅较长时可能需要数分钟，请稍候...');
			$.ajax({
				type: 'POST',
				url: '<%=basePath%>reports/aigenerate.html',
				data: {'lessonid': lessonid},
				dataType: 'json',
				timeout: 620000,
				success: function(json){
					try{
						if(json.success){
							fillReportSections(json.data || {});
							$('#reportEmpty').hide();
							$('#reportEditor').show();
							$('#aiGenerateMsg').html(json.message || 'AI一键生成报告已完成，请核对后保存。');
						}else{
							$('#aiGenerateMsg').html(json.message || 'AI 生成失败');
							alert(json.message || 'AI 生成失败');
						}
					}catch(e){
						if(window.console && console.error){
							console.error(e);
						}
						var msg = '报告内容已返回，但页面暂时无法完成展示，请刷新后重试；如多次失败，请联系管理员。';
						$('#aiGenerateMsg').html(msg);
						alert(msg);
					}
				},
				error: function(xhr, status){
					var msg = status == 'timeout' ? '报告生成时间较长，请稍后重试；如多次失败，请联系管理员。' : '报告生成失败，请稍后重试；如多次失败，请联系管理员。';
					$('#aiGenerateMsg').html(msg);
					alert(msg);
				},
				complete: function(){
					$('.js-ai-generate').prop('disabled', false);
				}
			});
		});

		function fillReportSections(data){
			setSectionHtml('reports', data.reports);
			setSectionHtml('content1', data.content1);
			setSectionHtml('content2', data.content2);
			setSectionHtml('content3', data.content3);
			setSectionHtml('content4', data.content4);
			setSectionHtml('content5', data.content5);
			syncReportSections();
		}

		function setSectionHtml(field, value){
			value = value || '';
			$('.report-section-body[data-field="'+field+'"]').html(value);
			$('textarea[name="'+field+'"]').val(value);
		}

		function syncReportSections(){
			$('.report-section-body[data-field]').each(function(){
				var field = $(this).attr('data-field');
				$('textarea[name="'+field+'"]').val($(this).html());
			});
		}
	</script>
</body>
</html>
