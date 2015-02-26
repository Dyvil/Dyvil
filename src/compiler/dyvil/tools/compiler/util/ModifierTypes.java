package dyvil.tools.compiler.util;

import dyvil.reflect.Modifiers;

public enum ModifierTypes
{
	ACCESS, CLASS_TYPE, CLASS, MEMBER, FIELD_OR_METHOD, FIELD, METHOD, PARAMETER;
	
	public String toString(int mod)
	{
		StringBuilder sb = new StringBuilder();
		switch (this)
		{
		case ACCESS:
			writeAccessModifiers(mod, sb);
			break;
		case CLASS_TYPE:
			writeClassTypeModifiers(mod, sb);
			break;
		case CLASS:
			writeAccessModifiers(mod, sb);
			writeClassModifiers(mod, sb);
			break;
		case MEMBER:
		case FIELD_OR_METHOD:
			// Do not write any Modifiers as this should never be called
			break;
		case FIELD:
			writeAccessModifiers(mod, sb);
			writeFieldModifiers(mod, sb);
			break;
		case METHOD:
			writeAccessModifiers(mod, sb);
			writeMethodModifiers(mod, sb);
			break;
		case PARAMETER:
			writeParameterModifier(mod, sb);
			break;
		}
		return sb.toString();
	}
	
	public int parse(String mod)
	{
		int m = 0;
		switch (this)
		{
		case ACCESS:
			m = readAccessModifier(mod);
			break;
		case CLASS_TYPE:
			m = readClassTypeModifier(mod);
			break;
		case CLASS:
			if ((m = readAccessModifier(mod)) == -1)
			{
				m = readClassModifier(mod);
			}
			break;
		case MEMBER:
			if ((m = readAccessModifier(mod)) == -1)
			{
				if ((m = readClassModifier(mod)) == -1)
				{
					if ((m = readFieldModifier(mod)) == -1)
					{
						m = readMethodModifier(mod);
					}
				}
			}
			break;
		case FIELD_OR_METHOD:
			if ((m = readAccessModifier(mod)) == -1)
			{
				if ((m = readFieldModifier(mod)) == -1)
				{
					m = readMethodModifier(mod);
				}
			}
			break;
		case FIELD:
			if ((m = readAccessModifier(mod)) == -1)
			{
				m = readFieldModifier(mod);
			}
			break;
		case METHOD:
			if ((m = readAccessModifier(mod)) == -1)
			{
				m = readMethodModifier(mod);
			}
			break;
		case PARAMETER:
			if ((m = readAccessModifier(mod)) == -1)
			{
				m = readParameterModifier(mod);
			}
			break;
		}
		return m;
	}
	
