<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <title>AI大模型管理</title>
	<%@ include file="common/js.jsp"%>
		<style type="text/css">
			.form-horizontal .form-group { margin-right: 0px; }
			.panel-body { padding: 0px; }
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
				width: 220px;
			}
			.page-toolbar .btn {
				margin-left: 0 !important;
			}
			.ai-result-box {
				white-space: pre-wrap;
				word-break: break-word;
				max-height: 180px;
				overflow: auto;
			}
			.table-wrap {
				padding-bottom: 0px;
				margin-top: 0px;
			}
			.model-state-checkbox {
				cursor: pointer;
				width: 16px;
				height: 16px;
				margin: 0;
			}
		</style>
</head>
<body style="padding-top: 0px; background-color: #f1f4f5">
	<nav class="breadcrumb">
		<i class="fa fa-home"></i> 系统管理
		<span class="c-gray en">&gt;</span> AI大模型管理
		<button class="btn btn-success btn-xs pull-right" style="margin-top:6px" onclick="location.replace(location.href)" title="刷新"><i class="fa fa-undo"></i></button>
	</nav>
    <div class="panel introduce-info" id="maindiv" style="margin-top: -22px;">
		<div class="panel panel-default">
            <div class="panel-body page-toolbar">
                <form id="formSearch" class="form-inline" method="post">
                    <div class="form-group">
                            <input type="text" class="form-control input-sm" id="queryname" name="queryname" placeholder="供应商/模型名称">
                            <button type="button" style="margin-left:10px" id="btn_query" class="btn btn-primary btn-sm">查询</button>
                            <button type="button" style="margin-left:10px" id="btn_add" class="btn btn-info btn-sm">添加</button>
                    </div>
                </form>
            </div>
        </div>
		<div class="panel-body table-wrap">
	        <table id="table"></table>
	    </div>
	</div>
	<div class="modal fade" id="myModal" tabindex="-1">
		<div class="modal-dialog" style="width: 820px;">
			<div class="modal-content">
				<div class="modal-header">
					<button class="close" data-dismiss="modal"><span>&times;</span></button>
					<h4 class="modal-title" id="modal_title"></h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal" role="form" id="addfrom">
				        <div class="panel panel-info">
				            <div class="panel-heading">模型配置</div>
				            <div class="panel-body">
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;">
	                            	<label class="col-sm-2 control-label">供应商</label>
									<div class="col-sm-4">
										<select id="provider" name="provider" class="form-control input-sm">
											<option value="OpenAI">OpenAI</option>
											<option value="Anthropic">Anthropic</option>
											<option value="DeepSeek">DeepSeek</option>
											<option value="通义千问">通义千问</option>
											<option value="智谱AI">智谱AI</option>
											<option value="文心一言">文心一言</option>
											<option value="豆包">豆包</option>
											<option value="其他">其他</option>
										</select>
										<input type="hidden" id="id" name="id">
									</div>
									<label class="col-sm-2 control-label">模型名称</label>
									<div class="col-sm-4">
										<input id="modelName" name="modelName" type="text" class="form-control input-sm" placeholder="例如 gpt-4o-mini" autocomplete="off">
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;">
	                            	<label class="col-sm-2 control-label">接口地址</label>
									<div class="col-sm-10">
										<input id="apiUrl" name="apiUrl" type="text" class="form-control input-sm" placeholder="例如 https://api.openai.com/v1" autocomplete="off">
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;">
	                            	<label class="col-sm-2 control-label">API Key</label>
									<div class="col-sm-10">
										<input id="apiKey" name="apiKey" type="password" class="form-control input-sm" placeholder="新增必填；修改时留空表示不变" autocomplete="off">
									</div>
			                    </div>
			                    <div class="form-group" style="margin-top:5px; margin-right: 5px;">
	                            	<label class="col-sm-2 control-label">备注</label>
									<div class="col-sm-10">
										<textarea id="remarks" name="remarks" class="form-control input-sm"></textarea>
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
		$(function () {
		    var pageHeight = document.documentElement.clientHeight;
		    var height = pageHeight-110;
		    if(height < 320){
		    	height = 320;
		    }
			document.getElementById('maindiv').style.minHeight = (pageHeight-40)+"px";
	        $('#table').bootstrapTable({
	            url: '<%=basePath%>aimodel/listdata.html',
	            method: 'get',
	            striped: true,
	            cache: false,
	            pagination: true,
	            sortable: true,
	            sortOrder: "asc",
	            sidePagination: "client",
	            pageNumber:1,
	            pageSize: 20,
	            pageList: [20, 50, 100],
	            search: false,
	            strictSearch: true,
	            showColumns: true,
	            minimumCountColumns: 2,
	            clickToSelect: true,
	            height: height,
	            uniqueId: "id",
	            showExport: true,
        		exportDataType: "all",
                queryParams : function (params) {
                    return { queryname:$("#queryname").val() };
                },
	            columns: [{
                    title: '序号',
                    width: '50px',
                    align:'center',
                    formatter: function (value, row, index) { return index+1; }
                },{
	                field: 'provider',
	                title: '供应商',
	                sortable: true
	            },{
	                field: 'modelName',
	                title: '模型名称',
	                sortable: true
	            },{
	                field: 'apiUrl',
	                title: '接口地址',
	                sortable: true
	            },{
	                field: 'enabled',
	                title: '启用',
	                align: 'center',
	                sortable: true,
	                formatter:function(value,row,index){
	                	var checked = value == '1' ? ' checked' : '';
	                	return '<input type="checkbox" class="model-state-checkbox" data-id="' + row.id + '" data-field="enabled"' + checked + '>';
	                }
	            },{
	                field: 'isDefault',
	                title: '默认',
	                align: 'center',
	                sortable: true,
	                formatter:function(value,row,index){
	                	var checked = value == '1' ? ' checked' : '';
	                	return '<input type="checkbox" class="model-state-checkbox" data-id="' + row.id + '" data-field="isDefault"' + checked + '>';
	                }
	            },{
	                field: 'createtime',
	                title: '创建时间',
	                sortable: true
	            },{
	                title: '操作',
	                align: 'center',
	                width: '150px',
	                formatter:function(value,row,index){
					    return "<button title='测试调用' class='btn btn-success btn-xs' onclick='testCall("+row.id+");'><i class='icon fa-play'></i></button> " +
						    "<button title='修改' class='btn btn-warning btn-xs' onclick='toupdate("+row.id+");'><i class='icon fa-edit'></i></button> " +
						    "<button title='删除' class='btn btn-danger btn-xs' onclick='del(\""+row.id +"\");'><i class='icon fa-close'></i></button> ";
					}
	            }]
	        });
		});
		$('#btn_query').on('click', function () {
			$('#table').bootstrapTable('refresh','');
		});
		$('#btn_add').on('click', function () {
			$('#modal_title').html('添加');
			$('#id').val('');
			$('#provider').val('OpenAI');
			$('#modelName').val('');
			$('#apiUrl').val('');
			$('#apiKey').val('');
			$('#remarks').val('');
			$('#update').hide();
            $('#add').show();
			$('#myModal').modal('show');
		});
		$('#add').on('click', function () {
			if(formcheck()){
				$.ajax({
	                cache: true,
	                type: "POST",
	                async:false,
	                url:"<%=basePath %>aimodel/add.html",
	                data:$('#addfrom').serialize(),
	                error: function(request) { parent.window.toastralert('error','网络连接错误，请联系管理员'); },
	                success: function(data) {
	                	if("新增成功"== data){
	                		parent.window.toastralert('success',data);
	                		window.location = "<%=basePath%>aimodel/list.html";
	                	}else{
	                		parent.window.toastralert('error',data);
	                	}
	                }
	            });
			}
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
		$('#table').on('change', '.model-state-checkbox', function(event){
			toggleModelState(event, $(this).data('id'), $(this).data('field'), this.checked);
		});
		function toggleModelState(event, id, field, checked){
			if(event && event.stopPropagation){
				event.stopPropagation();
			}
			$.ajax({
                cache: false,
                type: "POST",
                async:false,
                url:"<%=basePath%>aimodel/updatestatus.html",
                data:{"id":id, "field":field, "value":checked ? "1" : "0"},
                error: function(request) {
                	parent.window.toastralert('error','网络连接错误，请联系管理员');
                	$('#table').bootstrapTable('refresh','');
                },
                success: function(data) {
                	if("修改成功"== data){
                		parent.window.toastralert('success',data);
                	}else{
                		parent.window.toastralert('error',data);
                	}
                	$('#table').bootstrapTable('refresh','');
                }
            });
		}
		function toupdate(id){
			var row = getRowById(id);
			$('#modal_title').html('修改');
			$('#id').val(row.id);
			$('#provider').val(row.provider);
			$('#modelName').val(row.modelName);
			$('#apiUrl').val(row.apiUrl);
			$('#apiKey').val('');
			$('#remarks').val(row.remarks);
			$('#update').show();
            $('#add').hide();
			$('#myModal').modal('show');
		}
		$('#update').on('click', function () {
			if(formcheck()){
				$.ajax({
	                cache: true,
	                type: "POST",
	                async:false,
	                url:"<%=basePath %>aimodel/update.html",
	                data:$('#addfrom').serialize(),
	                error: function(request) { parent.window.toastralert('error','网络连接错误，请联系管理员'); },
	                success: function(data) {
	                	if("修改成功"== data){
	                		parent.window.toastralert('success',data);
	                		window.location = "<%=basePath%>aimodel/list.html";
	                	}else{
	                		parent.window.toastralert('error',data);
	                	}
	                }
	            });
			}
		});
		function del(id){
			if(confirm("确定要删除该信息？")){
				$.ajax({
	                cache: true,
	                type: "POST",
	                async:false,
	                url:"<%=basePath%>aimodel/del.html",
	                data:{"id":id},
	                error: function(request) { parent.window.toastralert('error','网络连接错误，请联系管理员'); },
	                success: function(data) {
	                	if("删除成功"== data){
	                		parent.window.toastralert('success',data);
	                		window.location = "<%=basePath%>aimodel/list.html";
	                	}else{
	                		parent.window.toastralert('error',data);
	                	}
	                }
	            });
			}
		}
		function testCall(id){
			parent.window.toastralert('info','正在测试模型调用，请稍候');
			$.ajax({
                cache: false,
                type: "POST",
                async:true,
                url:"<%=basePath%>aimodel/testcall.html",
                data:{"id":id},
                error: function(request) {
                	parent.window.toastralert('error','网络连接错误，请联系管理员');
                },
                success: function(data) {
                	if(data.indexOf('调用成功') == 0){
                		parent.window.toastralert('success',data);
                	}else{
                		parent.window.toastralert('error',data);
                	}
                }
            });
		}
		function formcheck(){
			if($('#provider').val()== null || $('#provider').val()== ''){
				parent.window.toastralert('error','供应商不能为空');
				return false;
			}
			if($('#modelName').val()== null || $('#modelName').val()== ''){
				parent.window.toastralert('error','模型名称不能为空');
				return false;
			}
			if($('#apiUrl').val()== null || $('#apiUrl').val()== ''){
				parent.window.toastralert('error','接口地址不能为空');
				return false;
			}
			if($('#id').val()== '' && ($('#apiKey').val()== null || $('#apiKey').val()== '')){
				parent.window.toastralert('error','API Key不能为空');
				return false;
			}
			return true;
		}
	</script>
</body>
</html>
