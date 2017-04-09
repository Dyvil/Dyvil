package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class SimpleIntrinsicData implements IntrinsicData
{
	private final IMethod method;
	private final int[]   opcodes;

	public SimpleIntrinsicData(IMethod method, int[] opcodes)
	{
		this.opcodes = opcodes;
		this.method = method;
	}

	@Override
	public void writeIntrinsic(MethodWriter writer, IValue receiver, ArgumentList arguments, int lineNumber)
		throws BytecodeException
	{
		for (int insn : this.opcodes)
		{
			IntrinsicData.writeInsn(writer, this.method, insn, receiver, arguments, lineNumber);
		}
	}

	@Override
	public void writeIntrinsic(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments, int lineNumber)
		throws BytecodeException
	{
		final int count = this.opcodes.length - 1;
		for (int i = 0; i < count; i++)
		{
			IntrinsicData.writeInsn(writer, this.method, this.opcodes[i], receiver, arguments, lineNumber);
		}

		final int lastInsn = this.opcodes[count];
		final int jumpInsn = Opcodes.getJumpOpcode(lastInsn);
		if (jumpInsn > 0)
		{
			writer.visitJumpInsn(jumpInsn, dest);
			return;
		}

		IntrinsicData.writeInsn(writer, this.method, lastInsn, receiver, arguments, lineNumber);
		writer.visitJumpInsn(Opcodes.IFNE, dest);
	}

	@Override
	public void writeInvIntrinsic(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments,
		                             int lineNumber) throws BytecodeException
	{
		final int count = this.opcodes.length - 1;
		for (int i = 0; i < count; i++)
		{
			IntrinsicData.writeInsn(writer, this.method, this.opcodes[i], receiver, arguments, lineNumber);
		}

		final int lastInsn = this.opcodes[count];
		final int jumpInsn = Opcodes.getInvJumpOpcode(lastInsn);
		if (jumpInsn > 0)
		{
			writer.visitJumpInsn(jumpInsn, dest);
			return;
		}

		IntrinsicData.writeInsn(writer, this.method, lastInsn, receiver, arguments, lineNumber);
		writer.visitJumpInsn(Opcodes.IFEQ, dest);
	}
}
