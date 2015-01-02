package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IMember;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ClassFormat;
import dyvil.tools.compiler.util.Symbols;

public class Type extends ASTNode implements IContext
{
	public static Type[]	EMPTY_TYPES	= new Type[0];
	
	public static Type		NONE		= new Type(null);
	
	public static Type		VOID		= new PrimitiveType("void", "dyvil.lang.Void", 0);
	public static Type		BOOLEAN		= new PrimitiveType("boolean", "dyvil.lang.Boolean", Opcodes.T_BOOLEAN);
	public static Type		BYTE		= new PrimitiveType("byte", "dyvil.lang.Byte", Opcodes.T_BOOLEAN);
	public static Type		SHORT		= new PrimitiveType("short", "dyvil.lang.Short", Opcodes.T_SHORT);
	public static Type		CHAR		= new PrimitiveType("char", "dyvil.lang.Char", Opcodes.T_CHAR);
	public static Type		INT			= new PrimitiveType("int", "dyvil.lang,Int", Opcodes.T_INT);
	public static Type		LONG		= new PrimitiveType("long", "dyvil.lang.Long", Opcodes.T_LONG);
	public static Type		FLOAT		= new PrimitiveType("float", "dyvil.lang.Float", Opcodes.T_FLOAT);
	public static Type		DOUBLE		= new PrimitiveType("double", "dyvil.lang.Double", Opcodes.T_DOUBLE);
	
	public static Type		OBJECT		= new Type("java.lang.Object");
	public static Type		PREDEF		= new Type("dyvil.lang.Predef");
	public static Type		ARRAY		= new Type("dyvil.lang.Array");
	public static Type		STRING		= new Type("java.lang.String");
	
	public static Type		ABytecode	= new AnnotationType("dyvil.lang.annotation.Bytecode");
	public static Type		AOverride	= new AnnotationType("java.lang.Override");
	public static Type		ARetention	= new AnnotationType("java.lang.annotation.Retention");
	public static Type		ATarget		= new AnnotationType("java.lang.annotation.Target");
	
	public String			name;
	public String			qualifiedName;
	public IClass			theClass;
	public int				arrayDimensions;
	
	public Type()
	{
	}
	
	public Type(String name)
	{
		this.name = name;
		this.qualifiedName = name;
	}
	
	public Type(String name, IClass iclass)
	{
		this.name = name;
		this.qualifiedName = name;
		this.theClass = iclass;
	}
	
	public Type(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
	}
	
	public Type(ICodePosition position, String name, IClass iclass)
	{
		this.position = position;
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
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
		
		OBJECT.theClass = Package.javaLang.resolveClass("Object");
		PREDEF.theClass = Package.dyvilLang.resolveClass("Predef");
		ARRAY.theClass = Package.dyvilLang.resolveClass("Array");
		STRING.theClass = Package.javaLang.resolveClass("String");
		
		ABytecode.theClass = Package.dyvilLangAnnotation.resolveClass("Bytecode");
		AOverride.theClass = Package.javaLang.resolveClass("Override");
		ARetention.theClass = Package.javaLangAnnotation.resolveClass("Retention");
		ATarget.theClass = Package.javaLangAnnotation.resolveClass("Target");
	}
	
