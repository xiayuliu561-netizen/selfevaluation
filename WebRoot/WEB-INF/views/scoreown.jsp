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
		<span class="c-gray en">&gt;</span> 总评成绩查询
		<button  class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新" ><i class="fa fa-undo"></i></button>
	</nav>
    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default" >
            <div class="panel-body">
                <form id="formSearch" class="form-inline" method="post" >
                    <div class="form-group" style="margin-top:5px; margin-bottom:5px; margin-left: 5px;">
				       	<button type="button" style="margin-left:5px" id="btn_query" class="btn btn-primary btn-sm">成绩查询</button>
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
		            url: '<%=basePath%>score/ownlistdata.html',         //请求后台的URL（*）
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
	                rowStyle: function(row, index) {
						var classes = ['active', 'success', 'info', 'warning', 'danger'];
						if(parseInt(row.sumscore)<60){
							return {classes: 'danger'};
						}
						return {};
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
		                field: 'user.no',
		                title: '学号',
		                sortable: true
		            },{
		                field: 'user.name',
		                title: '姓名',
		                sortable: true
		            },{
		                field: 'user.deptName',
		                title: '班级',
		                sortable: true
		            },{
			                field: 'lessonentity.name',
			                title: '课程',
			                sortable: true,
			                formatter: function(value, row){
			                	return value || row.lesson || '';
			                }
			            },{
		                field: 'sumscore',
		                title: '总评成绩',
		                sortable: true
		            },{
		                field: 'remarks',
		                title: '备注',
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
		$('#btn_download_template').on('click', function () {
			window.parent.location.href = '<%=basePath%>download.html?filePath=scoretemlpate.xls';
		})
		/**
		 * 
		 */
		$('#btn_createsumscore').on('click', function () {
			$.ajax({
                cache: true,
                type: "POST",
                async:false, 
                url:"<%=basePath%>score/createsumscore.html",
                data:{},
                error: function(request) {
                    parent.window.toastralert('error','网络连接错误，请联系管理员');
                },
                success: function(data) {
                	parent.window.toastralert('success',data);
                }
            });
		})
		//点击添加按钮，模态框出现
		$('#btn_importfile').on('click', function () {
			$('#modal_title').html('上传文件');
			$('#file').val('');
			$('#msg').html('');
			$('#bidding').val('');
            $('#importFileBtn').show();
			$('#importFileModal').modal('show');
		});
		
		//上传文件
		$('#importFileBtn').on('click', function () {
			var file = $('#file').val();
			if(file==''){
				document.getElementById("msg").innerHTML='ERROR:未选择上传文件!';
			}else{
				$.ajax({
	                cache: false,
	                type: "POST",
	                async:true, 
	                contentType: false,
	                processData:false,
	                url:"<%=basePath %>score/importfile.html",
	                data:new FormData($('#importFileFrom')[0]),
	              
	                error: function(request) {
	                	document.getElementById("msg").innerHTML=" ERROR:数据格式错误，上传失败!";
	                },
	                beforeSend: function(){
	                	document.getElementById("msg").innerHTML="上传中，请稍后...";
	                },
	                success: function(data) {
	                	document.getElementById("msg").innerHTML=data;
	                	window.location = "<%=basePath%>score/list.html";
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
	                url:"<%=basePath%>news/del.html",
	                data:{"id":id},
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	parent.window.toastralert('success',data);
	                	if("删除成功"== data){
	                		window.location = "<%=basePath%>news/list.html";
	                	}
	                }
	            });
			}
			
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


