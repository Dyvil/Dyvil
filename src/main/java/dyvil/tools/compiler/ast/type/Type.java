package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IClass;
import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IMember;
import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ClassFormat;
import dyvil.tools.compiler.util.Symbols;

public class Type extends ASTNode implements IContext, IType
{
	public static final IType[]	EMPTY_TYPES	= new IType[0];
	
	public static final Type	NONE		= new Type()
											{
												@Override
												public Object getFrameType()
												{
													return Opcodes.NULL;
												}
											};
	
	public static final Type	VOID		= new PrimitiveType("void", "Void", 0);
	public static final Type	BOOLEAN		= new PrimitiveType("boolean", "Boolean", Opcodes.T_BOOLEAN);
	public static final Type	BYTE		= new PrimitiveType("byte", "Byte", Opcodes.T_BOOLEAN);
	public static final Type	SHORT		= new PrimitiveType("short", "Short", Opcodes.T_SHORT);
	public static final Type	CHAR		= new PrimitiveType("char", "Char", Opcodes.T_CHAR);
	public static final Type	INT			= new PrimitiveType("int", "Int", Opcodes.T_INT);
	public static final Type	LONG		= new PrimitiveType("long", "Long", Opcodes.T_LONG);
	public static final Type	FLOAT		= new PrimitiveType("float", "Float", Opcodes.T_FLOAT);
	public static final Type	DOUBLE		= new PrimitiveType("double", "Double", Opcodes.T_DOUBLE);
	
	public static final Type	ANY			= new Type("Any");
	public static final Type	OBJECT		= new Type("Object");
	public static final Type	PREDEF		= new Type("Predef");
	public static final Type	ARRAY		= new Type("Array");
	public static final Type	STRING		= new Type("String");
	
	public static final Type	ABytecode	= new AnnotationType("Bytecode");
	public static final Type	AOverride	= new AnnotationType("Override");
	public static final Type	ARetention	= new AnnotationType("Retention");
	public static final Type	ATarget		= new AnnotationType("Target");
	
	public static IClass		PREDEF_CLASS;
	
	public String				name;
	public String				qualifiedName;
	public String				fullName;
	public IClass				theClass;
	public int					arrayDimensions;
	
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
		BOOLEAN.theClass = Package.dyvilLang.resolveClass("Boolean");
		BYTE.theClass = Package.dyvilLang.resolveClass("Byte");
		SHORT.theClass = Package.dyvilLang.resolveClass("Short");
		CHAR.theClass = Package.dyvilLang.resolveClass("Char");
		INT.theClass = Package.dyvilLang.resolveClass("Int");
		LONG.theClass = Package.dyvilLang.resolveClass("Long");
		FLOAT.theClass = Package.dyvilLang.resolveClass("Float");
		DOUBLE.theClass = Package.dyvilLang.resolveClass("Double");
		
		ANY.theClass = Package.dyvilLang.resolveClass("Any");
		OBJECT.theClass = Package.javaLang.resolveClass("Object");
		PREDEF.theClass = PREDEF_CLASS = Package.dyvilLang.resolveClass("Predef");
		ARRAY.theClass = Package.dyvilLang.resolveClass("Array");
		STRING.theClass = Package.javaLang.resolveClass("String");
		
		ABytecode.theClass = Package.dyvilLangAnnotation.resolveClass("Bytecode");
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
		else if (superType.equals(subType))
		{
			return true;
		}
		return superType.isAssignableFrom(subType);
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
	public Type resolve(IContext context)
	{
		if (this.theClass == null)
		{
			IClass iclass;
			// Try to resolve the name of this Type as a primitive type
			Type t = resolvePrimitive(this.qualifiedName);
			if (t != null)
			{
				// If the array dimensions of this type are 0, we can assume
				// that it is exactly the primitive type, so the primitive type
				// instance is returned.
				if (this.arrayDimensions == 0)
				{
					return t;
				}
				
				t = t.clone();
				t.arrayDimensions = this.arrayDimensions;
				return t;
			}
			else if (context == Package.rootPackage)
			{
				iclass = context.resolveClass(this.fullName);
			}
			else
			{
				// This type is probably not a primitive one, so resolve using
				// the context.
				iclass = context.resolveClass(this.qualifiedName);
			}
			
			if (iclass != null)
			{
				this.theClass = iclass;
				this.fullName = iclass.getFullName();
			}
		}
		return this;
	}
	
	protected static Type resolvePrimitive(String name)
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
	public FieldMatch resolveField(IContext context, String name)
	{
		if (this.arrayDimensions > 0)
		{
			return ARRAY.resolveField(context, name);
		}
		
		return this.theClass == null ? null : this.theClass.resolveField(context, name);
	}
	
	@Override
	public MethodMatch resolveMethod(IContext context, String name, IType[] argumentTypes)
	{
		if (this.arrayDimensions > 0)
		{
			MethodMatch match = ARRAY.resolveMethod(context, name, argumentTypes);
			if (match != null)
			{
				return match;
			}
		}
		
		if (this.theClass == null)
		{
			return null;
		}
		
		List<MethodMatch> list = new ArrayList();
		this.theClass.getMethodMatches(list, this, name, argumentTypes);
		
		if (list.isEmpty() && context != null)
		{
			IType t = context.getThisType();
			t.getMethodMatches(list, this, name, argumentTypes);
		}
		
		if (list.isEmpty() && this.theClass != PREDEF_CLASS)
		{
			PREDEF_CLASS.getMethodMatches(list, this, name, argumentTypes);
		}
		
		if (list.isEmpty())
		{
			return null;
		}
		
		Collections.sort(list);
		return list.get(0);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IType type, String name, IType[] argumentTypes)
	{
		this.theClass.getMethodMatches(list, type, name, argumentTypes);
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
		buffer.append(this.name);
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append(Formatting.Type.array);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		this.toString("", buffer);
		return buffer.toString();
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
