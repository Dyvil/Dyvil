package dyvil.tools.compiler.ast.type.builtin;

import dyvil.collection.Collection;
import dyvil.collection.Set;
import dyvil.collection.mutable.IdentityHashSet;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.dynamic.DynamicType;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.ast.type.compound.UnionType;
import dyvil.tools.compiler.ast.type.generic.ClassGenericType;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.ast.type.raw.InternalType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;

public final class Types
{
	public static IDyvilHeader LANG_HEADER;

	public static final PrimitiveType VOID    = new PrimitiveType(Names._void, PrimitiveType.VOID_CODE, 'V',
	                                                              Opcodes.ILOAD + Opcodes.RETURN - Opcodes.IRETURN,
	                                                              Opcodes.IALOAD, null);
	public static final PrimitiveType BOOLEAN = new PrimitiveType(Names._boolean, PrimitiveType.BOOLEAN_CODE, 'Z',
	                                                              Opcodes.ILOAD, Opcodes.BALOAD, ClassFormat.BOOLEAN);
	public static final PrimitiveType BYTE    = new PrimitiveType(Names._byte, PrimitiveType.BYTE_CODE, 'B',
	                                                              Opcodes.ILOAD, Opcodes.BALOAD, ClassFormat.BOOLEAN);
	public static final PrimitiveType SHORT   = new PrimitiveType(Names._short, PrimitiveType.SHORT_CODE, 'S',
	                                                              Opcodes.ILOAD, Opcodes.SALOAD, ClassFormat.SHORT);
	public static final PrimitiveType CHAR    = new PrimitiveType(Names._char, PrimitiveType.CHAR_CODE, 'C',
	                                                              Opcodes.ILOAD, Opcodes.CALOAD, ClassFormat.CHAR);
	public static final PrimitiveType INT     = new PrimitiveType(Names._int, PrimitiveType.INT_CODE, 'I',
	                                                              Opcodes.ILOAD, Opcodes.IALOAD, ClassFormat.INT);
	public static final PrimitiveType LONG    = new PrimitiveType(Names._long, PrimitiveType.LONG_CODE, 'J',
	                                                              Opcodes.LLOAD, Opcodes.LALOAD, ClassFormat.LONG);
	public static final PrimitiveType FLOAT   = new PrimitiveType(Names._float, PrimitiveType.FLOAT_CODE, 'F',
	                                                              Opcodes.FLOAD, Opcodes.FALOAD, ClassFormat.FLOAT);
	public static final PrimitiveType DOUBLE  = new PrimitiveType(Names._double, PrimitiveType.DOUBLE_CODE, 'D',
	                                                              Opcodes.DLOAD, Opcodes.DALOAD, ClassFormat.DOUBLE);

	public static final DynamicType DYNAMIC = new DynamicType();
	public static final UnknownType UNKNOWN = new UnknownType();
	public static final NullType    NULL    = new NullType();
	public static final AnyType     ANY     = new AnyType();

	public static final ClassType OBJECT = new ClassType();
	public static final ClassType STRING = new ClassType();

	public static final ClassType THROWABLE         = new ClassType();
	public static final ClassType EXCEPTION         = new ClassType();
	public static final ClassType RUNTIME_EXCEPTION = new ClassType();
	public static final ClassType SERIALIZABLE      = new ClassType();

	public static IClass VOID_CLASS;
	public static IClass BOOLEAN_CLASS;
	public static IClass BYTE_CLASS;
	public static IClass SHORT_CLASS;
	public static IClass CHAR_CLASS;
	public static IClass INT_CLASS;
	public static IClass LONG_CLASS;
	public static IClass FLOAT_CLASS;
	public static IClass DOUBLE_CLASS;

	public static IClass PRIMITIVES_CLASS;

	public static IClass OBJECT_CLASS;
	public static IClass STRING_CLASS;
	public static IClass NULL_CLASS;

	public static IClass THROWABLE_CLASS;
	public static IClass EXCEPTION_CLASS;
	public static IClass RUNTIME_EXCEPTION_CLASS;
	public static IClass SERIALIZABLE_CLASS;
	public static IClass INTRINSIC_CLASS;

	public static IClass OVERRIDE_CLASS;
	public static IClass MUTATING_CLASS;
	public static IClass MUTABLE_CLASS;
	public static IClass IMMUTABLE_CLASS;
	public static IClass REIFIED_CLASS;

	public static IClass BOOLEAN_CONVERTIBLE_CLASS;
	public static IClass CHAR_CONVERTIBLE_CLASS;
	public static IClass INT_CONVERTIBLE_CLASS;
	public static IClass LONG_CONVERTIBLE_CLASS;
	public static IClass FLOAT_CONVERTIBLE_CLASS;
	public static IClass DOUBLE_CONVERTIBLE_CLASS;
	public static IClass STRING_CONVERTIBLE_CLASS;

