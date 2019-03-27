package dyvilx.tools.compiler.backend;

import dyvil.lang.Name;
import dyvil.ref.ObjectRef;
import dyvil.ref.simple.SimpleObjectRef;
import dyvilx.tools.asm.ASMConstants;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.external.ExternalConstructor;
import dyvilx.tools.compiler.ast.external.ExternalMethod;
import dyvilx.tools.compiler.ast.external.ExternalParameter;
import dyvilx.tools.compiler.ast.external.ExternalTypeParameter;
import dyvilx.tools.compiler.ast.generic.ITypeParametric;
import dyvilx.tools.compiler.ast.generic.Variance;
import dyvilx.tools.compiler.ast.method.IExternalCallableMember;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.reference.ReferenceType;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.AnyType;
import dyvilx.tools.compiler.ast.type.builtin.NoneType;
import dyvilx.tools.compiler.ast.type.builtin.NullType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.*;
import dyvilx.tools.compiler.ast.type.generic.GenericType;
import dyvilx.tools.compiler.ast.type.generic.InternalGenericType;
import dyvilx.tools.compiler.ast.type.raw.InternalType;
import dyvilx.tools.compiler.ast.type.typevar.InternalTypeVarType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings( { "UnnecessaryBoxing", "unused", "UnusedAssignment" })
public final class ClassFormat
{
	// =============== Constants ===============

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

	public static final int T_BOOLEAN = ASMConstants.T_BOOLEAN;
	public static final int T_CHAR    = ASMConstants.T_CHAR;
	public static final int T_FLOAT   = ASMConstants.T_FLOAT;
	public static final int T_DOUBLE  = ASMConstants.T_DOUBLE;
	public static final int T_BYTE    = ASMConstants.T_BYTE;
	public static final int T_SHORT   = ASMConstants.T_SHORT;
	public static final int T_INT     = ASMConstants.T_INT;
	public static final int T_LONG    = ASMConstants.T_LONG;

	public static final int ACC_SUPER = ASMConstants.ACC_SUPER;

	public static final Integer UNINITIALIZED_THIS = ASMConstants.UNINITIALIZED_THIS;
	public static final Integer NULL               = ASMConstants.NULL;
	public static final Integer TOP                = ASMConstants.TOP;

	public static final Integer BOOLEAN = new Integer(1);
	public static final Integer BYTE    = new Integer(1);
	public static final Integer SHORT   = new Integer(1);
	public static final Integer CHAR    = new Integer(1);
	public static final Integer INT     = ASMConstants.INTEGER;
	public static final Integer LONG    = ASMConstants.LONG;
	public static final Integer FLOAT   = ASMConstants.FLOAT;
	public static final Integer DOUBLE  = ASMConstants.DOUBLE;

	// =============== Static Methods ===============

	// --------------- Misc. ---------------

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

	public static boolean isTwoWord(Object type)
	{
		return type == LONG || type == DOUBLE;
	}

	// --------------- Format Conversions ---------------

	public static String packageToInternal(String qualified)
	{
		return qualified.replace('.', '/');
	}

	public static String packageToExtended(String qualified)
	{
		return internalToExtended(packageToInternal(qualified));
	}

	public static String internalToPackage(String internal)
	{
		return internal.replace('/', '.');
	}

	public static String internalToExtended(String internal)
	{
		return 'L' + internal + ';';
	}

	public static String extendedToPackage(String extended)
	{
		return internalToPackage(extendedToInternal(extended));
	}

