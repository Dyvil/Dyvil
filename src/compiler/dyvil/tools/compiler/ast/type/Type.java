package dyvil.tools.compiler.ast.type;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.TypeVariableType;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Type extends ASTNode implements IType
{
	public Name		name;
	public String	internalName;
	public IClass	theClass;
	public int		arrayDimensions;
	
	public Type()
	{
		super();
	}
	
	public Type(String internalName)
	{
		this.internalName = internalName;
		
		int index = internalName.lastIndexOf('/');
		if (index == -1)
		{
			this.name = Name.getQualified(internalName);
		}
		else
		{
			this.name = Name.getQualified(internalName.substring(index + 1));
		}
	}
	
	public Type(String internalName, Name name)
	{
		this.internalName = internalName;
		this.name = name;
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
		this.internalName = iclass.getInternalName();
		this.theClass = iclass;
	}
	
	public Type(ICodePosition position, IClass iclass)
	{
		this.position = position;
		this.name = iclass.getName();
		this.internalName = iclass.getInternalName();
		this.theClass = iclass;
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
		return Types.ANY;
	}
	
	private static IType superType(IType type1, IType type2)
	{
		if (type1.getArrayDimensions() != type2.getArrayDimensions())
		{
			return Types.OBJECT;
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
	
	@Override
	public int typeTag()
	{
		return TYPE;
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
		if (dimensions == this.arrayDimensions)
		{
			return this;
		}
		
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
	public boolean isResolved()
	{
		return this.theClass != null;
	}
	
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
		
		if (this.internalName != null)
		{
			iclass = Package.rootPackage.resolveInternalClass(this.internalName);
		}
		else
		{
			ITypeVariable typeVar = context.resolveTypeVariable(this.name);
			if (typeVar != null)
			{
				return new TypeVariableType(typeVar, this.arrayDimensions);
			}
			
			// This type is probably not a primitive one, so resolve using
			// the context.
			iclass = context.resolveClass(this.name);
		}
		
		if (iclass != null)
		{
			this.theClass = iclass;
			this.internalName = iclass.getInternalName();
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
			return Types.VOID;
		}
		if (name == Name._boolean)
		{
			return Types.BOOLEAN;
		}
		if (name == Name._byte)
		{
			return Types.BYTE;
		}
		if (name == Name._short)
		{
			return Types.SHORT;
		}
		if (name == Name._char)
		{
			return Types.CHAR;
		}
		if (name == Name._int)
		{
			return Types.INT;
		}
		if (name == Name._long)
		{
			return Types.LONG;
		}
		if (name == Name._float)
		{
			return Types.FLOAT;
		}
		if (name == Name._double)
		{
			return Types.DOUBLE;
		}
		if (name == Name.any)
		{
			return Types.ANY;
		}
		if (name == Name.dynamic)
		{
			return Types.DYNAMIC;
		}
		return null;
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
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.theClass == null ? null : this.theClass.resolveTypeVariable(name);
	}
	
	@Override
	public IField resolveField(Name name)
	{
		if (this.arrayDimensions > 0)
		{
			return null;
		}
		
		return this.theClass == null ? null : this.theClass.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			Types.getObjectArray().getMethodMatches(list, instance, name, arguments);
			return;
		}
		
		if (this.theClass != null)
		{
			this.theClass.getMethodMatches(list, instance, name, arguments);
		}
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
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return this.theClass == null ? null : this.theClass.getFunctionalMethod();
	}
	
	// Compilation
	
	@Override
	public void setInternalName(String name)
	{
		this.internalName = name;
	}
	
	@Override
	public String getInternalName()
	{
		if (this.arrayDimensions > 0)
		{
			StringBuilder buf = new StringBuilder();
			this.appendExtendedName(buf);
			return buf.toString();
		}
		return this.internalName;
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append('L').append(this.internalName).append(';');
	}
	
	@Override
	public String getSignature()
	{
		return null;
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append('L').append(this.internalName).append(';');
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
		t.internalName = this.internalName;
		t.arrayDimensions = this.arrayDimensions;
		return t;
	}
}
