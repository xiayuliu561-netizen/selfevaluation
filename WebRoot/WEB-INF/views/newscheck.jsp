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
			margin: 0 !important;
		}
		.page-toolbar .form-control {
			width: 220px !important;
			max-width: 100%;
		}
		.page-toolbar .btn {
			margin-left: 0 !important;
		}
		.table-wrap {
			padding-bottom: 0px;
			margin-top: 0px !important;
		}
	</style> 
</head>

<body style="padding-top: 0px; background-color: #f1f4f5">
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理 
		<span class="c-gray en">&gt;</span> 公告查看
		<button  class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新" ><i class="fa fa-undo"></i></button>
	</nav>
    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default" >
            <div class="panel-body page-toolbar">
                <form id="formSearch" class="form-inline" method="post" >
                    <div class="form-group">
                        <input type="text" class="form-control input-sm" id="queryname" name="queryname"  placeholder="请输入名称">
                        <button type="button" id="btn_query" class="btn btn-primary btn-sm">查询</button>
                    </div>
                </form>
            </div>
        </div> 
  
		<div class="panel-body table-wrap">
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
				            <div class="panel-heading">公告信息</div>
				            <div class="panel-body">
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
	                            	<label class="col-sm-2 control-label">公告标题</label>
									<div class="col-sm-10" >
										<input id="title" name="title" type="text" readonly="readonly" class="form-control input-sm" placeholder="请输入标题" autocomplete="off">
										<input type="hidden" id="id" name="id">
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
	                            	<label class="col-sm-2 control-label">内容</label>
									<div class="col-sm-10" >
										<textarea id="content" name="content" readonly="readonly" class="form-control input-sm" rows="" cols=""></textarea>
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
		            url: '<%=basePath%>news/listdata.html',         //请求后台的URL（*）
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
		                field: 'title',
		                title: '标题',
		                sortable: true
		            },{
		                field: 'time',
		                title: '发布时间',
		                sortable: true
		            },{
		                title: '操作', 
		                align: 'center',
		                width: '120px',
		                formatter:function(value,row,index){
						    var element = 
							    "<button title='查看' class='btn btn-info btn-xs' onclick='toview("+row.id+");'><i class='icon fa-eye'></i></button> ";
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
		function getRowById(id){
			var rows = $('#table').bootstrapTable('getData');
			for(var i=0; i<rows.length; i++){
				if(rows[i].id == id){
					return rows[i];
				}
			}
			return {};
		}
		function toview(id){
			var row = getRowById(id);
			$('#modal_title').html('查看');
			$('#id').val(row.id);
			$('#title').val(row.title);
			$('#content').val(row.content);
			$('#update').hide();
            $('#add').hide();
			$('#myModal').modal('show');
		}
		//数据校验
		function formcheck(){
			var isvalid = true;
			return isvalid;
		}
        //-->
	</script>
</body>
</html>


