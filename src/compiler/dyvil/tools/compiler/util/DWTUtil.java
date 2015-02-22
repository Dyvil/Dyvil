package dyvil.tools.compiler.util;

import dyvil.util.CharUtils;

public class DWTUtil
{
	public static String getAddMethodName(String s)
	{
		StringBuilder builder = new StringBuilder("add");
		int len = s.length() - 1;
		builder.append(CharUtils.toUpperCase(s.charAt(0)));
		for (int i = 1; i < len; i++)
		{
			builder.append(s.charAt(i));
		}
		return builder.toString();
	}
	
	public static String getSetMethodName(String s)
	{
		StringBuilder builder = new StringBuilder("set");
		int len = s.length();
		builder.append(CharUtils.toUpperCase(s.charAt(0)));
		for (int i = 1; i < len; i++)
		{
			builder.append(s.charAt(i));
		}
		return builder.toString();
	}
}
