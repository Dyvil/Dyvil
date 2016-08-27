package dyvil.tools.compiler.ast.type.alias;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface ITypeAlias extends IASTNode, INamed, ITyped, ITypeParametric, IObjectCompilable
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
	void addTypeParameter(ITypeParameter typeParameter);

	@Override
	int typeParameterCount();

	@Override
	ITypeParameter getTypeParameter(int index);

	@Override
	ITypeParameter[] getTypeParameters();

	@Override
	boolean isTypeParametric();

	@Override
	void setTypeParametric();

	@Override
	void setTypeParameter(int index, ITypeParameter typeParameter);

	@Override
	void setTypeParameters(ITypeParameter[] typeParameters, int count);

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
