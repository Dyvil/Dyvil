package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.ast.api.ITypeList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class GenericType extends Type implements ITypeList
{
	public List<IType>	generics	= new ArrayList();
	
	public GenericType(ICodePosition position, String name)
	{
		super(position, name);
	}
	
	@Override
	public void setTypes(List<IType> types)
	{
		this.generics = types;
	}
	
	@Override
	public List<IType> getTypes()
	{
		return this.generics;
	}
	
	@Override
	public void addType(IType type)
	{
		this.generics.add(type);
	}
}
