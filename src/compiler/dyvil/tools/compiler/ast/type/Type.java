package dyvil.tools.compiler.ast.type;

import java.util.List;
import java.util.Map;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.CaptureClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.dynamic.DynamicType;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.generic.WildcardType;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.Symbols;

public class Type extends ASTNode implements IType
{
	public static final UnknownType		NONE		= new UnknownType();
	public static final DynamicType		DYNAMIC		= new DynamicType();
	
	public static final PrimitiveType	VOID		= new PrimitiveType("void", "Void", 0);
	public static final PrimitiveType	BOOLEAN		= new PrimitiveType("boolean", "Boolean", Opcodes.T_BOOLEAN);
	public static final PrimitiveType	BYTE		= new PrimitiveType("byte", "Byte", Opcodes.T_BOOLEAN);
	public static final PrimitiveType	SHORT		= new PrimitiveType("short", "Short", Opcodes.T_SHORT);
	public static final PrimitiveType	CHAR		= new PrimitiveType("char", "Char", Opcodes.T_CHAR);
	public static final PrimitiveType	INT			= new PrimitiveType("int", "Int", Opcodes.T_INT);
	public static final PrimitiveType	LONG		= new PrimitiveType("long", "Long", Opcodes.T_LONG);
	public static final PrimitiveType	FLOAT		= new PrimitiveType("float", "Float", Opcodes.T_FLOAT);
	public static final PrimitiveType	DOUBLE		= new PrimitiveType("double", "Double", Opcodes.T_DOUBLE);
	
	public static final Type			ANY			= new Type("Any");
	public static final Type			OBJECT		= new Type("Object");
	public static final Type			PREDEF		= new Type("Predef");
	public static final Type			ARRAY		= new Type("Array");
	public static final Type			STRING		= new Type("String");
	
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
	
	public static IClass				PREDEF_CLASS;
	public static IClass				STRING_CLASS;
	public static IClass				OBJECT_CLASS;
	
	public String						name;
	public String						qualifiedName;
	public String						fullName;
	public IClass						theClass;
	public int							arrayDimensions;
	
	public Type()
	{
		super();
	}
	
	protected Type(String name)
	{
		this.name = name;
		this.qualifiedName = name;
	}
	
	public Type(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
	}
	
	public Type(IClass iclass)
	{
		this.name = iclass.getName();
		this.qualifiedName = iclass.getQualifiedName();
		this.fullName = iclass.getFullName();
		this.theClass = iclass;
	}
	
	public static void init()
	{
		BOOLEAN.theClass = BOOLEAN_CLASS = Package.dyvilLang.resolveClass("Boolean");
		BOOLEAN.boxMethod = BOOLEAN_CLASS.getBody().getMethod("create");
		BOOLEAN.unboxMethod = BOOLEAN_CLASS.getBody().getMethod("booleanValue");
		BYTE.theClass = BYTE_CLASS = Package.dyvilLang.resolveClass("Byte");
		BYTE.boxMethod = BYTE_CLASS.getBody().getMethod("create");
		BYTE.unboxMethod = BYTE_CLASS.getBody().getMethod("byteValue");
		SHORT.theClass = SHORT_CLASS = Package.dyvilLang.resolveClass("Short");
		SHORT.boxMethod = SHORT_CLASS.getBody().getMethod("create");
		SHORT.unboxMethod = SHORT_CLASS.getBody().getMethod("shortValue");
		CHAR.theClass = CHAR_CLASS = Package.dyvilLang.resolveClass("Char");
		CHAR.boxMethod = CHAR_CLASS.getBody().getMethod("create");
		CHAR.unboxMethod = CHAR_CLASS.getBody().getMethod("charValue");
		INT.theClass = INT_CLASS = Package.dyvilLang.resolveClass("Int");
		INT.boxMethod = INT_CLASS.getBody().getMethod("create");
		INT.unboxMethod = INT_CLASS.getBody().getMethod("intValue");
		LONG.theClass = LONG_CLASS = Package.dyvilLang.resolveClass("Long");
		LONG.boxMethod = LONG_CLASS.getBody().getMethod("create");
		LONG.unboxMethod = LONG_CLASS.getBody().getMethod("longValue");
		FLOAT.theClass = FLOAT_CLASS = Package.dyvilLang.resolveClass("Float");
		FLOAT.boxMethod = FLOAT_CLASS.getBody().getMethod("create");
		FLOAT.unboxMethod = FLOAT_CLASS.getBody().getMethod("floatValue");
		DOUBLE.theClass = DOUBLE_CLASS = Package.dyvilLang.resolveClass("Double");
		DOUBLE.boxMethod = DOUBLE_CLASS.getBody().getMethod("create");
		DOUBLE.unboxMethod = DOUBLE_CLASS.getBody().getMethod("doubleValue");
		
		ANY.theClass = Package.dyvilLang.resolveClass("Any");
		ANY.fullName = "dyvil.lang.Any";
		OBJECT.theClass = OBJECT_CLASS = Package.javaLang.resolveClass("Object");
		OBJECT.fullName = "java.lang.Object";
		PREDEF.theClass = PREDEF_CLASS = Package.dyvilLang.resolveClass("Predef");
		PREDEF.fullName = "dyvil.lang.Predef";
		ARRAY.theClass = Package.dyvilLang.resolveClass("Array");
		ARRAY.fullName = "dyvil.lang.Array";
		STRING.theClass = STRING_CLASS = Package.javaLang.resolveClass("String");
		STRING.fullName = "java.lang.String";
		
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
		if (isSuperType(type1, type2))
		{
			return type1;
		}
		if (isSuperType(type2, type1))
		{
			return type2;
		}
		return null;
	}
	
