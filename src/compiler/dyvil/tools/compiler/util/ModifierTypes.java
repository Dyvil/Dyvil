package dyvil.tools.compiler.util;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.marker.MarkerList;

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
		if ((mod & Modifiers.INTERNAL) == Modifiers.INTERNAL)
		{
			sb.append("internal ");
		}
	}
	
	private static void writeClassTypeModifiers(int mod, StringBuilder sb)
	{
		if (mod == 0)
		{
			sb.append("class ");
		}
		else if ((mod & Modifiers.ANNOTATION) == Modifiers.ANNOTATION)
		{
			sb.append("@interface ");
		}
		else if ((mod & Modifiers.INTERFACE_CLASS) == Modifiers.INTERFACE_CLASS)
		{
			sb.append("interface ");
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
		if ((mod & Modifiers.SEALED) == Modifiers.SEALED)
		{
			sb.append("sealed ");
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
		if ((mod & Modifiers.EXTENSION) == Modifiers.EXTENSION)
		{
			sb.append("extension ");
		}
		else if ((mod & Modifiers.INFIX) != 0 && (mod & Modifiers.INFIX) != Modifiers.STATIC)
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
		if ((mod & Modifiers.SEALED) == Modifiers.SEALED)
		{
			sb.append("sealed ");
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
		case DyvilKeywords.PACKAGE:
			return Modifiers.PACKAGE;
		case DyvilKeywords.PUBLIC:
			return Modifiers.PUBLIC;
		case DyvilKeywords.PRIVATE:
			return Modifiers.PRIVATE;
		case DyvilKeywords.PROTECTED:
			return Modifiers.PROTECTED;
		case DyvilKeywords.INTERNAL:
			return Modifiers.INTERNAL;
		}
		return -1;
	}
	
	private static int readClassTypeModifier(int mod)
	{
		switch (mod)
		{
		case DyvilKeywords.CLASS:
			return 0;
		case DyvilKeywords.INTERFACE:
			return Modifiers.INTERFACE_CLASS;
		case DyvilKeywords.ENUM:
			return Modifiers.ENUM;
		case DyvilKeywords.OBJECT:
			return Modifiers.OBJECT_CLASS;
		}
		return -1;
	}
	
	private static int readClassModifier(int mod)
	{
		switch (mod)
		{
		case DyvilKeywords.STATIC:
			return Modifiers.STATIC;
		case DyvilKeywords.ABSTRACT:
			return Modifiers.ABSTRACT;
		case DyvilKeywords.FINAL:
			return Modifiers.FINAL;
		case DyvilKeywords.FUNCTIONAL:
			return Modifiers.FUNCTIONAL;
		case DyvilKeywords.CASE:
			return Modifiers.CASE_CLASS;
		}
		return -1;
	}
	
	private static int readFieldModifier(int mod)
	{
		switch (mod)
		{
		case DyvilKeywords.STATIC:
			return Modifiers.STATIC;
		case DyvilKeywords.FINAL:
			return Modifiers.FINAL;
		case DyvilKeywords.CONST:
			return Modifiers.CONST;
		case DyvilKeywords.LAZY:
			return Modifiers.LAZY;
		}
		return -1;
	}
	
	private static int readMethodModifier(int mod)
	{
		switch (mod)
		{
		case DyvilKeywords.STATIC:
			return Modifiers.STATIC;
		case DyvilKeywords.FINAL:
			return Modifiers.FINAL;
		case DyvilKeywords.CONST:
			return Modifiers.CONST;
		case DyvilKeywords.SYNCHRONIZED:
			return Modifiers.SYNCHRONIZED;
		case DyvilKeywords.ABSTRACT:
			return Modifiers.ABSTRACT;
		case DyvilKeywords.INLINE:
			return Modifiers.INLINE;
		case DyvilKeywords.INFIX:
			return Modifiers.INFIX;
		case DyvilKeywords.EXTENSION:
			return Modifiers.EXTENSION;
		case DyvilKeywords.POSTFIX:
			return Modifiers.INFIX;
		case DyvilKeywords.PREFIX:
			return Modifiers.PREFIX;
		case DyvilKeywords.OVERRIDE:
			return Modifiers.OVERRIDE;
		}
		return -1;
	}
	
	private static int readParameterModifier(int mod)
	{
		switch (mod)
		{
		case DyvilKeywords.FINAL:
			return Modifiers.FINAL;
		case DyvilKeywords.VAR:
			return Modifiers.VAR;
		}
		return -1;
	}
	
	public static void checkMethodModifiers(MarkerList markers, IClassMember member, int modifiers, boolean hasValue, String type)
	{
		boolean isStatic = (modifiers & Modifiers.STATIC) != 0;
		boolean isAbstract = (modifiers & Modifiers.ABSTRACT) != 0;
		boolean isNative = (modifiers & Modifiers.NATIVE) != 0;
		
		// If the method does not have an implementation and is static
		if (isStatic && isAbstract)
		{
			markers.add(I18n.createError(member.getPosition(), "modifiers.static.abstract", I18n.getString(type, member.getName())));
		}
		else if (isAbstract && isNative)
		{
			markers.add(I18n.createError(member.getPosition(), "modifiers.native.abstract", I18n.getString(type, member.getName())));
		}
		else
		{
			if (isStatic)
			{
				if (!hasValue)
				{
					markers.add(I18n.createError(member.getPosition(), "modifiers.static.unimplemented", I18n.getString(type, member.getName())));
				}
			}
			if (isNative)
			{
				if (!hasValue)
				{
					markers.add(I18n.createError(member.getPosition(), "modifiers.native.implemented", I18n.getString(type, member.getName())));
				}
			}
			if (isAbstract)
			{
				IClass theClass = member.getTheClass();
				if (!theClass.isAbstract())
				{
					markers.add(I18n.createError(member.getPosition(), "modifiers.abstract.concrete_class", I18n.getString(type, member.getName(), theClass.getName())));
				}
				if (hasValue)
				{
					markers.add(I18n.createError(member.getPosition(), "modifiers.abstract.implemented", I18n.getString(type, member.getName())));
				}
			}
		}
		if (!hasValue && !isAbstract && !isNative)
		{
			markers.add(I18n.createError(member.getPosition(), "modifiers.unimplemented", I18n.getString(type, member.getName())));
		}
	}
}
