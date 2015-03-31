package dyvil.tools.compiler.ast.generic;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class TypeVariableType extends ASTNode implements IType
{
	public int				arrayDimensions;
	public ITypeVariable	typeVar;
	
	public TypeVariableType(ITypeVariable typeVar)
	{
		this.typeVar = typeVar;
	}
	
	@Override
	public void setName(Name name)
	{
	}
	
	@Override
	public Name getName()
	{
		return null;
	}
	
	@Override
	public void setFullName(String name)
	{
	}
	
	@Override
	public String getFullName()
	{
		return null;
	}
	
	@Override
	public void setClass(IClass theClass)
	{
	}
	
	@Override
	public IClass getTheClass()
	{
		return null;
	}
	
	@Override
	public void setArrayDimensions(int dimensions)
	{
	}
	
	@Override
	public int getArrayDimensions()
	{
		return 0;
	}
	
	@Override
	public boolean isArrayType()
	{
		return false;
	}
	
	@Override
	public IType getSuperType()
	{
		return null;
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (this.arrayDimensions != type.getArrayDimensions())
		{
			return false;
		}
		return this.typeVar.isSuperTypeOf(type);
	}
	
	@Override
	public boolean isSuperTypeOf2(IType type)
	{
		return this.typeVar.isSuperTypeOf(type);
	}
	
	@Override
	public boolean equals(IType type)
	{
		if (this.arrayDimensions != type.getArrayDimensions())
		{
			return false;
		}
		return this.typeVar.isSuperTypeOf(type);
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		return this.typeVar.isSuperTypeOf(type);
	}
	
	@Override
	public IType resolveType(Name name)
	{
		if (this.typeVar.getName() == name)
		{
			return this;
		}
		return null;
	}
	
	@Override
	public IType resolveType(Name name, IType concrete)
	{
		if (this.typeVar.getName() == name && this.typeVar.isSuperTypeOf(concrete))
		{
			return concrete;
		}
		return null;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return true;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		IType t = context.resolveType(this.typeVar.getName());
		if (t != null)
		{
			if (this.arrayDimensions > 0)
			{
				return t.getArrayType(this.arrayDimensions);
			}
			return t;
		}
		return null;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public IType getThisType()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return null;
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public FieldMatch resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, Name name, IArguments arguments)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
	}
	
	@Override
	public ConstructorMatch resolveConstructor(IArguments arguments)
	{
		return null;
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public String getInternalName()
	{
		if (this.typeVar.upperBoundCount() > 0)
		{
			return this.typeVar.getUpperBound(0).getInternalName();
		}
		return "java/lang/Object";
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
	public void appendSignature(StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append('T').append(this.typeVar.getName().qualified).append(';');
	}
	
	@Override
	public int getLoadOpcode()
	{
		return 0;
	}
	
	@Override
	public int getArrayLoadOpcode()
	{
		return 0;
	}
	
	@Override
	public int getStoreOpcode()
	{
		return 0;
	}
	
	@Override
	public int getArrayStoreOpcode()
	{
		return 0;
	}
	
	@Override
	public int getReturnOpcode()
	{
		return 0;
	}
	
	@Override
	public IType clone()
	{
		return new TypeVariableType(this.typeVar);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.typeVar.getName());
	}
}
