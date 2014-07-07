package dyvil.tools.compiler.util;

public class Symbols
{
	public static String expand(String s)
	{
		switch (s)
		{
		case "=":
			return "$set";
			
		case "==":
			return "$eq";
		case "!=":
			return "$ue";
		case "<":
			return "$st";
		case "<=":
			return "$se";
		case ">":
			return "$gt";
		case ">=":
			return "$ge";
			
		case "+":
			return "$add";
		case "-":
			return "$sub";
		case "*":
			return "$mul";
		case "/":
			return "$div";
		case "%":
			return "$mod";
			
		case "&":
			return "$and";
		case "|":
			return "$or";
		case "^":
			return "$xor";
			
		case "<<":
			return "$bsl";
		case ">>":
			return "$bsr";
		case ">>>":
			return "$usr";
			
		case ".-":
			return "$neg";
		case "~":
			return "$inv";
		case "++":
			return "$inc";
		case "--":
			return "$dec";
		case "**":
			return "$sqr";
			// case "//": return "$rec";
		}
		return s;
	}
	
	public static String contract(String s)
	{
		switch (s)
		{
		case "$set":
			return "=";
			
		case "$eq":
			return "==";
		case "$ue":
			return "!=";
		case "$st":
			return "<";
		case "$se":
			return "<=";
		case "$gt":
			return ">";
		case "$ge":
			return ">=";
			
		case "$add":
			return "+";
		case "$sub":
			return "-";
		case "$mul":
			return "*";
		case "$div":
			return "/";
		case "$mod":
			return "%";
			
		case "$and":
			return "&";
		case "$or":
			return "|";
		case "$xor":
			return "^";
			
		case "$bsl":
			return "<<";
		case "$bsr":
			return ">>";
		case "$usr":
			return ">>>";
			
		case "$neg":
			return "-";
		case "$inv":
			return "~";
		case "$inc":
			return "++";
		case "$dec":
			return "--";
		case "$sqr":
			return "**";
			// case "$rev": return "//";
		}
		return s;
	}
}
