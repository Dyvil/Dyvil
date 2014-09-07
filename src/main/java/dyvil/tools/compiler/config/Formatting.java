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
		public static String	bodyEnd					= "\n}\n";
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
		public static String	parametersStart	= "(";
		public static String	parametersEnd	= ")";
	}
}
