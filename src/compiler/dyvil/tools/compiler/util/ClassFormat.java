package dyvil.tools.compiler.util;

import java.io.File;

import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.IParameterized;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;

public class ClassFormat
{
	public static File				javaRTJar;
	public static File				dyvilRTJar;
	
	private static final String[]	OPCODES	= jdk.internal.org.objectweb.asm.util.Printer.OPCODES;
	
	static
	{
		String s = System.getProperty("sun.boot.class.path");
		int index = s.indexOf("rt.jar");
		if (index != -1)
		{
			int index1 = s.lastIndexOf(':', index);
			int index2 = s.indexOf(':', index + 1);
			String s1 = s.substring(index1 + 1, index2);
			javaRTJar = new File(s1);
		}
		
		// TODO Actually use the installed Dyvil Runtime Library
		dyvilRTJar = new File("bin");
	}
	
	public static String packageToInternal(String name)
	{
		return name.replace('.', '/');
	}
	
	public static String internalToPackage(String name)
	{
		return name.replace('/', '.');
	}
	
	public static String userToInternal(String name)
	{
		switch (name)
		{
		case "boolean":
			return "Z";
		case "byte":
			return "B";
		case "short":
			return "S";
		case "char":
			return "C";
		case "int":
			return "I";
		case "long":
			return "J";
		case "float":
			return "F";
		case "double":
			return "D";
		case "void":
			return "V";
		}
		if (name.length() > 1)
		{
			return "L" + name.replace('.', '/') + ";";
		}
		return name;
	}
	
	public static String opcodeToString(int opcode)
	{
		return OPCODES[opcode];
	}
	
	public static int parseOpcode(String opcode)
	{
		int len = OPCODES.length;
		for (int i = 0; i < len; i++)
		{
			if (opcode.equalsIgnoreCase(OPCODES[i]))
			{
				return i;
			}
		}
		return -1;
	}
	
	private static IType parseBaseType(char c)
	{
		switch (c)
		{
		case 'V':
			return Type.VOID;
		case 'Z':
			return Type.BOOLEAN;
		case 'B':
			return Type.BYTE;
		case 'S':
			return Type.SHORT;
		case 'C':
			return Type.CHAR;
		case 'I':
			return Type.INT;
		case 'J':
			return Type.LONG;
		case 'F':
			return Type.FLOAT;
		case 'D':
			return Type.DOUBLE;
		}
		return null;
	}
	
	public static IType internalToType(String internal)
	{
		return internalToType(internal, new Type());
	}
	
	public static IType internalToType(String internal, IType type)
	{
		int len = internal.length();
		int arrayDimensions = 0;
		int i = 0;
		while (i < len && internal.charAt(i) == '[')
		{
			arrayDimensions++;
			i++;
		}
		
		char c = internal.charAt(i);
		
		if (c == 'L')
		{
			int l = len - 1;
			if (internal.charAt(l) == ';')
			{
				internal = internal.substring(i + 1, l);
			}
			setInternalName(type, internal);
		}
		else if (len - i == 1)
		{
			type = parseBaseType(c);
			if (arrayDimensions > 0)
			{
				type = type.clone();
			}
		}
		else
		{
			setInternalName(type, internal);
		}
		
		type.setArrayDimensions(arrayDimensions);
		return type;
	}
	
	protected static void setInternalName(IType type, String internal)
	{
		int index = internal.lastIndexOf('/');
		if (index == -1)
		{
			type.setName(Symbols.unqualify(internal), internal);
			type.setFullName(internal);
		}
		else
		{
			String name = internal.substring(index + 1);
			type.setName(Symbols.unqualify(name), name);
			type.setFullName(internal.replace('/', '.'));
		}
	}
	
	public static void readMethodType(String internal, IMethod method)
	{
		String methodName = method.getName();
		int index = internal.indexOf(')');
		
		readTypeList(internal, 1, index, method);
		
		IType t = internalToType(internal.substring(index + 1));
		method.setType(t);
	}
	
	protected static void readTypeList(String internal, int start, int end, IParameterized list)
	{
		int array = 0;
		for (int i = start; i < end; i++)
		{
			char c = internal.charAt(i);
			if (c == '[')
			{
				array++;
			}
			else if (c == 'L')
			{
				int end1 = internal.indexOf(';', i);
				
				String s = internal.substring(i + 1, end1);
				IType type = internalToType2(s);
				type.setArrayDimensions(array);
				array = 0;
				list.addParameterType(type);
				i = end1;
			}
			else
			{
				IType type = parseBaseType(c).clone();
				type.setArrayDimensions(array);
				array = 0;
				list.addParameterType(type);
			}
		}
	}
	
	protected static IType internalToType2(String internal)
	{
		IType type = new Type();
		setInternalName(type, internal);
		return type;
	}
}
