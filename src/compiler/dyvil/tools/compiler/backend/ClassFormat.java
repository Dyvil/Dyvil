package dyvil.tools.compiler.backend;

import java.io.File;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.ast.generic.TypeVariable;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.transform.Symbols;

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
		
		File bin = new File("bin");
		if (bin.exists())
		{
			dyvilRTJar = bin;
		}
		else
		{
			s = System.getenv("DYVIL_HOME");
			if (s == null || s.isEmpty())
			{
				throw new UnsatisfiedLinkError("No installed Dyvil Runtime Library found!");
			}
			else
			{
				dyvilRTJar = new File(s);
			}
		}
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
		int i = internal.indexOf('<');
		if (i != -1)
		{
			GenericType type = new GenericType();
			setInternalName(type, internal.substring(1, i));
			int index = getMatchingBracket(internal, i, internal.length());
			readTypeList(internal, i + 1, index, type);
			return type;
		}
		
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
		else if (c == 'T')
		{
			int l = len - 1;
			if (internal.charAt(l) == ';')
			{
				internal = internal.substring(i + 1, l);
			}
			type.setName(Symbols.unqualify(internal), internal);
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
	
	public static <T extends ITypeList & ITyped & IGeneric> void readMethodType(String internal, T method)
	{
		int index = internal.indexOf(')');
		
		int i = 1;
		int len = internal.length();
		
		if (internal.charAt(0) == '<')
		{
			int j = getMatchingBracket(internal, 0, index);
			readTypeArguments(internal, 1, j, method);
			i = j + 2;
		}
		
		readTypeList(internal, i, index, method);
		
		IType t = internalToType(internal.substring(index + 1));
		method.setType(t);
	}
	
	protected static void readTypeList(String internal, int start, int end, ITypeList list)
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
				int end1 = getMatchingSemicolon(internal, i, end);
				IType type = internalToType2(internal.substring(i + 1, end1));
				
				type.setArrayDimensions(array);
				array = 0;
				list.addType(type);
				i = end1;
			}
			else if (c == 'T')
			{
				int end1 = internal.indexOf(';', i);
				IType type = new Type(null, internal.substring(i + 1, end1));
				
				type.setArrayDimensions(array);
				array = 0;
				list.addType(type);
				i = end1;
			}
			else if (c == '*')
			{
				list.addType(new TypeVariable());
			}
			else if (c == '+' || c == '-')
			{
				int end1 = getMatchingSemicolon(internal, i, end);
				String name = internal.substring(i + 1, end1 + 1);
				IType type = internalToType(name);
				TypeVariable var = new TypeVariable();
				if (c == '-')
				{
					var.setLowerBound(type);
				}
				else
				{
					var.addUpperBound(type);
				}
				list.addType(var);
				i = end1;
			}
			else if (array == 0)
			{
				list.addType(parseBaseType(c));
			}
			else
			{
				IType type = parseBaseType(c).clone();
				type.setArrayDimensions(array);
				array = 0;
				list.addType(type);
			}
		}
	}
	
	protected static void readTypeArguments(String signature, int start, int end, IGeneric generic)
	{
		int array = 0;
		int mode = 0;
		TypeVariable var = null;
		for (int i = start; i < end; i++)
		{
			if (mode == 0)
			{
				if (signature.charAt(i) == '>')
				{
					return;
				}
				
				int index = signature.indexOf(':', i);
				String name = signature.substring(i, index);
				var = new TypeVariable(name);
				generic.addTypeVariable(var);
				mode = 1;
			}
			else if (mode == 1)
			{
				char c = signature.charAt(i);
				if (c == ':')
				{
					char c1 = signature.charAt(i + 1);
					if (c1 == '[')
					{
						i++;
						array++;
						continue;
					}
					
					if (c1 == 'L')
					{
						int end1 = getMatchingSemicolon(signature, i + 1, end);
						IType type = internalToType2(signature.substring(i + 2, end1));
						
						type.setArrayDimensions(array);
						var.setUpperBound(type);
						array = 0;
						mode = 2;
						i = end1;
					}
					else if (c1 == 'T')
					{
						int end1 = signature.indexOf(';', i);
						IType type = new Type(null, signature.substring(i + 2, end1));
						
						type.setArrayDimensions(array);
						var.setUpperBound(type);
						array = 0;
						mode = 2;
						i = end1;
					}
					
					mode = 2;
					continue;
				}
				
				mode = 0;
				i--;
			}
			else if (mode == 2)
			{
				char c = signature.charAt(i);
				if (c == ':')
				{
					char c1 = signature.charAt(i + 1);
					if (c1 == '[')
					{
						i++;
						array++;
						continue;
					}
					
					if (c1 == 'L')
					{
						int end1 = getMatchingSemicolon(signature, i + 1, end);
						IType type = internalToType2(signature.substring(i + 2, end1));
						
						type.setArrayDimensions(array);
						var.addUpperBound(type);
						array = 0;
						mode = 0;
						i = end1;
					}
					else if (c1 == 'T')
					{
						int end1 = signature.indexOf(';', i + 1);
						IType type = new Type(null, signature.substring(i + 2, end1));
						
						type.setArrayDimensions(array);
						var.setUpperBound(type);
						array = 0;
						mode = 2;
						i = end1;
					}
					
					continue;
				}
				
				mode = 0;
				i--;
			}
		}
	}
	
	protected static IType internalToType2(String internal)
	{
		int i = internal.indexOf('<');
		if (i != -1)
		{
			GenericType type = new GenericType();
			setInternalName(type, internal.substring(0, i));
			int index = getMatchingBracket(internal, i, internal.length());
			readTypeList(internal, i + 1, index, type);
			return type;
		}
		
		IType type = new Type();
		setInternalName(type, internal);
		return type;
	}
	
	public static void readClassSignature(String signature, IClass iclass)
	{
		int i = 0;
		int len = signature.length();
		
		if (signature.charAt(0) == '<')
		{
			int index = getMatchingBracket(signature, 0, len);
			readTypeArguments(signature, 1, index, iclass);
			i = index + 1;
		}
		
		boolean superClass = true;
		for (; i < len; i++)
		{
			char c = signature.charAt(i);
			if (c == 'L')
			{
				int end1 = getMatchingSemicolon(signature, i, len);
				String name = signature.substring(i + 1, end1);
				IType type = internalToType2(name);
				i = end1;
				
				if (superClass)
				{
					iclass.setSuperType(type);
					superClass = false;
				}
				else
				{
					iclass.addInterface(type);
				}
			}
		}
	}
	
	private static int getMatchingSemicolon(String s, int start, int end)
	{
		int depth = 0;
		for (int i = start; i < end; i++)
		{
			char c = s.charAt(i);
			if (c == '<')
				depth++;
			else if (c == '>')
				depth--;
			else if (c == ';' && depth == 0)
				return i;
		}
		return -1;
	}
	
	private static int getMatchingBracket(String s, int start, int end)
	{
		int depth = 0;
		for (int i = start; i < end; i++)
		{
			char c = s.charAt(i);
			if (c == '<')
				depth++;
			else if (c == '>')
			{
				depth--;
				if (depth == 0)
				{
					return i;
				}
			}
		}
		return -1;
	}
}
