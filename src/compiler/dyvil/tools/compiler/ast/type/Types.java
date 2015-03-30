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
	Type			ANY				= new Type("Any");
	Type			OBJECT			= new Type("Object");
	Type			PREDEF			= new Type("Predef");
	Type			ARRAY			= new Type("Array");
	Type			STRING			= new Type("String");
	Type			CLASS			= new Type("Class");
	
	AnnotationType	AIntrinsic		= new AnnotationType("Intrinsic");
	AnnotationType	AOverride		= new AnnotationType("Override");
	AnnotationType	ARetention		= new AnnotationType("Retention");
	AnnotationType	ATarget			= new AnnotationType("Target");
	
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
		Types.BOOLEAN.theClass = Types.BOOLEAN_CLASS;
		Types.BOOLEAN.boxMethod = Types.BOOLEAN_CLASS.getBody().getMethod(Name.apply);
		Types.BOOLEAN.unboxMethod = Types.BOOLEAN_CLASS.getBody().getMethod(Name.unapply);
		Types.BYTE.theClass = Types.BYTE_CLASS;
		Types.BYTE.boxMethod = Types.BYTE_CLASS.getBody().getMethod(Name.apply);
		Types.BYTE.unboxMethod = Types.BYTE_CLASS.getBody().getMethod(Name.unapply);
		Types.SHORT.theClass = Types.SHORT_CLASS;
		Types.SHORT.boxMethod = Types.SHORT_CLASS.getBody().getMethod(Name.apply);
		Types.SHORT.unboxMethod = Types.SHORT_CLASS.getBody().getMethod(Name.unapply);
		Types.CHAR.theClass = Types.CHAR_CLASS;
		Types.CHAR.boxMethod = Types.CHAR_CLASS.getBody().getMethod(Name.apply);
		Types.CHAR.unboxMethod = Types.CHAR_CLASS.getBody().getMethod(Name.unapply);
		Types.INT.theClass = Types.INT_CLASS;
		Types.INT.boxMethod = Types.INT_CLASS.getBody().getMethod(Name.apply);
		Types.INT.unboxMethod = Types.INT_CLASS.getBody().getMethod(Name.unapply);
		Types.LONG.theClass = Types.LONG_CLASS;
		Types.LONG.boxMethod = Types.LONG_CLASS.getBody().getMethod(Name.apply);
		Types.LONG.unboxMethod = Types.LONG_CLASS.getBody().getMethod(Name.unapply);
		Types.FLOAT.theClass = Types.FLOAT_CLASS;
		Types.FLOAT.boxMethod = Types.FLOAT_CLASS.getBody().getMethod(Name.apply);
		Types.FLOAT.unboxMethod = Types.FLOAT_CLASS.getBody().getMethod(Name.unapply);
		Types.DOUBLE.theClass = Types.DOUBLE_CLASS;
		Types.DOUBLE.boxMethod = Types.DOUBLE_CLASS.getBody().getMethod(Name.apply);
		Types.DOUBLE.unboxMethod = Types.DOUBLE_CLASS.getBody().getMethod(Name.unapply);
		
		Types.ANY.theClass = Types.OBJECT_CLASS;
		Types.ANY.fullName = "dyvil.lang.Any";
		Types.OBJECT.theClass = Types.OBJECT_CLASS;
		Types.OBJECT.fullName = "java.lang.Object";
		Types.STRING.theClass = Types.STRING_CLASS;
		Types.STRING.fullName = "java.lang.String";
		Types.CLASS.theClass = Types.CLASS_CLASS;
		Types.CLASS.fullName = "java.lang.Class";
		Types.ARRAY.theClass = Types.ARRAY_CLASS;
		Types.ARRAY.fullName = "dyvil.lang.Array";
		Types.PREDEF.theClass = Types.PREDEF_CLASS;
		Types.PREDEF.fullName = "dyvil.lang.Predef";
		
		Types.AIntrinsic.theClass = Package.dyvilLangAnnotation.resolveClass("Intrinsic");
		Types.AOverride.theClass = Package.javaLang.resolveClass("Override");
		Types.ARetention.theClass = Package.javaLangAnnotation.resolveClass("Retention");
		Types.ATarget.theClass = Package.javaLangAnnotation.resolveClass("Target");
	}
}
