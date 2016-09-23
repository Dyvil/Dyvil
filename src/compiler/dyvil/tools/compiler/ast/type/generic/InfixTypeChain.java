package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.operator.OperatorElement;
import dyvil.tools.compiler.ast.operator.OperatorStack;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.raw.IUnresolvedType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class InfixTypeChain extends OperatorStack<IType> implements IUnresolvedType
{
	@Override
	protected IType binaryOp(IType lhs, OperatorElement operator, IType rhs)
	{
		return new InfixType(operator.position, lhs, operator.name, rhs);
	}

	@Override
	protected IType ternaryOp(IType lhs, OperatorElement operator1, IType center, OperatorElement operator2, IType rhs)
	{
		// right-associative: T ? U : V becomes T ? (U : V)
		return this.binaryOp(lhs, operator1, this.binaryOp(center, operator2, rhs));
	}

	@Override
	public int typeTag()
	{
		return INFIX_CHAIN;
	}

	@Override
	public Name getName()
	{
		return this.operators[0].name;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.resolveOperators(markers, context);

		final IType treeify = this.treeify(markers);
		return treeify.resolveType(markers, context);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		// TODO
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		// TODO
	}

	@Override
	public IType clone()
	{
		return this;
	}
}
