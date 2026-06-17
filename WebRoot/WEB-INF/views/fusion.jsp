<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <title>融合数据分析</title>
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
		.page-toolbar .form-control { width: 220px !important; max-width: 100%; }
		.page-toolbar .btn { margin-left: 0 !important; }
		.content-wrap { padding: 10px; margin-top: 0 !important; }
		.result-title { font-weight: bold; margin: 12px 0 8px; }
		.analysis-msg { margin: 8px 0; color: #3c763d; }
		.table-small input { width: 90px; }
		.table-small td, .table-small th { vertical-align: middle !important; }
		.table-small .target-name { width: 220px !important; max-width: 100%; display: inline-block; }
		.table-small .target-content { width: 100% !important; min-height: 72px; resize: vertical; }
		.raw-box { display: none; margin-top: 10px; }
	</style>
</head>
<body style="padding-top: 0px; background-color: #f1f4f5">
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理
		<span class="c-gray en">&gt;</span> 融合数据分析
		<button class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新"><i class="fa fa-undo"></i></button>
	</nav>
    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default">
            <div class="panel-body page-toolbar">
                <form id="uploadForm" class="form-inline" method="post" enctype="multipart/form-data">
                    <div class="form-group">
                    	<select class="form-control input-sm" id="lessonid" name="lessonid">
                            <option value="">请选择课程</option>
                            <c:forEach var="lesson" items="${lessonlist}" varStatus="status">
							    <option value="${lesson.id}">${lesson.name}</option>
							</c:forEach>
                        </select>
                        <input id="file" name="file" type="file" class="form-control input-sm" accept=".doc,.docx,.pdf,.txt">
				       	<button type="button" id="btn_parse" class="btn btn-info btn-sm">AI分析</button>
				       	<button type="button" id="btn_save" class="btn btn-primary btn-sm">确认保存</button>
				       	<button type="button" id="btn_reload" class="btn btn-default btn-sm">读取已保存数据</button>
                    </div>
                </form>
            </div>
        </div>
		<div class="panel-body content-wrap">
			<div id="analysisMsg" class="analysis-msg"></div>
			<input type="hidden" id="analysisJson" name="analysisJson">
			<div class="result-title">总评成绩组成</div>
			<table class="table table-bordered table-striped table-small" id="componentTable">
				<thead>
					<tr><th style="width:80px;">序号</th><th>成绩组成部分</th><th style="width:160px;">总评占比(%)</th></tr>
				</thead>
				<tbody><tr><td colspan="3">暂无数据</td></tr></tbody>
			</table>
			<div class="result-title">课程目标考核比例</div>
			<div id="targetTables">暂无数据</div>
			</div>
	</div>
	<script>
		var currentData = null;
		$(function(){
			var height = document.documentElement.clientHeight-40;
			document.getElementById('maindiv').style.minHeight = height+"px";
		});
		$('#lessonid').on('change', function(){
			if($('#lessonid').val() != ''){
				loadSaved();
			}
		});
		$('#btn_reload').on('click', function(){
			loadSaved();
		});
		$('#btn_parse').on('click', function(){
			if($('#lessonid').val() == ''){
				parent.window.toastralert('warning','请选择课程');
				return;
			}
			if($('#file').val() == ''){
				parent.window.toastralert('warning','请选择教学大纲文件');
				return;
			}
			$('#analysisMsg').html('AI 分析中，请稍后...');
			$.ajax({
				cache: false,
				type: "POST",
				async: true,
				contentType: false,
				processData: false,
				url: "<%=basePath%>fusion/parse.html",
				data: new FormData($('#uploadForm')[0]),
				success: function(data){
					var json = typeof data == 'string' ? $.parseJSON(data) : data;
					if(json.success){
							currentData = json.data;
							$('#analysisJson').val(JSON.stringify(currentData));
							$('#analysisMsg').html(json.message);
							renderData(currentData);
					}else{
						$('#analysisMsg').html(json.message);
						parent.window.toastralert('error',json.message);
					}
				},
				error: function(){
					$('#analysisMsg').html('AI 分析暂时未完成，请稍后重试；如多次失败，请联系管理员。');
					parent.window.toastralert('error','AI 分析暂时未完成，请稍后重试；如多次失败，请联系管理员。');
				}
			});
		});
		$('#btn_save').on('click', function(){
			if($('#lessonid').val() == ''){
				parent.window.toastralert('warning','请选择课程');
				return;
			}
			var data = readDataFromTables();
			if(data.components.length == 0){
				parent.window.toastralert('warning','没有可保存的分析数据');
				return;
			}
			$.ajax({
				type: "POST",
				url: "<%=basePath%>fusion/save.html",
				data: {"lessonid": $('#lessonid').val(), "analysisJson": JSON.stringify(data)},
				success: function(data){
					parent.window.toastralert(data == '保存成功' ? 'success' : 'warning', data);
					$('#analysisMsg').html(data);
				},
				error: function(){
					parent.window.toastralert('error','网络连接错误，请联系管理员');
				}
			});
		});
		function loadSaved(){
			if($('#lessonid').val() == ''){
				return;
			}
			$.ajax({
				type: "GET",
				url: "<%=basePath%>fusion/get.html",
				data: {"lessonid": $('#lessonid').val()},
				success: function(data){
					var json = typeof data == 'string' ? $.parseJSON(data) : data;
					currentData = json.data;
					$('#analysisJson').val(JSON.stringify(currentData));
					$('#analysisMsg').html('已读取当前课程保存的融合分析数据');
					renderData(currentData);
				}
			});
		}
		function renderData(data){
			renderComponents(data.components || []);
			renderTargets(data.targets || []);
		}
		function renderComponents(components){
			if(components.length == 0){
				$('#componentTable tbody').html('<tr><td colspan="3">暂无数据</td></tr>');
				return;
			}
			var html = '';
			$.each(components, function(i, item){
				html += '<tr><td>'+(i+1)+'</td><td><input class="form-control input-sm component-name" value="'+htmlEscape(item.name || '')+'"></td><td><input class="form-control input-sm component-rate" value="'+(item.rate || 0)+'"></td></tr>';
			});
			$('#componentTable tbody').html(html);
		}
		function renderTargets(targets){
			if(targets.length == 0){
				$('#targetTables').html('暂无数据');
				return;
			}
			var html = '';
			$.each(targets, function(i, target){
				html += '<table class="table table-bordered table-striped table-small target-table">';
				html += '<thead><tr><th colspan="4">课程目标名称：<input class="form-control input-sm target-name" value="'+htmlEscape(target.name || ('课程目标'+(i+1)))+'"></th></tr>';
				html += '<tr><th colspan="4"><textarea class="form-control input-sm target-content" placeholder="课程目标原文，请与教学大纲保持一致">'+htmlEscape(target.content || '')+'</textarea></th></tr>';
				html += '<tr><th style="width:80px;">序号</th><th>考核方式</th><th style="width:160px;">占比(%)</th><th style="width:160px;">系数(%)</th></tr></thead><tbody>';
				$.each(target.items || [], function(j, item){
					html += '<tr><td>'+(j+1)+'</td><td><input class="form-control input-sm item-method" value="'+htmlEscape(item.method || '')+'"></td><td><input class="form-control input-sm item-weight" value="'+(item.weight || 0)+'"></td><td><input class="form-control input-sm item-coefficient" value="'+(item.coefficient || 0)+'"></td></tr>';
				});
				html += '</tbody></table>';
			});
			$('#targetTables').html(html);
		}
		function readDataFromTables(){
			var data = {"components": [], "targets": []};
			$('#componentTable tbody tr').each(function(){
				var name = $(this).find('.component-name').val();
				if(name){
					data.components.push({"name": name, "rate": parseFloat($(this).find('.component-rate').val() || 0)});
				}
			});
			$('.target-table').each(function(){
				var target = {"name": $(this).find('.target-name').val(), "content": $(this).find('.target-content').val(), "items": []};
				$(this).find('tbody tr').each(function(){
					var method = $(this).find('.item-method').val();
					if(method){
						target.items.push({
							"method": method,
							"weight": parseFloat($(this).find('.item-weight').val() || 0),
							"coefficient": parseFloat($(this).find('.item-coefficient').val() || 0)
						});
					}
				});
				data.targets.push(target);
			});
			return data;
		}
		function htmlEscape(value){
			return $('<div/>').text(value).html();
		}
	</script>
</body>
</html>
