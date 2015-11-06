package dyvil.tools.compiler.ast.type.alias;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

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
	
	// Phases
	
	public void resolveTypes(MarkerList markers, IContext context);
	
	public void resolve(MarkerList markers, IContext context);
	
	public void checkTypes(MarkerList markers, IContext context);
	
	public void check(MarkerList markers, IContext context);
	
	public void foldConstants();
	
	public void cleanup(IContext context, IClassCompilableList compilableList);
	
	@Override
	public void write(DataOutput dos) throws IOException;
	
	@Override
	public void read(DataInput dis) throws IOException;
}