	public static boolean isSuperType(IType superType, IType subType)
	{
		if (subType == NONE && !(superType instanceof PrimitiveType))
		{
			return true;
		}
		return superType.isSuperTypeOf(subType);
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
		this.qualifiedName = qualifiedName;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.qualifiedName = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.qualifiedName;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.qualifiedName.equals(name);
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
	public boolean isGeneric()
	{
		return false;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.theClass instanceof CaptureClass;
	}
	
	@Override
	public IType getConcreteType(Map<String, IType> typeVariables)
	{
		if (this.theClass instanceof CaptureClass)
		{
			return typeVariables.get(this.qualifiedName);
		}
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
	
	// Resolve
	
	@Override
	public IType resolve(List<Marker> markers, IContext context)
	{
		if (this.theClass != null)
		{
			return this;
		}
		
		IClass iclass;
		// Try to resolve the name of this Type as a primitive type
		IType t = resolvePrimitive(this.qualifiedName);
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
			iclass = context.resolveClass(this.qualifiedName);
		}
		
		if (iclass != null)
		{
			if (iclass instanceof CaptureClass)
			{
				return new WildcardType(position, (CaptureClass) iclass);
			}
			
			this.theClass = iclass;
			this.fullName = iclass.getFullName();
			return this;
		}
		if (markers != null)
		{
			markers.add(Markers.create(this.position, "resolve.type", this.toString()));
		}
		return this;
	}
	
	protected static IType resolvePrimitive(String name)
	{
		switch (name)
		{
		case "void":
			return VOID;
		case "boolean":
			return BOOLEAN;
		case "char":
			return CHAR;
		case "byte":
			return BYTE;
		case "short":
			return SHORT;
		case "int":
			return INT;
		case "long":
			return LONG;
		case "float":
			return FLOAT;
		case "double":
			return DOUBLE;
		case "string":
		case "String":
			// Both lower- and uppercase "string" resolve to java.lang.String.
			return STRING;
		case "any":
		case "Any":
			return ANY;
		case "dynamic":
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
	public Package resolvePackage(String name)
	{
		return this.theClass == null ? null : this.theClass.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.theClass == null ? null : this.theClass.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		if (this.arrayDimensions > 0)
		{
			return ARRAY.resolveField(name);
		}
		
		return this.theClass == null ? null : this.theClass.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		if (this.arrayDimensions > 0)
		{
			MethodMatch match = ARRAY.resolveMethod(instance, name, arguments);
			if (match != null)
			{
				return match;
			}
		}
		
		return this.theClass == null ? null : this.theClass.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
		if (this.arrayDimensions > 0)
		{
			ARRAY.getMethodMatches(list, instance, name, arguments);
			return;
		}
		
		if (this.theClass != null)
		{
			this.theClass.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public MethodMatch resolveConstructor(List<IValue> arguments)
	{
		if (this.arrayDimensions > 0)
		{
			return null;
		}
		return this.theClass == null ? null : this.theClass.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, List<IValue> arguments)
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
		return this.theClass == null ? ClassFormat.packageToInternal(this.qualifiedName) : this.theClass.getInternalName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append('L').append(this.getInternalName()).append(';');
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
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + this.arrayDimensions;
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		return result;
	}
	
	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof Type))
		{
			return false;
		}
		IType other = (IType) obj;
		if (this.arrayDimensions != other.getArrayDimensions())
		{
			return false;
		}
		return this.classEquals(other);
	}
	
	public boolean equals(IType type)
	{
		if (this.arrayDimensions != type.getArrayDimensions())
		{
			return false;
		}
		return this.classEquals(type);
	}
	
	@Override
	public Type clone()
	{
		Type t = new Type();
		t.theClass = this.theClass;
		t.name = this.name;
		t.qualifiedName = this.qualifiedName;
		t.fullName = this.fullName;
		t.arrayDimensions = this.arrayDimensions;
		return t;
	}
}
