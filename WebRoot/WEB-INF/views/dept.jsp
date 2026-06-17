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
	<%@ include file="common/js.jsp"%> 
	<style type="text/css">
		.form-horizontal .form-group {
		    margin-right: 0px;
		}
		.panel-body {
		    padding: 0px;
		}
		.page-toolbar {
			padding: 8px 10px;
		}
		.page-toolbar .form-group {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			gap: 8px;
			margin: 0;
		}
		.page-toolbar .form-control {
			width: 180px;
		}
		.page-toolbar .btn {
			margin-left: 0 !important;
		}
		.table-wrap {
			padding-bottom: 0px;
			margin-top: 0px;
		}
	</style> 
</head>

<body style="padding-top: 0px; background-color: #f1f4f5">
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理 
		<span class="c-gray en">&gt;</span> 基础信息管理
		<span class="c-gray en">&gt;</span> 班级管理
		<button  class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新" ><i class="fa fa-undo"></i></button>
	</nav>
    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default" >
            <div class="panel-body page-toolbar">
                <form id="formSearch" class="form-inline" method="post" >
                    <div class="form-group">
                            <input type="text" class="form-control input-sm" id="queryname" name="queryname"  placeholder="请输入名称">
                            <button type="button" style="margin-left:10px" id="btn_query" class="btn btn-primary btn-sm">查询</button>
                            <span class="help-block" style="display:inline-block;margin:0;color:#777;">班级信息可在学生名单导入时自动补全，也可在此维护</span>
                    </div>
                </form>
            </div>
        </div> 
  
	    <div class="panel-body table-wrap">
	        <div id="toolbar" class="btn-group">
	        	<button type="button" id="btn_batch_delete" class="btn btn-danger btn-sm"><i class="icon fa-trash"></i> 批量删除</button>
	        </div>
	        <table id="table"></table>
	    </div> 
	</div>
    <!-- 新增修改模态框声明 -->
	<div class="modal fade" id="myModal" tabindex="-1">
		<!-- 窗口声明 -->
		<div class="modal-dialog"  style="width: 650px;">
			<!-- 内容声明 -->
			<div class="modal-content">
				<div class="modal-header">
					<button class="close" data-dismiss="modal"><span>&times;</span></button>
					<h4 class="modal-title" id="modal_title"></h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal" role="form" id="addfrom">
				        <div class="panel panel-info" >
				            <div class="panel-heading">班级信息</div>
				            <div class="panel-body">
				            	<div class="form-group" style="margin-top:5px; margin-right: 5px;" >
									<label class="col-sm-3 control-label">学院</label>
									<div class="col-sm-8" >
										<select class="form-control input-sm" id="collegeId" name="collegeId" >
				                            <c:forEach var="college" items="${collegelist}" varStatus="status">
											    <option value="${college.id}">${college.collegeName}</option>
											</c:forEach>
			                            </select>
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
									<label class="col-sm-3 control-label">班级</label>
									<div class="col-sm-8" >
										<input id="deptName" name="deptName" type="text" class="form-control input-sm" placeholder="" autocomplete="off">
										<input id="id" name="id" type="hidden" >
									</div>
			                    </div>
				            </div>
				        </div>
					</form>
				</div>
				<div class="modal-footer">
					<button class="btn btn-warning" type="button" id="update">修改</button>
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
		            url: '<%=basePath%>dept/listdata.html',         //请求后台的URL（*）
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
		            search: true,                      //是否显示表格搜索，此搜索是客户端搜索，不会进服务端，所以，个人感觉意义不大
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
	                        queryname:$("#queryname").val()
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
		                field: 'deptName',
		                title: '班级',
		                sortable: true
		            },{
		                title: '操作', 
		                align: 'center',
		                width: '120px',
		                formatter:function(value,row,index){
						    var element = 
							    "<button title='修改' class='btn btn-warning btn-xs' onclick='toupdate(\""+row.id +"\",\""+row.collegeId +"\",\""+row.deptName +"\");'><i class='icon fa-edit'></i></button> "+ 
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
		//修改弹窗
		function toupdate(id, collegeId, deptName){
		
			$('#modal_title').html('修改');
			$('#id').val(id);
			$('#collegeId').val(collegeId);
			$('#deptName').val(deptName);
			$('#update').show();
			$('#myModal').modal('show');
			
		}
		//修改
			$('#update').on('click', function () {
				if(formcheck()){
					$.ajax({
		                cache: true,
		                type: "POST",
		                url:"<%=basePath %>dept/update.html",
	                data:$('#addfrom').serialize(),
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	parent.window.toastralert('success',data);
	                	if("修改成功"== data){
	                		window.location = "<%=basePath%>dept/list.html";
	                	}
	                }
	            });
			}
		});
			//删除按钮
			function del(id){
				window.SelfEvalUI.confirmDanger("确定要删除该班级信息？", "删除班级可能影响关联学生数据展示。", function(){
					$.ajax({
		                cache: true,
		                type: "POST",
		                url:"<%=basePath%>dept/del.html",
	                data:{"id":id},
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	parent.window.toastralert('success',data);
	                	if("删除成功"== data){
	                		window.location = "<%=basePath%>dept/list.html";
	                	}
		                }
		            });
				});
				
			}
		$('#btn_batch_delete').on('click', function () {
			var rows = $('#table').bootstrapTable('getSelections');
			if(rows.length == 0){
				parent.window.toastralert('warning','请选择要删除的班级');
				return;
			}
			var ids = [];
			for(var i=0; i<rows.length; i++){
				ids.push(rows[i].id);
			}
				window.SelfEvalUI.confirmDanger("确定要删除选中的 " + ids.length + " 条班级信息？", "批量删除后不可恢复，请确认已选记录。", function(){
					$.ajax({
		                cache: true,
		                type: "POST",
		                url:"<%=basePath%>dept/dels.html",
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
			var deptName = $('#deptName').val();
			
			if(deptName == null || deptName == ''){
				parent.window.toastralert('warning','专业不能为空！');
				isvalid = false;
			}
			return isvalid;
		}
        //-->
	</script>
</body>
</html>


