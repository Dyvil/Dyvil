package dyvil.tools.compiler.ast.type;

import dyvil.collection.Collection;
import dyvil.collection.Set;
import dyvil.collection.mutable.ArraySet;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.dynamic.DynamicType;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.transform.Names;

public final class Types
{
	public static IDyvilHeader LANG_HEADER;
	
	public static final PrimitiveType	VOID	= new PrimitiveType(Names._void, PrimitiveType.VOID_CODE, 'V', Opcodes.ILOAD + Opcodes.RETURN - Opcodes.IRETURN,
			Opcodes.IALOAD, null);
	public static final PrimitiveType	BOOLEAN	= new PrimitiveType(Names._boolean, PrimitiveType.BOOLEAN_CODE, 'Z', Opcodes.ILOAD, Opcodes.BALOAD,
			ClassFormat.BOOLEAN);
	public static final PrimitiveType	BYTE	= new PrimitiveType(Names._byte, PrimitiveType.BYTE_CODE, 'B', Opcodes.ILOAD, Opcodes.BALOAD,
			ClassFormat.BOOLEAN);
	public static final PrimitiveType	SHORT	= new PrimitiveType(Names._short, PrimitiveType.SHORT_CODE, 'S', Opcodes.ILOAD, Opcodes.SALOAD,
			ClassFormat.SHORT);
	public static final PrimitiveType	CHAR	= new PrimitiveType(Names._char, PrimitiveType.CHAR_CODE, 'C', Opcodes.ILOAD, Opcodes.CALOAD, ClassFormat.CHAR);
	public static final PrimitiveType	INT		= new PrimitiveType(Names._int, PrimitiveType.INT_CODE, 'I', Opcodes.ILOAD, Opcodes.IALOAD, ClassFormat.INT);
	public static final PrimitiveType	LONG	= new PrimitiveType(Names._long, PrimitiveType.LONG_CODE, 'J', Opcodes.LLOAD, Opcodes.LALOAD, ClassFormat.LONG);
	public static final PrimitiveType	FLOAT	= new PrimitiveType(Names._float, PrimitiveType.FLOAT_CODE, 'F', Opcodes.FLOAD, Opcodes.FALOAD,
			ClassFormat.FLOAT);
	public static final PrimitiveType	DOUBLE	= new PrimitiveType(Names._double, PrimitiveType.DOUBLE_CODE, 'D', Opcodes.DLOAD, Opcodes.DALOAD,
			ClassFormat.DOUBLE);
			
	public static final DynamicType	DYNAMIC	= new DynamicType();
	public static final UnknownType	UNKNOWN	= new UnknownType();
	public static final NullType	NULL	= new NullType();
	public static final AnyType		ANY		= new AnyType();
	
	public static final ClassType	OBJECT				= new ClassType();
	public static final ClassType	STRING				= new ClassType();
	public static final ClassType	ITERABLE			= new ClassType();
	public static final ClassType	THROWABLE			= new ClassType();
	public static final ClassType	RUNTIME_EXCEPTION	= new ClassType();
	public static final ClassType	IMMUTABLE			= new ClassType();
	
	public static IClass	VOID_CLASS;
	public static IClass	BOOLEAN_CLASS;
	public static IClass	BYTE_CLASS;
	public static IClass	SHORT_CLASS;
	public static IClass	CHAR_CLASS;
	public static IClass	INT_CLASS;
	public static IClass	LONG_CLASS;
	public static IClass	FLOAT_CLASS;
	public static IClass	DOUBLE_CLASS;
	
	public static IClass	OBJECT_CLASS;
	public static IClass	STRING_CLASS;
	public static IClass	NULL_CLASS;
	public static IClass	ITERABLE_CLASS;
	public static IClass	THROWABLE_CLASS;
	public static IClass	RUNTIME_EXCEPTION_CLASS;
	public static IClass	IMMUTABLE_CLASS;
	
	public static IClass	INTRINSIC_CLASS;
	public static IClass	OVERRIDE_CLASS;
	public static IClass	MUTATING_CLASS;
	
	public static IClass	BOOLEAN_CONVERTIBLE_CLASS;
	public static IClass	CHAR_CONVERTIBLE_CLASS;
	public static IClass	INT_CONVERTIBLE_CLASS;
	public static IClass	LONG_CONVERTIBLE_CLASS;
	public static IClass	FLOAT_CONVERTIBLE_CLASS;
	public static IClass	DOUBLE_CONVERTIBLE_CLASS;
	public static IClass	STRING_CONVERTIBLE_CLASS;
	
	private static IClass	OBJECT_ARRAY_CLASS;
	public static IClass	OBJECT_SIMPLE_REF_CLASS;
	public static IClass	OBJECT_REF_CLASS;
	
	public static void initHeaders()
	{
		LANG_HEADER = Package.dyvil.resolveHeader("Lang");
	}
	
