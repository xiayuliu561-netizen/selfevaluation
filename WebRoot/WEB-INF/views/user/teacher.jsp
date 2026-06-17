<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <title>task</title>
	<%@ include file="../common/js.jsp"%> 
	<style type="text/css">
		.form-horizontal .form-group {
		    margin-right: 0px;
		}
		.panel-body {
		    padding: 0px;
		}
		.teacher-toolbar {
			padding: 8px 10px;
		}
		.teacher-toolbar .form-group {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			gap: 8px;
			margin: 0;
		}
		.teacher-toolbar .form-control {
			width: 220px;
		}
		.teacher-toolbar .btn {
			margin-left: 0 !important;
		}
		.teacher-table-wrap {
			padding-bottom: 0px;
			margin-top: 0px;
		}
	</style> 
</head>

<body style="padding-top: 0px; background-color: #f1f4f5">
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理 
		<span class="c-gray en">&gt;</span> 基础信息管理
		<span class="c-gray en">&gt;</span> 教师管理
		<button  class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新" ><i class="fa fa-undo"></i></button>
	</nav>
	    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default" >
	            <div class="panel-body teacher-toolbar">
	                <form id="formSearch" class="form-inline" method="post" >
	                    <div class="form-group">
	                        <input type="text" class="form-control input-sm" id="queryname" name="queryname"  placeholder="用户名/工号/姓名/邮箱/电话">
	                        <button type="button" id="btn_query" class="btn btn-primary btn-sm">查询</button>
	                        <button type="button" id="btn_add" class="btn btn-info btn-sm">添加</button>
	                        <button type="button" id="btn_download_template" class="btn btn-primary btn-sm">下载模板</button>
	                        <button type="button" id="btn_importfile" class="btn btn-info btn-sm">导入教师</button>
	                    </div>
	                </form>
	            </div>
	        </div> 
  
	    <div class="panel-body teacher-table-wrap">
	        <div id="toolbar" class="btn-group">
	        	<button type="button" id="btn_batch_delete" class="btn btn-danger btn-sm"><i class="icon fa-trash"></i> 批量删除</button>
	        </div>
	        <table id="table"></table>
	    </div> 
	</div>
    <!-- 新增修改模态框声明 -->
	<div class="modal fade" id="myModal" tabindex="-1">
		<!-- 窗口声明 -->
		<div class="modal-dialog"  style="width: 950px;">
			<!-- 内容声明 -->
			<div class="modal-content">
				<div class="modal-header">
					<button class="close" data-dismiss="modal"><span>&times;</span></button>
					<h4 class="modal-title" id="modal_title"></h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal" role="form" id="addfrom">
				        <div class="panel panel-info" >
				            <div class="panel-heading">教师信息</div>
				            <div class="panel-body">
				            	<div class="form-group" style="margin-top:5px; margin-right: 5px;" >
									<label class="col-sm-2 control-label">学院</label>
									<div class="col-sm-4" >
										<select class="form-control input-sm" id="collegeId" name="collegeId" >
											<option value="">请选择</option>
				                            <c:forEach var="college" items="${collegelist}" varStatus="status">
											    <option value="${college.id}">${college.collegeName}</option>
											</c:forEach>
			                            </select>
									</div>
									<label class="col-sm-2 control-label">邮箱</label>
									<div class="col-sm-4" >
										<input id="email" name="email" type="text" class="form-control input-sm" placeholder="请输入邮箱" autocomplete="off">
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
			                    	<label class="col-sm-2 control-label">工号</label>
									<div class="col-sm-4" >
										<input id="no" name="no" type="text" class="form-control input-sm" placeholder="请输入工号" autocomplete="off">
										<input id="id" name="id" type="hidden" >
										<input id="isadmin" name="isadmin" value="1" type="hidden" >
									</div>
									<label class="col-sm-2 control-label">姓名</label>
									<div class="col-sm-4" >
										<input id="name" name="name" type="text" class="form-control input-sm" placeholder="请输入姓名" autocomplete="off">
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
	                            	<label class="col-sm-2 control-label">用户名</label>
									<div class="col-sm-4" >
										<input id="username" name="username" type="text" class="form-control input-sm" placeholder="请输入用户名" autocomplete="off">
									</div>
									<label class="col-sm-2 control-label">密码</label>
									<div class="col-sm-4" >
										<input id="password" name="password" type="password" class="form-control input-sm" placeholder="请输入密码" autocomplete="off">
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
	                            	<label class="col-sm-2 control-label">性别</label>
									<div class="col-sm-4" >
										<select id="sex" name="sex" class="form-control input-sm">
					                		<option value="男" <c:if test="${'男' == user.sex}"> selected="selected"</c:if>>男</option>
					                		<option value="女" <c:if test="${'女' == user.sex}"> selected="selected"</c:if>>女</option>
					                	</select>
									</div>
									<label class="col-sm-2 control-label">电话</label>
									<div class="col-sm-4" >
										<input id="tel" name="tel" type="text" class="form-control input-sm" placeholder="请输入电话" autocomplete="off">
									</div>
			                    </div>
				            </div>
				        </div>
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" type="button" id="add">新增</button>
					<button class="btn btn-warning" type="button" id="update">修改</button>
				</div>
			</div>
		</div>
	</div>
	<div class="modal fade" id="importFileModal" tabindex="-1">
		<div class="modal-dialog" style="width: 650px; max-width: 96%;">
			<div class="modal-content">
				<div class="modal-header">
					<button class="close" data-dismiss="modal"><span>&times;</span></button>
					<h4 class="modal-title">导入教师</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal" role="form" id="importFileFrom" enctype="multipart/form-data">
			        <div class="panel panel-info">
			            <div class="panel-heading">上传</div>
			            <div class="panel-body" style="padding: 10px;">
		                    <div class="form-group" style="margin-top:5px; margin-right: 5px;">
	                            <label class="col-sm-2 control-label">选择文件</label>
									<div class="col-sm-8">
										<input id="file" name="file" type="file" class="form-control input-sm" accept=".xls,.xlsx">
									</div>
		                    </div>
			            </div>
			            <div class="panel-body" style="margin-left: 50px;" id="msg"></div>
			        </div>
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" type="button" id="importFileBtn">上传</button>
				</div>
			</div>
		</div>
	</div>
	<script>
		<!--

		$(function () {
		    //1.初始化Table
		    var oTable = new TableInit();
		    oTable.Init();
		
		    //2.初始化Button的点击事件
		    var oButtonInit = new ButtonInit();
		    oButtonInit.Init();

		});
		var TableInit = function () {
			//设置div自适应高度
			var pageHeight = document.documentElement.clientHeight;
			var height = pageHeight-110;
			if(height < 320){
				height = 320;
			}
			document.getElementById('maindiv').style.minHeight = (pageHeight-40)+"px";
		    var oTableInit = new Object();
		    //初始化Table
		    oTableInit.Init = function () {
		        $('#table').bootstrapTable({
		            url: '<%=basePath%>user/listdata.html',         //请求后台的URL（*）
		            method: 'get',                      //请求方式（*）
		            toolbar: '#toolbar',                //工具按钮用哪个容器
		            striped: true,                      //是否显示行间隔色
		            cache: false,                       //是否使用缓存，默认为true，所以一般情况下需要设置一下这个属性（*）
		            pagination: true,                   //是否显示分页（*）
		            sortable: true,                     //是否启用排序
		            sortOrder: "asc",                   //排序方式
		            queryParams: oTableInit.queryParams,//传递参数（*）
		            sidePagination: "client",           //分页方式：client客户端分页，server服务端分页（*）
		            pageNumber:1,                       //初始化加载第一页，默认第一页
		            pageSize: 20,                       //每页的记录行数（*）
		            pageList: [20, 50, 100],        //可供选择的每页的行数（*）
		            search: false,                      //使用上方查询框走后台精确查询
		            strictSearch: true,
		            showColumns: true,                  //是否显示所有的列
		            //showRefresh: true,                  //是否显示刷新按钮
		            minimumCountColumns: 2,             //最少允许的列数
		            clickToSelect: true,                //是否启用点击选中行
		            height: height,                  //行高，如果没有设置height属性，表格自动根据记录条数觉得表格高度
		            uniqueId: "id",                     //每一行的唯一标识，一般为主键列
		            //showToggle:true,                    //是否显示详细视图和列表视图的切换按钮
		            cardView: false,                    //是否显示详细视图
		            detailView: false,                  //是否显示父子表
		            showExport: true,                   //是否显示导出
            		exportDataType: "all",              //basic', 'all', 'selected'.
            		//得到查询的参数
	                queryParams : function (params) {
	                    var temp = {   
	                        queryname:$("#queryname").val(),
	                        isadmin:1
	                    };
	                    return temp;
	                },
		            columns: [{
		            	checkbox: true,
		            	width: '36px',
		            	align: 'center'
		            },{  
                        //field: 'Number',//可不加  
                        title: '序号',//标题  可不加  
                        width: '50px',
                        align:'center',
                        formatter: function (value, row, index) {  
                            return index+1;  
                        }  
                    },{
		                field: 'collegeName',
		                title: '学院',
		                sortable: true
		            },{
		                field: 'username',
		                title: '用户名',
		                sortable: true
		            },{
		                field: 'no',
		                title: '工号',
		                sortable: true
		            },{
		                field: 'name',
		                title: '姓名',
		                sortable: true
		            },{
		                field: 'sex',
		                title: '性别',
		                sortable: true
		            },{
		                field: 'email',
		                title: '邮箱',
		                sortable: true
		            },{
		                field: 'tel',
		                title: '电话',
		                sortable: true
		            },{
		                title: '操作', 
		                align: 'center',
		                width: '120px',
		                formatter:function(value,row,index){
						    var element = 
							    "<button title='修改' class='btn btn-warning btn-xs' onclick='toupdate("+row.id+");'><i class='icon fa-edit'></i></button> "+ 
							    "<button title='删除' class='btn btn-danger btn-xs' onclick='del(\""+row.id +"\");'><i class='icon fa-close'></i></button> ";
						    return element;  
						} 
		            }]
		        });
		    };
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
		//点击添加按钮，模态框出现
		$('#btn_add').on('click', function () {
			$('#modal_title').html('添加');
			$('#email').val('');
			$('#collegeId').val('');
			$('#username').val('');
			$('#password').val('');
			$('#no').val('');
			$('#name').val('');
			$('#sex').val('');
			$('#tel').val('');
			$('#id').val('');
			$('#update').hide();
            $('#add').show();
			$('#myModal').modal('show');
		});
		
		//添加
			$('#add').on('click', function () {
				if(formcheck()){
					$.ajax({
	                cache: true,
	                type: "POST",
	                url:"<%=basePath %>user/add.html",
	                data:$('#addfrom').serialize(),
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	if("新增成功"== data){
	                		parent.window.toastralert('success',data);
	                		window.location = "<%=basePath%>user/teacherlist.html";
	                	}else{
	                		parent.window.toastralert('error',data);
	                	}
	                }
	            });
			}
		});
		//修改弹窗
		function getRowById(id){
			var rows = $('#table').bootstrapTable('getData');
			for(var i=0; i<rows.length; i++){
				if(rows[i].id == id){
					return rows[i];
				}
			}
			return {};
		}
		function toupdate(id){
			var row = getRowById(id);
			$('#modal_title').html('修改');
			$('#id').val(row.id);
			$('#email').val(row.email);
			$('#collegeId').val(row.collegeId);
			$('#username').val(row.username);
			$('#password').val(row.password);
			$('#no').val(row.no);
			$('#name').val(row.name);
			$('#sex').val(row.sex);
			$('#tel').val(row.tel);
			$('#update').show();
            $('#add').hide();
			$('#myModal').modal('show');
			
		}
		//修改
			$('#update').on('click', function () {
				if(formcheck()){
					$.ajax({
	                cache: true,
	                type: "POST",
	                url:"<%=basePath %>user/update.html",
	                data:$('#addfrom').serialize(),
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	if("修改成功"== data){
	                		parent.window.toastralert('success',data);
	                		window.location = "<%=basePath%>user/teacherlist.html";
	                	}else{
	                		parent.window.toastralert('error',data);
	                	}
	                }
	            });
			}
		});
			//删除按钮
			function del(id){
				window.SelfEvalUI.confirmDanger("确定要删除该教师信息？", "删除后该教师账号将无法继续使用，请确认当前记录。", function(){
					$.ajax({
	                cache: true,
	                type: "POST",
	                url:"<%=basePath%>user/del.html",
	                data:{"id":id},
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	parent.window.toastralert('success',data);
	                	if("删除成功"== data){
	                		window.location = "<%=basePath%>user/teacherlist.html";
	                	}
	                }
	            });
			});
			
		}
		$('#btn_batch_delete').on('click', function () {
			var rows = $('#table').bootstrapTable('getSelections');
			if(rows.length == 0){
				parent.window.toastralert('warning','请选择要删除的教师');
				return;
			}
			var ids = [];
			for(var i=0; i<rows.length; i++){
				ids.push(rows[i].id);
			}
			window.SelfEvalUI.confirmDanger("确定要删除选中的 " + ids.length + " 条教师信息？", "批量删除后不可恢复，请确认已选记录。", function(){
				$.ajax({
	                cache: true,
	                type: "POST",
	                url:"<%=basePath%>user/dels.html",
	                data:{"ids":ids.join(',')},
	                error: function(request) {
	                    window.SelfEvalUI.toastAjaxError(request);
	                },
	                success: function(data) {
	                	parent.window.toastralert(data == "删除成功" ? 'success' : 'error',data);
	                	if("删除成功"== data){
	                		$('#table').bootstrapTable('refresh','');
	                	}
	                }
	            });
			});
		});
		//数据校验
		function formcheck(){
			var isvalid = true;
			if($('#collegeId').val()== null || $('#collegeId').val()== ''){
				parent.window.toastralert('error','学院不能为空');
				isvalid = false;
			}
			if($('#no').val()== null || $('#no').val()== ''){
				parent.window.toastralert('error','工号不能为空');
				isvalid = false;
			}
			if($('#username').val()== null || $('#username').val()== ''){
				parent.window.toastralert('error','用户名不能为空');
				isvalid = false;
			}
			if($('#password').val()== null || $('#password').val()== ''){
				parent.window.toastralert('error','密码不能为空');
				isvalid = false;
			}
			if($('#tel').val()== null || $('#tel').val()== ''){
				parent.window.toastralert('error','电话不能为空');
				isvalid = false;
			}
			return isvalid;
		}
		function changeCollege(value){
			$.ajax({
                cache: true,
                type: "POST",
                url:"<%=basePath %>dept/getByCollegeId.html",
                data:{"collegeId":value},
                error: function(request) {
                    parent.window.toastralert('error','网络连接错误，请联系管理员');
                },
                success: function(data) {
                	var dataObj = typeof data == 'string' ? $.parseJSON(data) : data;
					var dept = document.getElementById("deptId");
					if(dataObj != null){
						dept.options.length=0;  
						dept.options.add(new Option("请选择",""));
						for(var i = 0;i<dataObj.length;i++){
							var item=dataObj[i];
							dept.options.add(new Option(item.deptName,item.id));
						}
					}
                }
            });
		}
		$('#btn_download_template').on('click', function () {
			window.parent.location.href = '<%=basePath%>user/teachertemplate.html';
		});
			$('#btn_importfile').on('click', function () {
				$('#file').val('');
			window.SelfEvalUI.setStatusMessage('#msg', 'info', '请选择教师模板 Excel 文件');
				$('#importFileModal').modal('show');
			});
			$('#importFileBtn').on('click', function () {
				if($('#file').val() == ''){
				window.SelfEvalUI.setStatusMessage('#msg', 'error', '未选择上传文件');
				return;
			}
			$.ajax({
                cache: false,
                type: "POST",
                async:true, 
                contentType: false,
                processData:false,
                url:"<%=basePath %>user/import.html",
                data:new FormData($('#importFileFrom')[0]),
	                error: function() {
	                	window.SelfEvalUI.setStatusMessage('#msg', 'error', '网络连接错误，请联系管理员');
	                	window.SelfEvalUI.setButtonBusy('#importFileBtn', false);
	                },
	                beforeSend: function(){
	                	window.SelfEvalUI.setStatusMessage('#msg', 'loading', '正在上传并导入教师数据，请稍后...');
	                	window.SelfEvalUI.setButtonBusy('#importFileBtn', true, '上传中');
	                },
                success: function(data) {
	                	window.SelfEvalUI.setButtonBusy('#importFileBtn', false);
	                	window.SelfEvalUI.setStatusMessage('#msg', data.indexOf('成功') >= 0 ? 'success' : 'warning', data);
	                	parent.window.toastralert(data.indexOf('成功') >= 0 ? 'success' : 'warning', data);
	                	if("导入成功"== data){
	                		window.location = "<%=basePath%>user/teacherlist.html";
	                	}else if(data.indexOf('导入成功') >= 0){
	                		$('#importFileModal').modal('hide');
	                		$('#table').bootstrapTable('refresh','');
	                	}
                }
            });
		});
        //-->
	</script>
</body>
</html>


