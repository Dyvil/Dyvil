package dyvil.tools.compiler.backend;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;
import dyvil.tools.asm.ASMConstants;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.external.ExternalConstructor;
import dyvil.tools.compiler.ast.external.ExternalMethod;
import dyvil.tools.compiler.ast.external.ExternalParameter;
import dyvil.tools.compiler.ast.external.ExternalTypeParameter;
import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.method.IExternalCallableMember;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.ast.type.builtin.AnyType;
import dyvil.tools.compiler.ast.type.builtin.NoneType;
import dyvil.tools.compiler.ast.type.builtin.NullType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.*;
import dyvil.tools.compiler.ast.type.generic.GenericType;
import dyvil.tools.compiler.ast.type.generic.InternalGenericType;
import dyvil.tools.compiler.ast.type.raw.InternalType;
import dyvil.tools.compiler.ast.type.typevar.InternalTypeVarType;
import dyvil.tools.parsing.Name;

@SuppressWarnings( { "UnnecessaryBoxing", "unused" })
public final class ClassFormat
{
	public static final int CLASS_VERSION = ASMConstants.V1_8;
	public static final int ASM_VERSION   = ASMConstants.ASM5;

	public static final String BSM_HEAD = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;";
	public static final String BSM_TAIL = ")Ljava/lang/invoke/CallSite;";

	public static final int H_GETFIELD         = ASMConstants.H_GETFIELD;
	public static final int H_GETSTATIC        = ASMConstants.H_GETSTATIC;
	public static final int H_PUTFIELD         = ASMConstants.H_PUTFIELD;
	public static final int H_PUTSTATIC        = ASMConstants.H_PUTSTATIC;
	public static final int H_INVOKEVIRTUAL    = ASMConstants.H_INVOKEVIRTUAL;
	public static final int H_INVOKESTATIC     = ASMConstants.H_INVOKESTATIC;
	public static final int H_INVOKESPECIAL    = ASMConstants.H_INVOKESPECIAL;
	public static final int H_NEWINVOKESPECIAL = ASMConstants.H_NEWINVOKESPECIAL;
	public static final int H_INVOKEINTERFACE  = ASMConstants.H_INVOKEINTERFACE;

	public static final int T_BOOLEAN = 4;
	public static final int T_CHAR    = 5;
	public static final int T_FLOAT   = 6;
	public static final int T_DOUBLE  = 7;
	public static final int T_BYTE    = 8;
	public static final int T_SHORT   = 9;
	public static final int T_INT     = 10;
	public static final int T_LONG    = 11;

	public static final int ACC_SUPER = ASMConstants.ACC_SUPER;

	public static final Integer UNINITIALIZED_THIS = ASMConstants.UNINITIALIZED_THIS;
	public static final Integer NULL               = ASMConstants.NULL;
	public static final Integer TOP                = ASMConstants.TOP;
	public static final Integer BOOLEAN            = new Integer(1);
	public static final Integer BYTE               = new Integer(1);
	public static final Integer SHORT              = new Integer(1);
	public static final Integer CHAR               = new Integer(1);
	public static final Integer INT                = ASMConstants.INTEGER;
	public static final Integer LONG               = ASMConstants.LONG;
	public static final Integer FLOAT              = ASMConstants.FLOAT;
	public static final Integer DOUBLE             = ASMConstants.DOUBLE;

	public static int insnToHandle(int invokeOpcode)
	{
		switch (invokeOpcode)
		{
		case ASMConstants.INVOKEVIRTUAL:
			return ClassFormat.H_INVOKEVIRTUAL;
		case ASMConstants.INVOKESTATIC:
			return ClassFormat.H_INVOKESTATIC;
		case ASMConstants.INVOKEINTERFACE:
			return ClassFormat.H_INVOKEINTERFACE;
		case ASMConstants.INVOKESPECIAL:
			return ClassFormat.H_INVOKESPECIAL;
		}
		return -1;
	}

	public static ModifierSet readModifiers(int access)
	{
		if ((access & Modifiers.VISIBILITY_MODIFIERS) == 0)
		{
			access |= Modifiers.PACKAGE;
		}
		return new FlagModifierSet(access);
	}

	public static String packageToInternal(String pack)
	{
		return pack.replace('.', '/');
	}

	public static String internalToPackage(String internal)
	{
		return internal.replace('/', '.');
	}

