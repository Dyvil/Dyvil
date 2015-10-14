package dyvil.tools.compiler.ast.type.alias;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class TypeAlias implements ITypeAlias
{
	protected Name	name;
	protected IType	type;
	
	public TypeAlias()
	{
	}
	
	public TypeAlias(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
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
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.TYPE);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}
	
	@Override
	public void foldConstants()
	{
		this.type.foldConstants();
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
	}
	
	@Override
	public void write(DataOutput dos) throws IOException
	{
		dos.writeUTF(this.name.qualified);
		this.type.write(dos);
	}
	
	@Override
	public void read(DataInput dis) throws IOException
	{
		this.name = Name.getQualified(dis.readUTF());
		this.type = IType.readType(dis);
	}
	
	@Override
	public String toString()
	{
		return "type " + this.name + " = " + this.type;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("type ").append(this.name).append(Formatting.Field.keyValueSeperator);
		this.type.toString(prefix, buffer);
	}
}
