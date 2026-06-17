package com.mywork.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mywork.util.ImageUtil;

@Controller
@RequestMapping(value="/kindeditor")
public class KindEditorController extends BaseController{

	@SuppressWarnings("unchecked")
	@ResponseBody
    @RequestMapping(value ="/fileUpload")
    public void fileUpload(HttpServletRequest request,HttpServletResponse response) 
    	throws ServletException, IOException, FileUploadException {
		
		//这里的路径可以是绝对路径，也可以获取项目下的目录，这里使用的是绝对路径;  
        //String savePath = request.getServletContext().getRealPath("/")+"yourPath/" ;
//        String savePath = "runtime/upload/";
        String savePath = request.getSession().getServletContext().getRealPath("/") + "kindeditor/";
        //这里的路径需要符合下面另一个RequestMapping方法
        String saveUrl = request.getContextPath() + "/kindeditor/";
        // 定义允许上传的文件扩展名
        HashMap<String, String> extMap = new HashMap<String, String>();
        extMap.put("image", "gif,jpg,jpeg,png,bmp");
        extMap.put("flash", "swf,flv");
        extMap.put("media", "swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb");
        extMap.put("file", "doc,docx,xls,xlsx,ppt,htm,html,txt,zip,rar,gz,bz2");
//        // 最大文件大小
//        long maxSize = 1000000;

        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (!ServletFileUpload.isMultipartContent(request)) {
            System.err.println("请选择文件。");
        }
        String dirName = request.getParameter("dir");
        if (dirName == null) {
            dirName = "image";
        }
        if (!extMap.containsKey(dirName)) {
        	System.err.println("目录名不正确。");
        }
        // 创建文件夹
        savePath += dirName + "/";
        saveUrl += dirName + "/";
        File saveDirFile = new File(savePath);
        if (!saveDirFile.exists()) {
            saveDirFile.mkdirs();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String ymd = sdf.format(new Date());
        savePath += ymd + "/";
        saveUrl += ymd + "/";
        File dirFile = new File(savePath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        multipartRequest.setCharacterEncoding("UTF-8");
        MultipartFile qqfile=multipartRequest.getFile("uploadFile");
        String oldName=qqfile.getOriginalFilename();
        String ext=qqfile.getOriginalFilename().substring(oldName.lastIndexOf(".") + 1);
        if (!Arrays.<String>asList(extMap.get(dirName).split(",")).contains(ext)) {

            System.err.println("<font size='3'>非常抱歉，目前上传附件格式类型只允许为：<br/>" + extMap.get(dirName)
                    + "，你选择的文件【" + oldName + "】不符合要求，无法上传！</font>");
            return ;
        }
		//获得文件名
		String fileName = null;
		String pic0name=qqfile.getOriginalFilename();
		if(!pic0name.equals("")){
			int address=pic0name.lastIndexOf(".");
			String pic0type = pic0name.substring(address+1,pic0name.length());
			String date=new SimpleDateFormat("yyyyMMddHHmmssSS").format(Calendar.getInstance().getTime());
			fileName=date+"."+pic0type;
			String pic0ChangePath=savePath+fileName;//实际保存路径
			ImageUtil.uploadfile(qqfile, pic0ChangePath, fileName);
		}
		JSONObject obj = new JSONObject();
		obj.put("error", 0);
		obj.put("url", saveUrl+fileName);
		ajax(response, obj.toString());
        
        
        
	}
	public String calculateFileSize(long size){
        //字节数少于1024，直接以B为单位
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            size = size / 1024;
        }
        //字节数除于1024之后，少于1024，则可直接以KB作为单位
        if (size < 1024) {
            return String.valueOf(size) + "KB";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            //以MB为单位的话，保留最后1位小数
            size = size * 100;
            return String.valueOf((size / 100)) + "."
                    + String.valueOf((size % 100)) + "MB";
        } else {
            //以GB为单位
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "."
                    + String.valueOf((size % 100)) + "GB";
        }

    }

}
