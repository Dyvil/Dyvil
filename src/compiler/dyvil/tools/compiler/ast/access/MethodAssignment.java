package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class MethodAssignment extends AbstractCall implements IValue
{
	public MethodAssignment(ICodePosition position, IValue receiver, IMethod method, IArguments argument)
	{
		this.position = position;
		this.receiver = receiver;
		this.method = method;
		this.arguments = argument;
	}

	@Override
	public int valueTag()
	{
		return METHOD_ASSIGN;
	}

	@Override
	public IValue resolveCall(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public void reportResolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (Types.isVoid(type))
		{
			super.writeExpression(writer, type);
			return;
		}

		final IValue expression = this.arguments.getFirstValue();
		final Variable receiverVar = new Variable(null, this.receiver.getType(), this.receiver);
		final Variable expressionVar = new Variable(null, expression.getType(), expression);
		receiverVar.writeInit(writer);
		expressionVar.writeInit(writer);

		this.method.writeCall(writer, new FieldAccess(receiverVar), new SingleArgument(new FieldAccess(expressionVar)),
		                      this.genericData, Types.VOID, this.getLineNumber());

		expressionVar.writeGet(writer);
	}

	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.writeExpression(writer, Types.BOOLEAN);
		writer.visitJumpInsn(Opcodes.IFNE, dest);
	}

	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.writeExpression(writer, Types.BOOLEAN);
		writer.visitJumpInsn(Opcodes.IFEQ, dest);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
			buffer.append('.');
		}
		buffer.append(this.method.getName());
		buffer.append(' ');
		this.arguments.getFirstValue().toString(prefix, buffer);
	}
}
