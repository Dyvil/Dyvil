package dyvil.tools.compiler.config;

public class Formatting
{
	public static class Package
	{
		public static boolean	newLine	= true;
	}
	
	public static class Import
	{
		public static String	packageImportEnd		= "._";
		
		public static String	multiImportSeperator	= ", ";
		public static String	multiImportStart		= ".{";
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
		public static String	keyValueSeperator		= " = ";
		
		public static boolean	convertQualifiedNames	= false;
		public static boolean	useJavaFormat			= true;
		public static String	dotlessSeperator		= " ";
		
		public static String	propertyGet				= "get: ";
		public static String	propertySet				= "set: ";
	}
	
	public static class Method
	{
		public static String	emptyParameters				= "()";
		public static String	parametersStart				= "(";
		public static String	parametersEnd				= ")";
		public static String	parameterSeperator			= ", ";
		
		public static boolean	convertQualifiedNames		= false;
		public static boolean	useJavaFormat				= true;
		public static String	dotlessSeperator			= " ";
		public static String	sugarCallSeperator			= " ";
		
		public static String	throwsSeperator				= ", ";
		public static String	signatureThrowsSeperator	= " throws ";
		public static String	signatureBodySeperator		= " = ";
		public static String	indent						= "\t";
	}
	
	public static class Statements
	{
		public static String	ifStart		= "if (";
		public static String	ifEnd		= ") ";
		public static String	ifElse		= " else ";
		
		public static String	whileStart	= "while (";
		public static String	whileEnd	= ") ";
	}
	
	public static class Expression
	{
		public static String	emptyExpression	= "{ }";
		
		public static boolean	convertTuples	= true;
		public static String	emptyTuple		= "()";
		public static String	tupleStart		= "(";
		public static String	tupleEnd		= ")";
		public static String	tupleSeperator	= ", ";
		
		public static String	emptyArray		= "{ }";
		public static String	arrayStart		= "{ ";
		public static String	arrayEnd		= " }";
		public static String	arraySeperator	= ", ";
		
		public static String	lambdaSeperator	= " => ";
	}
}