	private static IClass OBJECT_ARRAY_CLASS;
	private static IClass OBJECT_SIMPLE_REF_CLASS;
	private static IClass OBJECT_REF_CLASS;

	public static void initHeaders()
	{
		LANG_HEADER = Package.dyvil.resolveHeader("Lang");
	}

	public static void initTypes()
	{
		VOID.theClass = VOID_CLASS = Package.dyvilLang.resolveClass("Void");
		BOOLEAN.theClass = BOOLEAN_CLASS = Package.javaLang.resolveClass("Boolean");
		BYTE.theClass = BYTE_CLASS = Package.javaLang.resolveClass("Byte");
		SHORT.theClass = SHORT_CLASS = Package.javaLang.resolveClass("Short");
		CHAR.theClass = CHAR_CLASS = Package.javaLang.resolveClass("Character");
		INT.theClass = INT_CLASS = Package.javaLang.resolveClass("Integer");
		LONG.theClass = LONG_CLASS = Package.javaLang.resolveClass("Long");
		FLOAT.theClass = FLOAT_CLASS = Package.javaLang.resolveClass("Float");
		DOUBLE.theClass = DOUBLE_CLASS = Package.javaLang.resolveClass("Double");

		PRIMITIVES_CLASS = Package.dyvilLang.resolveClass("Primitives");

		NULL_CLASS = Package.dyvilLang.resolveClass("Null");
		OBJECT.theClass = OBJECT_CLASS = Package.javaLang.resolveClass("Object");
		STRING.theClass = STRING_CLASS = Package.javaLang.resolveClass("String");
		THROWABLE.theClass = THROWABLE_CLASS = Package.javaLang.resolveClass("Throwable");
		EXCEPTION.theClass = EXCEPTION_CLASS = Package.javaLang.resolveClass("Exception");
		RUNTIME_EXCEPTION.theClass = RUNTIME_EXCEPTION_CLASS = Package.javaLang.resolveClass("RuntimeException");
		SERIALIZABLE.theClass = SERIALIZABLE_CLASS = Package.javaIO.resolveClass("Serializable");

		OVERRIDE_CLASS = Package.javaLang.resolveClass("Override");
		INTRINSIC_CLASS = Package.dyvilAnnotation.resolveClass("Intrinsic");
		MUTATING_CLASS = Package.dyvilAnnotation.resolveClass("Mutating");
		MUTABLE_CLASS = Package.dyvilAnnotation.resolveClass("Mutable");
		IMMUTABLE_CLASS = Package.dyvilAnnotation.resolveClass("Immutable");
		REIFIED_CLASS = Package.dyvilAnnotation.resolveClass("Reified");

		INT_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("IntConvertible");
		BOOLEAN_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("BooleanConvertible");
		CHAR_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("CharConvertible");
		LONG_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("LongConvertible");
		FLOAT_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("FloatConvertible");
		DOUBLE_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("DoubleConvertible");
		STRING_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("StringConvertible");

		final IClassBody primitivesBody = PRIMITIVES_CLASS.getBody();

		VOID.boxMethod = primitivesBody.getMethod(Name.getQualified("Void"));
		VOID.unboxMethod = primitivesBody.getMethod(Name.getQualified("toVoid"));
		BOOLEAN.boxMethod = primitivesBody.getMethod(Name.getQualified("Boolean"));
		BOOLEAN.unboxMethod = primitivesBody.getMethod(Name.getQualified("toBoolean"));
		BYTE.boxMethod = primitivesBody.getMethod(Name.getQualified("Byte"));
		BYTE.unboxMethod = primitivesBody.getMethod(Name.getQualified("toByte"));
		SHORT.boxMethod = primitivesBody.getMethod(Name.getQualified("Short"));
		SHORT.unboxMethod = primitivesBody.getMethod(Name.getQualified("toShort"));
		CHAR.boxMethod = primitivesBody.getMethod(Name.getQualified("Char"));
		CHAR.unboxMethod = primitivesBody.getMethod(Name.getQualified("toChar"));
		INT.boxMethod = primitivesBody.getMethod(Name.getQualified("Int"));
		INT.unboxMethod = primitivesBody.getMethod(Name.getQualified("toInt"));
		LONG.boxMethod = primitivesBody.getMethod(Name.getQualified("Long"));
		LONG.unboxMethod = primitivesBody.getMethod(Name.getQualified("toLong"));
		FLOAT.boxMethod = primitivesBody.getMethod(Name.getQualified("Float"));
		FLOAT.unboxMethod = primitivesBody.getMethod(Name.getQualified("toFloat"));
		DOUBLE.boxMethod = primitivesBody.getMethod(Name.getQualified("Double"));
		DOUBLE.unboxMethod = primitivesBody.getMethod(Name.getQualified("toDouble"));
	}

