package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.dynamic.DynamicType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassFormat;

public final class Types
{
	public static final UnknownType		UNKNOWN				= new UnknownType();
	public static final PrimitiveType	VOID				= new PrimitiveType(Name._void, 0);
	public static final PrimitiveType	BOOLEAN				= new PrimitiveType(Name._boolean, ClassFormat.T_BOOLEAN);
	public static final PrimitiveType	BYTE				= new PrimitiveType(Name._byte, ClassFormat.T_BOOLEAN);
	public static final PrimitiveType	SHORT				= new PrimitiveType(Name._short, ClassFormat.T_SHORT);
	public static final PrimitiveType	CHAR				= new PrimitiveType(Name._char, ClassFormat.T_CHAR);
	public static final PrimitiveType	INT					= new PrimitiveType(Name._int, ClassFormat.T_INT);
	public static final PrimitiveType	LONG				= new PrimitiveType(Name._long, ClassFormat.T_LONG);
	public static final PrimitiveType	FLOAT				= new PrimitiveType(Name._float, ClassFormat.T_FLOAT);
	public static final PrimitiveType	DOUBLE				= new PrimitiveType(Name._double, ClassFormat.T_DOUBLE);
	
	public static final DynamicType		DYNAMIC				= new DynamicType();
	public static final Type			ANY					= new Type("dyvil/lang/Any", Name.any);
	public static final Type			OBJECT				= new Type("java/lang/Object", Name.getQualified("Object"));
	public static final Type			STRING				= new Type("java/lang/String", Name.getQualified("String"));
	public static final Type			CLASS				= new Type("java/lang/Class", Name.getQualified("Class"));
	public static final Type			PREDEF				= new Type("dyvil/lang/Predef", Name.getQualified("Predef"));
	
	public static final Type			MAP					= new Type("dyvil/lang/Map", Name.getQualified("Map"));
	
	public static final Type			AIntrinsic			= new Type("dyvil/annotation/Intrinsic", Name.getQualified("Intrinsic"));
	public static final Type			AOverride			= new Type("java/lang/Override", Name.getQualified("Override"));
	public static final Type			ARetention			= new Type("java/lang/annotation/Retention", Name.getQualified("Retention"));
	public static final Type			ATarget				= new Type("java/lang/annotation/Target", Name.getQualified("Target"));
	
	public static final IClass			VOID_CLASS			= Package.dyvilLang.resolveClass("Void");
	public static final IClass			BOOLEAN_CLASS		= Package.dyvilLang.resolveClass("Boolean");
	public static final IClass			BYTE_CLASS			= Package.dyvilLang.resolveClass("Byte");
	public static final IClass			SHORT_CLASS			= Package.dyvilLang.resolveClass("Short");
	public static final IClass			CHAR_CLASS			= Package.dyvilLang.resolveClass("Char");
	public static final IClass			INT_CLASS			= Package.dyvilLang.resolveClass("Int");
	public static final IClass			LONG_CLASS			= Package.dyvilLang.resolveClass("Long");
	public static final IClass			FLOAT_CLASS			= Package.dyvilLang.resolveClass("Float");
	public static final IClass			DOUBLE_CLASS		= Package.dyvilLang.resolveClass("Double");
	
	public static final IClass			OBJECT_CLASS		= Package.javaLang.resolveClass("Object");
	public static final IClass			STRING_CLASS		= Package.javaLang.resolveClass("String");
	public static final IClass			CLASS_CLASS			= Package.javaLang.resolveClass("Class");
	public static final IClass			PREDEF_CLASS		= Package.dyvilLang.resolveClass("Predef");
	
	public static final IClass			MAP_CLASS			= Package.dyvilLang.resolveClass("Map");
	public static final IClass			TUPLE2_CLASS		= TupleType.getTupleClass(2);
	
	private static IClass				OBJECT_ARRAY_CLASS;
	private static IClass				BOOLEAN_ARRAY_CLASS;
	private static IClass				BYTE_ARRAY_CLASS;
	private static IClass				SHORT_ARRAY_CLASS;
	private static IClass				CHAR_ARRAY_CLASS;
	private static IClass				INT_ARRAY_CLASS;
	private static IClass				LONG_ARRAY_CLASS;
	private static IClass				FLOAT_ARRAY_CLASS;
	private static IClass				DOUBLE_ARRAY_CLASS;
	
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
		
		MAP.theClass = MAP_CLASS;
		
		ATarget.theClass = Package.javaLangAnnotation.resolveClass("Target");
		ARetention.theClass = Package.javaLangAnnotation.resolveClass("Retention");
		AOverride.theClass = Package.javaLang.resolveClass("Override");
		AIntrinsic.theClass = Package.dyvilAnnotation.resolveClass("Intrinsic");
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
}