	private static void writeAccessModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.PUBLIC) == Modifiers.PUBLIC)
		{
			sb.append("public ");
		}
		
		if ((mod & Modifiers.DERIVED) == Modifiers.DERIVED)
		{
			sb.append("derived ");
		}
		else
		{
			if ((mod & Modifiers.PROTECTED) == Modifiers.PROTECTED)
			{
				sb.append("protected ");
			}
			if ((mod & Modifiers.PRIVATE) == Modifiers.PRIVATE)
			{
				sb.append("private ");
			}
		}
		
		if ((mod & Modifiers.DEPRECATED) == Modifiers.DEPRECATED)
		{
			sb.append("deprecated ");
		}
		if ((mod & Modifiers.SEALED) == Modifiers.SEALED)
		{
			sb.append("sealed ");
		}
	}
	
	private static void writeClassTypeModifiers(int mod, StringBuilder sb)
	{
		if (mod == 0)
		{
			sb.append("class ");
		}
		else if ((mod & Modifiers.INTERFACE_CLASS) == Modifiers.INTERFACE_CLASS)
		{
			sb.append("interface ");
		}
		else if ((mod & Modifiers.ANNOTATION) == Modifiers.ANNOTATION)
		{
			sb.append("annotation ");
		}
		else if ((mod & Modifiers.ENUM) == Modifiers.ENUM)
		{
			sb.append("enum ");
		}
		else if ((mod & Modifiers.OBJECT_CLASS) == Modifiers.OBJECT_CLASS)
		{
			sb.append("object ");
		}
		else
		{
			sb.append("class ");
		}
	}
	
	private static void writeClassModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.STATIC) == Modifiers.STATIC)
		{
			sb.append("static ");
		}
		if ((mod & Modifiers.ABSTRACT) == Modifiers.ABSTRACT)
		{
			sb.append("abstract ");
		}
		if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
		{
			sb.append("final ");
		}
		if ((mod & Modifiers.STRICT) == Modifiers.STRICT)
		{
			sb.append("strictfp ");
		}
		if ((mod & Modifiers.FUNCTIONAL) == Modifiers.FUNCTIONAL)
		{
			sb.append("functional ");
		}
		if ((mod & Modifiers.CASE_CLASS) == Modifiers.CASE_CLASS)
		{
			sb.append("case ");
		}
	}
	
	private static void writeFieldModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.LAZY) == Modifiers.LAZY)
		{
			sb.append("lazy ");
		}
		else if ((mod & Modifiers.CONST) == Modifiers.CONST)
		{
			sb.append("const ");
		}
		else
		{
			if ((mod & Modifiers.STATIC) == Modifiers.STATIC)
			{
				sb.append("static ");
			}
			if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
			{
				sb.append("final ");
			}
		}
		
		if ((mod & Modifiers.TRANSIENT) == Modifiers.TRANSIENT)
		{
			sb.append("transient ");
		}
		if ((mod & Modifiers.VOLATILE) == Modifiers.VOLATILE)
		{
			sb.append("volatile ");
		}
	}
	
	private static void writeMethodModifiers(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.INFIX) == Modifiers.INFIX)
		{
			sb.append("infix ");
		}
		else if ((mod & Modifiers.STATIC) == Modifiers.STATIC)
		{
			sb.append("static ");
		}
		if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
		{
			sb.append("final ");
		}
		if ((mod & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			sb.append("prefix ");
		}
		
		if ((mod & Modifiers.SYNCHRONIZED) == Modifiers.SYNCHRONIZED)
		{
			sb.append("synchronized ");
		}
		if ((mod & Modifiers.NATIVE) == Modifiers.NATIVE)
		{
			sb.append("native ");
		}
		if ((mod & Modifiers.ABSTRACT) == Modifiers.ABSTRACT)
		{
			sb.append("abstract ");
		}
		if ((mod & Modifiers.STRICT) == Modifiers.STRICT)
		{
			sb.append("strictfp ");
		}
		if ((mod & Modifiers.INLINE) == Modifiers.INLINE)
		{
			sb.append("inline ");
		}
		if ((mod & Modifiers.OVERRIDE) == Modifiers.OVERRIDE)
		{
			sb.append("override ");
		}
	}
	
	private static void writeParameterModifier(int mod, StringBuilder sb)
	{
		if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
		{
			sb.append("final ");
		}
		if ((mod & Modifiers.BYREF) == Modifiers.BYREF)
		{
			sb.append("byref ");
		}
	}
	
	private static int readAccessModifier(String mod)
	{
		switch (mod)
		{
		case "package":
			return Modifiers.PACKAGE;
		case "public":
			return Modifiers.PUBLIC;
		case "private":
			return Modifiers.PRIVATE;
		case "protected":
			return Modifiers.PROTECTED;
		case "derived":
			return Modifiers.DERIVED;
		case "sealed":
			return Modifiers.SEALED;
		case "deprecated":
			return Modifiers.DEPRECATED;
		}
		return -1;
	}
	
	private static int readClassTypeModifier(String mod)
	{
		switch (mod)
		{
		case "class":
			return 0;
		case "interface":
			return Modifiers.INTERFACE_CLASS;
		case "annotation":
			return Modifiers.ANNOTATION;
		case "enum":
			return Modifiers.ENUM;
		case "object":
			return Modifiers.OBJECT_CLASS;
		}
		return -1;
	}
	
	private static int readClassModifier(String mod)
	{
		switch (mod)
		{
		case "static":
			return Modifiers.STATIC;
		case "abstract":
			return Modifiers.ABSTRACT;
		case "final":
			return Modifiers.FINAL;
		case "strictfp":
			return Modifiers.STRICT;
		case "functional":
			return Modifiers.FUNCTIONAL;
		case "case":
			return Modifiers.CASE_CLASS;
		}
		return -1;
	}
	
	private static int readFieldModifier(String mod)
	{
		switch (mod)
		{
		case "static":
			return Modifiers.STATIC;
		case "final":
			return Modifiers.FINAL;
		case "const":
			return Modifiers.CONST;
		case "transient":
			return Modifiers.TRANSIENT;
		case "volatile":
			return Modifiers.VOLATILE;
		case "lazy":
			return Modifiers.LAZY;
		}
		return -1;
	}
	
	private static int readMethodModifier(String mod)
	{
		switch (mod)
		{
		case "static":
			return Modifiers.STATIC;
		case "final":
			return Modifiers.FINAL;
		case "const":
			return Modifiers.CONST;
		case "synchronized":
			return Modifiers.SYNCHRONIZED;
		case "native":
			return Modifiers.NATIVE;
		case "abstract":
			return Modifiers.ABSTRACT;
		case "strictfp":
			return Modifiers.STRICT;
		case "inline":
			return Modifiers.INLINE;
		case "infix":
			return Modifiers.INFIX;
		case "override":
			return Modifiers.OVERRIDE;
		}
		return -1;
	}
	
	private static int readParameterModifier(String mod)
	{
		switch (mod)
		{
		case "final":
			return Modifiers.FINAL;
		case "byref":
			return Modifiers.BYREF;
		}
		return -1;
	}
}
