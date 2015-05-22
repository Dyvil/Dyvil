package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.dynamic.DynamicType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassFormat;

public final class Types
{
	public static final UnknownType		UNKNOWN					= new UnknownType();
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
	public static final Type			ANY						= new Type("java/lang/Object", Name.any);
	public static final Type			OBJECT					= new Type("java/lang/Object", Name.getQualified("Object"));
	public static final Type			STRING					= new Type("java/lang/String", Name.getQualified("String"));
	public static final Type			CLASS					= new Type("java/lang/Class", Name.getQualified("Class"));
	public static final Type			PREDEF					= new Type("dyvil/lang/Predef", Name.getQualified("Predef"));
	public static final Type			TYPE					= new Type("dyvil/lang/Type", Name.getQualified("Type"));
	public static final Type			THROWABLE				= new Type("java/lang/Throwable", Name.getQualified("Throwable"));
	public static final Type			RUNTIME_EXCEPTION		= new Type("java/lang/RuntimeException", Name.getQualified("RuntimeException"));
	
	public static final Type			MAP						= new Type("dyvil/lang/Map", Name.getQualified("Map"));
	public static final Type			IMMUTABLE				= new Type(Package.dyvilLang.resolveClass("Immutable"));
	
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
	public static final IClass			THROWABLE_CLASS			= Package.javaLang.resolveClass("Throwable");
	public static final IClass			RUNTIME_EXCEPTION_CLASS	= Package.javaLang.resolveClass("RuntimeException");
	
	public static final IClass			MAP_CLASS				= Package.dyvilLang.resolveClass("Map");
	public static final IClass			TUPLE2_CLASS			= TupleType.getTupleClass(2);
	
	public static final IClass			INTRINSIC_CLASS			= Package.dyvilAnnotation.resolveClass("Intrinsic");
	public static final IClass			OVERRIDE_CLASS			= Package.javaLang.resolveClass("Override");
	public static final IClass			RETENTION_CLASS			= Package.javaLangAnnotation.resolveClass("Retention");
	public static final IClass			TARGET_CLASS			= Package.javaLangAnnotation.resolveClass("Target");
	public static final IClass			MUTATING_CLASS			= Package.dyvilAnnotation.resolveClass("mutating");
	
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
	private static Type					BOOLEAN_REF;
	private static Type					BYTE_REF;
	private static Type					SHORT_REF;
	private static Type					CHAR_REF;
	private static Type					INT_REF;
	private static Type					LONG_REF;
	private static Type					FLOAT_REF;
	private static Type					DOUBLE_REF;
	
	public static void init()
	{
		VOID.theClass = VOID_CLASS;
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
		
		ANY.theClass = OBJECT_CLASS;
		OBJECT.theClass = OBJECT_CLASS;
		STRING.theClass = STRING_CLASS;
		CLASS.theClass = CLASS_CLASS;
		PREDEF.theClass = PREDEF_CLASS;
		TYPE.theClass = TYPE_CLASS;
		THROWABLE.theClass = THROWABLE_CLASS;
		RUNTIME_EXCEPTION.theClass = RUNTIME_EXCEPTION_CLASS;
		
		MAP.theClass = MAP_CLASS;
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
				return BOOLEAN_REF = new Type(Package.dyvilLangRef.resolveClass("BooleanRef"));
			}
			return BOOLEAN_REF;
		case ClassFormat.T_BYTE:
			if (BYTE_REF == null)
			{
				return BYTE_REF = new Type(Package.dyvilLangRef.resolveClass("ByteRef"));
			}
			return BYTE_REF;
		case ClassFormat.T_SHORT:
			if (SHORT_REF == null)
			{
				return SHORT_REF = new Type(Package.dyvilLangRef.resolveClass("ShortRef"));
			}
			return SHORT_REF;
		case ClassFormat.T_CHAR:
			if (CHAR_REF == null)
			{
				return CHAR_REF = new Type(Package.dyvilLangRef.resolveClass("CharRef"));
			}
			return CHAR_REF;
		case ClassFormat.T_INT:
			if (INT_REF == null)
			{
				return INT_REF = new Type(Package.dyvilLangRef.resolveClass("IntRef"));
			}
			return INT_REF;
		case ClassFormat.T_LONG:
			if (LONG_REF == null)
			{
				return LONG_REF = new Type(Package.dyvilLangRef.resolveClass("LongRef"));
			}
			return LONG_REF;
		case ClassFormat.T_FLOAT:
			if (FLOAT_REF == null)
			{
				return FLOAT_REF = new Type(Package.dyvilLangRef.resolveClass("FloatRef"));
			}
			return FLOAT_REF;
		case ClassFormat.T_DOUBLE:
			if (DOUBLE_REF == null)
			{
				return DOUBLE_REF = new Type(Package.dyvilLangRef.resolveClass("DoubleRef"));
			}
			return DOUBLE_REF;
		}
		return null;
	}
	
	public static IType getRefType(IType type)
	{
		switch (type.typeTag())
		{
		case IType.PRIMITIVE_TYPE:
			return getPrimitiveRefType(((PrimitiveType) type).typecode);
		default:
			if (OBJECT_REF_CLASS == null)
			{
				OBJECT_REF_CLASS = Package.dyvilLangRef.resolveClass("ObjectRef");
			}
			GenericType gt = new GenericType(OBJECT_REF_CLASS);
			gt.addType(type);
			return gt;
		}
	}
}
