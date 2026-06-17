<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <title>课程质量分析图表</title>
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
		.field-label {
			margin: 0 2px 0 0;
			font-weight: 600;
			color: #333;
			white-space: nowrap;
		}
		.chart-grid {
			display: grid;
			grid-template-columns: 1fr 1fr;
			gap: 10px;
			padding: 10px;
		}
		.chart-box {
			height: 360px;
			background: #fff;
			border: 1px solid #ddd;
		}
		.scatter-panel {
			grid-column: 1 / span 2;
			background: #fff;
			border: 1px solid #ddd;
		}
		.scatter-toolbar {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			gap: 8px;
			padding: 8px 10px;
			border-bottom: 1px solid #e5e5e5;
		}
		.scatter-toolbar .form-control { width: 260px; max-width: 100%; }
		.chart-wide {
			height: 360px;
			border: 0;
		}
		@media (max-width: 900px) {
			.chart-grid { grid-template-columns: 1fr; }
			.scatter-panel { grid-column: auto; }
		}
	</style>
</head>
<body style="padding-top: 0px; background-color: #f1f4f5">
	<c:if test="${msg!=null}">
		<script>alert('${msg}');</script>
	</c:if>
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理
		<span class="c-gray en">&gt;</span> 课程质量分析图表
		<button class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新"><i class="fa fa-undo"></i></button>
	</nav>
    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default">
            <div class="panel-body page-toolbar">
                <form id="formSearch" class="form-inline" method="post">
                    <div class="form-group">
                   	<label class="field-label" for="lessonid">课程</label>
                   	<select class="form-control input-sm" id="lessonid" name="lessonid">
                            <c:forEach var="lesson" items="${lessonlist}" varStatus="status">
							    <option value="${lesson.id}" <c:if test="${lessonid == lesson.id}"> selected="selected"</c:if>>${lesson.name}</option>
							</c:forEach>
                        </select>
			        </div>
                </form>
            </div>
        </div>
		<div class="panel-body">
			<div class="chart-grid">
				<div id="radarChart" class="chart-box"></div>
				<div id="barChart" class="chart-box"></div>
				<div class="scatter-panel">
					<div class="scatter-toolbar">
						<label class="field-label" for="targetid">散点图课程目标</label>
						<select class="form-control input-sm" id="targetid" name="targetid">
                    		<c:forEach var="target" items="${targetlist}" varStatus="status">
							    <option value="${target.id}" <c:if test="${target.id == targetid}"> selected="selected"</c:if>>${target.targetName}</option>
							</c:forEach>
                    	</select>
					</div>
					<div id="scatterChart" class="chart-box chart-wide"></div>
				</div>
			</div>
	    </div>
	</div>
	<script type="text/javascript" src="<%=basePath %>resources/lib/echarts/echarts.min.js"></script>
	<script>
		function buildChartsUrl(lessonid, targetid){
			var url = "<%=basePath%>score/charts.html?lessonid=" + encodeURIComponent(lessonid || '');
			if(targetid){
				url += "&targetid=" + encodeURIComponent(targetid);
			}
			if(window.appendSessionRole){
				url = window.appendSessionRole(url);
			}
			return url;
		}
		function refreshByLesson(){
			var lessonid = $('#lessonid').val();
			if(lessonid){
				window.location = buildChartsUrl(lessonid, '');
			}
		}
		$('#formSearch').on('submit', function (event) {
			event.preventDefault();
			refreshByLesson();
		});
		$('#lessonid').on('change', function () {
			refreshByLesson();
		});
		var pageHeight = document.documentElement.clientHeight;
		document.getElementById('maindiv').style.minHeight = (pageHeight-40)+"px";
		var scatterData = ${scatterData};
		var scatterMap = ${scatterMap};
		var targetRateMap = ${targetRateMap};
		var targetNameMap = ${targetNameMap};
		var radarIndicators = ${radarIndicators};
		var radarData = ${radarData};
		var barNames = ${barNames};
		var barData = ${barData};
		var targetrate = ${targetrate};
		var radarChart = echarts.init(document.getElementById("radarChart"));
		var barChart = echarts.init(document.getElementById("barChart"));
		var scatterChart = echarts.init(document.getElementById("scatterChart"));
		radarChart.setOption({
			title: { text: '课程目标总体达成度', left: 'center', top: 10 },
			tooltip: {},
			radar: { indicator: radarIndicators, radius: '60%' },
			series: [{ type: 'radar', data: [{ value: radarData, name: '总体达成度', areaStyle: { opacity: 0.25 } }] }]
		});
		barChart.setOption({
			title: { text: '各课程目标达成度', left: 'center', top: 10 },
			tooltip: {},
			xAxis: { type: 'category', data: barNames, axisLabel: { interval: 0 } },
			yAxis: { type: 'value', min: 0 },
			series: [{ type: 'bar', data: barData, itemStyle: { color: '#4e79a7' } }]
		});
		function renderScatterChart(){
			var targetid = $('#targetid').val();
			var currentScatterData = scatterMap[targetid] || scatterData || [];
			var currentTargetRate = targetRateMap[targetid] || targetrate || 0;
			var currentTargetName = targetNameMap[targetid] || $('#targetid option:selected').text() || '';
			scatterChart.setOption({
			title: { text: currentTargetName + '学生达成度散点图', left: 'center', top: 10 },
			tooltip: {},
			toolbox: {
	            show: true,
	            feature: { saveAsImage: { show: true }, dataView: { show: true }, restore:{ show: true }, dataZoom:{ show: true } }
	        },
			xAxis: { name: '学生序号' },
			yAxis: { name: '达成度', min: 0 },
			series: [{
				symbolSize: 10,
				data: currentScatterData,
				type: 'scatter',
				markLine: {
					symbol: ['none', 'none'],
					itemStyle: { normal: { lineStyle: { type: 'solid', color:'red' }, label: { show: false } } },
					data: [{ yAxis: currentTargetRate }]
				}
			}]
			}, true);
		}
		renderScatterChart();
		$('#targetid').on('change', function(){
			renderScatterChart();
			if(window.history && window.history.replaceState){
				window.history.replaceState(null, '', buildChartsUrl($('#lessonid').val(), $('#targetid').val()));
			}
		});
		window.onresize = function(){
			radarChart.resize();
			barChart.resize();
			scatterChart.resize();
		};
	</script>
</body>
</html>
