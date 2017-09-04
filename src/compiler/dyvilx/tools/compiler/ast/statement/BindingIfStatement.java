package dyvilx.tools.compiler.ast.statement;

import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.optional.OptionalUnwrapOperator;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.compound.NullableType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public class BindingIfStatement extends IfStatement implements IDefaultContext
{
	private IVariable variable;

	public BindingIfStatement(SourcePosition position)
	{
		super(position);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return name == this.variable.getName() ? this.variable : null;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.variable = ((VariableStatement) this.condition).variable;
		this.variable.setValue(new OptionalUnwrapOperator(this.variable.getValue(), true));

		super.resolveTypes(markers, context);
	}

	@Override
	protected IContext thenContext(IContext context)
	{
		return new CombiningContext(this, context);
	}

	private IValue getConditionValue()
	{
		final IValue varValue = this.variable.getValue();
		if (varValue.valueTag() == OPTIONAL_UNWRAP)
		{
			return ((OptionalUnwrapOperator) varValue).getReceiver();
		}

		return varValue;
	}

	@Override
	protected void checkConditionType(MarkerList markers, IContext context)
	{
		final IValue conditionValue = this.getConditionValue();
		final IType type = conditionValue.getType();
		if (!NullableType.isNullable(type))
		{
			final Marker marker = Markers.semanticError(conditionValue.getPosition(), "if.binding.nonnull");
			marker.addInfo(Markers.getSemantic("value.type", type));
			markers.add(marker);
		}
	}

	@Override
	protected void writeCondition(MethodWriter writer, Label elseStart)
	{
		final IValue nullable = this.getConditionValue();

		nullable.writeExpression(writer, null);
		final int varIndex = writer.localCount();

		// first store the nullable value in the variable
		writer.visitVarInsn(Opcodes.ASTORE, varIndex);
		writer.visitVarInsn(Opcodes.ALOAD, varIndex);

		// branch if necessary
		writer.visitJumpInsn(Opcodes.IFNULL, elseStart);

		// load and unwrap the variable
		writer.visitVarInsn(Opcodes.ALOAD, varIndex);
		writer.resetLocals(varIndex);

		nullable.getType().writeCast(writer, this.variable.getType(), nullable.lineNumber());

		// store, but this time with the right type
		this.variable.writeInit(writer, null);
	}

	@Override
	protected void headToString(String indent, StringBuilder buffer)
	{
		buffer.append(' ');
		this.condition.toString(indent, buffer);
	}
}
