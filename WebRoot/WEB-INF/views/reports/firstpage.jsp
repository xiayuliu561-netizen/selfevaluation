<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = path + "/";
%>

			<p style="text-align:center;"><br /></p>
			<h1 style="text-align:center;">
				<strong><span style="font-family:Microsoft YaHei;"><span style="line-height:2;">${teacher.collegeName }</span><br />
			</span></strong><strong><span style="font-family:" line-height:2;"="">${teacher.deptName }课程教学</span></strong> 
			</h1>
			<p style="text-align:center;">
				<strong><span style="font-family:Microsoft YaHei;"><br />
			</span></strong> 
			</p>
			<div style="text-align:center; font-size: 60px;">
				<strong><span style="font-family:Microsoft YaHei;"><span style="line-height:2;">质</span><br /></span></strong>
				<strong><span style="font-family:Microsoft YaHei;"><span style="line-height:2;">量</span><br /></span></strong>
				<strong><span style="font-family:Microsoft YaHei;"><span style="line-height:2;">报</span><br /></span></strong>
				<strong><span style="font-family:Microsoft YaHei;"><span style="line-height:2;">告</span></strong> 
			</div>
			<p style="text-align:center;">
				<br />
			</p>
			<h2 style="text-align:center;">
				<strong><span style="font-family:Microsoft YaHei;"><span style="line-height:2;">教务处制</span><br />
			</span></strong><strong><span style="font-family:" line-height:2;"="">${month }</span></strong> 
			</h2>
			<p style="text-align:center;">
				<br />
				<br />
				<br />
			</p>
			<p>
				<br />
			</p>
			<h2 style="text-align:center;">
				<strong><span style="font-family:Microsoft YaHei;"><span style="line-height:2;">课程质量分析表（理论类）</span>
			</h2>
			<h3 style="text-align:center;">
				<strong>
					<span style="font-family:Microsoft YaHei;line-height:2;">
						课程名称：${lessonname }&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;课程教师：${teacher.name }<br/>
						&nbsp;&nbsp;评价日期：${day }
					</span>
				</strong>
			</h3>
			