package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.dynamic.DynamicType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassFormat;

public final class Types
{
	public static final PrimitiveType	VOID					= new PrimitiveType(Name._void, 0);
	public static final PrimitiveType	BOOLEAN					= new PrimitiveType(Name._boolean, ClassFormat.T_BOOLEAN);
	public static final PrimitiveType	BYTE					= new PrimitiveType(Name._byte, ClassFormat.T_BYTE);
	public static final PrimitiveType	SHORT					= new PrimitiveType(Name._short, ClassFormat.T_SHORT);
	public static final PrimitiveType	CHAR					= new PrimitiveType(Name._char, ClassFormat.T_CHAR);
	public static final PrimitiveType	INT						= new PrimitiveType(Name._int, ClassFormat.T_INT);
	public static final PrimitiveType	LONG					= new PrimitiveType(Name._long, ClassFormat.T_LONG);
	public static final PrimitiveType	FLOAT					= new PrimitiveType(Name._float, ClassFormat.T_FLOAT);
	public static final PrimitiveType	DOUBLE					= new PrimitiveType(Name._double, ClassFormat.T_DOUBLE);
	
	public static final DynamicType		DYNAMIC					= new DynamicType();
	public static final UnknownType		UNKNOWN					= new UnknownType();
	public static final NullType		NULL					= new NullType();
	public static final AnyType			ANY						= new AnyType();
	
	public static final IClass			VOID_CLASS				= Package.dyvilLang.resolveClass("Void");
	public static final IClass			BOOLEAN_CLASS			= Package.dyvilLang.resolveClass("Boolean");
	public static final IClass			BYTE_CLASS				= Package.dyvilLang.resolveClass("Byte");
	public static final IClass			SHORT_CLASS				= Package.dyvilLang.resolveClass("Short");
	public static final IClass			CHAR_CLASS				= Package.dyvilLang.resolveClass("Char");
	public static final IClass			INT_CLASS				= Package.dyvilLang.resolveClass("Int");
	public static final IClass			LONG_CLASS				= Package.dyvilLang.resolveClass("Long");
	public static final IClass			FLOAT_CLASS				= Package.dyvilLang.resolveClass("Float");
	public static final IClass			DOUBLE_CLASS			= Package.dyvilLang.resolveClass("Double");
	
	public static final IClass			OBJECT_CLASS			= Package.javaLang.resolveClass("Object");
	public static final IClass			STRING_CLASS			= Package.javaLang.resolveClass("String");
	public static final IClass			CLASS_CLASS				= Package.javaLang.resolveClass("Class");
	public static final IClass			PREDEF_CLASS			= Package.dyvilLang.resolveClass("Predef");
	public static final IClass			TYPE_CLASS				= Package.dyvilLang.resolveClass("Type");
	public static final IClass			ITERABLE_CLASS			= Package.javaLang.resolveClass("Iterable");
	public static final IClass			THROWABLE_CLASS			= Package.javaLang.resolveClass("Throwable");
	public static final IClass			RUNTIME_EXCEPTION_CLASS	= Package.javaLang.resolveClass("RuntimeException");
	
	public static final IClass			MAP_CLASS				= Package.dyvilLang.resolveClass("Map");
	public static final IClass			TUPLE2_CLASS			= TupleType.getTupleClass(2);
	
	public static final IClass			INTRINSIC_CLASS			= Package.dyvilAnnotation.resolveClass("Intrinsic");
	public static final IClass			OVERRIDE_CLASS			= Package.javaLang.resolveClass("Override");
	public static final IClass			RETENTION_CLASS			= Package.javaLangAnnotation.resolveClass("Retention");
	public static final IClass			TARGET_CLASS			= Package.javaLangAnnotation.resolveClass("Target");
	public static final IClass			MUTATING_CLASS			= Package.dyvilAnnotation.resolveClass("mutating");
	
	public static final ClassType		OBJECT					= new ClassType(OBJECT_CLASS);
	public static final ClassType		STRING					= new ClassType(STRING_CLASS);
	public static final ClassType		CLASS					= new ClassType(CLASS_CLASS);
	public static final ClassType		PREDEF					= new ClassType(PREDEF_CLASS);
	public static final ClassType		TYPE					= new ClassType(TYPE_CLASS);
	public static final IType			ITERABLE				= new ClassType(ITERABLE_CLASS);
	public static final ClassType		THROWABLE				= new ClassType(THROWABLE_CLASS);
	public static final ClassType		RUNTIME_EXCEPTION		= new ClassType(RUNTIME_EXCEPTION_CLASS);
	public static final ClassType		MAP						= new ClassType(MAP_CLASS);
	public static final ClassType		IMMUTABLE				= new ClassType(Package.dyvilLang.resolveClass("Immutable"));
	
