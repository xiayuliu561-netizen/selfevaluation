<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>

<!DOCTYPE HTML>
<html lang="zh-cn">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta charset="utf-8">
	<title>工程教育专业认证自评系统</title>
	<link rel="stylesheet" href="<%=basePath %>resources/css/fonts/font-awesome/font-awesome.css">
	<link rel="stylesheet" type="text/css" href="<%=basePath %>resources/css/style.css" />
	<script type="text/javascript" src="<%=basePath %>resources/js/jquery.min.js"></script>
	<script type="text/javascript">
		function check(){
			var usernameInput = document.getElementById("username");
			var passwordInput = document.getElementById("password");
			var error = document.getElementById("loginClientError");
			var submit = document.getElementById("loginSubmit");
			var username = usernameInput.value.replace(/(^\s*)|(\s*$)/g, "");
			var password = passwordInput.value.replace(/(^\s*)|(\s*$)/g, "");
			usernameInput.parentNode.className = usernameInput.parentNode.className.replace(/\s?is-invalid/g, "");
			passwordInput.parentNode.className = passwordInput.parentNode.className.replace(/\s?is-invalid/g, "");
			if(error){
				error.hidden = true;
				error.innerHTML = "";
			}
			if(username == '' || password == ''){
				if(username == ''){
					usernameInput.parentNode.className += " is-invalid";
				}
				if(password == ''){
					passwordInput.parentNode.className += " is-invalid";
				}
				if(error){
					error.innerHTML = '<i class="fa fa-exclamation-circle"></i> 账号和密码不能为空';
					error.hidden = false;
				}
				return false;
			}
			if(submit){
				submit.disabled = true;
				submit.className += " is-loading";
				submit.innerHTML = '<i class="fa fa-spinner fa-spin"></i> 登录中';
			}
			return true;
		}
		document.addEventListener("DOMContentLoaded", function(){
			var form = document.getElementById("form1");
			var username = document.getElementById("username");
			var password = document.getElementById("password");
			var card = document.querySelector(".login-card");
			var rafId = null;
			var pointer = { x: 0, y: 0, active: false };
			function submitOnEnter(event){
				if(event.key === "Enter" || event.keyCode === 13){
					event.preventDefault();
					if(form.requestSubmit){
						form.requestSubmit();
					}else if(check()){
						form.submit();
					}
				}
			}
			if(username){
				username.addEventListener("keydown", submitOnEnter);
			}
			if(password){
				password.addEventListener("keydown", submitOnEnter);
			}
			function updateLight(){
				rafId = null;
				if(!card){
					return;
				}
				card.style.setProperty("--glow-x", pointer.x + "px");
				card.style.setProperty("--glow-y", pointer.y + "px");
				card.style.setProperty("--glow-opacity", pointer.active ? ".34" : "0");
			}
			function scheduleLightUpdate(){
				if(!rafId){
					rafId = requestAnimationFrame(updateLight);
				}
			}
			if(card){
				card.addEventListener("mousemove", function(event){
					var rect = card.getBoundingClientRect();
					pointer.x = Math.round(event.clientX - rect.left);
					pointer.y = Math.round(event.clientY - rect.top);
					pointer.active = true;
					scheduleLightUpdate();
				});
				card.addEventListener("mouseleave", function(){
					pointer.active = false;
					scheduleLightUpdate();
				});
			}
		});
	</script>
	<style type="text/css">
		.login-page * {
			box-sizing: border-box;
		}
		body.login-page {
			min-height: 100vh;
			margin: 0;
			overflow-x: hidden;
			background:
				radial-gradient(circle at 18% 18%, rgba(8, 145, 178, .18), transparent 28%),
				radial-gradient(circle at 86% 14%, rgba(109, 91, 208, .14), transparent 30%),
				linear-gradient(135deg, #eef6f8 0%, #f8fbff 46%, #edf4f1 100%);
			color: #16304f;
			font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", Arial, sans-serif;
		}
		body.login-page:before {
			content: "";
			position: fixed;
			inset: 0;
			z-index: 0;
			background:
				linear-gradient(rgba(23, 107, 135, .055) 1px, transparent 1px),
				linear-gradient(90deg, rgba(23, 107, 135, .055) 1px, transparent 1px);
			background-size: 32px 32px;
			mask-image: linear-gradient(180deg, rgba(0, 0, 0, .52), transparent 72%);
			pointer-events: none;
		}
		.login-bg-light {
			position: fixed;
			inset: 0;
			z-index: 0;
			overflow: hidden;
			pointer-events: none;
		}
		.login-orb {
			position: absolute;
			width: 220px;
			height: 220px;
			border-radius: 50%;
			background: rgba(8, 145, 178, .12);
			filter: blur(34px);
			animation: login-orb-drift 13s ease-in-out infinite alternate;
		}
		.login-orb.one {
			top: 10%;
			left: 9%;
		}
		.login-orb.two {
			right: 12%;
			bottom: 10%;
			width: 260px;
			height: 260px;
			background: rgba(109, 91, 208, .1);
			animation-duration: 15s;
			animation-delay: -3s;
		}
		@keyframes login-orb-drift {
			from {
				transform: translate3d(-18px, -10px, 0);
			}
			to {
				transform: translate3d(24px, 18px, 0);
			}
		}
		.login-shell {
			position: relative;
			z-index: 1;
			min-height: 100vh;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			padding: 34px 20px;
		}
		.login-title {
			margin: 0 0 18px;
			color: #13263a;
			font-size: 26px;
			font-weight: 780;
			letter-spacing: 0;
			line-height: 1.25;
			text-align: center;
			text-shadow: none;
		}
		.login-layout {
			width: min(430px, calc(100vw - 40px));
			max-width: calc(100vw - 40px);
		}
		.login-card {
			--glow-x: 50%;
			--glow-y: 50%;
			--glow-opacity: 0;
			position: relative;
			min-height: 0;
			display: block;
			overflow: hidden;
			border: 1px solid rgba(203, 214, 226, .86);
			border-radius: 16px;
			background: rgba(255, 255, 255, .98);
			box-shadow: 0 18px 42px rgba(0, 0, 0, .12);
			transition: border-color .22s ease, box-shadow .22s ease, transform .22s ease;
		}
		.login-card:hover {
			border-color: rgba(23, 107, 135, .28);
			box-shadow: 0 22px 48px rgba(0, 0, 0, .14);
			transform: translateY(-1px);
		}
		.login-card:before,
		.login-card:after {
			content: "";
			position: absolute;
			inset: 0;
			pointer-events: none;
			z-index: 0;
		}
		.login-card:before {
			background: radial-gradient(circle 180px at var(--glow-x) var(--glow-y), rgba(23, 107, 135, var(--glow-opacity)), transparent 70%);
			transition: opacity .22s ease;
		}
		.login-card:after {
			border-radius: inherit;
			box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .7);
		}
		.login-form-panel {
			position: relative;
			z-index: 1;
			display: flex;
			flex-direction: column;
			justify-content: center;
			padding: 34px 34px 32px;
			background: linear-gradient(180deg, #ffffff, #fbfdff);
		}
		.login-form-panel h2 {
			margin: 0;
			color: #102a43;
			font-size: 22px;
			font-weight: 780;
			line-height: 1.2;
		}
		.login-form {
			width: 100%;
			margin-top: 22px;
		}
		.login-field {
			margin-bottom: 15px;
		}
		.login-field label {
			display: block;
			margin-bottom: 7px;
			color: #26384d;
			font-size: 13px;
			font-weight: 750;
		}
		.login-input {
			position: relative;
		}
		.login-input i {
			position: absolute;
			top: 50%;
			left: 16px;
			transform: translateY(-50%);
			color: #768aa0;
			font-size: 15px;
		}
		.login-input input {
			width: 100%;
			height: 44px;
			padding: 0 16px 0 44px;
			border: 1px solid #cbd6e2;
			border-radius: 10px;
			background: #f8fafc;
			color: #102a43;
			font-size: 15px;
			outline: none;
			transition: border-color .18s ease, box-shadow .18s ease, background-color .18s ease;
		}
		.login-input input::placeholder {
			color: #7d8fa4;
		}
		.login-input input:focus {
			border-color: #176b87;
			background: #ffffff;
			box-shadow: 0 0 0 4px rgba(23, 107, 135, .14);
		}
		.login-input.is-invalid input {
			border-color: #c2413b;
			background: #fff7f7;
		}
		.login-input.is-invalid i {
			color: #c2413b;
		}
		.login-error {
			display: flex;
			align-items: center;
			gap: 8px;
			margin: 0 0 14px;
			padding: 10px 12px;
			border: 1px solid rgba(194, 65, 59, .24);
			border-radius: 10px;
			background: #fff1f1;
			color: #b42318;
			font-size: 13px;
			line-height: 20px;
		}
		.login-error[hidden] {
			display: none !important;
		}
		.login-button {
			width: 100%;
			height: 46px;
			margin-top: 4px;
			border: 0;
			border-radius: 10px;
			background: #176b87;
			color: #ffffff;
			cursor: pointer;
			font-size: 15px;
			font-weight: 780;
			letter-spacing: 0;
			box-shadow: 0 10px 20px rgba(0, 0, 0, .12);
			transition: background-color .18s ease, transform .18s ease, box-shadow .18s ease;
		}
		.login-button:hover,
		.login-button:focus {
			background: #0f536b;
			box-shadow: 0 12px 24px rgba(0, 0, 0, .14);
		}
		.login-button:active {
			transform: translateY(1px);
		}
		.login-button:disabled,
		.login-button.is-loading {
			opacity: .76;
			cursor: wait;
			transform: none;
		}
		@media (max-width: 900px) {
			.login-title {
				font-size: 25px;
				margin-bottom: 18px;
			}
			.login-form-panel {
				padding: 34px;
			}
		}
		@media (max-width: 560px) {
			.login-shell {
				padding: 26px 14px;
			}
			.login-title {
				font-size: 22px;
			}
			.login-layout {
				width: min(94vw, 380px);
			}
			.login-card {
				border-radius: 18px;
			}
			.login-form-panel {
				padding: 30px 22px;
			}
			.login-form-panel h2 {
				font-size: 22px;
			}
		}
		@media (prefers-reduced-motion: reduce) {
			.login-orb {
				animation: none !important;
			}
			.login-card,
			.login-button,
			.login-input input {
				transition-duration: .01ms !important;
			}
		}
	</style>
</head>
<body class="login-page">
	<div class="login-bg-light" aria-hidden="true">
		<span class="login-orb one"></span>
		<span class="login-orb two"></span>
	</div>
	<div class="login-shell">
		<h1 class="login-title">工程教育专业认证自评系统</h1>
		<div class="login-layout">
				<div class="login-card">
					<section class="login-form-panel">
						<h2>登录</h2>
						<form class="login-form" action="<%=basePath %>user/main.html" method="post" id="form1" onsubmit="return check();">
							<input type="hidden" name="isadmin" id="isadmin" value="">
							<div class="login-field">
								<label for="username">账号</label>
								<div class="login-input">
									<i class="fa fa-user"></i>
									<input name="username" id="username" type="text" placeholder="账号" autocomplete="username" aria-label="账号">
								</div>
							</div>
							<div class="login-field">
								<label for="password">密码</label>
								<div class="login-input">
									<i class="fa fa-lock"></i>
									<input name="password" id="password" value="" type="password" placeholder="密码" autocomplete="current-password" aria-label="密码">
								</div>
							</div>
							<div class="login-error" id="loginClientError" hidden></div>
							<c:if test="${not empty msg}">
								<div class="login-error"><i class="fa fa-exclamation-circle"></i> ${msg}</div>
							</c:if>
							<button class="login-button" id="loginSubmit" type="submit">登录</button>
						</form>
					</section>
				</div>
			</div>
		</div>
</body>
</html>
