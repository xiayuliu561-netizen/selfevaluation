<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML>
<html lang="zh-cn">
<head>
	<title>工程教育专业认证自评系统</title>
	<meta charset="utf-8">
	<meta name="renderer" content="webkit|ie-comp|ie-stand">
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no" />
	<meta http-equiv="Cache-Control" content="no-siteapp" />
	<link rel="Bookmark" href="/favicon.ico" >
	<link rel="Shortcut Icon" href="/favicon.ico" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/lib/H-ui/css/H-ui.min.css" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/lib/H-ui/css/H-ui.admin.css" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/lib/H-ui/css/H-ui.doc.css" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/css/fonts/iconfont/iconfont.min.css" />
	<link rel="stylesheet" href="<%=basePath %>resources/css/fonts/font-awesome/font-awesome.css">
	<link rel="stylesheet" href="<%=basePath %>resources/css/fonts/web-icons/web-icons.css">
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/lib/toastr/toastr.css" />
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/css/skin.css"/>
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/css/style.css" />
	<script type="text/javascript" src="<%=basePath %>resources/js/jquery.min.js"></script>
</head>
<body class="modern-admin-shell">
	<header class="app-header">
		<div class="app-header-left">
			<button type="button" class="icon-button visible-xs-inline" id="mobileMenuToggle" aria-label="打开菜单">
				<i class="fa fa-bars"></i>
			</button>
			<button type="button" class="icon-button hidden-xs" id="sidebarToggle" aria-label="折叠菜单">
				<i class="fa fa-bars"></i>
			</button>
			<div class="app-title-block">
				<div class="app-title">工程教育专业认证自评系统</div>
				<div class="app-subtitle">V1.0 自评与课程质量管理平台</div>
			</div>
		</div>
		<div class="app-header-right">
			<span class="role-badge">
				<c:choose>
					<c:when test="${user.isadmin == 0}">管理员</c:when>
					<c:when test="${user.isadmin == 1}">教师</c:when>
					<c:when test="${user.isadmin == 2}">学生</c:when>
					<c:otherwise>用户</c:otherwise>
				</c:choose>
			</span>
			<div class="user-menu">
				<button type="button" class="user-menu-trigger" id="userMenuToggle" aria-haspopup="true" aria-expanded="false">
					<span class="user-avatar"><i class="fa fa-user"></i></span>
					<span class="user-meta hidden-xs">
						<span class="user-name">${user.name }</span>
						<span class="user-account">${user.username }</span>
					</span>
					<i class="fa fa-angle-down hidden-xs"></i>
				</button>
				<div class="user-dropdown" id="userDropdown">
					<a href="javascript:changeframesrc('user/personalinfo.html','');"><i class="fa fa-user"></i> 个人信息</a>
					<a href="javascript:changeframesrc('user/personalinfo.html','');"><i class="fa fa-key"></i> 修改密码</a>
					<a href="<%=basePath %>user/logout.html?sessionRole=${sessionRole}" class="danger"><i class="fa fa-sign-out"></i> 退出登录</a>
				</div>
			</div>
		</div>
	</header>

	<div class="sidebar-backdrop" id="sidebarBackdrop"></div>
	<aside class="Hui-aside app-sidebar">
		<input runat="server" id="divScrollValue" type="hidden" value="" />
		<div class="sidebar-brand">
			<div class="brand-mark"><i class="fa fa-graduation-cap"></i></div>
			<div class="brand-copy">
				<strong>认证自评</strong>
				<span>Engineering Education</span>
			</div>
		</div>
		<div class="menu_dropdown modern-menu" id="menubar0">
			<c:if test="${user.isadmin == 0}">
				<dl>
					<dt><i class="icon fa fa-sitemap"></i><span class="nav-text">组织管理</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("college/list.html","");'><i class="icon fa fa-bank"></i><span class="nav-text">学院管理</span></a></li>
							<li><a href='javascript:changeframesrc("dept/list.html","");'><i class="icon fa fa-th-large"></i><span class="nav-text">班级管理</span></a></li>
						</ul>
					</dd>
				</dl>
				<dl>
					<dt><i class="icon fa fa-users"></i><span class="nav-text">用户管理</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("user/teacherlist.html","");'><i class="icon fa fa-user"></i><span class="nav-text">教师管理</span></a></li>
							<li><a href='javascript:changeframesrc("user/studentlist.html","");'><i class="icon fa fa-users"></i><span class="nav-text">学生管理</span></a></li>
						</ul>
					</dd>
				</dl>
				<dl>
					<dt><i class="icon fa fa-cloud"></i><span class="nav-text">AI大模型管理</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("aimodel/list.html","");'><i class="icon fa fa-sliders"></i><span class="nav-text">AI大模型管理</span></a></li>
						</ul>
					</dd>
				</dl>
			</c:if>
			<c:if test="${user.isadmin == 1}">
				<dl>
					<dt><i class="icon fa fa-book"></i><span class="nav-text">课程管理</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("lesson/list.html","");'><i class="icon fa fa-bookmark-o"></i><span class="nav-text">课程管理</span></a></li>
						</ul>
					</dd>
				</dl>
				<dl>
					<dt><i class="icon fa fa-magic"></i><span class="nav-text">融合数据分析</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("fusion/list.html","");'><i class="icon fa fa-random"></i><span class="nav-text">融合数据分析</span></a></li>
						</ul>
					</dd>
				</dl>
				<dl>
					<dt><i class="icon fa fa-table"></i><span class="nav-text">成绩管理</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("score/list.html","");'><i class="icon fa fa-pencil-square-o"></i><span class="nav-text">成绩管理</span></a></li>
						</ul>
					</dd>
				</dl>
				<dl>
					<dt><i class="icon fa fa-line-chart"></i><span class="nav-text">课程质量分析</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("score/selfevalution.html","");'><i class="icon fa fa-bar-chart"></i><span class="nav-text">课程质量分析</span></a></li>
							<li><a href='javascript:changeframesrc("score/charts.html","");'><i class="icon fa fa-area-chart"></i><span class="nav-text">课程质量分析图表</span></a></li>
						</ul>
					</dd>
				</dl>
				<dl>
					<dt><i class="icon fa fa-file-text-o"></i><span class="nav-text">课程质量报告</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("reports/form.html","");'><i class="icon fa fa-file-word-o"></i><span class="nav-text">课程质量报告</span></a></li>
						</ul>
					</dd>
				</dl>
				<dl>
					<dt><i class="icon fa fa-check-square-o"></i><span class="nav-text">调查问卷管理</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("question/list.html","");'><i class="icon fa fa-list-alt"></i><span class="nav-text">调查问卷管理</span></a></li>
							<li><a href='javascript:changeframesrc("question/questionanswerscorelist.html","");'><i class="icon fa fa-pie-chart"></i><span class="nav-text">调查问卷成绩查看</span></a></li>
						</ul>
					</dd>
				</dl>
			</c:if>
			<c:if test="${user.isadmin == 2}">
				<dl>
					<dt><i class="icon fa fa-search"></i><span class="nav-text">成绩查询</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("score/ownlist.html","");'><i class="icon fa fa-graduation-cap"></i><span class="nav-text">总评成绩查询</span></a></li>
						</ul>
					</dd>
				</dl>
				<dl>
					<dt><i class="icon fa fa-commenting-o"></i><span class="nav-text">调查问卷</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
					<dd>
						<ul>
							<li><a href='javascript:changeframesrc("question/questionanswer.html","");'><i class="icon fa fa-edit"></i><span class="nav-text">调查问卷</span></a></li>
						</ul>
					</dd>
				</dl>
			</c:if>
			<dl>
				<dt><i class="icon fa fa-bullhorn"></i><span class="nav-text">公告管理</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
				<dd>
					<ul>
						<c:if test="${user.isadmin == 0}">
							<li><a href='javascript:changeframesrc("news/list.html","");'><i class="icon fa fa-newspaper-o"></i><span class="nav-text">公告管理</span></a></li>
						</c:if>
						<c:if test="${user.isadmin == 1}">
							<li><a href='javascript:changeframesrc("news/checklist.html","");'><i class="icon fa fa-newspaper-o"></i><span class="nav-text">公告管理</span></a></li>
						</c:if>
						<c:if test="${user.isadmin == 2}">
							<li><a href='javascript:changeframesrc("news/checklist.html","");'><i class="icon fa fa-newspaper-o"></i><span class="nav-text">公告管理</span></a></li>
						</c:if>
					</ul>
				</dd>
			</dl>
			<dl>
				<dt><i class="icon fa fa-user"></i><span class="nav-text">个人信息管理</span><i class="Hui-iconfont menu_dropdown-arrow">&#xe6d5;</i></dt>
				<dd>
					<ul>
						<li><a href='javascript:changeframesrc("user/personalinfo.html","");'><i class="icon fa fa-user"></i><span class="nav-text">个人信息管理</span></a></li>
					</ul>
				</dd>
			</dl>
		</div>
	</aside>

	<section class="Hui-article-box app-main">
		<div class="frame-header">
			<div class="frame-heading">
				<div class="frame-breadcrumb" id="frameBreadcrumb">首页 / 工作台</div>
				<h1 id="frameTitle">工作台</h1>
			</div>
			<div class="frame-actions">
				<button type="button" class="btn btn-default btn-sm" id="showWorkbenchBtn"><i class="fa fa-dashboard"></i> 工作台</button>
				<button type="button" class="btn btn-primary btn-sm" id="refreshFrameBtn"><i class="fa fa-refresh"></i> 刷新当前页</button>
			</div>
		</div>

		<div class="workbench" id="workbench">
				<div class="welcome-panel">
					<div>
						<div class="welcome-kicker">工程教育专业认证自评系统</div>
						<h2>欢迎您，${user.name }</h2>
					</div>
				<div class="welcome-status">
					<span class="status-dot"></span>
					<c:choose>
						<c:when test="${user.isadmin == 0}">管理员工作区</c:when>
						<c:when test="${user.isadmin == 1}">教师工作区</c:when>
						<c:when test="${user.isadmin == 2}">学生工作区</c:when>
						<c:otherwise>用户工作区</c:otherwise>
					</c:choose>
				</div>
			</div>

				<c:if test="${user.isadmin == 0}">
					<div class="service-stats-grid" id="adminServiceStats">
						<div class="service-stat-card">
							<div class="service-stat-icon"><i class="fa fa-line-chart"></i></div>
							<div class="service-stat-content">
								<div class="service-stat-title">累计服务人次</div>
								<div class="service-stat-main">当前已累计 <strong id="gradeServiceCount">--</strong><span id="gradeServiceCountUnit"> 人次</span></div>
								<div class="service-stat-desc">根据成功处理成绩的学生人数自动累计</div>
								<div class="service-stat-trend"><i class="fa fa-arrow-up"></i> 持续累计</div>
							</div>
						</div>
					</div>
				</c:if>

			<div class="quick-section">
				<div class="section-heading">
					<h3>快捷入口</h3>
					<span>进入常用业务页面</span>
				</div>
				<div class="quick-grid">
					<c:if test="${user.isadmin == 0}">
						<a class="quick-card" href='javascript:changeframesrc("college/list.html","");'><i class="fa fa-bank"></i><span>学院管理</span></a>
						<a class="quick-card" href='javascript:changeframesrc("dept/list.html","");'><i class="fa fa-th-large"></i><span>班级管理</span></a>
						<a class="quick-card" href='javascript:changeframesrc("user/teacherlist.html","");'><i class="fa fa-users"></i><span>教师管理</span></a>
						<a class="quick-card" href='javascript:changeframesrc("aimodel/list.html","");'><i class="fa fa-cloud"></i><span>AI大模型管理</span></a>
					</c:if>
					<c:if test="${user.isadmin == 1}">
						<a class="quick-card" href='javascript:changeframesrc("lesson/list.html","");'><i class="fa fa-book"></i><span>课程管理</span></a>
						<a class="quick-card" href='javascript:changeframesrc("fusion/list.html","");'><i class="fa fa-magic"></i><span>融合数据分析</span></a>
						<a class="quick-card" href='javascript:changeframesrc("score/list.html","");'><i class="fa fa-table"></i><span>成绩管理</span></a>
						<a class="quick-card" href='javascript:changeframesrc("reports/form.html","");'><i class="fa fa-file-text-o"></i><span>课程质量报告</span></a>
					</c:if>
					<c:if test="${user.isadmin == 2}">
						<a class="quick-card" href='javascript:changeframesrc("score/ownlist.html","");'><i class="fa fa-search"></i><span>总评成绩查询</span></a>
						<a class="quick-card" href='javascript:changeframesrc("question/questionanswer.html","");'><i class="fa fa-commenting-o"></i><span>调查问卷</span></a>
					</c:if>
					<a class="quick-card" href='javascript:changeframesrc("news/checklist.html","");'><i class="fa fa-bullhorn"></i><span>公告管理</span></a>
					<a class="quick-card" href='javascript:changeframesrc("user/personalinfo.html","");'><i class="fa fa-user"></i><span>个人信息</span></a>
				</div>
			</div>
		</div>

		<div class="frame-panel is-hidden" id="framePanel">
			<div class="frame-loader" id="frameLoader">
				<div class="skeleton-line wide"></div>
				<div class="skeleton-line"></div>
				<div class="skeleton-table"></div>
			</div>
				<iframe id="myframe" name="myframe" src="" scrolling="no" frameborder="0" height="100%" width="100%" title="业务页面"></iframe>
		</div>
	</section>

	<script type="text/javascript" src="<%=basePath %>resources/js/prettify.js"></script>
	<script type="text/javascript" src="<%=basePath %>resources/lib/H-ui/js/H-ui.min.js"></script>
	<script type="text/javascript" src="<%=basePath %>resources/js/common.js"></script>
	<script type="text/javascript" src="<%=basePath %>resources/lib/toastr/toastr.min.js"></script>
	<script type="text/javascript">
		var currentRole = "${sessionRole}";
		var frameTitleMap = {
			"college/list.html": "学院管理",
			"dept/list.html": "班级管理",
			"user/teacherlist.html": "教师管理",
			"user/studentlist.html": "学生管理",
			"aimodel/list.html": "AI大模型管理",
			"lesson/list.html": "课程管理",
			"fusion/list.html": "融合数据分析",
			"score/list.html": "成绩管理",
			"score/selfevalution.html": "课程质量分析",
			"score/charts.html": "课程质量分析图表",
			"reports/form.html": "课程质量报告",
			"question/list.html": "调查问卷管理",
			"question/questionanswerscorelist.html": "调查问卷成绩查看",
			"score/ownlist.html": "总评成绩查询",
			"question/questionanswer.html": "调查问卷",
			"news/list.html": "公告管理",
			"news/checklist.html": "公告管理",
			"user/personalinfo.html": "个人信息"
		};
		function normalizeFrameUrl(url){
			return String(url || '').split('?')[0].replace(/^\/+/, '');
		}
		function setFrameHeading(title){
			var label = title || '业务页面';
			$('#frameTitle').text(label);
			$('#frameBreadcrumb').text('首页 / ' + label);
		}
		function formatServiceCount(count){
			var number = Number(count);
			if(isNaN(number) || number < 0){
				return "0";
			}
			return number.toLocaleString('zh-CN');
		}
		function loadGradeServiceCount(){
			if(currentRole != "0" || $('#gradeServiceCount').length == 0){
				return;
			}
			$.ajax({
				type: "GET",
				url: "<%=basePath%>statistics/grade-service-count.html",
				data: {"sessionRole": currentRole},
				success: function(data){
					var json = data;
					if(typeof data == 'string'){
						try{
							json = $.parseJSON(data);
						}catch(e){
							json = null;
						}
					}
					if(json && json.code == 200 && json.data){
						$('#gradeServiceCount').text(formatServiceCount(json.data.count));
						$('#gradeServiceCountUnit').text(' 人次');
					}else{
						$('#gradeServiceCount').text('暂无数据');
						$('#gradeServiceCountUnit').text('');
					}
				},
				error: function(){
					$('#gradeServiceCount').text('暂无数据');
					$('#gradeServiceCountUnit').text('');
				}
			});
		}
		function changeframesrc(url, menuid){
			setFrameHeading(frameTitleMap[normalizeFrameUrl(url)] || '业务页面');
			var src = "<%=basePath%>"+url;
			var sessionRole = "${sessionRole}";
			if(src.indexOf("?")>0){
				src += "&menuid="+menuid;
			}else{
				src += "?menuid="+menuid;
			}
			if(sessionRole != ""){
				src += "&sessionRole="+encodeURIComponent(sessionRole);
			}
			$('#workbench').hide();
			$('.app-main').addClass('is-frame-view');
			$('#framePanel').removeClass('is-hidden').addClass('is-loading');
			document.getElementById("myframe").src=src;
		}
		function displaynavbar(){
			$('body').toggleClass('sidebar-collapsed');
		}
		function resizeFrameToContent(){
			var frame = document.getElementById('myframe');
			if(!frame || !frame.contentWindow || !frame.contentWindow.document || !frame.src){
				return;
			}
			try{
				var doc = frame.contentWindow.document;
				var body = doc.body;
				if(!body){
					return;
				}
				var bodyStyle = frame.contentWindow.getComputedStyle(body);
				var bodyPaddingBottom = parseFloat(bodyStyle.paddingBottom) || 0;
				var contentBottom = 0;
				$(body).children(':visible').each(function(){
					var rect = this.getBoundingClientRect();
					var style = frame.contentWindow.getComputedStyle(this);
					var marginBottom = parseFloat(style.marginBottom) || 0;
					contentBottom = Math.max(contentBottom, rect.bottom + marginBottom);
				});
				var height = Math.max(Math.ceil(contentBottom + bodyPaddingBottom), 640);
				var currentHeight = parseInt(frame.style.height, 10) || 0;
				if(Math.abs(currentHeight - height) > 2){
					frame.style.height = height + 'px';
				}
			}catch(e){
				frame.style.height = '100%';
			}
		}
		function bindFrameAutoResize(){
			var frame = document.getElementById('myframe');
			if(!frame || !frame.contentWindow || !frame.contentWindow.document){
				return;
			}
			try{
				var doc = frame.contentWindow.document;
				if(frame._selfEvalResizeObserver){
					frame._selfEvalResizeObserver.disconnect();
				}
				if(window.ResizeObserver){
					frame._selfEvalResizeObserver = new ResizeObserver(function(){
						resizeFrameToContent();
					});
					frame._selfEvalResizeObserver.observe(doc.documentElement);
					if(doc.body){
						frame._selfEvalResizeObserver.observe(doc.body);
					}
				}
					setTimeout(resizeFrameToContent, 60);
					setTimeout(resizeFrameToContent, 260);
					setTimeout(resizeFrameToContent, 900);
				}catch(e){}
			}
		function toastralert(type, msg){
			if(window.toastr){
				toastr.options = {
					closeButton: true,
					progressBar: true,
					positionClass: 'toast-top-right',
					timeOut: 2600
				};
				if(type == 'info'){
					toastr.info(msg);
				}else if(type == 'success'){
					toastr.success(msg);
				}else if(type == 'warning'){
					toastr.warning(msg);
				}else if(type == 'error'){
					toastr.error(msg);
				}else{
					toastr.info(msg);
				}
			}else{
				alert(msg);
			}
		}
		$(function(){
			var $body = $('body');
			var $menu = $('.modern-menu');
			var $menuItems = $menu.find('dl');
			var resizeTimer;
			loadGradeServiceCount();

			$menuItems.addClass('sidebar-menu-item');
			$menuItems.children('dt').addClass('sidebar-menu-trigger').attr({
				'tabindex': '0',
				'role': 'button',
				'aria-expanded': 'false'
			});
			$menuItems.children('dd').addClass('sidebar-submenu').attr('aria-hidden', 'true');
			$menuItems.children('dt').off('click');

			function closeSiblingMenus($item, instant){
				$item.siblings('.sidebar-menu-item.open').each(function(){
					setMenuOpen($(this), false, instant, true);
				});
			}

			function setMenuOpen($item, isOpen, instant, keepSiblings){
				var $submenu = $item.children('.sidebar-submenu');
				var submenu = $submenu[0];
				if(!submenu){
					return;
				}
				if(isOpen && !keepSiblings){
					closeSiblingMenus($item, instant);
				}
				$item.toggleClass('open', isOpen);
				$item.children('.sidebar-menu-trigger').attr('aria-expanded', isOpen ? 'true' : 'false');
				$submenu.toggleClass('show', isOpen).attr('aria-hidden', isOpen ? 'false' : 'true');
				if(isOpen && !$body.hasClass('sidebar-collapsed')){
					if(instant){
						$submenu.css('transition', 'none');
					}
					$submenu.css('max-height', submenu.scrollHeight + 'px');
					if(instant){
						submenu.offsetHeight;
						$submenu.css('transition', '');
					}
				}else{
					$submenu.css('max-height', '0px');
				}
			}

			function refreshOpenMenus(){
				$menuItems.filter('.open').each(function(){
					setMenuOpen($(this), true, true, true);
				});
			}

			$menuItems.each(function(index){
				setMenuOpen($(this), index === 0, true, true);
			});

			$menu.off('click.selfEvalMenu').on('click.selfEvalMenu', '.sidebar-menu-trigger', function(e){
				e.preventDefault();
				e.stopPropagation();
				var $item = $(this).closest('.sidebar-menu-item');
				var wasCollapsed = $body.hasClass('sidebar-collapsed');
				if($body.hasClass('sidebar-collapsed')){
					$body.removeClass('sidebar-collapsed');
					localStorage.setItem('selfEvalSidebarCollapsed', '0');
				}
				setMenuOpen($item, wasCollapsed ? true : !$item.hasClass('open'));
			});

			$menu.off('keydown.selfEvalMenu').on('keydown.selfEvalMenu', '.sidebar-menu-trigger', function(e){
				if(e.key === 'Enter' || e.key === ' ' || e.keyCode === 13 || e.keyCode === 32){
					e.preventDefault();
					$(this).trigger('click');
				}
			});
			$menu.on('click', 'li a', function(){
				var label = $.trim($(this).text());
				$menu.find('li').removeClass('current');
				$(this).parent('li').addClass('current');
				setMenuOpen($(this).closest('.sidebar-menu-item'), true, true);
				$(this).parents('.sidebar-menu-item').each(function(){
					setMenuOpen($(this), true, true);
				});
				$('#frameTitle').text(label || '业务页面');
				$('#frameBreadcrumb').text('首页 / ' + (label || '业务页面'));
				$body.removeClass('sidebar-mobile-open');
			});
			$('#sidebarToggle').on('click', function(){
				$body.toggleClass('sidebar-collapsed');
				localStorage.setItem('selfEvalSidebarCollapsed', $body.hasClass('sidebar-collapsed') ? '1' : '0');
				refreshOpenMenus();
			});
			if(localStorage.getItem('selfEvalSidebarCollapsed') == '1'){
				$body.addClass('sidebar-collapsed');
			}
			refreshOpenMenus();
			$(window).on('resize.selfEvalMenu', function(){
				clearTimeout(resizeTimer);
				resizeTimer = setTimeout(refreshOpenMenus, 120);
			});
			$('#mobileMenuToggle').on('click', function(){
				$body.removeClass('sidebar-collapsed').addClass('sidebar-mobile-open');
				localStorage.setItem('selfEvalSidebarCollapsed', '0');
				refreshOpenMenus();
			});
			$('#sidebarBackdrop').on('click', function(){
				$body.removeClass('sidebar-mobile-open');
			});
			$('#userMenuToggle').on('click', function(e){
				e.stopPropagation();
				$('.user-menu').toggleClass('open');
				$(this).attr('aria-expanded', $('.user-menu').hasClass('open') ? 'true' : 'false');
			});
			$(document).on('click', function(){
				$('.user-menu').removeClass('open');
				$('#userMenuToggle').attr('aria-expanded', 'false');
			});
			$('#myframe').on('load', function(){
				$('#framePanel').removeClass('is-loading');
				bindFrameAutoResize();
			});
			$('#refreshFrameBtn').on('click', function(){
				var frame = document.getElementById('myframe');
				if(frame && frame.src){
					$('#framePanel').addClass('is-loading');
					frame.contentWindow.location.reload();
				}else{
					location.reload();
				}
			});
			$('#showWorkbenchBtn').on('click', function(){
				var frame = document.getElementById('myframe');
				if(frame && frame._selfEvalResizeObserver){
					frame._selfEvalResizeObserver.disconnect();
					frame._selfEvalResizeObserver = null;
				}
				$('#myframe').attr('src', '').css('height', '');
				$('#framePanel').addClass('is-hidden').removeClass('is-loading');
				$('#workbench').show();
				$('.app-main').removeClass('is-frame-view');
				$('#frameTitle').text('工作台');
				$('#frameBreadcrumb').text('首页 / 工作台');
				$menu.find('li').removeClass('current');
			});
			$(window).on('resize.selfEvalFrame', function(){
				setTimeout(resizeFrameToContent, 120);
			});
		});
	</script>
</body>
</html>
