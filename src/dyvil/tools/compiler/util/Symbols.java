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
			
		case "->":
			return "$arr";
		case "<-":
			return "$arl";
		case "=>":
			return "$cst";
		case ":>":
			return "$iof";
			
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
		case "set":
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
			
		case "$arr":
			return "->";
		case "$arl":
			return "<-";
		case "$cst":
			return "=>";
		case "$iof":
			return ":>";
			
		case "$add":
		case "add":
			return "+";
		case "$sub":
		case "substract":
		case "remove":
			return "-";
		case "$mul":
		case "multiply":
			return "*";
		case "$div":
		case "divide":
			return "/";
		case "$mod":
		case "modulo":
			return "%";
			
		case "$and":
		case "and":
			return "&";
		case "$or":
		case "or":
			return "|";
		case "$xor":
		case "xor":
			return "^";
			
		case "$bsl":
			return "<<";
		case "$bsr":
			return ">>";
		case "$usr":
			return ">>>";
			
		case "$neg":
		case "negate":
			return "-";
		case "$inv":
		case "invert":
			return "~";
		case "$inc":
		case "increment":
			return "++";
		case "$dec":
		case "decrement":
			return "--";
		case "$sqr":
		case "square":
			return "**";
			
			// case "$rev": return "//";
		}
		return s;
	}
}