	public static void initTypes()
	{
		VOID.theClass = VOID_CLASS = Package.dyvilLang.resolveClass("Void");
		BOOLEAN.theClass = BOOLEAN_CLASS = Package.dyvilLang.resolveClass("Boolean");
		BYTE.theClass = BYTE_CLASS = Package.dyvilLang.resolveClass("Byte");
		SHORT.theClass = SHORT_CLASS = Package.dyvilLang.resolveClass("Short");
		CHAR.theClass = CHAR_CLASS = Package.dyvilLang.resolveClass("Char");
		INT.theClass = INT_CLASS = Package.dyvilLang.resolveClass("Int");
		LONG.theClass = LONG_CLASS = Package.dyvilLang.resolveClass("Long");
		FLOAT.theClass = FLOAT_CLASS = Package.dyvilLang.resolveClass("Float");
		DOUBLE.theClass = DOUBLE_CLASS = Package.dyvilLang.resolveClass("Double");
		
		NULL_CLASS = Package.dyvilLang.resolveClass("Null");
		OBJECT.theClass = OBJECT_CLASS = Package.javaLang.resolveClass("Object");
		STRING.theClass = STRING_CLASS = Package.javaLang.resolveClass("String");
		ITERABLE.theClass = ITERABLE_CLASS = Package.javaLang.resolveClass("Iterable");
		THROWABLE.theClass = THROWABLE_CLASS = Package.javaLang.resolveClass("Throwable");
		RUNTIME_EXCEPTION.theClass = RUNTIME_EXCEPTION_CLASS = Package.javaLang.resolveClass("RuntimeException");
		IMMUTABLE.theClass = IMMUTABLE_CLASS = Package.dyvilUtil.resolveClass("Immutable");
		
		INTRINSIC_CLASS = Package.dyvilAnnotation.resolveClass("Intrinsic");
		OVERRIDE_CLASS = Package.javaLang.resolveClass("Override");
		MUTATING_CLASS = Package.dyvilAnnotation.resolveClass("mutating");
		
		INT_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("IntConvertible");
		BOOLEAN_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("BooleanConvertible");
		CHAR_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("CharConvertible");
		LONG_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("LongConvertible");
		FLOAT_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("FloatConvertible");
		DOUBLE_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("DoubleConvertible");
		STRING_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("StringConvertible");
		
		VOID.boxMethod = VOID_CLASS.getBody().getMethod(Names.apply);
		VOID.unboxMethod = VOID_CLASS.getBody().getMethod(Names.unapply);
		BOOLEAN.boxMethod = BOOLEAN_CLASS.getBody().getMethod(Names.apply);
		BOOLEAN.unboxMethod = BOOLEAN_CLASS.getBody().getMethod(Names.unapply);
		BYTE.boxMethod = BYTE_CLASS.getBody().getMethod(Names.apply);
		BYTE.unboxMethod = BYTE_CLASS.getBody().getMethod(Names.unapply);
		SHORT.boxMethod = SHORT_CLASS.getBody().getMethod(Names.apply);
		SHORT.unboxMethod = SHORT_CLASS.getBody().getMethod(Names.unapply);
		CHAR.boxMethod = CHAR_CLASS.getBody().getMethod(Names.apply);
		CHAR.unboxMethod = CHAR_CLASS.getBody().getMethod(Names.unapply);
		INT.boxMethod = INT_CLASS.getBody().getMethod(Names.apply);
		INT.unboxMethod = INT_CLASS.getBody().getMethod(Names.unapply);
		LONG.boxMethod = LONG_CLASS.getBody().getMethod(Names.apply);
		LONG.unboxMethod = LONG_CLASS.getBody().getMethod(Names.unapply);
		FLOAT.boxMethod = FLOAT_CLASS.getBody().getMethod(Names.apply);
		FLOAT.unboxMethod = FLOAT_CLASS.getBody().getMethod(Names.unapply);
		DOUBLE.boxMethod = DOUBLE_CLASS.getBody().getMethod(Names.apply);
		DOUBLE.unboxMethod = DOUBLE_CLASS.getBody().getMethod(Names.unapply);
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
	
	protected static ReferenceType getRef(IType type)
	{
		if (OBJECT_REF_CLASS == null)
		{
			OBJECT_REF_CLASS = Package.dyvilLangRef.resolveClass("ObjectRef");
		}
		
		return new ReferenceType(OBJECT_REF_CLASS, type);
	}
	
	protected static IType getSimpleRef(IType type)
	{
		if (OBJECT_SIMPLE_REF_CLASS == null)
		{
			OBJECT_SIMPLE_REF_CLASS = Package.dyvilLangRefSimple.resolveClass("SimpleObjectRef");
		}
		
		ClassGenericType gt = new ClassGenericType(OBJECT_SIMPLE_REF_CLASS);
		gt.addType(type);
		return gt;
	}
	
	public static IType combine(IType type1, IType type2)
	{
		if (type1.equals(type2))
		{
			return type1;
		}
		if (type1 == Types.VOID || type2 == Types.VOID)
		{
			return Types.VOID;
		}
		if (type1.typeTag() == IType.NULL)
		{
			return type2.getObjectType();
		}
		if (type2.typeTag() == IType.NULL)
		{
			return type1.getObjectType();
		}
		
		Set<IType> types1 = superTypes(type1);
		Set<IType> types2 = superTypes(type2);
		
		for (IType t1 : types1)
		{
			IClass class1 = t1.getTheClass();
			if (class1 == Types.OBJECT_CLASS)
			{
				continue;
			}
			
			for (IType t2 : types2)
			{
				if (class1 == t2.getTheClass())
				{
					return new ClassType(class1);
				}
			}
		}
		
		return Types.ANY;
	}
	
	private static Set<IType> superTypes(IType type)
	{
		Set<IType> types = new ArraySet();
		addSuperTypes(type, types);
		return types;
	}
	
	private static void addSuperTypes(IType type, Collection<IType> types)
	{
		types.add(type);
		IType superType = type.getSuperType();
		if (superType != null)
		{
			addSuperTypes(superType.getConcreteType(type), types);
		}
		
		IClass iclass = type.getTheClass();
		int count = iclass.interfaceCount();
		for (int i = 0; i < count; i++)
		{
			addSuperTypes(iclass.getInterface(i).getConcreteType(type), types);
		}
	}
}
