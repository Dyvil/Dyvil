package dyvilx.tools.compiler.ast.type.alias;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeParametric;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.header.IObjectCompilable;
import dyvilx.tools.compiler.ast.method.IOverloadable;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvil.lang.Name;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ITypeAlias extends ASTNode, Resolvable, IOverloadable, ITypeParametric, IObjectCompilable
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

	// Resolution

	void checkMatch(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments);

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
