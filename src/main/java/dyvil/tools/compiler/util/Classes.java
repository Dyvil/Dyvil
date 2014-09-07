package dyvil.tools.compiler.util;

public class Classes
{
	public static final int	CLASS		= 1;
	public static final int	OBJECT		= 2;
	public static final int	INTERFACE	= 3;
	public static final int	ENUM		= 4;
	public static final int	ANNOTATION	= 5;
	public static final int	MODULE		= 6;
	
	public static String toString(int type)
	{
		switch (type)
		{
		case CLASS:
			return "class";
		case OBJECT:
			return "object";
		case INTERFACE:
			return "interface";
		case ENUM:
			return "enum";
		case ANNOTATION:
			return "annotation";
		case MODULE:
			return "module";
		}
		return "";
	}
	
	public static int parse(String type)
	{
		switch (type)
		{
		case "class":
			return CLASS;
		case "object":
			return OBJECT;
		case "interface":
			return INTERFACE;
		case "enum":
			return ENUM;
		case "annotation":
			return ANNOTATION;
		case "module":
			return MODULE;
		}
		return -1;
	}
}
