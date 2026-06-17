<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <title>调查问卷</title>
	<%@ include file="common/js.jsp"%>
	<style type="text/css">
		body { padding-top:0; background-color:#f1f4f5; }
		.answer-page { margin-top:-22px; padding:14px; min-height:560px; }
		.answer-filter { padding:12px 14px; background:#fff; border:1px solid #e5e9ef; border-radius:4px; }
		.survey-card { background:#fff; border:1px solid #dde5ee; border-radius:4px; margin-top:14px; padding:16px; }
		.survey-title { font-size:16px; font-weight:600; color:#263238; margin-bottom:6px; }
		.survey-desc { color:#66788a; margin-bottom:10px; }
		.question-block { padding:12px 0; border-top:1px solid #edf1f5; }
		.question-title { font-weight:600; color:#2f3b46; margin-bottom:8px; }
		.option-wrap { display:flex; flex-wrap:wrap; gap:8px 18px; }
		.option-wrap label { font-weight:normal; margin-bottom:0; }
		.survey-actions { margin-top:12px; text-align:right; }
		.submitted { display:inline-block; padding:4px 8px; color:#31708f; background:#d9edf7; border:1px solid #bce8f1; border-radius:3px; font-size:12px; }
		.empty-state { padding:40px 16px; text-align:center; color:#778899; background:#fff; border:1px dashed #cfd8e3; border-radius:4px; margin-top:14px; }
		@media (max-width: 768px) {
			.option-wrap { display:block; }
			.option-wrap label { display:block; margin:6px 0; }
		}
	</style>
</head>

<body>
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理
		<span class="c-gray en">&gt;</span> 调查问卷
		<button class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新"><i class="fa fa-undo"></i></button>
	</nav>
	<c:if test="${msg!=null}">
		<script>
			alert('${msg}');
		</script>
	</c:if>
	<div class="answer-page" id="maindiv">
		<div class="answer-filter">
			<form id="formSearch" class="form-inline" method="post" action="<%=basePath%>question/questionanswer.html">
				<div class="form-group">
					<select class="form-control input-sm" id="queryid" name="queryid" style="min-width:220px;">
						<c:forEach var="score" items="${scorelist}" varStatus="status">
							<option value="${score.queryid}" <c:if test="${queryid == score.queryid}"> selected="selected"</c:if>>${score.teacher.name}-${score.lessonentity.name}</option>
						</c:forEach>
					</select>
				</div>
				<button type="button" style="margin-left:8px" id="btn_query" class="btn btn-primary btn-sm"><i class="fa fa-search"></i> 查询</button>
			</form>
		</div>

		<c:if test="${empty surveylist}">
			<div class="empty-state">当前课程暂无可填写的调查问卷。</div>
		</c:if>
		<c:forEach var="survey" items="${surveylist}" varStatus="surveyStatus">
			<div class="survey-card">
				<div class="survey-title">${survey.surveyName}</div>
				<c:if test="${survey.surveyDesc != null && survey.surveyDesc != ''}">
					<div class="survey-desc">${survey.surveyDesc}</div>
				</c:if>
				<form class="answerform" method="post" action="<%=basePath%>question/answer.html">
					<input type="hidden" name="queryid" value="${queryid}">
					<input type="hidden" name="teacherid" value="${survey.teacherid}">
					<input type="hidden" name="lessonid" value="${survey.lessonid}">
					<input type="hidden" name="surveyId" value="${survey.surveyId}">
					<c:forEach var="question" items="${survey.questions}" varStatus="questionStatus">
						<div class="question-block">
							<div class="question-title">${questionStatus.index+1}. ${question.title}</div>
							<div class="option-wrap">
								<c:forEach var="option" items="${question.options}" varStatus="optionStatus">
									<label class="radio-inline">
										<input type="radio" value="${option.index}|${option.score}" name="${question.id}" <c:if test="${optionStatus.last}">checked</c:if> <c:if test="${survey.answered}">disabled</c:if>>
										${option.text}
									</label>
								</c:forEach>
							</div>
						</div>
					</c:forEach>
					<div class="survey-actions">
						<c:choose>
							<c:when test="${survey.answered}">
								<span class="submitted"><i class="fa fa-check"></i> 已提交</span>
							</c:when>
							<c:otherwise>
								<button type="button" class="btn btn-info btn-sm btn_submit"><i class="fa fa-paper-plane"></i> 提交问卷</button>
							</c:otherwise>
						</c:choose>
					</div>
				</form>
			</div>
		</c:forEach>
	</div>

	<script>
		$(function () {
			var height = document.documentElement.clientHeight - 40;
			document.getElementById('maindiv').style.minHeight = height + 'px';
		});
		$('#btn_query').on('click', function () {
			$('#formSearch').submit();
		});
		$('.btn_submit').on('click', function () {
			var form = $(this).closest('form');
			var valid = true;
			form.find('.question-block').each(function(){
				if($(this).find('input[type=radio]:checked').length === 0){
					valid = false;
					return false;
				}
			});
			if(!valid){
				parent.window.toastralert('warning','请完成全部问题后再提交');
				return;
			}
			form.submit();
		});
	</script>
</body>
</html>
