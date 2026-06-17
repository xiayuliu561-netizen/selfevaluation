package com.mywork.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.mywork.util.SysModel;
/**
 * 下载服务器文件 controller
 * @author gaozq
 *
 */
@Controller 
public class DownLoadController {

	@RequestMapping(value="/download")
	public String download(HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException{
		Log log=LogFactory.getLog(DownLoadController.class);
		String filePath=request.getParameter("filePath");
		//"..";
		if(filePath == null || filePath.trim().equals("") || filePath.indexOf("..")!=-1
				|| filePath.indexOf("/")!=-1 || filePath.indexOf("\\")!=-1 || new File(filePath).isAbsolute()){
			log.debug("download failed ....");
			throw new ServletException("请进行正确操作");
		}
		File f=getDownloadFile(request, filePath);
		if(f==null || !f.exists()){
			//Log.error(path+ " file not exists . ");
			return null;
		}
		FileInputStream in=new FileInputStream(f);
		String fileName=f.getName();
		String encodedFileName=URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-disposition","attachment;filename=\""+new String(fileName.getBytes("UTF-8"), "ISO-8859-1")+"\";filename*=UTF-8''"+encodedFileName);
		response.setContentLength((int) f.length());
		ServletOutputStream out=response.getOutputStream();
		byte[] b=new byte[1024];
		int i=0;
		while((i=in.read(b))>0){
			out.write(b, 0, i);
		}
		in.close();
		out.close();
		return null;
	}

	private File getDownloadFile(HttpServletRequest request, String filePath){
		String resolvedFilePath=getTemplateAlias(filePath);
		File uploadFile=new File(SysModel.get("uploadRoot").toString()+resolvedFilePath);
		if(uploadFile.exists()){
			return uploadFile;
		}
		String webRoot=request.getSession().getServletContext().getRealPath("/");
		if(webRoot != null){
			File webRootFile=new File(webRoot);
			File webFile=new File(webRootFile, resolvedFilePath);
			if(webFile.exists()){
				return webFile;
			}
			File webTemplateFile=new File(new File(webRootFile, "templates"), resolvedFilePath);
			if(webTemplateFile.exists()){
				return webTemplateFile;
			}
			File projectRoot=webRootFile.getParentFile();
			if(projectRoot != null){
				File projectFile=new File(projectRoot, resolvedFilePath);
				if(projectFile.exists()){
					return projectFile;
				}
				File projectTemplateFile=new File(new File(projectRoot, "模板"), resolvedFilePath);
				if(projectTemplateFile.exists()){
					return projectTemplateFile;
				}
			}
		}
		return uploadFile;
	}

	private String getTemplateAlias(String filePath){
		if("stutemlpate.xls".equals(filePath) || "studenttemplate.xls".equals(filePath)){
			return "学生导入模板.xls";
		}
		if("scoretemlpate.xls".equals(filePath) || "scoretemplate.xls".equals(filePath)){
			return "成绩导入模板.xls";
		}
		return filePath;
	}
}
