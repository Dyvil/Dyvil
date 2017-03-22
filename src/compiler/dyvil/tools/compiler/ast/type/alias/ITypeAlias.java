package dyvil.tools.compiler.ast.type.alias;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.generic.TypeParameterList;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.header.IObjectCompilable;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.phase.IResolvable;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ITypeAlias extends IASTNode, IResolvable, INamed, ITyped, ITypeParametric, IObjectCompilable
{
	IHeaderUnit getEnclosingHeader();

	void setEnclosingHeader(IHeaderUnit header);

	@Override
	void setName(Name name);
	
	@Override
	Name getName();
	
	@Override
	void setType(IType type);
	
	@Override
	IType getType();

	@Override
	boolean isTypeParametric();

	@Override
	TypeParameterList getTypeParameters();

	// Phases
	
	@Override
	void resolveTypes(MarkerList markers, IContext context);
	
	@Override
	void resolve(MarkerList markers, IContext context);
	
	@Override
	void checkTypes(MarkerList markers, IContext context);
	
	@Override
	void check(MarkerList markers, IContext context);
	
	@Override
	void foldConstants();

	@Override
	void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);

	@Override
	void write(DataOutput out) throws IOException;
	
	@Override
	void read(DataInput in) throws IOException;
}
