package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.dynamic.DynamicType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassFormat;

public interface Types
{
	UnknownType		UNKNOWN			= new UnknownType();
	PrimitiveType	VOID			= new PrimitiveType(Name._void, 0);
	PrimitiveType	BOOLEAN			= new PrimitiveType(Name._boolean, ClassFormat.T_BOOLEAN);
	PrimitiveType	BYTE			= new PrimitiveType(Name._byte, ClassFormat.T_BOOLEAN);
	PrimitiveType	SHORT			= new PrimitiveType(Name._short, ClassFormat.T_SHORT);
	PrimitiveType	CHAR			= new PrimitiveType(Name._char, ClassFormat.T_CHAR);
	PrimitiveType	INT				= new PrimitiveType(Name._int, ClassFormat.T_INT);
	PrimitiveType	LONG			= new PrimitiveType(Name._long, ClassFormat.T_LONG);
	PrimitiveType	FLOAT			= new PrimitiveType(Name._float, ClassFormat.T_FLOAT);
	PrimitiveType	DOUBLE			= new PrimitiveType(Name._double, ClassFormat.T_DOUBLE);
	
	DynamicType		DYNAMIC			= new DynamicType();
	Type			ANY				= new Type("dyvil/lang/Any", Name.any);
	Type			OBJECT			= new Type("java/lang/Object", Name.getQualified("Object"));
	Type			STRING			= new Type("java/lang/String", Name.getQualified("String"));
	Type			CLASS			= new Type("java/lang/Class", Name.getQualified("Class"));
	Type			PREDEF			= new Type("dyvil/lang/Predef", Name.getQualified("Predef"));
	Type			ARRAY			= new Type("dyvil/lang/Array", Name.getQualified("Array"));
	
	Type			AIntrinsic		= new Type("dyvil/lang/annotation/Intrinsic", Name.getQualified("Intrinsic"));
	Type			AOverride		= new Type("java/lang/Override", Name.getQualified("Override"));
	Type			ARetention		= new Type("java/lang/annotation/Retention", Name.getQualified("Retention"));
	Type			ATarget			= new Type("java/lang/annotation/Target", Name.getQualified("Target"));
	
	IClass			VOID_CLASS		= Package.dyvilLang.resolveClass("Void");
	IClass			BOOLEAN_CLASS	= Package.dyvilLang.resolveClass("Boolean");
	IClass			BYTE_CLASS		= Package.dyvilLang.resolveClass("Byte");
	IClass			SHORT_CLASS		= Package.dyvilLang.resolveClass("Short");
	IClass			CHAR_CLASS		= Package.dyvilLang.resolveClass("Char");
	IClass			INT_CLASS		= Package.dyvilLang.resolveClass("Int");
	IClass			LONG_CLASS		= Package.dyvilLang.resolveClass("Long");
	IClass			FLOAT_CLASS		= Package.dyvilLang.resolveClass("Float");
	IClass			DOUBLE_CLASS	= Package.dyvilLang.resolveClass("Double");
	
	IClass			OBJECT_CLASS	= Package.javaLang.resolveClass("Object");
	IClass			STRING_CLASS	= Package.javaLang.resolveClass("String");
	IClass			CLASS_CLASS		= Package.javaLang.resolveClass("Class");
	IClass			ARRAY_CLASS		= Package.dyvilLang.resolveClass("Array");
	IClass			PREDEF_CLASS	= Package.dyvilLang.resolveClass("Predef");
	
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
		ARRAY.theClass = ARRAY_CLASS;
		PREDEF.theClass = PREDEF_CLASS;
		ARRAY.theClass = ARRAY_CLASS;
		
		ATarget.theClass = Package.javaLangAnnotation.resolveClass("Target");
		ARetention.theClass = Package.javaLangAnnotation.resolveClass("Retention");
		AOverride.theClass = Package.javaLang.resolveClass("Override");
		AIntrinsic.theClass = Package.dyvilLangAnnotation.resolveClass("Intrinsic");
	}
}
