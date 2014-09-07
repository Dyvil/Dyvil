package dyvil.tools.compiler.util;

public class Symbols
{
	public static String expand(String s)
	{
		switch (s)
		{
		case "=": return "$eq";
		case ">": return "$greater";
		case "<": return "$less";
		case "+": return "$plus";
		case "-": return "$minus";
		case "*": return "$times";
		case "/": return "$div";
		case "!": return "$bang";
		case "@": return "$at";
		case "#": return "$hash";
		case "%": return "$percent";
		case "^": return "$up";
		case "&": return "$amp";
		case "~": return "$tilde";
		case "?": return "$qmark";
		case "|": return "$bar";
		case "\\": return "$bslash";
		case ":": return "$colon";
		}
		return s;
	}
	
	public static String contract(String s)
	{
		switch (s)
		{
		case "$eq": return "=";
		case "$greater": return ">";
		case "$less": return "<";
		case "$plus": return "+";
		case "$minus": return "-";
		case "$times": return "*";
		case "$div": return "/";
		case "$bang": return "!";
		case "$at": return "@";
		case "$hash": return "#";
		case "$percent": return "%";
		case "$up": return "^";
		case "$amp": return "&";
		case "$tilde": return "~";
		case "$qmark": return "?";
		case "$bar": return "|";
		case "$bslash": return "\\";
		case "$colon": return ":";
		}
		return s;
	}
}
