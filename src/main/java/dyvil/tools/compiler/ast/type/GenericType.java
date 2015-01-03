package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.IClass;
import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.ast.api.ITypeList;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class GenericType extends Type implements ITypeList
{
	public List<IType>	generics	= new ArrayList();
	
	public GenericType()
	{
		super();
	}
	
	public GenericType(String name)
	{
		super(name);
	}
	
	public GenericType(String name, IClass iclass)
	{
		super(name, iclass);
	}
	
	public GenericType(ICodePosition position, String name)
	{
		super(position, name);
	}
	
	public GenericType(ICodePosition position, String name, IClass iclass)
	{
		super(position, name, iclass);
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
	
	@Override
	public String getSignature()
	{
		if (this.generics == null)
		{
			return null;
		}
		
		StringBuilder buf = new StringBuilder();
		this.appendSignature(buf);
		return buf.toString();
	}
	
	@Override
	public void appendSignature(StringBuilder buf)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buf.append('[');
		}
		buf.append('L').append(this.getInternalName());
		buf.append('<');
		for (IType t : this.generics)
		{
			t.appendSignature(buf);
		}
		buf.append('>').append(';');
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		buffer.append('<');
		Util.astToString(this.generics, Formatting.Type.genericSeperator, buffer);
		buffer.append('>');
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append(Formatting.Type.array);
		}
	}
}
