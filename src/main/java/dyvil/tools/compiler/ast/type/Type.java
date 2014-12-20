package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.ClassFormat;

public class Type extends ASTNode implements IContext
{
	public static Type[]	EMPTY_TYPES	= new Type[0];
	
	public static Type		NONE		= new Type(null);
	
	public static Type		VOID		= new PrimitiveType("void", "dyvil.lang.Void");
	public static Type		BOOL		= new PrimitiveType("boolean", "dyvil.lang.Boolean");
	public static Type		BYTE		= new PrimitiveType("byte", "dyvil.lang.Byte");
	public static Type		SHORT		= new PrimitiveType("short", "dyvil.lang.Short");
	public static Type		CHAR		= new PrimitiveType("char", "dyvil.lang.Char");
	public static Type		INT			= new PrimitiveType("int", "dyvil.lang,Int");
	public static Type		LONG		= new PrimitiveType("long", "dyvil.lang.Long");
	public static Type		FLOAT		= new PrimitiveType("float", "dyvil.lang.Float");
	public static Type		DOUBLE		= new PrimitiveType("double", "dyvil.lang.Double");
	
	public static Type		OBJECT		= new Type("java.lang.Object");
	public static Type		PREDEF		= new Type("dyvil.lang.Predef");
	public static Type		STRING		= new StringType("java.lang.String");
	
	public static Type		ABytecode	= new Type("dyvil.lang.annotation.bytecode");
	
	public String			name;
	public String			qualifiedName;
	public IClass			theClass;
	public char				seperator;
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
	
	public Type(String name, ICodePosition position)
	{
		this.name = name;
		this.qualifiedName = name;
		this.position = position;
	}
	
	public Type(String name, IClass iclass, ICodePosition position)
	{
		this.name = name;
		this.qualifiedName = name;
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
		PREDEF.theClass = Package.dyvilLang.resolveClass("Predef");
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
	
	public int getLoadOpcode()
	{
		if (this.arrayDimensions > 0)
		{
			return Opcodes.AALOAD;
		}
		return Opcodes.ALOAD;
	}
	
	public int getStoreOpcode()
	{
		if (this.arrayDimensions > 0)
		{
			return Opcodes.AASTORE;
		}
		return Opcodes.ASTORE;
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
			
			IClass iclass = context.resolveClass(this.name);
			
			if (iclass != null)
			{
				this.theClass = iclass;
				this.qualifiedName = iclass.getName();
			}
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
		return this.theClass.resolveField(context, name);
	}
	
	@Override
	public MethodMatch resolveMethod(IContext context, String name, Type... argumentTypes)
	{
		if (this.theClass == null)
		{
			return null;
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
		if (obj instanceof PrimitiveType)
		{
			return ((PrimitiveType) obj).equals(this);
		}
		if (!(obj instanceof Type))
		{
			return false;
		}
		return this.equals((Type) obj);
	}
	
	protected boolean equals(Type type)
	{
		if (this.arrayDimensions != type.arrayDimensions)
		{
			return false;
		}
		
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
}
