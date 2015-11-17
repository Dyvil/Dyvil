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
	void setName(Name name);
	
	@Override
	Name getName();
	
	@Override
	void setType(IType type);
	
	@Override
	IType getType();
	
	// Phases
	
	void resolveTypes(MarkerList markers, IContext context);
	
	void resolve(MarkerList markers, IContext context);
	
	void checkTypes(MarkerList markers, IContext context);
	
	void check(MarkerList markers, IContext context);
	
	void foldConstants();
	
	void cleanup(IContext context, IClassCompilableList compilableList);
	
	@Override
	void write(DataOutput dos) throws IOException;
	
	@Override
	void read(DataInput dis) throws IOException;
}
