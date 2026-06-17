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
	</style> 
</head>

<body style="padding-top: 0px; background-color: #f1f4f5">
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理 
		<span class="c-gray en">&gt;</span> 基础信息管理
		<span class="c-gray en">&gt;</span> 学生管理
		<button  class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新" ><i class="fa fa-undo"></i></button>
	</nav>
    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default" >
            <div class="panel-body">
                <form id="formSearch" class="form-horizontal" method="post" >
                    <div class="form-group" style="margin-top:5px; margin-left: -5px;" >
                        <div class="col-xs-2">
                            <input type="text" class="form-control input-sm" style="width: 140px;" id="queryname" name="queryname"  placeholder="请输入名称">
                        </div>
                        <div class="col-xs-2" style="text-align:left; width: 240px;  margin-left: -35px;">
                            <button type="button" style="margin-left:10px" id="btn_query" class="btn btn-primary btn-sm">查询</button>
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
		            url: '<%=basePath%>user/ownlistdata.html',         //请求后台的URL（*）
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
	                        queryname:$("#queryname").val(),
	                        isadmin:2
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
		                field: 'collegeName',
		                title: '学院',
		                sortable: true
		            },{
		                field: 'deptName',
		                title: '班级',
		                sortable: true
		            },{
		                field: 'username',
		                title: '用户名',
		                sortable: true
		            },{
		                field: 'no',
		                title: '学号',
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
		function formcheck(){
			var isvalid = true;
			return isvalid;
		}
        //-->
	</script>
</body>
</html>


