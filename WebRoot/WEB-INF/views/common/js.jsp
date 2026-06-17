<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<!-- bootstrap -->
<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap/css/bootstrap.min.css">

<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap/css/radio.css">
<script src="<%=basePath %>resources/js/jquery.min.js"></script>
<script src="<%=basePath %>resources/lib/bootstrap/js/bootstrap.min.js"></script>
<!-- 页面基础 -->
<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/css/style.css" />
<!-- ztree -->
<link rel="stylesheet" href="<%=basePath %>resources/lib/ztree/css/metroStyle/metroStyle.css" type="text/css">
<script type="text/javascript" src="<%=basePath %>resources/lib/ztree/js/jquery.ztree.core.js"></script>
<script type="text/javascript" src="<%=basePath %>resources/lib/ztree/js/jquery.ztree.excheck.js"></script>
<script type="text/javascript" src="<%=basePath %>resources/lib/ztree/js/jquery.ztree.exedit.js"></script>
<!-- 图标 CSS-->
<link rel="stylesheet" href="<%=basePath %>resources/css/fonts/font-awesome/font-awesome.css">
<link rel="stylesheet" href="<%=basePath %>resources/css/fonts/web-icons/web-icons.css">
<!-- fontawesome -->
<link rel="stylesheet" href="<%=basePath %>resources/lib/fontawesome-iconpicker/iconpicker.css">
<script src="<%=basePath %>resources/lib/fontawesome-iconpicker/fontawesome-iconpicker.min.js"></script>
<!-- 滚动条 -->
<script type="text/javascript" src="<%=basePath %>resources/lib/slimscroll/jquery.slimscroll.min.js"></script>
<!-- bootstrap-table -->
<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap-table/bootstrap-table.css">
<script src="<%=basePath %>resources/lib/bootstrap-table/bootstrap-table.js"></script>
<!-- bootstrap-table汉化包 -->
<script src="<%=basePath %>resources/lib/bootstrap-table/bootstrap-table-zh-CN.js"></script>
<!--bootstrap-table 导出 -->
<script src="<%=basePath %>resources/lib/bootstrap-table/bootstrap-table-export.js"></script>
<script src="<%=basePath %>resources/lib/bootstrap-table/tableExport.js"></script>
<!-- bootstrap-multiselect -->
<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap-multiselect/css/bootstrap-multiselect.css">
<script src="<%=basePath %>resources/lib/bootstrap-multiselect/js/bootstrap-multiselect.js"></script>
<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap-multiselect/css/multiselect-defined.css">
<!-- bootstrap-datetimepicker -->
<link rel="stylesheet" href="<%=basePath %>resources/lib/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css">
<script src="<%=basePath %>resources/lib/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="<%=basePath %>resources/lib/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js"></script>
<!-- common -->
<script src="<%=basePath %>resources/js/common.js"></script>
<script type="text/javascript">
	window.SELF_EVAL_SESSION_ROLE = '${sessionRole}';
	(function(){
		function role(){
			return window.SELF_EVAL_SESSION_ROLE || '';
		}
		function appendRoleToUrl(url){
			var currentRole = role();
			if(!currentRole || !url || url.indexOf('javascript:') === 0 || url.indexOf('#') === 0){
				return url;
			}
			if(url.indexOf('sessionRole=') >= 0){
				return url;
			}
			var hash = '';
			var hashIndex = url.indexOf('#');
			if(hashIndex >= 0){
				hash = url.substring(hashIndex);
				url = url.substring(0, hashIndex);
			}
			return url + (url.indexOf('?') >= 0 ? '&' : '?') + 'sessionRole=' + encodeURIComponent(currentRole) + hash;
		}
		window.appendSessionRole = appendRoleToUrl;
		$(function(){
			var currentRole = role();
			if(!currentRole){
				return;
			}
			$('form').each(function(){
				var form = $(this);
				if(form.find('input[name="sessionRole"]').length === 0){
					form.append('<input type="hidden" name="sessionRole" value="'+currentRole+'">');
				}
				var action = form.attr('action');
				if(action){
					form.attr('action', appendRoleToUrl(action));
				}
			});
		});
		$(document).ajaxSend(function(event, jqxhr, settings){
			settings.url = appendRoleToUrl(settings.url);
			if(settings.data instanceof FormData){
				if(!settings.data.has || !settings.data.has('sessionRole')){
					settings.data.append('sessionRole', role());
				}
			}else if(settings.type && settings.type.toUpperCase() !== 'GET' && role()){
				if(!settings.data){
					settings.data = 'sessionRole=' + encodeURIComponent(role());
				}else if(typeof settings.data === 'string' && settings.data.indexOf('sessionRole=') < 0){
					settings.data += '&sessionRole=' + encodeURIComponent(role());
				}else if(typeof settings.data === 'object' && settings.data.sessionRole == null){
					settings.data.sessionRole = role();
				}
			}
		});
		var oldBootstrapTable = $.fn.bootstrapTable;
		if(oldBootstrapTable){
			$.fn.bootstrapTable = function(){
				if(arguments.length > 0 && typeof arguments[0] === 'object' && arguments[0].url){
					arguments[0].url = appendRoleToUrl(arguments[0].url);
				}
				return oldBootstrapTable.apply(this, arguments);
			};
			$.fn.bootstrapTable.Constructor = oldBootstrapTable.Constructor;
			$.fn.bootstrapTable.defaults = oldBootstrapTable.defaults;
			$.fn.bootstrapTable.locales = oldBootstrapTable.locales;
		}
	})();
</script>
