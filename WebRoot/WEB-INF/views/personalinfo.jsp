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
</head>
<body>
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理 
		<span class="c-gray en">&gt;</span> 个人信息
		<button  class="btn btn-success btn-xs pull-right"  style="margin-top:6px; padding: 1px 5px; font-size: 12px; height: 23px;" onclick="location.replace(location.href)" title="刷新" ><i class="fa fa-undo"></i></button>
	</nav>
	<div class="page-container" id="maindiv" style="padding-top: 0px; margin-top: -10px;">
		<div id="tab-system" class="HuiTab">
			<div class="tabBar cl">
				<span>密码设置</span>
				<span>个人信息</span>
			</div>
			<div class="tabCon">
				<form class="form form-horizontal" id="personalinfo" method="post" onsubmit="return check();">
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							<span class="c-red">*</span>
							原密码：</label>
						<div class="formControls col-xs-8 col-sm-9">
							<input type="password" id="oldPwd" name="oldPwd" placeholder="请输入原来的密码" value="" class="input-text">
						</div>
					</div>
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							<span class="c-red">*</span>
							新密码：</label>
						<div class="formControls col-xs-8 col-sm-9">
							<input type="password" id="newPwd" name="newPwd"  placeholder="请输入新密码" value="" class="input-text">
						</div>
					</div>
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							<span class="c-red">*</span>
							重复新密码：</label>
						<div class="formControls col-xs-8 col-sm-9">
							<input type="password" id="confirm" name="confirm"  placeholder="请再次输入新密码" value="" class="input-text">
						</div>
					</div>
					<div class="row cl" >
						<div class="col-xs-8 col-sm-9 col-xs-offset-4 col-sm-offset-2">
							<button type="submit" class="btn btn-primary radius" ><i class="Hui-iconfont">&#xe632;</i> 保存</button>
						</div>
					</div>
				</form>
			</div>
			<div class="tabCon">
				<form class="form form-horizontal" >
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							姓名：</label>
						<div class="formControls col-xs-8 col-sm-9">
							<input type="text" value="${user.name}" class="input-text" disabled="disabled">
						</div>
					</div>
					<div class="row cl">
						<label class="form-label col-xs-4 col-sm-2">
							用户名：</label>
						<div class="formControls col-xs-8 col-sm-9">
							<input type="text" value="${user.username}" class="input-text" disabled="disabled">
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
			//1.初始化Table
		    var oTable = new TableInit();
		    oTable.Init();
		
		    //2.初始化Button的点击事件
		    var oButtonInit = new ButtonInit();
		    oButtonInit.Init();
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
 			var oldPwd = $("#oldPwd").val();
 			var newPwd = $("#newPwd").val();
 			var confirm = $("#confirm").val();
 			var regex = new RegExp(/^(?![^a-zA-Z]+$)(?!\D+$)/);
 			if(oldPwd == ''){
 				parent.window.toastralert('error','原密码不能为空');
 				return false;
 			}else if(newPwd == ''){
 				parent.window.toastralert('error','新密码不能为空');
 				return false;
 			}else if(newPwd != confirm){
 				parent.window.toastralert('error','两次输入的密码不一致');
 				return false;
 			}else{
	 			$.ajax({
	                cache: true,
	                type: "POST",
	                async:false, 
	                url:"<%=basePath %>user/changepassword.html",
	                data:$('#personalinfo').serialize(),// 你的formid
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	if(data == 'error old'){
	                		parent.window.toastralert('error','原密码输入错误');
	                	}else{
	                		alert('修改成功，请重新登录系统');
	                		window.parent.location.href = '<%=basePath%>user/login.html';
	                	}
	                }
	            });
 			}
		}
 		
	</script>
</body>
</html>
