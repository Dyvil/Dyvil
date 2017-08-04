package dyvil.tools.compiler.ast.expression.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;

public class MethodAssignment extends MethodCall
{
	public MethodAssignment(SourcePosition position, IValue receiver, IMethod method, ArgumentList argument)
	{
		super(position, receiver, method, argument);
	}

	public MethodAssignment(SourcePosition position, IValue receiver, Name name, ArgumentList argument)
	{
		super(position, receiver, name, argument);
	}

	@Override
	public int valueTag()
	{
		return METHOD_ASSIGN;
	}

	@Override
	protected Name getReferenceName()
	{
		return null;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (Types.isVoid(type))
		{
			super.writeExpression(writer, type);
			return;
		}

		final IValue expression = this.arguments.getFirst();
		final Variable receiverVar = new Variable(null, this.receiver.getType(), this.receiver);
		final Variable expressionVar = new Variable(null, expression.getType(), expression);
		receiverVar.writeInit(writer);
		expressionVar.writeInit(writer);

		this.method.writeCall(writer, new FieldAccess(receiverVar), new ArgumentList(new FieldAccess(expressionVar)),
		                      this.genericData, Types.VOID, this.lineNumber());

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
		this.arguments.getFirst().toString(prefix, buffer);
	}
}
