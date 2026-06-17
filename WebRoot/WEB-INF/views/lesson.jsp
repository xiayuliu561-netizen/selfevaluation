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
		.student-filter {
			display: flex;
			flex-wrap: wrap;
			gap: 8px;
			margin-bottom: 10px;
		}
		.student-filter .form-control {
			width: 220px;
			max-width: 100%;
		}
		.student-list {
			border: 1px solid #ddd;
			height: 360px;
			overflow: auto;
			padding: 10px;
			background: #fff;
		}
		.student-item {
			display: inline-block;
			width: 32%;
			min-width: 180px;
			margin-bottom: 8px;
			vertical-align: top;
		}
		.selected-student {
			background: #eef7ff;
			border-radius: 3px;
			padding: 2px 4px;
		}
		.import-panel {
			border-top: 1px solid #e5e5e5;
			margin-top: 12px;
			padding-top: 12px;
		}
		.import-actions {
			display: flex;
			align-items: center;
			flex-wrap: wrap;
			gap: 8px;
			margin-bottom: 8px;
		}
		.import-actions .form-control {
			width: 280px;
			max-width: 100%;
		}
		.import-summary {
			color: #555;
			margin: 6px 0;
		}
		.preview-table-wrap {
			max-height: 230px;
			overflow: auto;
			border: 1px solid #ddd;
			background: #fff;
			display: none;
		}
		.preview-table {
			margin-bottom: 0;
		}
		.preview-table td,
		.preview-table th {
			white-space: nowrap;
			font-size: 12px;
		}
		.preview-valid {
			color: #008a00;
		}
		.preview-invalid {
			color: #c9302c;
		}
	</style> 
</head>