	public static IType fromASMType(dyvil.tools.asm.Type type)
	{
		switch (type.getSort())
		{
		case dyvil.tools.asm.Type.VOID:
			return VOID;
		case dyvil.tools.asm.Type.BOOLEAN:
			return BOOLEAN;
		case dyvil.tools.asm.Type.BYTE:
			return BYTE;
		case dyvil.tools.asm.Type.SHORT:
			return SHORT;
		case dyvil.tools.asm.Type.CHAR:
			return CHAR;
		case dyvil.tools.asm.Type.INT:
			return INT;
		case dyvil.tools.asm.Type.LONG:
			return LONG;
		case dyvil.tools.asm.Type.FLOAT:
			return FLOAT;
		case dyvil.tools.asm.Type.DOUBLE:
			return DOUBLE;
		case dyvil.tools.asm.Type.OBJECT:
			return new InternalType(type.getInternalName());
		case dyvil.tools.asm.Type.ARRAY:
			return new ArrayType(fromASMType(type.getElementType()));
		}
		return null;
	}

	public static IClass getObjectArray()
	{
		if (OBJECT_ARRAY_CLASS == null)
		{
			return OBJECT_ARRAY_CLASS = Package.dyvilArray.resolveClass("ObjectArray");
		}
		return OBJECT_ARRAY_CLASS;
	}

	public static IClass getObjectRefClass()
	{
		if (OBJECT_REF_CLASS == null)
		{
			return OBJECT_REF_CLASS = Package.dyvilRef.resolveClass("ObjectRef");
		}
		return OBJECT_REF_CLASS;
	}

	public static ReferenceType getObjectRef(IType type)
	{
		return new ReferenceType(getObjectRefClass(), type);
	}

	public static IClass getObjectSimpleRefClass()
	{
		if (OBJECT_SIMPLE_REF_CLASS == null)
		{
			return OBJECT_SIMPLE_REF_CLASS = Package.dyvilRefSimple.resolveClass("SimpleObjectRef");
		}
		return OBJECT_SIMPLE_REF_CLASS;
	}

	public static IType getObjectSimpleRef(IType type)
	{
		ClassGenericType gt = new ClassGenericType(getObjectSimpleRefClass());
		gt.addType(type);
		return gt;
	}

	public static String getInternalRef(IType type, String prefix)
	{
		return "dyvil/ref/" + prefix + type.getTypePrefix() + "Ref";
	}

	public static String getReferenceFactoryName(IType type, String prefix)
	{
		return "new" + prefix + type.getTypePrefix() + "Ref";
	}

	public static IType combine(IType type1, IType type2)
	{
		return UnionType.combine(type1, type2, null);
	}

	public static Set<IClass> commonClasses(IType type1, IType type2)
	{
		final Set<IClass> superTypes1 = superClasses(type1);
		final Set<IClass> superTypes2 = superClasses(type2);
		superTypes1.intersect(superTypes2);
		return superTypes1;
	}

	public static IClass commonClass(IType type1, IType type2)
	{
		final Set<IClass> superTypes1 = superClasses(type1);
		final Set<IClass> superTypes2 = superClasses(type2);

		for (IClass superClass1 : superTypes1)
		{
			if (superClass1 == Types.OBJECT_CLASS)
			{
				continue;
			}

			for (IClass superClass2 : superTypes2)
			{
				if (superClass1 == superClass2)
				{
					return superClass1;
				}
			}
		}

		return Types.OBJECT_CLASS;
	}

	private static Set<IClass> superClasses(IType type)
	{
		Set<IClass> types = new IdentityHashSet<>();
		addSuperClasses(type, types);
		return types;
	}

	private static void addSuperClasses(IType type, Collection<IClass> types)
	{
		final IClass theClass = type.getTheClass();
		if (theClass == null)
		{
			return;
		}

		types.add(theClass);

		final IType superType = theClass.getSuperType();
		if (superType != null)
		{
			addSuperClasses(superType, types);
		}

		for (int i = 0, count = theClass.interfaceCount(); i < count; i++)
		{
			addSuperClasses(theClass.getInterface(i), types);
		}
	}

	public static IType resolvePrimitive(Name name)
	{
		if (name == Names._void)
		{
			return VOID;
		}
		if (name == Names._boolean)
		{
			return BOOLEAN;
		}
		if (name == Names._byte)
		{
			return BYTE;
		}
		if (name == Names._short)
		{
			return SHORT;
		}
		if (name == Names._char)
		{
			return CHAR;
		}
		if (name == Names._int)
		{
			return INT;
		}
		if (name == Names._long)
		{
			return LONG;
		}
		if (name == Names._float)
		{
			return FLOAT;
		}
		if (name == Names._double)
		{
			return DOUBLE;
		}
		if (name == Names.any)
		{
			return ANY;
		}
		if (name == Names.dynamic)
		{
			return DYNAMIC;
		}
		if (name == Names.auto)
		{
			return UNKNOWN;
		}
		return null;
	}
}
