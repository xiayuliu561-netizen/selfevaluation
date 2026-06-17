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
</head>
<body>
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理 
		<span class="c-gray en">&gt;</span> 考核项系数信息
		<button  class="btn btn-success btn-xs pull-right"  style="margin-top:6px; padding: 1px 5px; font-size: 12px; height: 23px;" onclick="location.replace(location.href)" title="刷新" ><i class="fa fa-undo"></i></button>
	</nav>
	<div class="page-container" id="maindiv" style="padding-top: 0px; margin-top: -10px;">
		<div id="tab-system" class="HuiTab">
			<div class="tabBar cl">
				<span>考核项系数设置</span>
			</div>
			<div class="tabCon">
				<form class="form form-horizontal" id="rateinfo" method="post" onsubmit="return check();">
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							<span class="c-red">*</span>
							课堂表现系数：</label>
						<div class="formControls col-xs-3 col-sm-4">
							<div class="input-group">
								<input type="number" id="showrate" name="showrate" value="${rate.showrate}" class="form-control" placeholder="">
								<div class="input-group-addon">%</div>
							</div>
							<input type="hidden" id="id" name="id" value="${rate.id}" class="form-control" placeholder="">
						</div>
					</div>
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							<span class="c-red">*</span>
							课程作业系数：</label>
						<div class="formControls col-xs-3 col-sm-4">
							<div class="input-group">
								<input type="number" id="homeworkrate" name="homeworkrate" value="${rate.homeworkrate}" class="form-control" placeholder="">
								<div class="input-group-addon">%</div>
							</div>
						</div>
					</div>
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							<span class="c-red">*</span>
							课程实验系数：</label>
						<div class="formControls col-xs-3 col-sm-4">
							<div class="input-group">
								<input type="number" id="testrate" name="testrate" value="${rate.testrate}" class="form-control" placeholder="">
								<div class="input-group-addon">%</div>
							</div>
						</div>
					</div>
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							<span class="c-red">*</span>
							课程设计系数：</label>
						<div class="formControls col-xs-3 col-sm-4">
							<div class="input-group">
								<input type="number" id="designrate" name="designrate" value="${rate.designrate}" class="form-control" placeholder="">
								<div class="input-group-addon">%</div>
							</div>
						</div>
					</div>
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							<span class="c-red">*</span>
							期中成绩系数：</label>
						<div class="formControls col-xs-3 col-sm-4">
							<div class="input-group">
								<input type="number" id="middlerate" name="middlerate" value="${rate.middlerate}" class="form-control" placeholder="">
								<div class="input-group-addon">%</div>
							</div>
						</div>
					</div>
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							<span class="c-red">*</span>
							期末成绩系数：</label>
						<div class="formControls col-xs-3 col-sm-4">
							<div class="input-group">
								<input type="number" id="endrate" name="endrate" value="${rate.endrate}" class="form-control" placeholder="">
								<div class="input-group-addon">%</div>
							</div>
						</div>
					</div>
					<div class="row cl" >
						<div class="col-xs-8 col-sm-9 col-xs-offset-4 col-sm-offset-2">
							<button type="submit" class="btn btn-primary radius" ><i class="Hui-iconfont">&#xe632;</i> 保存</button>
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
			
		});
		var TableInit = function () {
			//设置div自适应高度
			var height = document.documentElement.clientHeight-145;
			document.getElementById('maindiv').style.minHeight = height+"px";
		    var oTableInit = new Object();
		   
		    return oTableInit;
		};
		
		
		var ButtonInit = function () {
		    var oInit = new Object();
		    var postdata = {};
		    oInit.Init = function () {
		        //初始化页面上面的按钮事件
		    };
		    return oInit;
		};
		
		$('#btn_query').on('click', function () {
			$('#table').bootstrapTable('refresh','');
		});
		
 		function check(){
 			var showrate = $("#showrate").val();
 			var homeworkrate = $("#homeworkrate").val();
 			var testrate = $("#testrate").val();
 			var designrate = $("#designrate").val();
 			var middlerate = $("#middlerate").val();
 			var endrate = $("#endrate").val();
 			var sumrate = parseInt(showrate)+parseInt(homeworkrate)+parseInt(testrate)+parseInt(designrate)+parseInt(middlerate)+parseInt(endrate);
 			console.log(sumrate)
 			if(sumrate != 100){
 				parent.window.toastralert('error','比例总和不为100%');
 				return false;
 			}else{
	 			$.ajax({
	                cache: true,
	                type: "POST",
	                async:false, 
	                url:"<%=basePath %>rate/add.html",
	                data:$('#rateinfo').serialize(),// 你的formid
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
                		alert('操作成功');
	                }
	            });
 			}
		}
 		
	</script>
</body>
</html>
