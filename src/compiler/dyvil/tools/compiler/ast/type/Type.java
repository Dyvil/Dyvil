package dyvil.tools.compiler.ast.type;

import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.CaptureClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.dynamic.DynamicType;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.WildcardType;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Type extends ASTNode implements IType
{
	public static final UnknownType		NONE		= new UnknownType();
	public static final DynamicType		DYNAMIC		= new DynamicType();
	
	public static final PrimitiveType	VOID		= new PrimitiveType(Name._void, 0);
	public static final PrimitiveType	BOOLEAN		= new PrimitiveType(Name._boolean, MethodWriter.T_BOOLEAN);
	public static final PrimitiveType	BYTE		= new PrimitiveType(Name._byte, MethodWriter.T_BOOLEAN);
	public static final PrimitiveType	SHORT		= new PrimitiveType(Name._short, MethodWriter.T_SHORT);
	public static final PrimitiveType	CHAR		= new PrimitiveType(Name._char, MethodWriter.T_CHAR);
	public static final PrimitiveType	INT			= new PrimitiveType(Name._int, MethodWriter.T_INT);
	public static final PrimitiveType	LONG		= new PrimitiveType(Name._long, MethodWriter.T_LONG);
	public static final PrimitiveType	FLOAT		= new PrimitiveType(Name._float, MethodWriter.T_FLOAT);
	public static final PrimitiveType	DOUBLE		= new PrimitiveType(Name._double, MethodWriter.T_DOUBLE);
	
	public static final Type			ANY			= new Type("Any");
	public static final Type			OBJECT		= new Type("Object");
	public static final Type			PREDEF		= new Type("Predef");
	public static final Type			ARRAY		= new Type("Array");
	public static final Type			STRING		= new Type("String");
	public static final Type			CLASS		= new Type("Class");
	
	public static final AnnotationType	AIntrinsic	= new AnnotationType("Intrinsic");
	public static final AnnotationType	AOverride	= new AnnotationType("Override");
	public static final AnnotationType	ARetention	= new AnnotationType("Retention");
	public static final AnnotationType	ATarget		= new AnnotationType("Target");
	
	public static IClass				BOOLEAN_CLASS;
	public static IClass				BYTE_CLASS;
	public static IClass				SHORT_CLASS;
	public static IClass				CHAR_CLASS;
	public static IClass				INT_CLASS;
	public static IClass				LONG_CLASS;
	public static IClass				FLOAT_CLASS;
	public static IClass				DOUBLE_CLASS;
	
	public static IClass				OBJECT_CLASS;
	public static IClass				STRING_CLASS;
	public static IClass				PREDEF_CLASS;
	public static IClass				ARRAY_CLASS;
	public static IClass				CLASS_CLASS;
	
	public Name							name;
	public String						fullName;
	public IClass						theClass;
	public int							arrayDimensions;
	
	public Type()
	{
		super();
	}
	
	private Type(String name)
	{
		this.name = Name.getQualified(name);
	}
	
	public Type(Name name)
	{
		this.name = name;
	}
	
	public Type(ICodePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}
	
	public Type(IClass iclass)
	{
		this.name = iclass.getName();
		this.fullName = iclass.getFullName();
		this.theClass = iclass;
	}
	
	public static void init()
	{
		BOOLEAN.theClass = BOOLEAN_CLASS = Package.dyvilLang.resolveClass("Boolean");
		BOOLEAN.boxMethod = BOOLEAN_CLASS.getBody().getMethod(Name.apply);
		BOOLEAN.unboxMethod = BOOLEAN_CLASS.getBody().getMethod(Name.unapply);
		BYTE.theClass = BYTE_CLASS = Package.dyvilLang.resolveClass("Byte");
		BYTE.boxMethod = BYTE_CLASS.getBody().getMethod(Name.apply);
		BYTE.unboxMethod = BYTE_CLASS.getBody().getMethod(Name.unapply);
		SHORT.theClass = SHORT_CLASS = Package.dyvilLang.resolveClass("Short");
		SHORT.boxMethod = SHORT_CLASS.getBody().getMethod(Name.apply);
		SHORT.unboxMethod = SHORT_CLASS.getBody().getMethod(Name.unapply);
		CHAR.theClass = CHAR_CLASS = Package.dyvilLang.resolveClass("Char");
		CHAR.boxMethod = CHAR_CLASS.getBody().getMethod(Name.apply);
		CHAR.unboxMethod = CHAR_CLASS.getBody().getMethod(Name.unapply);
		INT.theClass = INT_CLASS = Package.dyvilLang.resolveClass("Int");
		INT.boxMethod = INT_CLASS.getBody().getMethod(Name.apply);
		INT.unboxMethod = INT_CLASS.getBody().getMethod(Name.unapply);
		LONG.theClass = LONG_CLASS = Package.dyvilLang.resolveClass("Long");
		LONG.boxMethod = LONG_CLASS.getBody().getMethod(Name.apply);
		LONG.unboxMethod = LONG_CLASS.getBody().getMethod(Name.unapply);
		FLOAT.theClass = FLOAT_CLASS = Package.dyvilLang.resolveClass("Float");
		FLOAT.boxMethod = FLOAT_CLASS.getBody().getMethod(Name.apply);
		FLOAT.unboxMethod = FLOAT_CLASS.getBody().getMethod(Name.unapply);
		DOUBLE.theClass = DOUBLE_CLASS = Package.dyvilLang.resolveClass("Double");
		DOUBLE.boxMethod = DOUBLE_CLASS.getBody().getMethod(Name.apply);
		DOUBLE.unboxMethod = DOUBLE_CLASS.getBody().getMethod(Name.unapply);
		
		OBJECT.theClass = OBJECT_CLASS = Package.javaLang.resolveClass("Object");
		OBJECT.fullName = "java.lang.Object";
		ANY.theClass = OBJECT_CLASS;
		ANY.fullName = "dyvil.lang.Any";
		PREDEF.theClass = PREDEF_CLASS = Package.dyvilLang.resolveClass("Predef");
		PREDEF.fullName = "dyvil.lang.Predef";
		ARRAY.theClass = ARRAY_CLASS = Package.dyvilLang.resolveClass("Array");
		ARRAY.fullName = "dyvil.lang.Array";
		STRING.theClass = STRING_CLASS = Package.javaLang.resolveClass("String");
		STRING.fullName = "java.lang.String";
		CLASS.theClass = CLASS_CLASS = Package.javaLang.resolveClass("Class");
		CLASS.fullName = "java.lang.Class";
		
		AIntrinsic.theClass = Package.dyvilLangAnnotation.resolveClass("Intrinsic");
		AOverride.theClass = Package.javaLang.resolveClass("Override");
		ARetention.theClass = Package.javaLangAnnotation.resolveClass("Retention");
		ATarget.theClass = Package.javaLangAnnotation.resolveClass("Target");
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
	
	private static IType superType(IType type1, IType type2)
	{
		if (type1.getArrayDimensions() != type2.getArrayDimensions())
		{
			return OBJECT;
		}
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
	
	// Names
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public void setFullName(String name)
	{
		this.fullName = name;
	}
	
	@Override
	public String getFullName()
	{
		return this.fullName;
	}
	
	@Override
	public void setClass(IClass theClass)
	{
		this.theClass = theClass;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.arrayDimensions != 0;
	}
	
	@Override
	public boolean isGeneric()
	{
		return false;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		return this;
	}
	
	@Override
	public void setArrayDimensions(int dimensions)
	{
		this.arrayDimensions = dimensions;
	}
	
	@Override
	public int getArrayDimensions()
	{
		return this.arrayDimensions;
	}
	
	@Override
	public IType getElementType()
	{
		Type t = this.clone();
		t.arrayDimensions--;
		return t;
	}
	
	@Override
	public IType getArrayType()
	{
		Type t = this.clone();
		t.arrayDimensions++;
		return t;
	}
	
	@Override
	public IType getArrayType(int dimensions)
	{
		Type t = this.clone();
		t.arrayDimensions = dimensions;
		return t;
	}
	
	@Override
	public void addArrayDimension()
	{
		this.arrayDimensions++;
	}
	
	@Override
	public void removeArrayDimension()
	{
		this.arrayDimensions--;
	}
	
	@Override
	public boolean isArrayType()
	{
		return this.arrayDimensions > 0;
	}
	
	// Super Type
	
	@Override
	public IType getSuperType()
	{
		if (this.theClass != null)
		{
			return this.theClass.getSuperType();
		}
		return null;
	}
	
	@Override
	public boolean isSuperTypeOf2(IType type)
	{
		IClass thisClass = this.theClass;
		IClass thatClass = type.getTheClass();
		if (thatClass != null)
		{
			return thatClass == thisClass || thatClass.isSubTypeOf(this);
		}
		return false;
	}
	
	@Override
	public boolean equals(IType type)
	{
		if (this.arrayDimensions != type.getArrayDimensions())
		{
			return false;
		}
		return this.theClass == type.getTheClass();
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		return this.theClass == type.getTheClass();
	}
	
	// Resolve
	
	@Override
	public IType resolve(MarkerList markers, IContext context)
	{
		if (this.theClass != null)
		{
			return this;
		}
		
		IClass iclass;
		// Try to resolve the name of this Type as a primitive type
		IType t = resolvePrimitive(this.name);
		if (t != null)
		{
			// If the array dimensions of this type are 0, we can assume
			// that it is exactly the primitive type, so the primitive type
			// instance is returned.
			if (this.arrayDimensions == 0)
			{
				return t;
			}
			
			return t.getArrayType(this.arrayDimensions);
		}
		else if (this.fullName != null)
		{
			iclass = Package.rootPackage.resolveClass(this.fullName);
		}
		else
		{
			// This type is probably not a primitive one, so resolve using
			// the context.
			iclass = context.resolveClass(this.name);
		}
		
		if (iclass != null)
		{
			if (iclass instanceof CaptureClass)
			{
				return new WildcardType(this.position, this.arrayDimensions, (CaptureClass) iclass);
			}
			
			this.theClass = iclass;
			this.fullName = iclass.getFullName();
			return this;
		}
		if (markers != null)
		{
			markers.add(this.position, "resolve.type", this.toString());
		}
		return this;
	}
	
	protected static IType resolvePrimitive(Name name)
	{
		if (name == Name._void)
		{
			return VOID;
		}
		if (name == Name._boolean)
		{
			return BOOLEAN;
		}
		if (name == Name._byte)
		{
			return BYTE;
		}
		if (name == Name._short)
		{
			return SHORT;
		}
		if (name == Name._char)
		{
			return CHAR;
		}
		if (name == Name._int)
		{
			return INT;
		}
		if (name == Name._long)
		{
			return LONG;
		}
		if (name == Name._float)
		{
			return FLOAT;
		}
		if (name == Name._double)
		{
			return DOUBLE;
		}
		if (name == Name.any)
		{
			return ANY;
		}
		if (name == Name.dynamic)
		{
			return DYNAMIC;
		}
		return null;
	}
	
	@Override
	public boolean isResolved()
	{
		return this.theClass != null;
	}
	
	// IContext
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.theClass == null ? null : this.theClass.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.theClass == null ? null : this.theClass.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(Name name)
	{
		if (this.arrayDimensions > 0)
		{
			return null;
		}
		
		return this.theClass == null ? null : this.theClass.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, Name name, IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			return ARRAY_CLASS.resolveMethod(instance, name, arguments);
		}
		
		return this.theClass == null ? null : this.theClass.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			ARRAY_CLASS.getMethodMatches(list, instance, name, arguments);
			return;
		}
		
		if (this.theClass != null)
		{
			this.theClass.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public ConstructorMatch resolveConstructor(IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			return null;
		}
		return this.theClass == null ? null : this.theClass.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		if (this.arrayDimensions == 0 && this.theClass != null)
		{
			this.theClass.getConstructorMatches(list, arguments);
		}
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.theClass == null ? 0 : this.theClass.getAccessibility(member);
	}
	
	// Compilation
	
	@Override
	public String getInternalName()
	{
		if (this.arrayDimensions > 0)
		{
			StringBuilder buf = new StringBuilder();
			this.appendExtendedName(buf);
			return buf.toString();
		}
		return this.theClass == null ? ClassFormat.packageToInternal(this.fullName) : this.theClass.getInternalName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append('L').append(this.theClass == null ? ClassFormat.packageToInternal(this.fullName) : this.theClass.getInternalName()).append(';');
	}
	
	@Override
	public String getSignature()
	{
		return this.theClass.getSignature();
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append('L').append(this.getInternalName()).append(';');
	}
	
	@Override
	public int getLoadOpcode()
	{
		return Opcodes.ALOAD;
	}
	
	@Override
	public int getArrayLoadOpcode()
	{
		return Opcodes.AALOAD;
	}
	
	@Override
	public int getStoreOpcode()
	{
		return Opcodes.ASTORE;
	}
	
	@Override
	public int getArrayStoreOpcode()
	{
		return Opcodes.AASTORE;
	}
	
	@Override
	public int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}
	
	// Misc
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append(this.name);
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append(']');
		}
	}
	
	@Override
	public Type clone()
	{
		Type t = new Type();
		t.theClass = this.theClass;
		t.name = this.name;
		t.fullName = this.fullName;
		t.arrayDimensions = this.arrayDimensions;
		return t;
	}
}
