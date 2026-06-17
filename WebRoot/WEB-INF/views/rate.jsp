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
	</style> 
</head>

<body style="padding-top: 0px; background-color: #f1f4f5">
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理 
		<span class="c-gray en">&gt;</span> 考核项系数信息
		<button  class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新" ><i class="fa fa-undo"></i></button>
	</nav>
    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default" >
            <div class="panel-body">
                <form id="formSearch" class="form-horizontal" method="post" >
                    <div class="form-group" style="margin-top:5px; margin-left: -5px;" >
                        <div class="col-xs-2">
                            <select class="form-control input-sm" id="queryname" name="queryname" >
                            	<option value="">请选择课程</option>
	                            <c:forEach var="lesson" items="${lessonlist}" varStatus="status">
								    <option value="${lesson.id}">${lesson.name}</option>
								</c:forEach>
                            </select>
                        </div>
                        <div class="col-xs-2" style="text-align:left; width: 240px;  margin-left: -35px;">
                            <button type="button" style="margin-left:10px" id="btn_query" class="btn btn-primary btn-sm">查询</button>
                            <button type="button" style="margin-left:10px" id="btn_add" class="btn btn-info btn-sm">添加</button>
                        </div>
                    </div>
                </form>
            </div>
        </div> 
  
		<div class="panel-body" style="padding-bottom:0px; margin-top: -41px;">
	        <div id="toolbar" class="btn-group">
	        	
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
				            <div class="panel-heading">考核项系数信息</div>
				            <div class="panel-body">
				            	<div class="form-group" style="margin-top:5px; margin-right: 5px;" >
									<label class="col-sm-3 control-label">课程</label>
									<div class="col-sm-8" >
										<select class="form-control input-sm" id="lessonid" name="lessonid" >
				                            <c:forEach var="lesson" items="${lessonlist}" varStatus="status">
											    <option value="${lesson.id}">${lesson.name}</option>
											</c:forEach>
			                            </select>
			                            <input id="id" name="id" type="hidden" >
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
									<label class="col-sm-3 control-label">课堂表现系数</label>
									<div class="col-sm-8" >
										<div class="input-group">
										<input id="showrate" name="showrate" type="number" class="form-control input-sm" placeholder="" autocomplete="off">
											<div class="input-group-addon">%</div>
										</div>
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
									<label class="col-sm-3 control-label">课程作业系数</label>
									<div class="col-sm-8" >
										<div class="input-group">
										<input id="homeworkrate" name="homeworkrate" type="number" class="form-control input-sm" placeholder="" autocomplete="off">
											<div class="input-group-addon">%</div>
										</div>
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
									<label class="col-sm-3 control-label">课程实验系数</label>
									<div class="col-sm-8" >
										<div class="input-group">
										<input id="testrate" name="testrate" type="number" class="form-control input-sm" placeholder="" autocomplete="off">
											<div class="input-group-addon">%</div>
										</div>
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
									<label class="col-sm-3 control-label">课程设计系数</label>
									<div class="col-sm-8" >
										<div class="input-group">
										<input id="designrate" name="designrate" type="number" class="form-control input-sm" placeholder="" autocomplete="off">
											<div class="input-group-addon">%</div>
										</div>
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
									<label class="col-sm-3 control-label">期中成绩系数</label>
									<div class="col-sm-8" >
										<div class="input-group">
										<input id="middlerate" name="middlerate" type="number" class="form-control input-sm" placeholder="" autocomplete="off">
											<div class="input-group-addon">%</div>
										</div>
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
									<label class="col-sm-3 control-label">期末成绩系数</label>
									<div class="col-sm-8" >
										<div class="input-group">
										<input id="endrate" name="endrate" type="number" class="form-control input-sm" placeholder="" autocomplete="off">
											<div class="input-group-addon">%</div>
										</div>
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
			var height = document.documentElement.clientHeight-40;
			document.getElementById('maindiv').style.minHeight = height+"px";
		    var oTableInit = new Object();
		    //初始化Table
		    oTableInit.Init = function () {
		        $('#table').bootstrapTable({
		            url: '<%=basePath%>rate/listdata.html',         //请求后台的URL（*）
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
		            uniqueId: "ID",                     //每一行的唯一标识，一般为主键列
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
                        //field: 'Number',//可不加  
                        title: '序号',//标题  可不加  
                        width: '50px',
                        align:'center',
                        formatter: function (value, row, index) {  
                            return index+1;  
                        }  
                    },{
		                field: 'lesson.name',
		                title: '课程',
		                sortable: true
		            },{
		                field: 'showrate',
		                title: '课堂表现系数',
		                sortable: true
		            },{
		                field: 'homeworkrate',
		                title: '课程作业系数',
		                sortable: true
		            },{
		                field: 'testrate',
		                title: '课程实验系数',
		                sortable: true
		            },{
		                field: 'designrate',
		                title: '课程设计系数',
		                sortable: true
		            },{
		                field: 'middlerate',
		                title: '期中成绩系数',
		                sortable: true
		            },{
		                field: 'endrate',
		                title: '期末成绩系数',
		                sortable: true
		            },{
		                title: '操作', 
		                align: 'center',
		                width: '120px',
		                formatter:function(value,row,index){
						    var element = 
							    "<button title='修改' class='btn btn-warning btn-xs' onclick='toupdate(\""+row.id +"\",\""+row.lessonid +"\",\""+row.showrate +"\",\""+row.homeworkrate +"\",\""+row.testrate +"\",\""+row.designrate +"\",\""+row.middlerate +"\",\""+row.endrate +"\");'><i class='icon fa-edit'></i></button> "+ 
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
			$('#lessonid').val('');
			$('#showrate').val('');
			$('#homeworkrate').val('');
			$('#testrate').val('');
			$('#designrate').val('');
			$('#middlerate').val('');
			$('#endrate').val('');
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
	                async:false, 
	                url:"<%=basePath %>rate/add.html",
	                data:$('#addfrom').serialize(),
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	if("新增成功"== data){
	                		parent.window.toastralert('success',data);
	                		window.location = "<%=basePath%>rate/list.html";
	                	}else{
	                		parent.window.toastralert('error',data);
	                	}
	                }
	            });
			}
		});
		//修改弹窗
		function toupdate(id, lessonid, showrate,homeworkrate,testrate,designrate,middlerate,endrate){
		
			$('#modal_title').html('修改');
			$('#id').val(id);
			$('#lessonid').val(lessonid);
			$('#showrate').val(showrate);
			$('#homeworkrate').val(homeworkrate);
			$('#testrate').val(testrate);
			$('#designrate').val(designrate);
			$('#middlerate').val(middlerate);
			$('#endrate').val(endrate);
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
	                async:false, 
	                url:"<%=basePath %>rate/update.html",
	                data:$('#addfrom').serialize(),
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	parent.window.toastralert('success',data);
	                	if("修改成功"== data){
	                		window.location = "<%=basePath%>rate/list.html";
	                	}
	                }
	            });
			}
		});
		//删除按钮
		function del(id){
			if(confirm("确定要删除该信息？")){
				$.ajax({
	                cache: true,
	                type: "POST",
	                async:false, 
	                url:"<%=basePath%>rate/del.html",
	                data:{"id":id},
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	parent.window.toastralert('success',data);
	                	if("删除成功"== data){
	                		window.location = "<%=basePath%>rate/list.html";
	                	}
	                }
	            });
			}
			
		}
		//数据校验
		function formcheck(){
			var isvalid = true;
			var rateName = $('#rateName').val();
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
 			}
			return isvalid;
		}
        //-->
	</script>
</body>
</html>


