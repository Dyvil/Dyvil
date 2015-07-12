package dyvil.tools.compiler.ast.generic.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class ClassGenericType extends GenericType
{
	protected IClass	theClass;
	
	public ClassGenericType()
	{
	}
	
	public ClassGenericType(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	public ClassGenericType(IClass iclass, IType[] typeArguments, int typeArgumentCount)
	{
		super(typeArguments, typeArgumentCount);
		this.theClass = iclass;
	}
	
	@Override
	public int typeTag()
	{
		return GENERIC;
	}
	
	// ITypeList Overrides
	
	@Override
	public boolean isGenericType()
	{
		return this.theClass.isGeneric();
	}
	
	@Override
	public Name getName()
	{
		return this.theClass.getName();
	}
	
	// IType Overrides
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	@Override
	public boolean equals(IType type)
	{
		if (this == type)
		{
			return true;
		}
		
		if (!super.equals(type))
		{
			return false;
		}
		
		return this.argumentsMatch(type);
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (this == type)
		{
			return true;
		}
		
		if (!super.isSuperTypeOf(type))
		{
			return false;
		}
		
		return !type.isGenericType() || this.argumentsMatch(type);
	}
	
	protected boolean argumentsMatch(IType type)
	{
		int count = Math.min(this.typeArgumentCount, this.theClass.genericCount());
		for (int i = 0; i < count; i++)
		{
			ITypeVariable typeVar = this.theClass.getTypeVariable(i);
			
			IType otherType = type.resolveType(typeVar);
			if (!typeVar.getVariance().checkCompatible(this.typeArguments[i], otherType))
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public IType combine(IType type)
	{
		if (this.argumentsMatch(type))
		{
			return this;
		}
		
		return new ClassType(this.theClass);
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		int index = typeVar.getIndex();
		if (this.theClass.getTypeVariable(index) != typeVar)
		{
			return this.theClass.resolveType(typeVar, this);
		}
		return this.typeArguments[index];
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			if (this.typeArguments[i].hasTypeVariables())
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		ClassGenericType copy = this.clone();
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			copy.typeArguments[i] = this.typeArguments[i].getConcreteType(context);
		}
		return copy;
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			ITypeVariable typeVar = this.theClass.getTypeVariable(i);
			IType concreteType = concrete.resolveType(typeVar);
			this.typeArguments[i].inferTypes(concreteType, typeContext);
		}
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
	{
		return this;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return this.theClass.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.theClass.getConstructorMatches(list, arguments);
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return this.theClass.getFunctionalMethod();
	}
	
	@Override
	public String getInternalName()
	{
		return this.theClass.getInternalName();
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.theClass.getInternalName());
		this.writeTypeArguments(out);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		String internal = in.readUTF();
		this.theClass = Package.rootPackage.resolveInternalClass(internal);
		this.readTypeArguments(in);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(this.theClass.getFullName());
		this.appendFullTypes(sb);
		return sb.toString();
	}
	
	@Override
	public ClassGenericType clone()
	{
		ClassGenericType t = new ClassGenericType(this.theClass);
		this.copyTypeArguments(t);
		return t;
	}
}
