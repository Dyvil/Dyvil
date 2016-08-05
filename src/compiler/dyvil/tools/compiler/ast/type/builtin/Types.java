package dyvil.tools.compiler.ast.type.builtin;

import dyvil.collection.Collection;
import dyvil.collection.Set;
import dyvil.collection.mutable.IdentityHashSet;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.dynamic.DynamicType;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.ast.type.compound.UnionType;
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

	public static void initHeaders()
	{
		LANG_HEADER = Package.dyvil.resolveHeader("Lang");
	}

	public static void initTypes()
	{
		VOID.theClass = VOID_CLASS = Package.javaLang.resolveClass("Void");
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

		VOID.boxMethod = primitivesBody.getMethod(Name.fromRaw("Void"));
		VOID.unboxMethod = primitivesBody.getMethod(Name.fromRaw("toVoid"));
		BOOLEAN.boxMethod = primitivesBody.getMethod(Name.fromRaw("Boolean"));
		BOOLEAN.unboxMethod = primitivesBody.getMethod(Name.fromRaw("toBoolean"));
		BYTE.boxMethod = primitivesBody.getMethod(Name.fromRaw("Byte"));
		BYTE.unboxMethod = primitivesBody.getMethod(Name.fromRaw("toByte"));
		SHORT.boxMethod = primitivesBody.getMethod(Name.fromRaw("Short"));
		SHORT.unboxMethod = primitivesBody.getMethod(Name.fromRaw("toShort"));
		CHAR.boxMethod = primitivesBody.getMethod(Name.fromRaw("Char"));
		CHAR.unboxMethod = primitivesBody.getMethod(Name.fromRaw("toChar"));
		INT.boxMethod = primitivesBody.getMethod(Name.fromRaw("Int"));
		INT.unboxMethod = primitivesBody.getMethod(Name.fromRaw("toInt"));
		LONG.boxMethod = primitivesBody.getMethod(Name.fromRaw("Long"));
		LONG.unboxMethod = primitivesBody.getMethod(Name.fromRaw("toLong"));
		FLOAT.boxMethod = primitivesBody.getMethod(Name.fromRaw("Float"));
		FLOAT.unboxMethod = primitivesBody.getMethod(Name.fromRaw("toFloat"));
		DOUBLE.boxMethod = primitivesBody.getMethod(Name.fromRaw("Double"));
		DOUBLE.unboxMethod = primitivesBody.getMethod(Name.fromRaw("toDouble"));
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

	public static IClass getObjectArrayClass()
	{
		if (OBJECT_ARRAY_CLASS == null)
		{
			return OBJECT_ARRAY_CLASS = Package.dyvilArray.resolveClass("ObjectArray");
		}
		return OBJECT_ARRAY_CLASS;
	}

	public static boolean isSameClass(IType type1, IType type2)
	{
		return type1 == type2 || type1.isSameClass(type2);
	}

	public static boolean isSameType(IType type1, IType type2)
	{
		if (type1 == type2)
		{
			return true;
		}

		if (type2.subTypeCheckLevel() > type1.subTypeCheckLevel())
		{
			return type2.isSameType(type1);
		}
		return type1.isSameType(type2);
	}

	public static boolean isVoid(IType type)
	{
		return type.getTypecode() == PrimitiveType.VOID_CODE;
	}

	public static boolean isExactType(IType type1, IType type2)
	{
		return type1 == type2 || type1.isSameType(type2) && type2.isSameType(type1);
	}

	public static boolean isSuperClass(IClass superClass, IClass subClass)
	{
		return superClass == subClass || superClass.getClassType().isSuperClassOf(subClass.getClassType());
	}

	public static boolean isSuperClass(IType superType, IType subType)
	{
		if (superType == subType)
		{
			return true;
		}
		if (subType.subTypeCheckLevel() > superType.subTypeCheckLevel())
		{
			return subType.isSubClassOf(superType);
		}
		return superType.isSuperClassOf(subType);
	}

	public static boolean isSuperType(IType superType, IType subType)
	{
		if (superType == subType)
		{
			return true;
		}
		if (subType.subTypeCheckLevel() > superType.subTypeCheckLevel())
		{
			return subType.isSubTypeOf(superType);
		}
		return superType.isSuperTypeOf(subType);
	}

	public static boolean isAssignable(IType fromType, IType toType)
	{
		return isSuperType(toType, fromType) || isConvertible(fromType, toType);
	}

	public static boolean isConvertible(IType fromType, IType toType)
	{
		return toType.isConvertibleFrom(fromType) || fromType.isConvertibleTo(toType);
	}

	public static Set<IClass> commonClasses(IType type1, IType type2)
	{
		final Set<IClass> superTypes1 = superClasses(type1);
		final Set<IClass> superTypes2 = superClasses(type2);
		superTypes1.retainAll(superTypes2);
		return superTypes1;
	}

	public static IType combine(IType type1, IType type2)
	{
		return UnionType.combine(type1, type2, null);
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

	public static IType resolveTypeSafely(IType type, ITypeParameter typeVar)
	{
		final IType resolved = type.resolveType(typeVar);
		return resolved != null ? resolved : typeVar.getDefaultType();
	}
}
