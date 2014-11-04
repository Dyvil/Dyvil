package dyvil.tools.compiler.ast.type;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ClassFormat;

public class Type extends ASTObject implements IContext
{
	public static Type[]	EMPTY_TYPES	= new Type[0];
	
	public static Type		NONE		= new Type(null);
	
	public static Type		VOID		= new PrimitiveType("void");
	public static Type		BOOL		= new PrimitiveType("boolean");
	public static Type		BYTE		= new PrimitiveType("byte");
	public static Type		SHORT		= new PrimitiveType("short");
	public static Type		CHAR		= new PrimitiveType("char");
	public static Type		INT			= new PrimitiveType("int");
	public static Type		LONG		= new PrimitiveType("long");
	public static Type		FLOAT		= new PrimitiveType("float");
	public static Type		DOUBLE		= new PrimitiveType("double");
	
	public static Type OBJECT = new Type("java.lang.Object");
	public static Type		STRING		= new StringType("java.lang.String");
	
	public String			name;
	public IClass			theClass;
	public char				seperator;
	public int				arrayDimensions;
	
	public Type()
	{}
	
	public Type(String name)
	{
		this.name = name;
	}
	
	public Type(String name, IClass iclass)
	{
		this.name = name;
		this.theClass = iclass;
	}
	
	public Type(String name, ICodePosition position)
	{
		this.name = name;
		this.position = position;
	}
	
	public Type(String name, IClass iclass, ICodePosition position)
	{
		this.name = name;
		this.theClass = iclass;
		this.position = position;
	}
	
	public static void init()
	{
		BOOL.theClass = Package.dyvilLang.resolveClass("Boolean");
		BYTE.theClass = Package.dyvilLang.resolveClass("Byte");
		SHORT.theClass = Package.dyvilLang.resolveClass("Short");
		CHAR.theClass = Package.dyvilLang.resolveClass("Char");
		INT.theClass = Package.dyvilLang.resolveClass("Int");
		LONG.theClass = Package.dyvilLang.resolveClass("Long");
		FLOAT.theClass = Package.dyvilLang.resolveClass("Float");
		DOUBLE.theClass = Package.dyvilLang.resolveClass("Double");
		
		OBJECT.theClass = Package.javaLang.resolveClass("Object");
		STRING.theClass = Package.javaLang.resolveClass("String");
	}
	
	/**
	 * Returns true if {@code t2} is equal to or a subclass of {@code t1}.
	 * 
	 * @param superType
	 *            the super type
	 * @param subType
	 *            the sub type
	 * @return true if t2 is equal to or a subclass of t1
	 */
	public static boolean isSuperType(Type superType, Type subType)
	{
		if (superType == VOID)
		{
			return true;
		}
		else if (subType == NONE && !(superType instanceof PrimitiveType))
		{
			return true;
		}
		else if (superType.equals(subType))
		{
			return true;
		}
		return superType.isAssignableFrom(subType);
	}
	
	public void setClass(IClass theClass)
	{
		this.theClass = theClass;
	}
	
	public void setSeperator(char seperator)
	{
		this.seperator = seperator;
	}
	
	public char getSeperator()
	{
		return this.seperator;
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
	
	protected boolean isAssignableFrom(Type that)
	{
		if (that.theClass != null)
		{
			return that.theClass.isSuperType(this);
		}
		return false;
	}
	
	public String getInternalName()
	{
		return this.theClass == null ? ClassFormat.packageToInternal(this.name) : this.theClass.getInternalName();
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
	
	public Type resolve(IContext context)
	{
		if (this.theClass == null)
		{
			switch (this.name)
			{
			case "void":
				return VOID;
			case "int":
				return INT;
			case "long":
				return LONG;
			case "float":
				return FLOAT;
			case "double":
				return DOUBLE;
			case "char":
				return CHAR;
			case "bool":
				return BOOL;
			case "java.lang.String":
				return STRING;
			}
			
			this.theClass = context.resolveClass(this.name);
		}
		return this;
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
		return this.theClass.isStatic();
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public IField resolveField(String name)
	{
		return this.theClass.resolveField(name);
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		return this.theClass.resolveMethod(name, args);
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
	public boolean equals(Object obj)
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
		if (this.name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!this.name.equals(other.name))
		{
			return false;
		}
		return true;
	}
}
