<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <title>成绩管理</title>
	<%@ include file="common/js.jsp"%>
	<style type="text/css">
		.panel-body { padding: 0px; }
		.page-toolbar { padding: 8px 10px; }
		.page-toolbar .form-group {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			gap: 8px;
			margin: 0 !important;
		}
		.page-toolbar .form-control { width: 180px !important; max-width: 100%; }
		.page-toolbar .btn { margin-left: 0 !important; }
		.table-wrap { padding-bottom:0px; margin-top:0px !important; }
			.score-input {
				width: 76px;
				height: 26px;
				padding: 2px 6px;
				text-align: center;
			}
			.score-input-zero {
				color: #a94442;
				border-color: #d9534f;
				background-color: #f2dede;
				font-weight: 600;
			}
			.import-line {
				display: flex;
				align-items: center;
				flex-wrap: wrap;
				gap: 8px;
				margin-bottom: 10px;
			}
			.import-line .form-control { width: auto; min-width: 220px; }
			.import-summary {
				margin: 8px 0;
				padding: 8px 10px;
				border: 1px solid #d9edf7;
				background: #f7fbfd;
				color: #245269;
				line-height: 1.6;
			}
			.import-fields {
				margin: 8px 0;
				line-height: 1.8;
			}
			.import-field-tag {
				display: inline-block;
				margin: 0 6px 6px 0;
				padding: 2px 7px;
				border: 1px solid #ddd;
				background: #fafafa;
				border-radius: 3px;
			}
			.import-preview-wrap {
				max-height: 360px;
				overflow: auto;
				border: 1px solid #e5e5e5;
			}
			.import-preview-table {
				margin-bottom: 0;
				font-size: 12px;
			}
			.import-preview-table th {
				background: #f6f8fa;
				white-space: nowrap;
			}
			.import-preview-table td {
				vertical-align: middle !important;
			}
			.import-msg {
				min-height: 20px;
				color: #666;
			}
			.score-import-grid {
				display: grid;
				grid-template-columns: minmax(240px, 320px) minmax(0, 1fr);
				gap: 14px;
				align-items: start;
			}
			.score-import-side {
				padding: 14px;
				border: 1px solid var(--se-line);
				border-radius: var(--se-radius);
				background: #f8fafc;
			}
			.score-import-side .control-label {
				display: block;
				margin: 10px 0 6px;
				padding: 0;
				text-align: left;
			}
			.score-import-side .form-control {
				width: 100% !important;
			}
			.score-import-main {
				min-width: 0;
			}
			@media (max-width: 900px) {
				.score-import-grid {
					grid-template-columns: 1fr;
				}
			}
	</style>
