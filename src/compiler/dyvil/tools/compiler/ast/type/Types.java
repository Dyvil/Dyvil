package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.dynamic.DynamicType;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassFormat;

public final class Types
{
	public static final PrimitiveType		VOID					= new PrimitiveType(Name._void, 0);
	public static final PrimitiveType		BOOLEAN					= new PrimitiveType(Name._boolean, ClassFormat.T_BOOLEAN);
	public static final PrimitiveType		BYTE					= new PrimitiveType(Name._byte, ClassFormat.T_BYTE);
	public static final PrimitiveType		SHORT					= new PrimitiveType(Name._short, ClassFormat.T_SHORT);
	public static final PrimitiveType		CHAR					= new PrimitiveType(Name._char, ClassFormat.T_CHAR);
	public static final PrimitiveType		INT						= new PrimitiveType(Name._int, ClassFormat.T_INT);
	public static final PrimitiveType		LONG					= new PrimitiveType(Name._long, ClassFormat.T_LONG);
	public static final PrimitiveType		FLOAT					= new PrimitiveType(Name._float, ClassFormat.T_FLOAT);
	public static final PrimitiveType		DOUBLE					= new PrimitiveType(Name._double, ClassFormat.T_DOUBLE);
	
	public static final DynamicType			DYNAMIC					= new DynamicType();
	public static final UnknownType			UNKNOWN					= new UnknownType();
	public static final NullType			NULL					= new NullType();
	public static final AnyType				ANY						= new AnyType();
	
	public static final IClass				VOID_CLASS				= Package.dyvilLang.resolveClass("Void");
	public static final IClass				BOOLEAN_CLASS			= Package.dyvilLang.resolveClass("Boolean");
	public static final IClass				BYTE_CLASS				= Package.dyvilLang.resolveClass("Byte");
	public static final IClass				SHORT_CLASS				= Package.dyvilLang.resolveClass("Short");
	public static final IClass				CHAR_CLASS				= Package.dyvilLang.resolveClass("Char");
	public static final IClass				INT_CLASS				= Package.dyvilLang.resolveClass("Int");
	public static final IClass				LONG_CLASS				= Package.dyvilLang.resolveClass("Long");
	public static final IClass				FLOAT_CLASS				= Package.dyvilLang.resolveClass("Float");
	public static final IClass				DOUBLE_CLASS			= Package.dyvilLang.resolveClass("Double");
	
	public static final IClass				OBJECT_CLASS			= Package.javaLang.resolveClass("Object");
	public static final IClass				STRING_CLASS			= Package.javaLang.resolveClass("String");
	public static final IClass				CLASS_CLASS				= Package.javaLang.resolveClass("Class");
	public static final IClass				PREDEF_CLASS			= Package.dyvilLang.resolveClass("Predef");
	public static final IClass				TYPE_CLASS				= Package.dyvilLang.resolveClass("Type");
	public static final IClass				ITERABLE_CLASS			= Package.javaLang.resolveClass("Iterable");
	public static final IClass				THROWABLE_CLASS			= Package.javaLang.resolveClass("Throwable");
	public static final IClass				RUNTIME_EXCEPTION_CLASS	= Package.javaLang.resolveClass("RuntimeException");
	
	public static final IClass				MAP_CLASS				= Package.dyvilLang.resolveClass("Map");
	public static final IClass				TUPLE2_CLASS			= TupleType.getTupleClass(2);
	
	public static final IClass				INTRINSIC_CLASS			= Package.dyvilAnnotation.resolveClass("Intrinsic");
	public static final IClass				OVERRIDE_CLASS			= Package.javaLang.resolveClass("Override");
	public static final IClass				RETENTION_CLASS			= Package.javaLangAnnotation.resolveClass("Retention");
	public static final IClass				TARGET_CLASS			= Package.javaLangAnnotation.resolveClass("Target");
	public static final IClass				MUTATING_CLASS			= Package.dyvilAnnotation.resolveClass("mutating");
	
	public static final ClassType			OBJECT					= new ClassType(OBJECT_CLASS);
	public static final ClassType			STRING					= new ClassType(STRING_CLASS);
	public static final ClassType			CLASS					= new ClassType(CLASS_CLASS);
	public static final ClassType			PREDEF					= new ClassType(PREDEF_CLASS);
	public static final ClassType			TYPE					= new ClassType(TYPE_CLASS);
	public static final IType				ITERABLE				= new ClassType(ITERABLE_CLASS);
	public static final ClassType			THROWABLE				= new ClassType(THROWABLE_CLASS);
	public static final ClassType			RUNTIME_EXCEPTION		= new ClassType(RUNTIME_EXCEPTION_CLASS);
	public static final ClassType			MAP						= new ClassType(MAP_CLASS);
	public static final ClassType			IMMUTABLE				= new ClassType(Package.dyvilLang.resolveClass("Immutable"));
	
	private static IClass					OBJECT_ARRAY_CLASS;
	private static final IClass[]			PRIMITIVE_ARRAY_CLASS	= new IClass[16];
	
	public static IClass					OBJECT_SIMPLE_REF_CLASS;
	private static final ClassType[]		PRIMITIVE_SIMPLE_REF	= new ClassType[16];
	
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
	
	public static IClass getPrimitiveArray(PrimitiveType type)
	{
		IClass iclass = PRIMITIVE_ARRAY_CLASS[type.typecode];
		if (iclass == null)
		{
			String className = type.theClass.getName().qualified + "Array";
			return PRIMITIVE_ARRAY_CLASS[type.typecode] = Package.dyvilArray.resolveClass(className);
		}
		return iclass;
	}
	
	private static ReferenceType getPrimitiveRef(PrimitiveType type)
	{
		ReferenceType itype = PRIMITIVE_REF[type.typecode];
		if (itype == null)
		{
			String className = type.theClass.getName().qualified + "Ref";
			return PRIMITIVE_REF[type.typecode] = new ReferenceType(Package.dyvilLangRef.resolveClass(className), type);
		}
		return itype;
	}
	
	{
		{
		}
	}
	public static IType getSimpleRef(IType type)
	{
		if (type.isPrimitive())
		{
			return getPrimitiveSimpleRef((PrimitiveType) type);
		}
		
		if (OBJECT_SIMPLE_REF_CLASS == null)
		{
			OBJECT_SIMPLE_REF_CLASS = Package.dyvilLangRefSimple.resolveClass("SimpleObjectRef");
		}
		
		ClassGenericType gt = new ClassGenericType(OBJECT_SIMPLE_REF_CLASS);
		gt.addType(type);
		return gt;
	}
	
	private static IType getPrimitiveSimpleRef(PrimitiveType type)
	{
		IType itype = PRIMITIVE_SIMPLE_REF[type.typecode];
		if (itype == null)
		{
			String className = "Simple" + type.theClass.getName().qualified + "Ref";
			return PRIMITIVE_SIMPLE_REF[type.typecode] = new ClassType(Package.dyvilLangRefSimple.resolveClass(className));
		}
		return itype;
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
