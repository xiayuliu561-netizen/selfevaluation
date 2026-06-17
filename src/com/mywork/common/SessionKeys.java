package com.mywork.common;
/**
 * session keys 信息保存
 * 保存session中的所有Key以方便查找取得
 * @author 
 *
 */
public class SessionKeys {
	public static String LOGIN_USER = "loginuser";
	public static String ACTIVE_ROLE = "activeRole";

	public static String loginUserRoleKey(String role){
		return LOGIN_USER + "_" + role;
	}
}