	private static IClass				OBJECT_ARRAY_CLASS;
	private static IClass				BOOLEAN_ARRAY_CLASS;
	private static IClass				BYTE_ARRAY_CLASS;
	private static IClass				SHORT_ARRAY_CLASS;
	private static IClass				CHAR_ARRAY_CLASS;
	private static IClass				INT_ARRAY_CLASS;
	private static IClass				LONG_ARRAY_CLASS;
	private static IClass				FLOAT_ARRAY_CLASS;
	private static IClass				DOUBLE_ARRAY_CLASS;
	
	public static IClass				OBJECT_REF_CLASS;
	private static ClassType			BOOLEAN_REF;
	private static ClassType			BYTE_REF;
	private static ClassType			SHORT_REF;
	private static ClassType			CHAR_REF;
	private static ClassType			INT_REF;
	private static ClassType			LONG_REF;
	private static ClassType			FLOAT_REF;
	private static ClassType			DOUBLE_REF;
	
	public static void init()
	{
		VOID.theClass = VOID_CLASS;
		VOID.boxMethod = VOID_CLASS.getBody().getMethod(Name.apply);
		VOID.unboxMethod = VOID_CLASS.getBody().getMethod(Name.unapply);
		BOOLEAN.theClass = BOOLEAN_CLASS;
		BOOLEAN.boxMethod = BOOLEAN_CLASS.getBody().getMethod(Name.apply);
		BOOLEAN.unboxMethod = BOOLEAN_CLASS.getBody().getMethod(Name.unapply);
		BYTE.theClass = BYTE_CLASS;
		BYTE.boxMethod = BYTE_CLASS.getBody().getMethod(Name.apply);
		BYTE.unboxMethod = BYTE_CLASS.getBody().getMethod(Name.unapply);
		SHORT.theClass = SHORT_CLASS;
		SHORT.boxMethod = SHORT_CLASS.getBody().getMethod(Name.apply);
		SHORT.unboxMethod = SHORT_CLASS.getBody().getMethod(Name.unapply);
		CHAR.theClass = CHAR_CLASS;
		CHAR.boxMethod = CHAR_CLASS.getBody().getMethod(Name.apply);
		CHAR.unboxMethod = CHAR_CLASS.getBody().getMethod(Name.unapply);
		INT.theClass = INT_CLASS;
		INT.boxMethod = INT_CLASS.getBody().getMethod(Name.apply);
		INT.unboxMethod = INT_CLASS.getBody().getMethod(Name.unapply);
		LONG.theClass = LONG_CLASS;
		LONG.boxMethod = LONG_CLASS.getBody().getMethod(Name.apply);
		LONG.unboxMethod = LONG_CLASS.getBody().getMethod(Name.unapply);
		FLOAT.theClass = FLOAT_CLASS;
		FLOAT.boxMethod = FLOAT_CLASS.getBody().getMethod(Name.apply);
		FLOAT.unboxMethod = FLOAT_CLASS.getBody().getMethod(Name.unapply);
		DOUBLE.theClass = DOUBLE_CLASS;
		DOUBLE.boxMethod = DOUBLE_CLASS.getBody().getMethod(Name.apply);
		DOUBLE.unboxMethod = DOUBLE_CLASS.getBody().getMethod(Name.unapply);
	}
	
