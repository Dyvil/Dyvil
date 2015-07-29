package dyvil.tools.compiler.ast.type.alias;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface ITypeAlias extends IASTNode, INamed, ITyped, IObjectCompilable
{
	@Override
	public void setName(Name name);
	
	@Override
	public Name getName();
	
	@Override
	public void setType(IType type);
	
	@Override
	public IType getType();
	
	public void resolve(MarkerList markers, IContext context);
	
	@Override
	public void write(DataOutput dos) throws IOException;
	
	@Override
	public void read(DataInput dis) throws IOException;
}
