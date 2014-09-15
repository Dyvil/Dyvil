package dyvil.tools.compiler.config;

public class Formatting
{
	public static class Package
	{
		public static boolean	newLine	= true;
	}
	
	public static class Import
	{
		public static String	multiImportSeperator	= ", ";
		public static String	multiImportStart		= "{";
		public static String	multiImportEnd			= "}";
		
		public static boolean	newLine					= true;
	}
	
	public static class Class
	{
		public static String	superClassesSeperator	= ", ";
		public static String	bodyStart				= "\n{\n";
		public static String	bodyEnd					= "}\n";
		public static String	bodyIndent				= "\t";
		
		public static boolean	newLine					= true;
	}
	
	public static class Type
	{
		public static String	array	= "[]";
	}
	
	public static class Field
	{
		public static String	keyValueSeperator	= " = ";
	}
	
	public static class Method
	{
		public static String	parametersStart			= "(";
		public static String	parametersEnd			= ")";
		public static String	parameterSeperator		= ", ";
		
		public static boolean	convertSugarCalls		= true;
		public static String	sugarCallStart			= " ";
		public static String	sugarCallEnd			= " ";
		
		public static String	signatureBodySeperator	= " = ";
		public static String	indent					= "\t";
	}
	
	public static class Statements
	{
		public static String	ifStart	= "if (";
		public static String	ifEnd	= ") ";
		public static String	ifElse	= " else ";
	}
	
	public static class Expression
	{
		public static String	emptyExpression	= "{ }";
	}
}