	public static IType fromASMType(org.objectweb.asm.Type type)
	{
		switch (type.getSort())
		{
		case org.objectweb.asm.Type.VOID:
			return VOID;
		case org.objectweb.asm.Type.BOOLEAN:
			return BOOLEAN;
		case org.objectweb.asm.Type.BYTE:
			return BYTE;
		case org.objectweb.asm.Type.SHORT:
			return SHORT;
		case org.objectweb.asm.Type.CHAR:
			return CHAR;
		case org.objectweb.asm.Type.INT:
			return INT;
		case org.objectweb.asm.Type.LONG:
			return LONG;
		case org.objectweb.asm.Type.FLOAT:
			return FLOAT;
		case org.objectweb.asm.Type.DOUBLE:
			return DOUBLE;
		case org.objectweb.asm.Type.OBJECT:
			return new InternalType(type.getInternalName());
		case org.objectweb.asm.Type.ARRAY:
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
	
	public static IClass getPrimitiveArray(int typecode)
	{
		switch (typecode)
		{
		case ClassFormat.T_BOOLEAN:
			if (BOOLEAN_ARRAY_CLASS == null)
			{
				return BOOLEAN_ARRAY_CLASS = Package.dyvilArray.resolveClass("BooleanArray");
			}
			return BOOLEAN_ARRAY_CLASS;
		case ClassFormat.T_BYTE:
			if (BYTE_ARRAY_CLASS == null)
			{
				return BYTE_ARRAY_CLASS = Package.dyvilArray.resolveClass("ByteArray");
			}
			return BYTE_ARRAY_CLASS;
		case ClassFormat.T_SHORT:
			if (SHORT_ARRAY_CLASS == null)
			{
				return SHORT_ARRAY_CLASS = Package.dyvilArray.resolveClass("ShortArray");
			}
			return SHORT_ARRAY_CLASS;
		case ClassFormat.T_CHAR:
			if (CHAR_ARRAY_CLASS == null)
			{
				return CHAR_ARRAY_CLASS = Package.dyvilArray.resolveClass("CharArray");
			}
			return CHAR_ARRAY_CLASS;
		case ClassFormat.T_INT:
			if (INT_ARRAY_CLASS == null)
			{
				return INT_ARRAY_CLASS = Package.dyvilArray.resolveClass("IntArray");
			}
			return INT_ARRAY_CLASS;
		case ClassFormat.T_LONG:
			if (LONG_ARRAY_CLASS == null)
			{
				return LONG_ARRAY_CLASS = Package.dyvilArray.resolveClass("LongArray");
			}
			return LONG_ARRAY_CLASS;
		case ClassFormat.T_FLOAT:
			if (FLOAT_ARRAY_CLASS == null)
			{
				return FLOAT_ARRAY_CLASS = Package.dyvilArray.resolveClass("FloatArray");
			}
			return FLOAT_ARRAY_CLASS;
		case ClassFormat.T_DOUBLE:
			if (DOUBLE_ARRAY_CLASS == null)
			{
				return DOUBLE_ARRAY_CLASS = Package.dyvilArray.resolveClass("DoubleArray");
			}
			return DOUBLE_ARRAY_CLASS;
		}
		return getObjectArray();
	}
	
	private static IType getPrimitiveRefType(int typecode)
	{
		switch (typecode)
		{
		case ClassFormat.T_BOOLEAN:
			if (BOOLEAN_REF == null)
			{
				return BOOLEAN_REF = new ClassType(Package.dyvilLangRefSimple.resolveClass("SimpleBooleanRef"));
			}
			return BOOLEAN_REF;
		case ClassFormat.T_BYTE:
			if (BYTE_REF == null)
			{
				return BYTE_REF = new ClassType(Package.dyvilLangRefSimple.resolveClass("SimpleByteRef"));
			}
			return BYTE_REF;
		case ClassFormat.T_SHORT:
			if (SHORT_REF == null)
			{
				return SHORT_REF = new ClassType(Package.dyvilLangRefSimple.resolveClass("SimpleShortRef"));
			}
			return SHORT_REF;
		case ClassFormat.T_CHAR:
			if (CHAR_REF == null)
			{
				return CHAR_REF = new ClassType(Package.dyvilLangRefSimple.resolveClass("SimpleCharRef"));
			}
			return CHAR_REF;
		case ClassFormat.T_INT:
			if (INT_REF == null)
			{
				return INT_REF = new ClassType(Package.dyvilLangRefSimple.resolveClass("SimpleIntRef"));
			}
			return INT_REF;
		case ClassFormat.T_LONG:
			if (LONG_REF == null)
			{
				return LONG_REF = new ClassType(Package.dyvilLangRefSimple.resolveClass("SimpleLongRef"));
			}
			return LONG_REF;
		case ClassFormat.T_FLOAT:
			if (FLOAT_REF == null)
			{
				return FLOAT_REF = new ClassType(Package.dyvilLangRefSimple.resolveClass("SimpleFloatRef"));
			}
			return FLOAT_REF;
		case ClassFormat.T_DOUBLE:
			if (DOUBLE_REF == null)
			{
				return DOUBLE_REF = new ClassType(Package.dyvilLangRefSimple.resolveClass("SimpleDoubleRef"));
			}
			return DOUBLE_REF;
		}
		return null;
	}
	
	public static IType getRefType(IType type)
	{
		switch (type.typeTag())
		{
		case IType.PRIMITIVE:
			return getPrimitiveRefType(((PrimitiveType) type).typecode);
		default:
			if (OBJECT_REF_CLASS == null)
			{
				OBJECT_REF_CLASS = Package.dyvilLangRefSimple.resolveClass("SimpleObjectRef");
			}
			GenericType gt = new GenericType(OBJECT_REF_CLASS);
			gt.addType(type);
			return gt;
		}
	}
	
	public static IType findCommonSuperType(IType type1, IType type2)
	{
		IType t = superType(type1, type2);
		if (t != null)
		{
			return t;
		}
		
		IType superType1 = type1;
		while (true)
		{
			superType1 = superType1.getSuperType();
			if (superType1 == null)
			{
				break;
			}
			
			IType superType2 = type2;
			while (true)
			{
				superType2 = superType2.getSuperType();
				if (superType2 == null)
				{
					break;
				}
				
				t = superType(superType1, superType2);
				if (t != null)
				{
					return t;
				}
			}
		}
		return ANY;
	}
	
	static IType superType(IType type1, IType type2)
	{
		if (type1.isSuperTypeOf(type2))
		{
			return type1;
		}
		if (type2.isSuperTypeOf(type1))
		{
			return type2;
		}
		return null;
	}
}
