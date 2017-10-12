package dyvilx.tools.compiler.ast.expression.optional;

import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.constant.NullValue;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.statement.BindingIfStatement;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.compound.NullableType;

public interface OptionalChainAware extends IValue
{
	IValue getReceiver();

	void setReceiver(IValue value);

	@Override
	IType getType();

	@Override
	void setType(IType type);

	static IValue transform(IValue value)
	{
		if (value instanceof OptionalChainAware)
		{
			return transform((OptionalChainAware) value);
		}
		return value;
	}

	static IValue transform(OptionalChainAware oca)
	{
		// oca = receiver?.access
		//       <- oco ->

		final IValue oco = oca.getReceiver();

		if (oco == null || oco.valueTag() != IValue.OPTIONAL_CHAIN)
		{
			// no transformation needed as it is actually not an optional chain
			return oca;
		}

		final SourcePosition position = oca.getPosition();
		final IValue receiver = ((OptionalChainOperator) oco).getReceiver();
		// receiver is now the actual receiver of the optional chain operator

		BindingIfStatement bindingIf;
		if (receiver instanceof BindingIfStatement
		    && (bindingIf = (BindingIfStatement) receiver).getElse() == NullValue.NULL)
		{
			// safe bet that the receiver used to be an optional chain

			// Perform the following transformation (the entire statement is the receiver):
			// if let $0 = oldReceiver { $0.oldAccess } else null
			// becomes
			// if (let $0 = oldReceiver, let $1 = $0.oldAccess) { $1.access } else null

			final Variable var = newVar(position, bindingIf.getThen());
			bindingIf.addVariable(var);
			oca.setReceiver(new FieldAccess(var));
			bindingIf.setThen(oca);
			return bindingIf;
		}

		// oca = receiver?.access, and receiver is not an optional chain
		// receiver?.access
		// becomes
		// if let $0 = receiver { $0.access } else null

		final Variable var = newVar(position, receiver);

		bindingIf = new BindingIfStatement(position);
		bindingIf.addVariable(var);
		oca.setReceiver(new FieldAccess(var));
		bindingIf.setThen(oca);
		bindingIf.setElse(NullValue.NULL);
		return bindingIf;
	}

	static IValue nullCoalescing(IValue lhs, IValue rhs)
	{
		BindingIfStatement bindingIf;
		if (lhs instanceof BindingIfStatement && (bindingIf = (BindingIfStatement) lhs).getElse() == NullValue.NULL)
		{
			// safe bet that the rhs used to be an optional chain
			// this branch is actually an optimization and technically not necessary, but it is very common
			// to use the null coalescing operator after an optional chain.

			final IValue then = bindingIf.getThen();
			if (NullableType.isNullable(then.getType()))
			{
				// Perform the following transformation:
				// if let $0 = oldReceiver { $0.oldAccess } else null
				// becomes
				// if (let $0 = oldReceiver, let $1 = $0.oldAccess) { $1 } else { <rhs> }

				final Variable var = newVar(bindingIf.getPosition(), then);
				bindingIf.addVariable(var);
				bindingIf.setThen(new FieldAccess(var));
			}

			// if the then branch of the old binding if is not nullable, we simply set the rhs
			bindingIf.setElse(rhs);
			return bindingIf;
		}

		// the lhs was not an optional chain, so we set up an impromptu null coalescing inline implementation

		final SourcePosition position = lhs.getPosition();

		// let l = <lhs>
		final Variable var = newVar(position, lhs);

		// if let l = <lhs> { l } else { <rhs> }
		bindingIf = new BindingIfStatement(position);
		bindingIf.addVariable(var);
		bindingIf.setThen(new FieldAccess(var));
		bindingIf.setElse(rhs);
		return bindingIf;
	}

	/**
	 * Creates a new variable for use in binding if statements. The lhs automatically gets wrapped in an optional unwrap
	 * operator.
	 */
	static Variable newVar(SourcePosition position, IValue lhs)
	{
		final IValue value = new OptionalUnwrapOperator(lhs, true);
		final Variable var = new Variable(position, null, value.getType(), AttributeList.of(Modifiers.FINAL));
		var.setValue(value);
		return var;
	}
}
