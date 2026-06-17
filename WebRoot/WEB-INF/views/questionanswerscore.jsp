<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <title>调查问卷统计</title>
	<%@ include file="common/js.jsp"%>
	<style type="text/css">
		body { padding-top:0; background-color:#f1f4f5; }
		.stats-page { margin-top:-22px; padding:14px; min-height:560px; }
		.stats-filter { padding:12px 14px; background:#fff; border:1px solid #e5e9ef; border-radius:4px; }
		.stats-list { margin-top:14px; }
		.survey-card { background:#fff; border:1px solid #dde5ee; border-radius:4px; margin-bottom:14px; padding:16px; }
		.survey-header { display:flex; justify-content:space-between; gap:12px; align-items:flex-start; border-bottom:1px solid #edf1f5; padding-bottom:10px; margin-bottom:10px; }
		.survey-title { font-size:16px; font-weight:600; color:#263238; margin-bottom:6px; }
		.survey-meta { color:#66788a; font-size:12px; line-height:22px; }
		.survey-summary { display:flex; flex-wrap:wrap; gap:8px; justify-content:flex-end; }
		.summary-item { min-width:92px; text-align:center; border:1px solid #e3e9f0; background:#fbfcfe; border-radius:4px; padding:6px 10px; }
		.summary-item strong { display:block; color:#263238; font-size:16px; }
		.summary-item span { color:#66788a; font-size:12px; }
		.question-block { padding:12px 0; border-bottom:1px solid #edf1f5; }
		.question-block:last-child { border-bottom:0; padding-bottom:0; }
		.question-title { font-weight:600; color:#2f3b46; margin-bottom:8px; }
		.question-meta { color:#66788a; font-size:12px; margin-left:8px; font-weight:normal; }
		.option-row { display:grid; grid-template-columns:minmax(150px, 240px) 1fr 120px; gap:10px; align-items:center; margin:8px 0; }
		.option-name { color:#34495e; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
		.progress-wrap { height:14px; border-radius:7px; background:#edf2f7; overflow:hidden; }
		.progress-bar { height:14px; background:#2f80ed; min-width:0; }
		.option-count { color:#4b5b68; text-align:right; font-size:12px; }
		.empty-state { padding:42px 16px; text-align:center; color:#778899; background:#fff; border:1px dashed #cfd8e3; border-radius:4px; }
		.loading-state { padding:28px 16px; text-align:center; color:#66788a; background:#fff; border:1px solid #e5e9ef; border-radius:4px; }
		@media (max-width: 768px) {
			.survey-header { display:block; }
			.survey-summary { justify-content:flex-start; margin-top:10px; }
			.option-row { grid-template-columns:1fr; gap:4px; }
			.option-name { white-space:normal; }
			.option-count { text-align:left; }
		}
	</style>
</head>

<body>
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理
		<span class="c-gray en">&gt;</span> 调查问卷统计
		<button class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新"><i class="fa fa-undo"></i></button>
	</nav>
    <div class="stats-page" id="maindiv">
		<div class="stats-filter">
			<form id="formSearch" class="form-inline" method="post">
				<div class="form-group">
					<select class="form-control input-sm" id="lessonid" name="lessonid" style="min-width:180px;">
						<option value="">全部课程</option>
						<c:forEach var="lesson" items="${lessonlist}" varStatus="status">
							<option value="${lesson.id}">${lesson.name}</option>
						</c:forEach>
					</select>
				</div>
				<button type="button" style="margin-left:8px" id="btn_query" class="btn btn-primary btn-sm"><i class="fa fa-search"></i> 查询统计</button>
				<button type="button" style="margin-left:8px" id="btn_back" class="btn btn-success btn-sm"><i class="fa fa-refresh"></i> 重新发起测试</button>
			</form>
		</div>
		<div id="statsList" class="stats-list"></div>
	</div>

	<script>
		$(function () {
			var height = document.documentElement.clientHeight - 40;
			document.getElementById('maindiv').style.minHeight = height + 'px';
			loadStats();
		});

		$('#btn_query').on('click', function () {
			loadStats();
		});
		$('#lessonid').on('change', function () {
			loadStats();
		});
		$('#btn_back').on('click', function () {
			if(confirm('确定要重新发起测试？该操作会清空当前教师已收集的问卷答卷。')){
				$.ajax({
	                cache: true,
	                type: 'POST',
	                async:false,
	                url:'<%=basePath%>question/delscore.html',
	                data:{},
	                error: function() {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	parent.window.toastralert('success',data);
	                	loadStats();
	                }
	            });
			}
		});

		function loadStats(){
			$('#statsList').html('<div class="loading-state">正在汇总问卷数据...</div>');
			$.ajax({
				type:'GET',
				url:'<%=basePath%>question/questionanswerstatsdata.html',
				data:{lessonid:$('#lessonid').val()},
				success:function(res){
					var result = typeof res === 'string' ? JSON.parse(res) : res;
					renderStats(result.data || []);
				},
				error:function(){
					$('#statsList').html('<div class="empty-state">问卷统计加载失败，请检查网络或重新登录。</div>');
				}
			});
		}

		function renderStats(surveys){
			if(!surveys.length){
				$('#statsList').html('<div class="empty-state">当前条件下暂无问卷。请先在“调查问卷管理”中创建问卷，并等待学生提交。</div>');
				return;
			}
			var html = '';
			for(var i=0;i<surveys.length;i++){
				var survey = surveys[i];
				html += '<div class="survey-card">';
				html += '<div class="survey-header">';
				html += '<div><div class="survey-title">'+escapeHtml(survey.surveyName || '默认问卷')+'</div>';
				html += '<div class="survey-meta">课程：'+escapeHtml(survey.lessonName || '')+'　问题数：'+(survey.questionCount || 0)+'</div></div>';
				html += '<div class="survey-summary">';
				html += '<div class="summary-item"><strong>'+(survey.responseStudentCount || 0)+'</strong><span>答卷人数</span></div>';
				html += '<div class="summary-item"><strong>'+(survey.answerRecordCount || 0)+'</strong><span>答题记录</span></div>';
				html += '<div class="summary-item"><strong>'+(survey.avgScore || '0.00')+'</strong><span>平均分</span></div>';
				html += '</div></div>';
				var questions = survey.questions || [];
				for(var j=0;j<questions.length;j++){
					html += renderQuestion(questions[j], j);
				}
				html += '</div>';
			}
			$('#statsList').html(html);
		}

		function renderQuestion(question, index){
			var html = '<div class="question-block">';
			html += '<div class="question-title">'+(index + 1)+'. '+escapeHtml(question.title || '');
			html += '<span class="question-meta">答题 '+(question.answerCount || 0)+' 人，均分 '+(question.avgScore || '0.00')+'</span></div>';
			var options = question.options || [];
			for(var i=0;i<options.length;i++){
				var option = options[i];
				var rate = parseFloat((option.rate || '0').replace('%',''));
				if(isNaN(rate)){ rate = 0; }
				html += '<div class="option-row">';
				html += '<div class="option-name" title="'+escapeAttr(option.text || '')+'">'+escapeHtml(option.text || '')+'</div>';
				html += '<div class="progress-wrap"><div class="progress-bar" style="width:'+Math.max(0, Math.min(100, rate))+'%;"></div></div>';
				html += '<div class="option-count">'+(option.count || 0)+' 人 / '+(option.rate || '0.00%')+'</div>';
				html += '</div>';
			}
			html += '</div>';
			return html;
		}

		function escapeHtml(value){
			return (value == null ? '' : value + '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
		}
		function escapeAttr(value){
			return escapeHtml(value).replace(/'/g,'&#39;');
		}
	</script>
</body>
</html>
