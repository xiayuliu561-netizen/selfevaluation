<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <title>调查问卷管理</title>
	<%@ include file="common/js.jsp"%>
	<style type="text/css">
		body { padding-top:0; background-color:#f1f4f5; }
		.survey-page { margin-top:-22px; padding:14px; min-height:560px; }
		.survey-filter { padding:12px 14px; background:#fff; border:1px solid #e5e9ef; border-radius:4px; }
		.survey-filter .form-control { min-width:160px; }
		.survey-list { margin-top:14px; }
		.survey-card { background:#fff; border:1px solid #dde5ee; border-radius:4px; margin-bottom:12px; padding:14px 16px; }
		.survey-card-header { display:flex; align-items:flex-start; justify-content:space-between; gap:12px; }
		.survey-title { font-size:16px; font-weight:600; color:#263238; margin-bottom:6px; }
		.survey-meta { color:#66788a; font-size:12px; line-height:22px; }
		.survey-desc { margin-top:8px; color:#4b5b68; }
		.question-preview { margin-top:12px; padding-top:10px; border-top:1px solid #edf1f5; }
		.question-preview ol { padding-left:20px; margin-bottom:0; }
		.question-preview li { margin-bottom:6px; }
		.empty-state { padding:40px 16px; text-align:center; color:#778899; background:#fff; border:1px dashed #cfd8e3; border-radius:4px; }
		.modal-lg { width:900px; max-width:96%; }
		.question-editor { border:1px solid #dde5ee; border-radius:4px; padding:12px; margin-bottom:12px; background:#fbfcfe; }
		.question-editor-title { display:flex; align-items:center; justify-content:space-between; gap:10px; margin-bottom:10px; }
		.option-row { display:flex; align-items:center; gap:8px; margin-bottom:8px; }
		.option-row .option-text { flex:1; }
		.option-row .option-score { width:100px; }
		.option-row .btn { flex:0 0 auto; }
		.editor-actions { display:flex; align-items:center; justify-content:space-between; gap:10px; }
		@media (max-width: 768px) {
			.survey-card-header, .editor-actions { display:block; }
			.survey-card-header .btn-group, .editor-actions .btn-group { margin-top:8px; }
			.option-row { flex-wrap:wrap; }
			.option-row .option-score { width:120px; }
		}
	</style>
</head>

<body>
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理
		<span class="c-gray en">&gt;</span> 调查问卷管理
		<button class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新"><i class="fa fa-undo"></i></button>
	</nav>
	<div class="survey-page" id="maindiv">
		<div class="survey-filter">
			<form id="formSearch" class="form-inline" method="post">
				<div class="form-group">
					<select class="form-control input-sm" id="lessonidFilter" name="lessonid">
						<option value="">全部课程</option>
						<c:forEach var="lesson" items="${lessonlist}" varStatus="status">
							<option value="${lesson.id}">${lesson.name}</option>
						</c:forEach>
					</select>
				</div>
				<div class="form-group" style="margin-left:8px;">
					<input type="text" class="form-control input-sm" id="queryname" name="queryname" placeholder="搜索问卷或题目">
				</div>
				<button type="button" id="btn_query" class="btn btn-primary btn-sm" style="margin-left:8px;"><i class="fa fa-search"></i> 查询</button>
				<button type="button" id="btn_add" class="btn btn-info btn-sm" style="margin-left:8px;"><i class="fa fa-plus"></i> 新建问卷</button>
			</form>
		</div>
		<div id="surveyList" class="survey-list"></div>
	</div>

	<div class="modal fade" id="surveyModal" tabindex="-1">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
					<button class="close" data-dismiss="modal"><span>&times;</span></button>
					<h4 class="modal-title" id="modal_title">问卷编辑</h4>
				</div>
				<div class="modal-body">
					<form id="surveyForm">
						<input type="hidden" id="surveyId" name="surveyId">
						<input type="hidden" id="surveyKey" name="surveyKey">
						<div class="row">
							<div class="col-sm-4">
								<div class="form-group">
									<label>所属课程</label>
									<select class="form-control input-sm" id="lessonid" name="lessonid">
										<c:forEach var="lesson" items="${lessonlist}" varStatus="status">
											<option value="${lesson.id}">${lesson.name}</option>
										</c:forEach>
									</select>
								</div>
							</div>
							<div class="col-sm-8">
								<div class="form-group">
									<label>问卷名称</label>
									<input type="text" class="form-control input-sm" id="surveyName" name="surveyName" placeholder="例如：课程满意度调查、学习达成情况问卷">
								</div>
							</div>
						</div>
						<div class="form-group">
							<label>问卷说明</label>
							<textarea class="form-control input-sm" id="surveyDesc" name="surveyDesc" rows="2" placeholder="可填写答题说明或问卷用途"></textarea>
						</div>
						<div id="questionEditorList"></div>
						<div class="editor-actions">
							<button type="button" class="btn btn-default btn-sm" id="btnAddQuestion"><i class="fa fa-plus"></i> 添加问题</button>
							<div class="btn-group">
								<button type="button" class="btn btn-primary" id="btnSaveSurvey"><i class="fa fa-save"></i> 保存问卷</button>
								<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
							</div>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>

	<script>
		var surveyData = [];
		var defaultOptions = [
			{text:'完全不达成(1分)', score:'20'},
			{text:'基本不达成(2分)', score:'40'},
			{text:'基本达成(3分)', score:'60'},
			{text:'达成(4分)', score:'80'},
			{text:'完全达成(5分)', score:'100'}
		];

		$(function () {
			var height = document.documentElement.clientHeight - 40;
			document.getElementById('maindiv').style.minHeight = height + 'px';
			loadSurveys();
		});

		$('#btn_query').on('click', function () {
			loadSurveys();
		});
		$('#lessonidFilter').on('change', function () {
			loadSurveys();
		});
		$('#btn_add').on('click', function () {
			openSurveyEditor(null);
		});
		$('#btnAddQuestion').on('click', function () {
			addQuestionEditor({title:'', options:defaultOptions});
		});
		$('#btnSaveSurvey').on('click', function () {
			saveSurvey();
		});

		function loadSurveys(){
			$('#surveyList').html('<div class="empty-state">正在加载问卷...</div>');
			$.ajax({
				type:'GET',
				url:'<%=basePath%>question/surveylistdata.html',
				data:{lessonid:$('#lessonidFilter').val(), queryname:$('#queryname').val()},
				success:function(res){
					var result = typeof res === 'string' ? JSON.parse(res) : res;
					surveyData = result.data || [];
					renderSurveyList();
				},
				error:function(){
					$('#surveyList').html('<div class="empty-state">问卷加载失败，请检查网络或重新登录。</div>');
				}
			});
		}

		function renderSurveyList(){
			if(!surveyData.length){
				$('#surveyList').html('<div class="empty-state">暂无问卷，请点击“新建问卷”添加。</div>');
				return;
			}
			var html = '';
			for(var i=0;i<surveyData.length;i++){
				var survey = surveyData[i];
				html += '<div class="survey-card">';
				html += '<div class="survey-card-header">';
				html += '<div><div class="survey-title">'+escapeHtml(survey.surveyName || '默认问卷')+'</div>';
				html += '<div class="survey-meta">课程：'+escapeHtml(survey.lessonName || '')+'　问题数：'+(survey.questionCount || 0)+'</div>';
				if(survey.surveyDesc){ html += '<div class="survey-desc">'+escapeHtml(survey.surveyDesc)+'</div>'; }
				html += '</div><div class="btn-group">';
				html += '<button type="button" class="btn btn-warning btn-xs" onclick="openSurveyEditorByIndex('+i+')"><i class="fa fa-edit"></i> 编辑</button>';
				html += '<button type="button" class="btn btn-danger btn-xs" onclick="deleteSurveyByIndex('+i+')"><i class="fa fa-trash"></i> 删除</button>';
				html += '</div></div>';
				html += '<div class="question-preview"><ol>';
				var questions = survey.questions || [];
				for(var j=0;j<questions.length && j<5;j++){
					html += '<li>'+escapeHtml(questions[j].title || '')+'</li>';
				}
				if(questions.length > 5){ html += '<li>...</li>'; }
				html += '</ol></div></div>';
			}
			$('#surveyList').html(html);
		}

		function openSurveyEditorByIndex(index){
			openSurveyEditor(surveyData[index]);
		}

		function openSurveyEditor(survey){
			$('#questionEditorList').empty();
			if(survey){
				$('#modal_title').text('编辑问卷');
				$('#surveyId').val(survey.surveyId || '');
				$('#surveyKey').val(survey.surveyKey || '');
				$('#lessonid').val(survey.lessonid || '');
				$('#surveyName').val(survey.surveyName || '');
				$('#surveyDesc').val(survey.surveyDesc || '');
				var questions = survey.questions || [];
				for(var i=0;i<questions.length;i++){
					addQuestionEditor(questions[i]);
				}
			}else{
				$('#modal_title').text('新建问卷');
				$('#surveyId').val('');
				$('#surveyKey').val('');
				$('#lessonid').val($('#lessonidFilter').val() || $('#lessonid option:first').val());
				$('#surveyName').val('');
				$('#surveyDesc').val('');
				addQuestionEditor({title:'', options:defaultOptions});
			}
			$('#surveyModal').modal('show');
		}

		function addQuestionEditor(question){
			var index = $('#questionEditorList .question-editor').length + 1;
			var html = '<div class="question-editor">';
			html += '<div class="question-editor-title"><strong>问题 <span class="question-no">'+index+'</span></strong>';
			html += '<button type="button" class="btn btn-danger btn-xs" onclick="removeQuestionEditor(this)"><i class="fa fa-trash"></i> 删除问题</button></div>';
			html += '<div class="form-group"><input type="text" class="form-control input-sm question-title" placeholder="请输入问题内容" value="'+escapeAttr(question.title || '')+'"></div>';
			html += '<div class="option-list"></div>';
			html += '<button type="button" class="btn btn-default btn-xs" onclick="addOptionEditor(this, null)"><i class="fa fa-plus"></i> 添加选项</button>';
			html += '</div>';
			$('#questionEditorList').append(html);
			var editor = $('#questionEditorList .question-editor:last');
			var options = question.options && question.options.length ? question.options : defaultOptions;
			for(var i=0;i<options.length;i++){
				addOptionEditor(editor.find('.btn-default')[0], options[i]);
			}
			refreshQuestionNo();
		}

		function addOptionEditor(button, option){
			var editor = $(button).closest('.question-editor');
			var optionList = editor.find('.option-list');
			var html = '<div class="option-row">';
			html += '<input type="text" class="form-control input-sm option-text" placeholder="选项文字" value="'+escapeAttr(option ? option.text : '')+'">';
			html += '<input type="number" min="0" max="100" class="form-control input-sm option-score" placeholder="分值" value="'+escapeAttr(option ? option.score : '')+'">';
			html += '<button type="button" class="btn btn-danger btn-xs" onclick="removeOptionEditor(this)"><i class="fa fa-close"></i></button>';
			html += '</div>';
			optionList.append(html);
		}

		function removeQuestionEditor(button){
			if($('#questionEditorList .question-editor').length <= 1){
				parent.window.toastralert('warning','至少保留一个问题');
				return;
			}
			$(button).closest('.question-editor').remove();
			refreshQuestionNo();
		}

		function removeOptionEditor(button){
			var optionList = $(button).closest('.option-list');
			if(optionList.find('.option-row').length <= 2){
				parent.window.toastralert('warning','每个问题至少保留两个选项');
				return;
			}
			$(button).closest('.option-row').remove();
		}

		function refreshQuestionNo(){
			$('#questionEditorList .question-editor').each(function(i){
				$(this).find('.question-no').text(i + 1);
			});
		}

		function saveSurvey(){
			var payload = collectSurveyPayload();
			if(!payload){ return; }
			$.ajax({
				type:'POST',
				url:'<%=basePath%>question/savesurvey.html',
				data:{
					lessonid:$('#lessonid').val(),
					surveyId:$('#surveyId').val(),
					surveyKey:$('#surveyKey').val(),
					surveyName:$('#surveyName').val(),
					surveyDesc:$('#surveyDesc').val(),
					surveyJson:JSON.stringify(payload)
				},
				success:function(data){
					if(data === '保存成功'){
						parent.window.toastralert('success', data);
						$('#surveyModal').modal('hide');
						loadSurveys();
					}else{
						parent.window.toastralert('error', data);
					}
				},
				error:function(){
					parent.window.toastralert('error','网络连接错误，请联系管理员');
				}
			});
		}

		function collectSurveyPayload(){
			if(!$('#lessonid').val()){
				parent.window.toastralert('warning','请选择课程');
				return null;
			}
			if(!$.trim($('#surveyName').val())){
				parent.window.toastralert('warning','请输入问卷名称');
				return null;
			}
			var payload = {questions:[]};
			var valid = true;
			$('#questionEditorList .question-editor').each(function(i){
				var title = $.trim($(this).find('.question-title').val());
				var question = {title:title, options:[]};
				if(!title){
					parent.window.toastralert('warning','第'+(i+1)+'个问题未填写题目');
					valid = false;
					return false;
				}
				$(this).find('.option-row').each(function(){
					var text = $.trim($(this).find('.option-text').val());
					var score = $.trim($(this).find('.option-score').val());
					if(text){
						question.options.push({text:text, score:score || '0'});
					}
				});
				if(question.options.length < 2){
					parent.window.toastralert('warning','第'+(i+1)+'个问题至少需要两个选项');
					valid = false;
					return false;
				}
				payload.questions.push(question);
			});
			return valid ? payload : null;
		}

		function deleteSurveyByIndex(index){
			var survey = surveyData[index];
			if(!confirm('确定要删除该问卷？已提交的该问卷答卷也会同步清理。')){
				return;
			}
			$.ajax({
				type:'POST',
				url:'<%=basePath%>question/delsurvey.html',
				data:{lessonid:survey.lessonid, surveyId:survey.surveyId, surveyKey:survey.surveyKey},
				success:function(data){
					parent.window.toastralert('success', data);
					loadSurveys();
				},
				error:function(){
					parent.window.toastralert('error','网络连接错误，请联系管理员');
				}
			});
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
