<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>

<!DOCTYPE HTML>
<html>
<head>
	<title>个人信息设置</title>
	<!-- bootstrap -->
	<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap/css/bootstrap.min.css">
	<script type="text/javascript" src="<%=basePath %>resources/js/jquery.min.js"></script>
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
	<script type="text/javascript" src="<%=basePath %>resources/js/common.js"></script>
	<link href="<%=basePath %>resources/kindeditor/themes/default/default.css" type="text/css" rel="stylesheet">
    <script type="text/javascript" src="<%=basePath %>resources/kindeditor/kindeditor-all-min.js"></script>
    <script type="text/javascript" src="<%=basePath %>resources/kindeditor/lang/zh-CN.js"></script>
	<script>
		var editor;
		KindEditor.ready(function(K) {
			editor = K.create('textarea[name="reports"]', {
	            filePostName: "uploadFile",
	            uploadJson: '<%=basePath %>kindeditor/fileUpload.html',
	            resizeType: 1,
	            allowPreviewEmoticons: true,
	            allowImageUpload: true
			});
		});
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
			<div class="tabBar cl">
				<span>课程质量报告</span>
			</div>
			<div class="tabCon">
				<form class="form form-horizontal" id="reportsinfo" method="post" action="<%=basePath %>reports/add.html">
					<div class="row cl">
						<div class="formControls col-xs-12 col-sm-12">
							<textarea id="reports" name="reports" rows="" cols="" style="width: 100%;">${reports.reports}</textarea>
							<input type="hidden" name="id" value="${reports.id}">
						</div>
					</div>
					<div class="row cl" >
						<div class="col-xs-8 col-sm-9 col-xs-offset-4 col-sm-offset-2">
							<button type="submit" class="btn btn-primary radius" ><i class="Hui-iconfont">&#xe632;</i> 保存</button>
							<button type="button" onclick="javascript:window.open('<%=basePath %>reports/check.html')" class="btn btn-success radius" ><i class="Hui-iconfont">&#xe632;</i> 打印预览</button>
						</div>
					</div>
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
			document.getElementById('reports').style.minHeight = height+"px";
			
		});
		
	</script>
</body>
</html>