	public static String extendedToInternal(String extended)
	{
		return extended.substring(1, extended.length() - 1);
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
			return packageToExtended(name);
		}
		return name;
	}

	// --------------- Type Parsing ---------------

	public static IType internalToType(String internal)
	{
		return new InternalType(internal);
	}

	public static IType extendedToType(String extended)
	{
		return readType(extended, 0, false);
	}

	@Deprecated
	public static IType readType(String desc, int start, int end, boolean nullables)
	{
		return readType(desc, start, nullables);
	}

	// --------------- Type Parsing Helpers ---------------

	private static IType readType(String desc, int start, boolean nullables)
	{
		ObjectRef<IType> target = new SimpleObjectRef<>(null);
		readType(desc, start, nullables, target::set);
		return target.get();
	}

	private static int readType(String desc, int start, boolean nullables, Consumer<IType> consumer)
	{
		switch (desc.charAt(start))
		{
		case 'V':
			consumer.accept(Types.VOID);
			return start + 1;
		case 'Z':
			consumer.accept(Types.BOOLEAN);
			return start + 1;
		case 'B':
			consumer.accept(Types.BYTE);
			return start + 1;
		case 'S':
			consumer.accept(Types.SHORT);
			return start + 1;
		case 'C':
			consumer.accept(Types.CHAR);
			return start + 1;
		case 'I':
			consumer.accept(Types.INT);
			return start + 1;
		case 'J':
			consumer.accept(Types.LONG);
			return start + 1;
		case 'F':
			consumer.accept(Types.FLOAT);
			return start + 1;
		case 'D':
			consumer.accept(Types.DOUBLE);
			return start + 1;
		case 'L': // class
			return readLType(desc, start, nullables, consumer);
		case 'R': // reference
		{
			final ReferenceType reference = new ReferenceType();
			final int end = readType(desc, start + 1, true, reference::setType);
			consumer.accept(reference);
			return end + 1;
		}
		case NullType.NULL_DESC: // null type
			consumer.accept(Types.NULL);
			return start + 1;
		case NoneType.NONE_DESC:
			consumer.accept(Types.NONE);
			return start + 1;
		case AnyType.ANY_DESC:
			consumer.accept(Types.ANY);
			return start + 1;
		case 'T': // type var reference
		{
			final int end = desc.indexOf(';', start);
			consumer.accept(new InternalTypeVarType(desc.substring(start + 1, end)));
			return end + 1;
		}
		case '*': // any wildcard
			consumer.accept(new WildcardType(Variance.COVARIANT));
			return start + 1;
		case '+': // covariant wildcard
		{
			final WildcardType var = new WildcardType(Variance.COVARIANT);
			final int end = readType(desc, start + 1, nullables, var::setType);
			consumer.accept(var);
			return end;
		}
		case '-': // contravariant wildcard
		{
			final WildcardType var = new WildcardType(Variance.CONTRAVARIANT);
			final int end = readType(desc, start + 1, nullables, var::setType);
			consumer.accept(var);
			return end;
		}
		case '[': // array
		{
			final ArrayType arrayType = new ArrayType();
			final int end = readType(desc, start + 1, true, arrayType::setElementType);
			consumer.accept(nullable(arrayType, nullables));
			return end;
		}
		case '|': // union
		{
			final UnionType union = new UnionType();
			final int end1 = readType(desc, start + 1, nullables, union::setLeft);
			final int end2 = readType(desc, end1, nullables, union::setRight);
			consumer.accept(union);
			return end2;
		}
		case '&': // intersection
		{
			final IntersectionType intersection = new IntersectionType();
			final int end1 = readType(desc, start + 1, nullables, intersection::setLeft);
			final int end2 = readType(desc, end1, nullables, intersection::setRight);
			consumer.accept(intersection);
			return end2;
		}
		case '?': // option
		{
			final NullableType nullableType = new NullableType();
			final int end = readType(desc, start + 1, false, nullableType::setElementType);
			consumer.accept(nullableType);
			return end;
		}
		}
		return start;
	}

	private static int readLType(String desc, int start, boolean nullables, Consumer<IType> consumer)
	{
		start++; // consume 'L'
		for (int end = start; end < desc.length(); end++)
		{
			switch (desc.charAt(end))
			{
			case ';':
				consumer.accept(nullable(new InternalType(desc.substring(start, end)), nullables));
				end++; // consume ';'
				return end;
			case '<':
				final GenericType type = new InternalGenericType(desc.substring(start, end));
				final TypeList arguments = type.getArguments();

				end++; // consume '<'
				while (desc.charAt(end) != '>')
				{
					end = readType(desc, end, true, arguments);
				}
				consumer.accept(nullable(type, nullables));

				end += 2; // consume '>;'
				return end;
			}
		}

		throw new IllegalArgumentException("missing ';' or '<' after reference type");
	}

	private static IType nullable(IType type, boolean nullables)
	{
		return nullables ? new ImplicitNullableType(type) : type;
	}

	// --------------- Signature Parsing ---------------

	public static IType readFieldType(String desc)
	{
		return readType(desc, 0, true);
	}

	public static IType readReturnType(String desc)
	{
		return readType(desc, desc.lastIndexOf(')') + 1, true);
	}

	public static void readClassSignature(String desc, IClass iclass)
	{
		int i = 0;
		i = readTypeParameters(desc, i, iclass);
		i = readType(desc, i, false, iclass::setSuperType);
		while (i < desc.length())
		{
			i = readType(desc, i, false, iclass.getInterfaces());
		}
	}

	public static void readMethodType(String desc, ExternalMethod method)
	{
		int i = 0;
		i = readTypeParameters(desc, i, method);
		i = readParameters(desc, i, method);
		i = readType(desc, i, true, method::setType);
		i = readExceptions(desc, i, method.getExternalExceptions());
	}

	public static void readConstructorType(String desc, ExternalConstructor constructor)
	{
		int i = 0;
		i = readParameters(desc, i, constructor);
		i++; // consume 'V'
		i = readExceptions(desc, i, constructor.getExternalExceptions());
	}

	public static void readExceptions(String[] exceptions, TypeList exceptionList)
	{
		for (String s : exceptions)
		{
			exceptionList.add(internalToType(s));
		}
	}

	// --------------- Signature Parsing Helpers ---------------

	private static Consumer<IType> parameterTypeConsumer(IExternalCallableMember methodSignature)
	{
		final ParameterList parameterList = methodSignature.getExternalParameterList();
		return type -> {
			final ExternalParameter parameter = new ExternalParameter(null, null, type);
			parameter.setMethod(methodSignature);
			parameterList.add(parameter);
		};
	}

	private static int readTypeParameters(String desc, int start, ITypeParametric typeParametric)
	{
		if (desc.charAt(start) != '<')
		{
			return start;
		}

		start++; // consume '<'
		while (desc.charAt(start) != '>')
		{
			start = readTypeParameter(desc, start, typeParametric);
		}
		start++; // consume '>'
		return start;
	}

	private static int readTypeParameter(String desc, int start, ITypeParametric typeParametric)
	{
		int index = desc.indexOf(':', start);
		final Name name = Name.fromRaw(desc.substring(start, index));
		final ExternalTypeParameter typeParam = new ExternalTypeParameter(typeParametric, name);

		if (desc.charAt(index + 1) == ':')
		{
			// name::
			index++;
		}

		List<IType> upperBounds = new ArrayList<>();
		while (desc.charAt(index) == ':')
		{
			index = readType(desc, index + 1, true, upperBounds::add);
		}

		typeParam.setUpperBounds(upperBounds.toArray(new IType[0]));
		typeParametric.getTypeParameters().add(typeParam);
		return index;
	}

	private static int readParameters(String desc, int start, IExternalCallableMember parametric)
	{
		if (desc.charAt(start) != '(')
		{
			return start;
		}

		start++; // consume '('
		while (desc.charAt(start) != ')')
		{
			start = readType(desc, start, true, parameterTypeConsumer(parametric));
		}
		start++; // consume ')'
		return start;
	}

	private static int readExceptions(String desc, int start, TypeList list)
	{
		while (start < desc.length() && desc.charAt(start) == '^')
		{
			start++; // consume '^'
			start = readType(desc, start, false, list);
		}
		return start;
	}
}