	public static String internalToExtended(String internal)
	{
		return 'L' + internal + ';';
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
		return readType(extended, 0, extended.length() - 1, false);
	}

	public static IType readFieldType(String desc)
	{
		return readType(desc, 0, desc.length() - 1, true);
	}

	public static IType readReturnType(String desc)
	{
		return readType(desc, desc.lastIndexOf(')') + 1, desc.length() - 1, true);
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
		i = readTyped(desc, i, iclass::setSuperType, false);
		while (i < len)
		{
			i = readTyped(desc, i, iclass::addInterface, false);
		}
	}

	private static ITypeConsumer parameterTypeConsumer(IExternalCallableMember methodSignature)
	{
		final IParameterList parameterList = methodSignature.getExternalParameterList();
		return type ->
		{
			final ExternalParameter parameter = new ExternalParameter(null, null, type);
			parameter.setMethod(methodSignature);
			parameterList.addParameter(parameter);
		};
	}

	public static void readMethodType(String desc, ExternalMethod method)
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
			i = readTyped(desc, i, parameterTypeConsumer(method), true);
		}
		i++;
		i = readTyped(desc, i, method, true);

		// Throwables
		int len = desc.length();
		while (i < len && desc.charAt(i) == '^')
		{
			i = readException(desc, i + 1, method.getExceptions());
		}
	}

	public static void readConstructorType(String desc, ExternalConstructor constructor)
	{
		int i = 1;
		while (desc.charAt(i) != ')')
		{
			i = readTyped(desc, i, parameterTypeConsumer(constructor), true);
		}
		i += 2;

		int len = desc.length();
		while (i < len && desc.charAt(i) == '^')
		{
			i = readException(desc, i + 1, constructor.getExceptions());
		}
	}

	public static void readExceptions(String[] exceptions, TypeList exceptionList)
	{
		for (String s : exceptions)
		{
			exceptionList.add(internalToType(s));
		}
	}

	public static IType readType(String desc, int start, int end, boolean nullables)
	{
		switch (desc.charAt(start))
		{
		// primitives
		case 'V':
			return Types.VOID;
		case 'Z':
			return Types.BOOLEAN;
		case 'B':
			return Types.BYTE;
		case 'S':
			return Types.SHORT;
		case 'C':
			return Types.CHAR;
		case 'I':
			return Types.INT;
		case 'J':
			return Types.LONG;
		case 'F':
			return Types.FLOAT;
		case 'D':
			return Types.DOUBLE;
		case 'L': // class type
			return readReferenceType(desc, start + 1, end, nullables);
		case 'R': // reference type
			final ReferenceType rt = new ReferenceType();
			readTyped(desc, start + 1, rt::setType, true);
			return rt;
		case NullType.NULL_DESC: // null
			return Types.NULL;
		case NoneType.NONE_DESC: // none
			return Types.NONE;
		case AnyType.ANY_DESC: // any
			return Types.ANY;
		case 'T': // type parameter reference
			return new InternalTypeVarType(desc.substring(start + 1, end));
		case '[': // array type
			final ArrayType arrayType = new ArrayType(readType(desc, start + 1, end, true));
			return nullable(arrayType, nullables);
		case '|': // union
		{
			final UnionType union = new UnionType();
			final int end1 = readTyped(desc, start + 1, union::setLeft, nullables);
			readTyped(desc, end1, union::setRight, nullables);
			return union;
		}
		case '&': // intersection
		{
			final IntersectionType intersection = new IntersectionType();
			final int end1 = readTyped(desc, start + 1, intersection::setLeft, nullables);
			readTyped(desc, end1, intersection::setRight, nullables);
			return intersection;
		}
		case '?': // option
			return new NullableType(readType(desc, start + 1, end, false));
		}
		return null;
	}

	private static IType readReferenceType(String desc, int start, int end, boolean nullables)
	{
		return nullable(readReferenceType(desc, start, end), nullables);
	}

	@NonNull
	private static IType readReferenceType(String desc, int start, int end)
	{
		int index = desc.indexOf('<', start);
		if (index >= 0 && index < end)
		{
			final GenericType type = new InternalGenericType(desc.substring(start, index));
			final TypeList arguments = type.getArguments();
			index++;

			while (desc.charAt(index) != '>')
			{
				index = readTyped(desc, index, arguments, true);
			}
			return type;
		}

		return new InternalType(desc.substring(start, end));
	}

	private static int readTyped(String desc, int start, ITypeConsumer consumer, boolean nullables)
	{
		switch (desc.charAt(start))
		{
		case 'V':
			consumer.setType(Types.VOID);
			return start + 1;
		case 'Z':
			consumer.setType(Types.BOOLEAN);
			return start + 1;
		case 'B':
			consumer.setType(Types.BYTE);
			return start + 1;
		case 'S':
			consumer.setType(Types.SHORT);
			return start + 1;
		case 'C':
			consumer.setType(Types.CHAR);
			return start + 1;
		case 'I':
			consumer.setType(Types.INT);
			return start + 1;
		case 'J':
			consumer.setType(Types.LONG);
			return start + 1;
		case 'F':
			consumer.setType(Types.FLOAT);
			return start + 1;
		case 'D':
			consumer.setType(Types.DOUBLE);
			return start + 1;
		case 'L': // class
		{
			final int end = getMatchingSemicolon(desc, start, desc.length());
			consumer.setType(readReferenceType(desc, start + 1, end, nullables));
			return end + 1;
		}
		case 'R': // reference
		{
			final ReferenceType reference = new ReferenceType();
			final int end = readTyped(desc, start + 1, reference::setType, true);
			consumer.setType(reference);
			return end + 1;
		}
		case NullType.NULL_DESC: // null type
			consumer.setType(Types.NULL);
			return start + 1;
		case NoneType.NONE_DESC:
			consumer.setType(Types.NONE);
			return start + 1;
		case AnyType.ANY_DESC:
			consumer.setType(Types.ANY);
			return start + 1;
		case 'T': // type var reference
		{
			final int end = desc.indexOf(';', start);
			consumer.setType(new InternalTypeVarType(desc.substring(start + 1, end)));
			return end + 1;
		}
		case '*': // any wildcard
			consumer.setType(new WildcardType(Variance.COVARIANT));
			return start + 1;
		case '+': // covariant wildcard
		{
			final WildcardType var = new WildcardType(Variance.COVARIANT);
			final int end = readTyped(desc, start + 1, var, nullables);
			consumer.setType(var);
			return end;
		}
		case '-': // contravariant wildcard
		{
			final WildcardType var = new WildcardType(Variance.CONTRAVARIANT);
			final int end = readTyped(desc, start + 1, var, nullables);
			consumer.setType(var);
			return end;
		}
		case '[': // array
		{
			final ArrayType arrayType = new ArrayType();
			final int end = readTyped(desc, start + 1, arrayType::setElementType, true);
			consumer.setType(nullable(arrayType, nullables));
			return end;
		}
		case '|': // union
		{
			final UnionType union = new UnionType();
			final int end1 = readTyped(desc, start + 1, union::setLeft, nullables);
			final int end2 = readTyped(desc, end1, union::setRight, nullables);
			consumer.setType(union);
			return end2;
		}
		case '&': // intersection
		{
			final IntersectionType intersection = new IntersectionType();
			final int end1 = readTyped(desc, start + 1, intersection::setLeft, nullables);
			final int end2 = readTyped(desc, end1, intersection::setRight, nullables);
			consumer.setType(intersection);
			return end2;
		}
		case '?': // option
		{
			final NullableType nullableType = new NullableType();
			final int end = readTyped(desc, start + 1, nullableType::setElementType, false);
			consumer.setType(nullableType);
			return end;
		}
		}
		return start;
	}

	private static IType nullable(IType type, boolean nullables)
	{
		return nullables ? new ImplicitNullableType(type) : type;
	}

	private static int readGeneric(String desc, int start, ITypeParametric generic)
	{
		int index = desc.indexOf(':', start);
		final Name name = Name.fromRaw(desc.substring(start, index));
		final ExternalTypeParameter typeParam = new ExternalTypeParameter(generic, name);

		if (desc.charAt(index + 1) == ':')
		{
			// name::
			index++;
		}
		while (desc.charAt(index) == ':')
		{
			index = readTyped(desc, index + 1, typeParam::addUpperBound, true);
		}
		generic.getTypeParameters().add(typeParam);
		return index;
	}

	private static int readException(String desc, int start, TypeList list)
	{
		return readTyped(desc, start, list, false);
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