	public static Type findCommonSuperType(Type type1, Type type2)
	{
		Type t = superType(type1, type2);
		if (t != null)
		{
			return t;
		}
		
		Type superType1 = type1;
		while (true)
		{
			superType1 = superType1.getSuperType();
			if (superType1 == null)
			{
				break;
			}
			
			Type superType2 = type2;
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
		return OBJECT;
	}
	
	private static Type superType(Type type1, Type type2)
	{
		if (type1.arrayDimensions != type2.arrayDimensions)
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
	
	public static boolean isSuperType(Type superType, Type subType)
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
	
	public void setQualifiedName(String name)
	{
		this.qualifiedName = name;
		int index = name.lastIndexOf('.');
		if (index == -1)
		{
			this.name = name;
			return;
		}
		this.name = name.substring(index + 1);
	}
	
	public void setClass(IClass theClass)
	{
		this.theClass = theClass;
	}
	
	public void setArrayDimensions(int dimensions)
	{
		this.arrayDimensions = dimensions;
	}
	
	public int getArrayDimensions()
	{
		return this.arrayDimensions;
	}
	
	public boolean isArrayType()
	{
		return this.arrayDimensions > 0;
	}
	
	public boolean isResolved()
	{
		return this.theClass != null;
	}
	
	public Type getSuperType()
	{
		if (this.theClass != null)
		{
			return this.theClass.getSuperType();
		}
		return null;
	}
	
	protected boolean isAssignableFrom(Type that)
	{
		if (this.arrayDimensions != that.arrayDimensions)
		{
			return false;
		}
		if (that.theClass != null)
		{
			return that.theClass.isSuperType(this);
		}
		return false;
	}
	
	public String getInternalName()
	{
		return this.theClass == null ? ClassFormat.packageToInternal(this.qualifiedName) : this.theClass.getInternalName();
	}
	
	public final String getExtendedName()
	{
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buf.append('[');
		}
		this.appendExtendedName(buf);
		return buf.toString();
	}
	
	protected void appendExtendedName(StringBuilder buf)
	{
		buf.append('L').append(this.getInternalName()).append(';');
	}
	
	public String getSignature()
	{
		// TODO Generic signature
		return null;
	}
	
	public Object getFrameType()
	{
		return this.getExtendedName();
	}
	
	public int getLoadOpcode()
	{
		return Opcodes.ALOAD;
	}
	
	public int getArrayLoadOpcode()
	{
		return Opcodes.AALOAD;
	}
	
	public int getStoreOpcode()
	{
		return Opcodes.ASTORE;
	}
	
	public int getArrayStoreOpcode()
	{
		return Opcodes.AASTORE;
	}
	
	public int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}
	
	public Type resolve(IContext context)
	{
		if (this.theClass == null)
		{
			IClass iclass;
			// Try to resolve the name of this Type as a primitive type
			Type t = resolvePrimitive(this.name);
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
				iclass = context.resolveClass(this.qualifiedName);
			}
			else
			{
				// This type is probably not a primitive one, so resolve using
				// the context.
				iclass = context.resolveClass(this.name);
			}
			
			if (iclass != null)
			{
				this.theClass = iclass;
				this.qualifiedName = iclass.getQualifiedName();
			}
		}
		return this;
	}
	
	private static Type resolvePrimitive(String name)
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
		}
		return null;
	}
	
	@Override
	public Type applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			Type type = this.resolve(context);
			if (!type.isResolved())
			{
				state.addMarker(new SemanticError(this.position, "'" + this.name + "' cannot be resolved to a type"));
			}
			return type;
		}
		return this;
	}
	
	@Override
	public Type getThisType()
	{
		return this;
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		if (this.theClass == null)
		{
			return null;
		}
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
	{
		if (this.theClass == null)
		{
			return null;
		}
		
		if (this.arrayDimensions > 0)
		{
			return ARRAY.resolveField(context, name);
		}
		
		return this.theClass.resolveField(context, name);
	}
	
	@Override
	public MethodMatch resolveMethod(IContext context, String name, Type... argumentTypes)
	{
		if (this.theClass == null)
		{
			return null;
		}
		
		if (this.arrayDimensions > 0)
		{
			MethodMatch match = ARRAY.resolveMethod(context, name, argumentTypes);
			if (match != null)
			{
				return match;
			}
		}
		
		List<MethodMatch> list = new ArrayList();
		this.theClass.getMethodMatches(list, null, name, argumentTypes);
		
		if (list.isEmpty() && context != null)
		{
			Type t = context.getThisType();
			t.theClass.getMethodMatches(list, this, name, argumentTypes);
		}
		
		if (list.isEmpty())
		{
			return null;
		}
		
		Collections.sort(list);
		return list.get(0);
	}
	
	public boolean isMember(IMember member)
	{
		return member.getTheClass() == this.theClass;
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.theClass == null ? 0 : this.theClass.getAccessibility(member);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append(this.name);
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append(Formatting.Type.array);
		}
		// TODO Generics
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
		Type other = (Type) obj;
		if (this.arrayDimensions != other.arrayDimensions)
		{
			return false;
		}
		return this.classEquals(other);
	}
	
	public boolean classEquals(Type type)
	{
		if (this.theClass != null && this.theClass == type.theClass)
		{
			return true;
		}
		
		String thisName = this.theClass != null ? this.theClass.getQualifiedName() : this.qualifiedName;
		String otherName = type.theClass != null ? type.theClass.getQualifiedName() : type.qualifiedName;
		
		if (thisName == null || !thisName.equals(otherName))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public Type clone()
	{
		Type t = new Type(this.position, this.name, this.theClass);
		t.arrayDimensions = arrayDimensions;
		return t;
	}
}
