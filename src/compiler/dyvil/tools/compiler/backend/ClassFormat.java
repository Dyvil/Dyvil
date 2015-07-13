package dyvil.tools.compiler.backend;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.ast.generic.TypeVariable;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.generic.type.GenericType;
import dyvil.tools.compiler.ast.generic.type.InternalGenericType;
import dyvil.tools.compiler.ast.generic.type.InternalTypeVarType;
import dyvil.tools.compiler.ast.generic.type.WildcardType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IExceptionList;
import dyvil.tools.compiler.ast.method.IMethodSignature;
import dyvil.tools.compiler.ast.type.*;

import org.objectweb.asm.Opcodes;

public final class ClassFormat
{
	public static final int		H_GETFIELD			= Opcodes.H_GETFIELD;
	public static final int		H_GETSTATIC			= Opcodes.H_GETSTATIC;
	public static final int		H_PUTFIELD			= Opcodes.H_PUTFIELD;
	public static final int		H_PUTSTATIC			= Opcodes.H_PUTSTATIC;
	public static final int		H_INVOKEVIRTUAL		= Opcodes.H_INVOKEVIRTUAL;
	public static final int		H_INVOKESTATIC		= Opcodes.H_INVOKESTATIC;
	public static final int		H_INVOKESPECIAL		= Opcodes.H_INVOKESPECIAL;
	public static final int		H_NEWINVOKESPECIAL	= Opcodes.H_NEWINVOKESPECIAL;
	public static final int		H_INVOKEINTERFACE	= Opcodes.H_INVOKEINTERFACE;
	
	public static final int		T_BOOLEAN			= 4;
	public static final int		T_CHAR				= 5;
	public static final int		T_FLOAT				= 6;
	public static final int		T_DOUBLE			= 7;
	public static final int		T_BYTE				= 8;
	public static final int		T_SHORT				= 9;
	public static final int		T_INT				= 10;
	public static final int		T_LONG				= 11;
	
	public static final int		ACC_SUPER			= Opcodes.ACC_SUPER;
	
	public static final Integer	UNINITIALIZED_THIS	= Opcodes.UNINITIALIZED_THIS;
	public static final Integer	NULL				= Opcodes.NULL;
	public static final Integer	TOP					= Opcodes.TOP;
	public static final Integer	BOOLEAN				= new Integer(1);
	public static final Integer	BYTE				= new Integer(1);
	public static final Integer	SHORT				= new Integer(1);
	public static final Integer	CHAR				= new Integer(1);
	public static final Integer	INT					= Opcodes.INTEGER;
	public static final Integer	LONG				= Opcodes.LONG;
	public static final Integer	FLOAT				= Opcodes.FLOAT;
	public static final Integer	DOUBLE				= Opcodes.DOUBLE;
	
	public static String packageToInternal(String pack)
	{
		return pack.replace('.', '/');
	}
	
	public static String internalToPackage(String internal)
	{
		return internal.replace('/', '.');
	}
	
	public static String extendedToInternal(String extended)
	{
		return extended.substring(1, extended.length() - 1);
	}
	
	public static String extendedToPackage(String extended)
	{
		int len = extended.length() - 1;
		StringBuilder builder = new StringBuilder(len - 1);
		for (int i = 1; i < len; i++)
		{
			char c = extended.charAt(i);
			if (c == '/')
			{
				builder.append('.');
				continue;
			}
			builder.append(c);
		}
		return builder.toString();
	}
	
	public static String userToExtended(String name)
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
	
	public static IType internalToType(String internal)
	{
		return new InternalType(internal);
	}
	
	public static IType extendedToType(String extended)
	{
		return readType(extended, 0, extended.length() - 1);
	}
	
	public static IType readReturnType(String desc)
	{
		return readType(desc, desc.lastIndexOf(')') + 1, desc.length());
	}
	
	public static void readClassSignature(String desc, IClass iclass)
	{
		int i = 0;
		if (desc.charAt(0) == '<')
		{
			i++;
			while (desc.charAt(i) != '>')
			{
				i = readGeneric(desc, i, iclass);
			}
			i++;
		}
		
		int len = desc.length();
		i = readTyped(desc, i, iclass::setSuperType);
		while (i < len)
		{
			i = readTyped(desc, i, iclass::addInterface);
		}
	}
	
	public static void readMethodType(String desc, IMethodSignature method)
	{
		int i = 1;
		if (desc.charAt(0) == '<')
		{
			while (desc.charAt(i) != '>')
			{
				i = readGeneric(desc, i, method);
			}
			i += 2;
		}
		while (desc.charAt(i) != ')')
		{
			i = readTyped(desc, i, method::addType);
		}
		i++;
		i = readTyped(desc, i, method);
		
		// Throwables
		int len = desc.length();
		while (i < len && desc.charAt(i) == '^')
		{
			i = readException(desc, i + 1, method);
		}
	}
	
	public static void readConstructorType(String desc, IConstructor constructor)
	{
		int i = 1;
		while (desc.charAt(i) != ')')
		{
			i = readTyped(desc, i, constructor::addType);
		}
		i += 2;
		
		int len = desc.length();
		while (i < len && desc.charAt(i) == '^')
		{
			i = readException(desc, i + 1, constructor);
		}
	}
	
