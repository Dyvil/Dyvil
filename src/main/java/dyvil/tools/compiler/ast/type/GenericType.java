package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.ITypeList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class GenericType extends Type implements ITypeList
{
	public List<Type>	generics	= new ArrayList();
	
	public GenericType(String name, ICodePosition position)
	{
		super(name, position);
	}
	
	@Override
	public void setTypes(List<Type> types)
	{
		this.generics = types;
	}
	
	@Override
	public List<Type> getTypes()
	{
		return this.generics;
	}
	
	@Override
	public void addType(Type type)
	{
		this.generics.add(type);
	}
}
