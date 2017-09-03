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
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.parsing.marker.MarkerList;

public class BindingIfStatement extends IfStatement implements IDefaultContext
{
	private IVariable variable;
	private IValue    nullable;

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
		this.nullable = this.variable.getValue();
		this.variable.setValue(new OptionalUnwrapOperator(this.nullable, true));

		super.resolveTypes(markers, context);
	}

	@Override
	protected IContext thenContext(IContext context)
	{
		return new CombiningContext(this, context);
	}

	@Override
	protected void checkConditionType(MarkerList markers, IContext context)
	{
	}

	@Override
	protected void writeCondition(MethodWriter writer, Label elseStart)
	{
		this.nullable.writeExpression(writer, null);
		final int varIndex = writer.localCount();

		// first store the nullable value in the variable
		writer.visitVarInsn(Opcodes.ASTORE, varIndex);
		writer.visitVarInsn(Opcodes.ALOAD, varIndex);

		// branch if necessary
		writer.visitJumpInsn(Opcodes.IFNULL, elseStart);

		// load and unwrap the variable
		writer.visitVarInsn(Opcodes.ALOAD, varIndex);
		writer.resetLocals(varIndex);

		this.nullable.getType().writeCast(writer, this.variable.getType(), this.nullable.lineNumber());

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