<body style="padding-top: 0px; background-color: #f1f4f5">
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理 
		<span class="c-gray en">&gt;</span> 课程管理
		<button  class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新" ><i class="fa fa-undo"></i></button>
	</nav>
    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default" >
            <div class="panel-body page-toolbar">
                <form id="formSearch" class="form-inline" method="post" >
                    <div class="form-group">
                        <input type="text" class="form-control input-sm" id="queryname" name="queryname"  placeholder="请输入课程名称">
                        <button type="button" id="btn_query" class="btn btn-primary btn-sm">查询</button>
                        <button type="button" id="btn_add" class="btn btn-info btn-sm">添加</button>
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
				            <div class="panel-heading">课程信息</div>
				            <div class="panel-body">
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
	                            	<label class="col-sm-2 control-label">课程名称</label>
									<div class="col-sm-10" >
										<input id="name" name="name" type="text" class="form-control input-sm" placeholder="" autocomplete="off">
										<input type="hidden" id="id" name="id">
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
	                            	<label class="col-sm-2 control-label">上课时间</label>
									<div class="col-sm-10" >
										<input id="time" name="time" type="text" class="form-control input-sm" placeholder="" autocomplete="off">
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
	                            	<label class="col-sm-2 control-label">上课地点</label>
									<div class="col-sm-10" >
										<input id="room" name="room" type="text" class="form-control input-sm" placeholder="" autocomplete="off">
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;" >
	                            	<label class="col-sm-2 control-label">课程周期</label>
									<div class="col-sm-10" >
										<input id="beginend" name="beginend" type="text" class="form-control input-sm" placeholder="" autocomplete="off">
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

	<div class="modal fade" id="studentModal" tabindex="-1">
		<div class="modal-dialog" style="width: 760px; max-width: 96%;">
			<div class="modal-content">
				<div class="modal-header">
					<button class="close" data-dismiss="modal"><span>&times;</span></button>
					<h4 class="modal-title">课程学生管理</h4>
				</div>
				<div class="modal-body">
					<input type="hidden" id="studentLessonId">
					<div class="student-filter">
						<select class="form-control input-sm" id="filterCollege">
							<option value="">请选择学院</option>
							<c:forEach var="college" items="${collegelist}" varStatus="status">
								<option value="${college.id}">${college.collegeName}</option>
							</c:forEach>
						</select>
						<select class="form-control input-sm" id="filterDept">
							<option value="">请选择班级</option>
						</select>
						<button type="button" class="btn btn-primary btn-sm" id="btnLoadStudents">筛选</button>
						<button type="button" class="btn btn-default btn-sm" id="btnCheckAll">全选</button>
						<button type="button" class="btn btn-default btn-sm" id="btnClearAll">清空</button>
					</div>
					<div id="studentList" class="student-list">请先选择学院和班级</div>
					<div id="studentMsg" style="padding-top: 8px;"></div>
					<div class="import-panel">
						<div class="import-actions">
							<input id="studentExcelFile" name="file" type="file" class="form-control input-sm" accept=".xls,.xlsx">
							<button type="button" class="btn btn-info btn-sm" id="btnPreviewImport"><i class="icon fa-search"></i> 智能识别名单</button>
							<a class="btn btn-default btn-sm" href="<%=basePath%>lesson/studenttemplate.html"><i class="icon fa-download"></i> 下载示例模板</a>
							<button type="button" class="btn btn-success btn-sm" id="btnConfirmImport" disabled="disabled"><i class="icon fa-check"></i> 确认导入</button>
						</div>
						<div id="importSummary" class="import-summary">可上传选课表、成绩表、班级名单等包含姓名、学号、学院、班级的 Excel 文件。</div>
						<div class="preview-table-wrap" id="previewTableWrap">
							<table class="table table-condensed table-bordered preview-table">
								<thead>
									<tr>
										<th style="width:60px;">行号</th>
										<th>姓名</th>
										<th>学号</th>
										<th>学院</th>
										<th>班级</th>
										<th style="width:80px;">状态</th>
										<th>提示</th>
									</tr>
								</thead>
								<tbody id="previewStudentBody"></tbody>
							</table>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" type="button" id="saveStudents">保存</button>
				</div>
			</div>
		</div>
	</div>

	<script>
		<!--
		var selectedStudentIds = {};
		var importPreviewToken = '';

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
		            url: '<%=basePath%>lesson/listdata.html',         //请求后台的URL（*）
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
		                field: 'name',
		                title: '课程',
		                sortable: true
		            },{
		                field: 'time',
		                title: '上课时间',
		                sortable: true
		            },{
		                field: 'room',
		                title: '上课地点',
		                sortable: true
		            },{
		                field: 'beginend',
		                title: '课程周期',
		                sortable: true
		            },{
		                title: '操作', 
		                align: 'center',
		                width: '160px',
		                formatter:function(value,row,index){
						    var element = 
						    	"<button title='课程学生管理' class='btn btn-info btn-xs' onclick='openStudentModal(\""+row.id +"\");'><i class='icon fa-users'></i></button> "+ 
							    "<button title='修改' class='btn btn-warning btn-xs' onclick='toupdate(\""+row.id +"\",\""+row.name +"\",\""+row.time +"\",\""+row.room +"\",\""+row.beginend +"\");'><i class='icon fa-edit'></i></button> "+ 
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
		function openStudentModal(lessonid){
			selectedStudentIds = {};
			$('#studentLessonId').val(lessonid);
			$('#filterCollege').val('');
			$('#filterDept').html('<option value="">请选择班级</option>');
			$('#studentList').html('请先选择学院和班级');
			$('#studentMsg').html('');
			resetImportPreview();
			$('#studentModal').modal('show');
			loadStudents();
		}
		$('#filterCollege').on('change', function(){
			mergeCurrentSelection();
			var collegeId = $('#filterCollege').val();
			$('#filterDept').html('<option value="">请选择班级</option>');
			$('#studentList').html('请继续选择班级');
			if(collegeId == ''){
				return;
			}
			$.ajax({
				type: "GET",
				url: "<%=basePath%>lesson/depts.html",
				data: {"collegeId": collegeId},
				success: function(data){
					var json = typeof data == 'string' ? $.parseJSON(data) : data;
					var html = '<option value="">请选择班级</option>';
					$.each(json.data, function(i, dept){
						html += '<option value="'+dept.id+'">'+dept.deptName+'</option>';
					});
					$('#filterDept').html(html);
				}
			});
		});
		$('#filterDept').on('change', function(){
			mergeCurrentSelection();
			loadStudents();
		});
		$('#btnLoadStudents').on('click', function(){
			mergeCurrentSelection();
			loadStudents();
		});
		$(document).on('change', '.student-check', function(){
			if($(this).is(':checked')){
				selectedStudentIds[$(this).val()] = true;
			}else{
				delete selectedStudentIds[$(this).val()];
			}
		});
		function mergeCurrentSelection(){
			$('.student-check').each(function(){
				if($(this).is(':checked')){
					selectedStudentIds[$(this).val()] = true;
				}else{
					delete selectedStudentIds[$(this).val()];
				}
			});
		}
		function loadStudents(){
			var collegeId = $('#filterCollege').val();
			var deptId = $('#filterDept').val();
			if((collegeId == '' && deptId != '') || (collegeId != '' && deptId == '')){
				$('#studentList').html('请先选择学院和班级');
				return;
			}
			$.ajax({
				type: "GET",
				url: "<%=basePath%>lesson/students.html",
				data: {"lessonid": $('#studentLessonId').val(), "collegeId": collegeId, "deptId": deptId},
				success: function(data){
					var json = typeof data == 'string' ? $.parseJSON(data) : data;
					var html = '';
					if(json.data.length == 0){
						html = '当前班级暂无学生';
					}
					$.each(json.data, function(i, user){
						if(user.checked){
							selectedStudentIds[user.id] = true;
						}
						var isChecked = selectedStudentIds[user.id] || user.checked;
						var checked = isChecked ? ' checked="checked"' : '';
						var selectedClass = isChecked ? ' selected-student' : '';
						html += '<label class="student-item'+selectedClass+'"><input type="checkbox" class="student-check" value="'+user.id+'"'+checked+'> '+user.name+'-'+user.no+'</label>';
					});
					$('#studentList').html(html);
				}
			});
		}
		$('#btnCheckAll').on('click', function(){
			$('.student-check').prop('checked', true);
			mergeCurrentSelection();
		});
		$('#btnClearAll').on('click', function(){
			$('.student-check').prop('checked', false);
			mergeCurrentSelection();
		});
		$('#saveStudents').on('click', function(){
			mergeCurrentSelection();
			var ids = [];
			for(var id in selectedStudentIds){
				if(selectedStudentIds.hasOwnProperty(id)){
					ids.push(id);
				}
			}
			$.ajax({
				type: "POST",
				url: "<%=basePath%>lesson/savestudents.html",
				data: {"lessonid": $('#studentLessonId').val(), "studentids": ids.join(',')},
				success: function(data){
					$('#studentMsg').html(data);
					parent.window.toastralert('success',data);
					$('#studentModal').modal('hide');
				},
				error: function(){
					parent.window.toastralert('error','网络连接错误，请联系管理员');
				}
			});
		});
		$('#btnPreviewImport').on('click', function(){
			var file = $('#studentExcelFile')[0].files[0];
			if(!file){
				parent.window.toastralert('warning','请选择Excel文件');
				return;
			}
			var formData = new FormData();
			formData.append('lessonid', $('#studentLessonId').val());
			formData.append('file', file);
			$('#btnPreviewImport').prop('disabled', true);
			$('#btnConfirmImport').prop('disabled', true);
			$('#importSummary').html('正在识别学生名单，请稍候...');
			$.ajax({
				type: "POST",
				url: "<%=basePath%>lesson/previewstudents.html",
				data: formData,
				contentType: false,
				processData: false,
				success: function(data){
					var json = typeof data == 'string' ? $.parseJSON(data) : data;
					if(!json.success){
						resetImportPreview();
						parent.window.toastralert('error', json.message || '识别失败');
						return;
					}
					renderImportPreview(json.data);
				},
				error: function(){
					resetImportPreview();
					parent.window.toastralert('error','网络连接错误，请联系管理员');
				},
				complete: function(){
					$('#btnPreviewImport').prop('disabled', false);
				}
			});
		});
		$('#btnConfirmImport').on('click', function(){
			if(!importPreviewToken){
				parent.window.toastralert('warning','请先识别名单');
				return;
			}
			$('#btnConfirmImport').prop('disabled', true);
			$('#importSummary').html('正在导入课程学生，请稍候...');
			$.ajax({
				type: "POST",
				url: "<%=basePath%>lesson/confirmstudents.html",
				data: {"lessonid": $('#studentLessonId').val(), "token": importPreviewToken},
				success: function(data){
					var json = typeof data == 'string' ? $.parseJSON(data) : data;
					if(json.success){
						parent.window.toastralert('success', json.message || '导入成功');
						$('#importSummary').html(json.message || '导入成功');
						resetImportPreviewToken();
						selectedStudentIds = {};
						loadStudents();
					}else{
						parent.window.toastralert('error', json.message || '导入失败');
						$('#btnConfirmImport').prop('disabled', false);
					}
				},
				error: function(){
					parent.window.toastralert('error','网络连接错误，请联系管理员');
					$('#btnConfirmImport').prop('disabled', false);
				}
			});
		});
		function renderImportPreview(preview){
			importPreviewToken = preview.token || '';
			var items = preview.items || [];
			var html = '';
			$.each(items, function(i, item){
				var valid = item.valid === true || item.valid === 'true';
				html += '<tr>';
				html += '<td>' + htmlEscape(item.rowNumber || '') + '</td>';
				html += '<td>' + htmlEscape(item.name || '') + '</td>';
				html += '<td>' + htmlEscape(item.no || '') + '</td>';
				html += '<td>' + htmlEscape(item.collegeName || '') + '</td>';
				html += '<td>' + htmlEscape(item.deptName || '') + '</td>';
				html += '<td class="' + (valid ? 'preview-valid' : 'preview-invalid') + '">' + (valid ? '可导入' : '需处理') + '</td>';
				html += '<td>' + htmlEscape(item.message || (item.existingStudent ? '系统已有学生，将复用并关联课程' : '')) + '</td>';
				html += '</tr>';
			});
			$('#previewStudentBody').html(html);
			$('#previewTableWrap').show();
			$('#importSummary').html((preview.source || '系统识别') + '：共识别 ' + preview.totalCount + ' 行，可导入 ' + preview.validCount
				+ ' 行，无效 ' + preview.invalidCount + ' 行，重复 ' + preview.duplicateCount + ' 行，系统已有 ' + preview.existingCount + ' 人。'
				+ (preview.message ? ' ' + preview.message : ''));
			$('#btnConfirmImport').prop('disabled', !importPreviewToken || Number(preview.validCount) <= 0);
		}
		function resetImportPreview(){
			resetImportPreviewToken();
			$('#studentExcelFile').val('');
			$('#previewStudentBody').html('');
			$('#previewTableWrap').hide();
			$('#importSummary').html('可上传选课表、成绩表、班级名单等包含姓名、学号、学院、班级的 Excel 文件。');
		}
		function resetImportPreviewToken(){
			importPreviewToken = '';
			$('#btnConfirmImport').prop('disabled', true);
		}
		function htmlEscape(value){
			return String(value == null ? '' : value)
				.replace(/&/g, '&amp;')
				.replace(/</g, '&lt;')
				.replace(/>/g, '&gt;')
				.replace(/"/g, '&quot;')
				.replace(/'/g, '&#39;');
		}
		//点击添加按钮，模态框出现
		$('#btn_add').on('click', function () {
			$('#modal_title').html('添加');
			$('#name').val('');
			$('#time').val('');
			$('#room').val('');
			$('#beginend').val('');
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
	                url:"<%=basePath %>lesson/add.html",
	                data:$('#addfrom').serialize(),
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	if("新增成功"== data){
	                		parent.window.toastralert('success',data);
	                		window.location = "<%=basePath%>lesson/list.html";
	                	}else{
	                		parent.window.toastralert('error',data);
	                	}
	                }
	            });
			}
		});
		//修改弹窗
		function toupdate(id, name, time,room,beginend){
		
			$('#modal_title').html('修改');
			$('#id').val(id);
			$('#name').val(name);
			$('#time').val(time);
			$('#room').val(room);
			$('#beginend').val(beginend);
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
	                url:"<%=basePath %>lesson/update.html",
	                data:$('#addfrom').serialize(),
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	parent.window.toastralert('success',data);
	                	if("修改成功"== data){
	                		window.location = "<%=basePath%>lesson/list.html";
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
	                url:"<%=basePath%>lesson/del.html",
	                data:{"id":id},
	                error: function(request) {
	                    parent.window.toastralert('error','网络连接错误，请联系管理员');
	                },
	                success: function(data) {
	                	parent.window.toastralert('success',data);
	                	if("删除成功"== data){
	                		window.location = "<%=basePath%>lesson/list.html";
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