	private static IType readType(String desc, int start, int end)
	{
		int array = 0;
		while (desc.charAt(start) == '[')
		{
			array++;
			start++;
		}
		
		switch (desc.charAt(start))
		{
		case 'V':
			return ArrayType.getArrayType(Types.VOID, array);
		case 'Z':
			return ArrayType.getArrayType(Types.BOOLEAN, array);
		case 'B':
			return ArrayType.getArrayType(Types.BYTE, array);
		case 'S':
			return ArrayType.getArrayType(Types.SHORT, array);
		case 'C':
			return ArrayType.getArrayType(Types.CHAR, array);
		case 'I':
			return ArrayType.getArrayType(Types.INT, array);
		case 'J':
			return ArrayType.getArrayType(Types.LONG, array);
		case 'F':
			return ArrayType.getArrayType(Types.FLOAT, array);
		case 'D':
			return ArrayType.getArrayType(Types.DOUBLE, array);
		case 'T':
			return new InternalTypeVarType(desc.substring(start + 1, end));
		case 'L':
			return readReferenceType(desc, start + 1, end);
		}
		return null;
	}
	
	private static IType readReferenceType(String desc, int start, int end)
	{
		int index = desc.indexOf('<', start);
		if (index >= 0 && index < end)
		{
			GenericType type = new InternalGenericType(desc.substring(start, index));
			index++;
			
			while (desc.charAt(index) != '>')
			{
				index = readTyped(desc, index, type);
			}
			return type;
		}
		
		return new InternalType(desc.substring(start, end));
	}
	
	private static int readTyped(String desc, int start, ITypeConsumer consumer)
	{
		int array = 0;
		char c;
		while ((c = desc.charAt(start)) == '[')
		{
			array++;
			start++;
		}
		
		switch (c)
		{
		case 'V':
			consumer.setType(ArrayType.getArrayType(Types.VOID, array));
			return start + 1;
		case 'Z':
			consumer.setType(ArrayType.getArrayType(Types.BOOLEAN, array));
			return start + 1;
		case 'B':
			consumer.setType(ArrayType.getArrayType(Types.BYTE, array));
			return start + 1;
		case 'S':
			consumer.setType(ArrayType.getArrayType(Types.SHORT, array));
			return start + 1;
		case 'C':
			consumer.setType(ArrayType.getArrayType(Types.CHAR, array));
			return start + 1;
		case 'I':
			consumer.setType(ArrayType.getArrayType(Types.INT, array));
			return start + 1;
		case 'J':
			consumer.setType(ArrayType.getArrayType(Types.LONG, array));
			return start + 1;
		case 'F':
			consumer.setType(ArrayType.getArrayType(Types.FLOAT, array));
			return start + 1;
		case 'D':
			consumer.setType(ArrayType.getArrayType(Types.DOUBLE, array));
			return start + 1;
		case 'L':
		{
			int end1 = getMatchingSemicolon(desc, start, desc.length());
			IType type = readReferenceType(desc, start + 1, end1);
			if (array > 0)
			{
				type = ArrayType.getArrayType(type, array);
			}
			consumer.setType(type);
			return end1 + 1;
		}
		case 'T':
		{
			int end1 = desc.indexOf(';', start);
			IType type = new InternalTypeVarType(desc.substring(start + 1, end1));
			if (array > 0)
			{
				type = ArrayType.getArrayType(type, array);
			}
			consumer.setType(type);
			return end1 + 1;
		}
		case '*':
			consumer.setType(new WildcardType(Variance.INVARIANT));
			return start + 1;
		case '+':
		{
			int end1 = getMatchingSemicolon(desc, start, desc.length());
			WildcardType var = new WildcardType(Variance.COVARIANT);
			var.setType(readType(desc, start + 1, end1));
			consumer.setType(var);
			return end1 + 1;
		}
		case '-':
		{
			int end1 = getMatchingSemicolon(desc, start, desc.length());
			WildcardType var = new WildcardType(Variance.CONTRAVARIANT);
			var.setType(readType(desc, start + 1, end1));
			consumer.setType(var);
			return end1 + 1;
		}
		}
		return start;
	}
	
	private static int readGeneric(String desc, int start, IGeneric generic)
	{
		int index = desc.indexOf(':', start);
		Name name = Name.getQualified(desc.substring(start, index));
		TypeVariable typeVar = new TypeVariable(generic, name);
		if (desc.charAt(index + 1) == ':')
		{
			index++;
			typeVar.addUpperBound(Types.OBJECT);
		}
		while (desc.charAt(index) == ':')
		{
			index = readTyped(desc, index + 1, typeVar::addUpperBound);
		}
		generic.addTypeVariable(typeVar);
		return index;
	}
	
	private static int readException(String desc, int start, IExceptionList list)
	{
		switch (desc.charAt(start))
		{
		case 'L':
		{
			int end1 = getMatchingSemicolon(desc, start, desc.length());
			IType type = readReferenceType(desc, start + 1, end1);
			list.addException(type);
			return end1 + 1;
		}
		case 'T':
		{
			int end1 = desc.indexOf(';', start);
			IType type = new InternalTypeVarType(desc.substring(start + 1, end1));
			list.addException(type);
			return end1 + 1;
		}
		}
		return start;
	}
	
	private static int getMatchingSemicolon(String s, int start, int end)
	{
		int depth = 0;
		for (int i = start; i < end; i++)
		{
			char c = s.charAt(i);
			if (c == '<')
			{
				depth++;
			}
			else if (c == '>')
			{
				depth--;
			}
			else if (c == ';' && depth == 0)
			{
				return i;
			}
		}
		return -1;
	}
}