</head>
<body style="padding-top: 0px; background-color: #f1f4f5">
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理
		<span class="c-gray en">&gt;</span> 成绩管理
		<button class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新"><i class="fa fa-undo"></i></button>
	</nav>
    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default">
            <div class="panel-body page-toolbar">
                <form id="formSearch" class="form-inline" method="post">
                    <div class="form-group">
                    	<select class="form-control input-sm" id="querylessonid" name="querylessonid">
                            <option value="">请选择课程</option>
                            <c:forEach var="lesson" items="${lessonlist}" varStatus="status">
							    <option value="${lesson.id}" <c:if test="${lessonid == lesson.id}"> selected="selected"</c:if>>${lesson.name}</option>
							</c:forEach>
                        </select>
				       	<button type="button" id="btn_query" class="btn btn-primary btn-sm">查询</button>
				       	<button type="button" id="btn_download_template" class="btn btn-primary btn-sm">下载模板</button>
				       	<button type="button" id="btn_importfile" class="btn btn-info btn-sm">上传文件</button>
				       	<button type="button" id="btn_createsumscore" class="btn btn-success btn-sm">重新计算总评</button>
                    </div>
                </form>
            </div>
        </div>
		<div class="panel-body table-wrap">
	        <div id="toolbar" class="btn-group"></div>
	        <table id="table"></table>
	    </div>
	</div>
	<div class="modal fade" id="importFileModal" tabindex="-1">
		<div class="modal-dialog" style="width: 980px; max-width: 96%;">
			<div class="modal-content">
				<div class="modal-header">
					<button class="close" data-dismiss="modal"><span>&times;</span></button>
					<h4 class="modal-title">智能导入成绩表</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal" role="form" id="importFileFrom" enctype="multipart/form-data">
						<input type="hidden" id="lessonid" name="lessonid">
						<div class="step-progress" id="scoreImportSteps">
							<div class="step-progress-item is-active" data-step="1" data-state="select"><strong>选择文件</strong><span>上传 Excel 成绩表</span></div>
							<div class="step-progress-item" data-step="2" data-state="parse"><strong>AI 识别</strong><span>解析字段与成绩项</span></div>
							<div class="step-progress-item" data-step="3" data-state="confirm"><strong>确认结果</strong><span>检查冲突和异常</span></div>
							<div class="step-progress-item" data-step="4" data-state="done"><strong>完成导入</strong><span>写入成绩数据</span></div>
						</div>
						<div class="score-import-grid">
							<div class="score-import-side">
								<label class="control-label" for="file">选择文件</label>
								<input id="file" name="file" type="file" class="form-control input-sm" accept=".xls,.xlsx">
								<label class="control-label" for="conflictStrategy">冲突策略</label>
								<select id="conflictStrategy" class="form-control input-sm">
									<option value="fill_blank">仅补充空缺项</option>
									<option value="skip">跳过已有成绩</option>
									<option value="overwrite">覆盖已有成绩</option>
								</select>
								<div class="import-msg" id="msg"></div>
							</div>
							<div class="score-import-main">
								<div id="scoreImportPreview" style="display:none;">
									<div id="importSummary" class="import-summary"></div>
									<div id="importFields" class="import-fields"></div>
									<div class="import-preview-wrap">
										<table class="table table-condensed table-bordered import-preview-table">
											<thead>
												<tr>
													<th>行号</th>
													<th>学号</th>
													<th>姓名</th>
													<th>系统姓名</th>
													<th>成绩项</th>
													<th>新成绩</th>
													<th>已有成绩</th>
													<th>动作</th>
													<th>来源</th>
													<th>说明</th>
												</tr>
											</thead>
											<tbody id="scorePreviewBody"></tbody>
										</table>
									</div>
								</div>
								<div class="empty-state" id="scoreImportEmpty">请选择成绩表并点击“识别预览”，系统会展示字段映射、冲突和异常数据。</div>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn btn-default" type="button" id="resetImportBtn">重新选择</button>
					<button class="btn btn-primary" type="button" id="importFileBtn">识别预览</button>
					<button class="btn btn-success" type="button" id="confirmImportBtn" disabled>确认导入</button>
				</div>
			</div>
		</div>
	</div>
	<script>
		var components = [];
		var importPreviewToken = '';
		var importPreviewData = null;
		$(function () {
			var pageHeight = document.documentElement.clientHeight;
			var height = pageHeight-110;
			if(height < 320){ height = 320; }
			document.getElementById('maindiv').style.minHeight = (pageHeight-40)+"px";
			loadComponentsAndTable();
		});
		function loadComponentsAndTable(){
			$.ajax({
				type: "GET",
				url: "<%=basePath%>score/components.html",
				data: {"lessonid": $('#querylessonid').val()},
				success: function(data){
					var json = typeof data == 'string' ? $.parseJSON(data) : data;
					components = json.components || [];
					initTable();
				}
			});
		}
		function initTable(){
			var height = document.documentElement.clientHeight-110;
			var columns = [{
				title: '序号',
				width: '50px',
				align:'center',
				formatter: function (value, row, index) { return index+1; }
			},{
				field: 'user.no',
				title: '学号',
				sortable: true
			},{
				field: 'user.name',
				title: '姓名',
				sortable: true
			},{
				field: 'user.deptName',
				title: '班级',
				sortable: true
			},{
				field: 'lessonentity.name',
				title: '课程',
				sortable: true
			}];
			$.each(components, function(i, component){
				columns.push({
					field: 'datamap.c'+i,
					title: component.componentName + '<br>(' + component.rate + '%)',
					sortable: true,
						formatter: function(value, row, index){
							var score = value == null ? 0 : value;
							var isZero = parseFloat(score) === 0;
							var inputClass = 'form-control input-sm score-input' + (isZero ? ' score-input-zero' : '');
							var title = isZero ? '该单项成绩为 0，请检查' : '';
							return '<input class="'+inputClass+'" title="'+title+'" data-userid="'+row.userid+'" data-component="'+htmlEscape(component.componentName)+'" value="'+score+'">';
						}
					});
				});
			columns.push({
				field: 'sumscore.sumscore',
				title: '总评成绩',
				sortable: true
			});
			columns.push({
				field: 'sumscore.remarks',
				title: '备注',
				sortable: true,
				formatter: function(value, row){
					if(row.datamap && row.datamap.warning){
						return row.datamap.warning + (value ? '；' + value : '');
					}
					return value || '';
				}
			});
			$('#table').bootstrapTable('destroy').bootstrapTable({
				url: '<%=basePath%>score/listdata.html',
				method: 'get',
				toolbar: '#toolbar',
				striped: true,
				cache: false,
				pagination: true,
				sortable: true,
				sortOrder: "asc",
				sidePagination: "client",
				pageNumber:1,
				pageSize: 20,
				pageList: [20, 50, 100],
				search: true,
				strictSearch: true,
				showColumns: true,
				minimumCountColumns: 2,
				clickToSelect: true,
				height: height,
				uniqueId: "userid",
				cardView: false,
				detailView: false,
				showExport: true,
				exportDataType: "all",
				queryParams: function(){
					return { querylessonid: $('#querylessonid').val() };
				},
				rowStyle: function(row){
					var total = row.sumscore ? parseFloat(row.sumscore.sumscore) : 0;
					if((row.datamap && row.datamap.rowStatus == 'MISSING') || total < 60){
						return {classes: 'danger'};
					}
					return {};
				},
				columns: columns
			});
		}
		$('#querylessonid').on('change', function(){
			loadComponentsAndTable();
		});
		$('#btn_query').on('click', function () {
			loadComponentsAndTable();
		});
		$('#btn_download_template').on('click', function () {
			if($('#querylessonid').val() == ''){
				parent.window.toastralert('warning','请选择课程');
				return;
			}
			window.parent.location.href = '<%=basePath%>score/downloadtemplate.html?lessonid=' + $('#querylessonid').val();
		});
		$('#btn_createsumscore').on('click', function () {
			$.ajax({
                type: "POST",
                url:"<%=basePath%>score/createsumscore.html",
                data: {"lessonid": $('#querylessonid').val()},
                error: function() {
                    parent.window.toastralert('error','网络连接错误，请联系管理员');
                },
                success: function(data) {
                	parent.window.toastralert('success',data);
                	$('#table').bootstrapTable('refresh','');
                }
            });
		});
		$('#btn_importfile').on('click', function () {
			if($('#querylessonid').val() == ''){
				parent.window.toastralert('warning','请选择课程');
				return;
			}
			$('#lessonid').val($('#querylessonid').val());
			resetScoreImportModal();
			$('#importFileModal').modal('show');
		});
		$('#importFileBtn').on('click', function () {
			if($('#file').val() == ''){
				setImportStatus('select', 'error', '未选择上传文件');
				return;
			}
			$.ajax({
                cache: false,
                type: "POST",
                async:true,
                contentType: false,
                processData:false,
                url:"<%=basePath%>score/previewimport.html",
                data:new FormData($('#importFileFrom')[0]),
                error: function(request) {
                	var message = window.SelfEvalUI && window.SelfEvalUI.ajaxErrorMessage
                		? window.SelfEvalUI.ajaxErrorMessage(request, "识别失败，请检查文件格式或稍后重试")
                		: "识别失败，请检查文件格式或稍后重试";
                	setImportStatus('select', 'error', message);
                	if(window.SelfEvalUI){
                		window.SelfEvalUI.setButtonBusy('#importFileBtn', false);
                	}
                },
                beforeSend: function(){
                	setImportStatus('parse', 'loading', 'AI 正在识别成绩表，请稍后...');
                	if(window.SelfEvalUI){
                		window.SelfEvalUI.setButtonBusy('#importFileBtn', true, '识别中');
                	}
                	$('#confirmImportBtn').prop('disabled', true);
                },
                success: function(data) {
                	if(window.SelfEvalUI){
                		window.SelfEvalUI.setButtonBusy('#importFileBtn', false);
                	}
                	var json = parseJsonResponse(data);
                	if(!json || !json.success){
                		setImportStatus('select', 'error', json && json.message ? json.message : data);
                		return;
                	}
                	renderScoreImportPreview(json.data);
                	parent.window.toastralert('success','成绩表识别完成，请确认后导入');
                }
            });
		});
		$('#confirmImportBtn').on('click', function () {
			if(!importPreviewToken){
				parent.window.toastralert('warning','请先识别成绩表');
				return;
			}
			var strategy = $('#conflictStrategy').val();
			if(strategy == 'overwrite' && importPreviewData && Number(importPreviewData.conflictCount || 0) > 0){
				if(window.SelfEvalUI && window.SelfEvalUI.confirm){
					window.SelfEvalUI.confirm({
						title: '覆盖已有成绩',
						message: '确认覆盖 ' + importPreviewData.conflictCount + ' 项已有成绩？',
						detail: '覆盖后将以本次识别结果为准，请确认预览表中的冲突项。',
						confirmText: '确认覆盖'
					}).done(function(){
						confirmScoreImport(strategy);
					});
					return;
				}else if(!confirm('确认覆盖 ' + importPreviewData.conflictCount + ' 项已有成绩？')){
					return;
				}
			}
			confirmScoreImport(strategy);
		});
		function confirmScoreImport(strategy){
			$.ajax({
				type: "POST",
				url: "<%=basePath%>score/confirmimport.html",
				data: {
					"lessonid": $('#querylessonid').val(),
					"token": importPreviewToken,
					"conflictStrategy": strategy
				},
					error: function(){
						setImportStatus('confirm', 'error', '网络连接错误，请联系管理员');
						if(window.SelfEvalUI){
							window.SelfEvalUI.setButtonBusy('#confirmImportBtn', false);
						}
						$('#confirmImportBtn').prop('disabled', false);
						parent.window.toastralert('error','网络连接错误，请联系管理员');
					},
				beforeSend: function(){
					setImportStatus('confirm', 'loading', '正在导入成绩，请稍后...');
					if(window.SelfEvalUI){
						window.SelfEvalUI.setButtonBusy('#confirmImportBtn', true, '导入中');
					}
					$('#confirmImportBtn').prop('disabled', true);
				},
				success: function(data){
					var json = parseJsonResponse(data);
					if(json && json.success){
						setImportStatus('done', 'success', json.message || '导入完成');
						parent.window.toastralert('success', json.message || '导入完成');
						$('#importFileModal').modal('hide');
						$('#table').bootstrapTable('refresh','');
						resetScoreImportModal();
					}else{
						setImportStatus('confirm', 'error', json && json.message ? json.message : data);
						$('#confirmImportBtn').prop('disabled', false);
						if(window.SelfEvalUI){
							window.SelfEvalUI.setButtonBusy('#confirmImportBtn', false);
						}
					}
				}
			});
		}
		$('#resetImportBtn').on('click', function(){
			resetScoreImportModal();
		});
		$(document).on('change', '.score-input', function(){
			var input = $(this);
			$.ajax({
				type: "POST",
				url: "<%=basePath%>score/updatescore.html",
				data: {
					"lessonid": $('#querylessonid').val(),
					"userid": input.data('userid'),
					"componentName": input.data('component'),
					"score": input.val()
				},
				success: function(data){
					var json = parseJsonResponse(data);
					if(json && json.success){
						parent.window.toastralert('success','成绩已更新');
						$('#table').bootstrapTable('refresh','');
					}else{
						parent.window.toastralert('error', (json && json.message) ? json.message : '成绩更新失败');
					}
				},
				error: function(){
					parent.window.toastralert('error','网络连接错误，请联系管理员');
				}
			});
		});
		function renderScoreImportPreview(preview){
			importPreviewData = preview || {};
			importPreviewToken = importPreviewData.token || '';
			setImportStatus('confirm', 'success', importPreviewData.message || '识别完成，请确认预览结果');
			var affected = importPreviewData.affectedComponents || [];
			var summary = '识别方式：' + htmlEscape(importPreviewData.source || '系统识别')
				+ '；表格类型：' + htmlEscape(importPreviewData.tableType || '成绩表')
				+ '；识别成绩项：' + htmlEscape(affected.length ? affected.join('、') : '无')
				+ '；共 ' + (importPreviewData.totalCount || 0) + ' 项，可处理 ' + (importPreviewData.validCount || 0)
				+ ' 项，新增 ' + (importPreviewData.createCount || 0) + ' 项，补充空缺 ' + (importPreviewData.fillCount || 0)
				+ ' 项，冲突 ' + (importPreviewData.conflictCount || 0) + ' 项，重复/跳过 '
				+ (importPreviewData.duplicateCount || 0) + ' 项，异常 ' + (importPreviewData.invalidCount || 0) + ' 项。';
			if(importPreviewData.questionScoreMessage){
				summary += '<br>' + htmlEscape(importPreviewData.questionScoreMessage);
			}
			$('#importSummary').html(summary);
			renderImportFields(importPreviewData.fieldMappings || {});
			renderImportRows(importPreviewData.items || []);
			$('#scoreImportPreview').show();
			$('#scoreImportEmpty').hide();
			if(window.SelfEvalUI){
				window.SelfEvalUI.setButtonBusy('#importFileBtn', false);
			}
			$('#confirmImportBtn').prop('disabled', !importPreviewToken || Number(importPreviewData.validCount || 0) <= 0);
		}
		function renderImportFields(mappings){
			var html = '';
			for(var key in mappings){
				if(mappings.hasOwnProperty(key)){
					html += '<span class="import-field-tag">' + htmlEscape(key) + ' → ' + htmlEscape(mappings[key]) + '</span>';
				}
			}
			$('#importFields').html(html || '<span class="text-muted">未返回字段映射</span>');
		}
		function renderImportRows(items){
			var html = '';
			var maxRows = Math.min(items.length, 300);
			for(var i=0; i<maxRows; i++){
				var item = items[i] || {};
				var rowClass = importRowClass(item);
				html += '<tr class="' + rowClass + '">';
				html += '<td>' + htmlEscape(item.rowNumber || '') + '</td>';
				html += '<td>' + htmlEscape(item.no || '') + '</td>';
				html += '<td>' + htmlEscape(item.name || '') + '</td>';
				html += '<td>' + htmlEscape(item.systemName || '') + '</td>';
				html += '<td>' + htmlEscape(item.componentName || '') + '</td>';
				html += '<td>' + htmlEscape(formatScoreValue(item.score)) + '</td>';
				html += '<td>' + htmlEscape(formatScoreValue(item.existingScore)) + '</td>';
				html += '<td>' + htmlEscape(actionText(item.action)) + '</td>';
				html += '<td>' + htmlEscape(item.scoreSource || item.totalScoreSource || '') + '</td>';
				html += '<td>' + htmlEscape(item.message || '') + '</td>';
				html += '</tr>';
			}
			if(items.length > maxRows){
				html += '<tr><td colspan="10">仅显示前 ' + maxRows + ' 项，剩余 ' + (items.length - maxRows) + ' 项将在确认时按同一策略处理。</td></tr>';
			}
			$('#scorePreviewBody').html(html || '<tr><td colspan="10">未识别到成绩数据</td></tr>');
		}
		function importRowClass(item){
			if(!item || item.valid === false || item.action == 'INVALID'){
				return 'danger';
			}
			if(item.action == 'CONFLICT'){
				return 'warning';
			}
			if(item.action == 'SKIP' || item.action == 'DUPLICATE'){
				return 'active';
			}
			return 'success';
		}
		function actionText(action){
			if(action == 'CREATE'){ return '新增'; }
			if(action == 'FILL'){ return '补充'; }
			if(action == 'CONFLICT'){ return '冲突'; }
			if(action == 'SKIP'){ return '跳过'; }
			if(action == 'DUPLICATE'){ return '重复'; }
			if(action == 'INVALID'){ return '异常'; }
			return action || '';
		}
		function formatScoreValue(value){
			if(value == null || value === ''){ return '-'; }
			var number = parseFloat(value);
			if(isNaN(number)){ return value; }
			return number.toFixed(2);
		}
		function resetScoreImportModal(){
			importPreviewToken = '';
			importPreviewData = null;
			$('#file').val('');
			setImportStatus('select', 'info', '请选择 .xls 或 .xlsx 成绩表');
			$('#scoreImportPreview').hide();
			$('#scoreImportEmpty').show();
			$('#importSummary').html('');
			$('#importFields').html('');
			$('#scorePreviewBody').html('');
			$('#conflictStrategy').val('fill_blank');
			if(window.SelfEvalUI){
				window.SelfEvalUI.setButtonBusy('#importFileBtn', false);
				window.SelfEvalUI.setButtonBusy('#confirmImportBtn', false);
			}
			$('#confirmImportBtn').prop('disabled', true);
		}
		function setImportStatus(step, type, message){
			var order = ['select', 'parse', 'confirm', 'done'];
			var activeIndex = Math.max(0, order.indexOf(step));
			$('#scoreImportSteps .step-progress-item').each(function(index){
				var item = $(this);
				item.removeClass('is-active is-done');
				if(index < activeIndex){
					item.addClass('is-done');
				}else if(index == activeIndex){
					item.addClass('is-active');
				}
			});
			if(window.SelfEvalUI && window.SelfEvalUI.setStatusMessage){
				window.SelfEvalUI.setStatusMessage('#msg', type || 'info', message || '');
			}else{
				$('#msg').html(message || '');
			}
		}
		function parseJsonResponse(data){
			if(typeof data == 'object'){
				return data;
			}
			try{
				return $.parseJSON(data);
			}catch(e){
				return null;
			}
		}
		function htmlEscape(value){
			return $('<div/>').text(value).html();
		}
	</script>
</body>
</html>
