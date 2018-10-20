package dyvilx.tools.compiler.ast.statement;

import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.CastOperator;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.constant.BooleanValue;
import dyvilx.tools.compiler.ast.expression.optional.OptionalUnwrapOperator;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.compound.NullableType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.ArrayList;
import java.util.List;

public class BindingIfStatement extends IfStatement
{
	private List<IVariable> variables = new ArrayList<>(1);

	public BindingIfStatement(SourcePosition position)
	{
		super(position);
		this.condition = BooleanValue.TRUE;
	}

	// region Variables

	public void addVariable(IVariable variable)
	{
		this.variables.add(variable);
	}

	// endregion

	// region Phase Overrides

	@Override
	protected IContext thenContext(IContext context)
	{
		return this.varContext(this.variables.size(), context);
	}

	private IContext varContext(int maxIndex, IContext outer)
	{
		assert maxIndex <= this.variables.size();

		final IContext inner = new IDefaultContext()
		{
			@Override
			public IDataMember resolveField(Name name)
			{
				for (int i = 0; i < maxIndex; i++)
				{
					final IVariable var = BindingIfStatement.this.variables.get(i);
					if (var.getName() == name)
					{
						return var;
					}
				}
				return null;
			}

			@Override
			public boolean isMember(IVariable variable)
			{
				// in this case checking the index makes no difference
				return BindingIfStatement.this.variables.contains(variable);
			}
		};
		return new CombiningContext(inner, outer);
	}

	@Override
	protected void resolveConditionTypes(MarkerList markers, IContext context)
	{
		final int size = this.variables.size();
		for (int i = 0; i < size; i++)
		{
			final IVariable var = this.variables.get(i);
			var.setValue(new OptionalUnwrapOperator(var.getValue(), true));
			var.resolveTypes(markers, this.varContext(i, context));
		}

		super.resolveConditionTypes(markers, this.varContext(size, context));
	}

	@Override
	protected void resolveCondition(MarkerList markers, IContext context)
	{
		final int size = this.variables.size();
		for (int i = 0; i < size; i++)
		{
			this.variables.get(i).resolve(markers, this.varContext(i, context));
		}

		super.resolveCondition(markers, this.varContext(size, context));
	}

	@Override
	protected void checkConditionTypes(MarkerList markers, IContext context)
	{
		final int size = this.variables.size();
		for (int i = 0; i < size; i++)
		{
			this.variables.get(i).checkTypes(markers, this.varContext(i, context));
		}

		super.checkConditionTypes(markers, this.varContext(size, context));
	}

	@Override
	protected void checkCondition(MarkerList markers, IContext context)
	{
		final int size = this.variables.size();
		for (int i = 0; i < size; i++)
		{
			final IVariable var = this.variables.get(i);
			var.check(markers, this.varContext(i, context));

			final IValue value = getOptionalValue(var);

			final IType type = value.getType();
			if (value.isResolved() && type.isResolved() && !NullableType.isNullable(type))
			{
				final Marker marker = Markers.semanticError(value.getPosition(), "if.binding.nonnull");
				marker.addInfo(Markers.getSemantic("value.type", type));
				markers.add(marker);
			}
		}

		super.checkCondition(markers, this.varContext(size, context));
	}

	@Override
	public IValue foldConstants()
	{
		for (IVariable var : this.variables)
		{
			var.foldConstants();
		}

		return super.foldConstants();
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (IVariable var : this.variables)
		{
			var.cleanup(compilableList, classCompilableList);
		}

		return super.cleanup(compilableList, classCompilableList);
	}

	@Override
	protected IValue foldWithCondition()
	{
		return this;
	}

	// endregion

	// region Compilation

	private static IValue getOptionalValue(IVariable var)
	{
		return ((OptionalUnwrapOperator) var.getValue()).getReceiver();
	}

	@Override
	protected void writeCondition(MethodWriter writer, Label elseStart)
	{
		for (IVariable var : this.variables)
		{
			IValue value = getOptionalValue(var);
			final CastOperator castOp;
			final int localCount = writer.localCount();
			final int varIndex;

			if (value instanceof CastOperator && (castOp = (CastOperator) value).isOptional())
			{
				// optimization for optional cast operators

				value = castOp.getValue();
				varIndex = value.writeStoreLoad(writer, null);

				// branch if necessary (also branches if null)
				writer.visitTypeInsn(Opcodes.INSTANCEOF, castOp.getType().getInternalName());
				writer.visitJumpInsn(Opcodes.IFEQ, elseStart);
			}
			else
			{
				varIndex = value.writeStoreLoad(writer, null);

				// branch if necessary
				writer.visitJumpInsn(Opcodes.IFNULL, elseStart);
			}

			// load and unwrap the variable
			writer.visitVarInsn(Opcodes.ALOAD, varIndex);
			writer.resetLocals(localCount);

			value.getType().writeCast(writer, var.getType(), value.lineNumber());

			// store, but this time with the right type
			var.writeInit(writer, null);
		}

		super.writeCondition(writer, elseStart);
	}

	// endregion

	@Override
	protected void conditionToString(String indent, StringBuilder buffer)
	{
		for (IVariable var : this.variables)
		{
			var.toString(indent, buffer);
			buffer.append(", ");
		}
		this.condition.toString(indent, buffer);
	}
}
