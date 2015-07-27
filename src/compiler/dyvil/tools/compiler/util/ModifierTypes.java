package dyvil.tools.compiler.util;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.transform.Keywords;

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
	
	public int parse(int mod)
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
		
		switch (mod & Modifiers.DERIVED)
		{
		case Modifiers.PACKAGE:
			break;
		case Modifiers.PROTECTED:
			sb.append("protected ");
			break;
		case Modifiers.PRIVATE:
			sb.append("private ");
			break;
		case Modifiers.DERIVED:
			sb.append("private protected ");
			break;
		}
		
		if ((mod & Modifiers.DEPRECATED) == Modifiers.DEPRECATED)
		{
			sb.append("@Deprecated ");
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
			sb.append("@Strict ");
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
			sb.append("@Transient ");
		}
		if ((mod & Modifiers.VOLATILE) == Modifiers.VOLATILE)
		{
			sb.append("@Volatile ");
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
			sb.append("@Native ");
		}
		if ((mod & Modifiers.ABSTRACT) == Modifiers.ABSTRACT)
		{
			sb.append("abstract ");
		}
		if ((mod & Modifiers.STRICT) == Modifiers.STRICT)
		{
			sb.append("@Strict ");
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
		if ((mod & Modifiers.LAZY) == Modifiers.LAZY)
		{
			sb.append("lazy ");
		}
		if ((mod & Modifiers.FINAL) == Modifiers.FINAL)
		{
			sb.append("final ");
		}
		if ((mod & Modifiers.VAR) == Modifiers.VAR)
		{
			sb.append("var ");
		}
	}
	
	private static int readAccessModifier(int mod)
	{
		switch (mod)
		{
		case Keywords.PACKAGE:
			return Modifiers.PACKAGE;
		case Keywords.PUBLIC:
			return Modifiers.PUBLIC;
		case Keywords.PRIVATE:
			return Modifiers.PRIVATE;
		case Keywords.PROTECTED:
			return Modifiers.PROTECTED;
		case Keywords.SEALED:
			return Modifiers.SEALED;
		}
		return -1;
	}
	
	private static int readClassTypeModifier(int mod)
	{
		switch (mod)
		{
		case Keywords.CLASS:
			return 0;
		case Keywords.INTERFACE:
			return Modifiers.INTERFACE_CLASS;
		case Keywords.ANNOTATION:
			return Modifiers.ANNOTATION;
		case Keywords.ENUM:
			return Modifiers.ENUM;
		case Keywords.OBJECT:
			return Modifiers.OBJECT_CLASS;
		}
		return -1;
	}
	
	private static int readClassModifier(int mod)
	{
		switch (mod)
		{
		case Keywords.STATIC:
			return Modifiers.STATIC;
		case Keywords.ABSTRACT:
			return Modifiers.ABSTRACT;
		case Keywords.FINAL:
			return Modifiers.FINAL;
		case Keywords.FUNCTIONAL:
			return Modifiers.FUNCTIONAL;
		case Keywords.CASE:
			return Modifiers.CASE_CLASS;
		}
		return -1;
	}
	
	private static int readFieldModifier(int mod)
	{
		switch (mod)
		{
		case Keywords.STATIC:
			return Modifiers.STATIC;
		case Keywords.FINAL:
			return Modifiers.FINAL;
		case Keywords.CONST:
			return Modifiers.CONST;
		case Keywords.LAZY:
			return Modifiers.LAZY;
		}
		return -1;
	}
	
	private static int readMethodModifier(int mod)
	{
		switch (mod)
		{
		case Keywords.STATIC:
			return Modifiers.STATIC;
		case Keywords.FINAL:
			return Modifiers.FINAL;
		case Keywords.CONST:
			return Modifiers.CONST;
		case Keywords.SYNCHRONIZED:
			return Modifiers.SYNCHRONIZED;
		case Keywords.ABSTRACT:
			return Modifiers.ABSTRACT;
		case Keywords.INLINE:
			return Modifiers.INLINE;
		case Keywords.INFIX:
			return Modifiers.INFIX;
		case Keywords.POSTFIX:
			return Modifiers.INFIX;
		case Keywords.PREFIX:
			return Modifiers.PREFIX;
		case Keywords.OVERRIDE:
			return Modifiers.OVERRIDE;
		}
		return -1;
	}
	
	private static int readParameterModifier(int mod)
	{
		switch (mod)
		{
		case Keywords.FINAL:
			return Modifiers.FINAL;
		case Keywords.VAR:
			return Modifiers.VAR;
		}
		return -1;
	}
}
