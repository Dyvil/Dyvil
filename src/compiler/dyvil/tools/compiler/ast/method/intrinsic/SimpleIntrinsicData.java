package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class SimpleIntrinsicData implements IntrinsicData
{
	private final IMethod	method;
	private final int[]		opcodes;
	
	public SimpleIntrinsicData(IMethod method, int[] opcodes)
	{
		this.opcodes = opcodes;
		this.method = method;
	}
	
	@Override
	public void writeIntrinsic(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		for (int insn : this.opcodes)
		{
			this.writeInsn(writer, instance, arguments, lineNumber, insn);
		}
	}
	
	private void writeInsn(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber, int insn) throws BytecodeException
	{
		if (insn < 0)
		{
			IntrinsicData.writeArgument(writer, this.method, ~insn, // = -insn+1
					instance, arguments);
			return;
		}
		
		writer.writeInsn(insn, lineNumber);
	}
	
	@Override
	public void writeIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		int count = this.opcodes.length - 1;
		for (int i = 0; i < count; i++)
		{
			this.writeInsn(writer, instance, arguments, lineNumber, this.opcodes[i]);
		}
		
		int lastInsn = this.opcodes[count];
		int jumpInsn = Opcodes.getJumpOpcode(lastInsn);
		if (jumpInsn > 0)
		{
			writer.writeJumpInsn(jumpInsn, dest);
			return;
		}
		
		this.writeInsn(writer, instance, arguments, lineNumber, lastInsn);
		writer.writeJumpInsn(Opcodes.IFNE, dest);
	}
	
	@Override
	public void writeInvIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		int count = this.opcodes.length - 1;
		for (int i = 0; i < count; i++)
		{
			this.writeInsn(writer, instance, arguments, lineNumber, this.opcodes[i]);
		}
		
		int lastInsn = this.opcodes[count];
		int jumpInsn = Opcodes.getInvJumpOpcode(lastInsn);
		if (jumpInsn > 0)
		{
			writer.writeJumpInsn(jumpInsn, dest);
			return;
		}
		
		this.writeInsn(writer, instance, arguments, lineNumber, lastInsn);
		writer.writeJumpInsn(Opcodes.IFEQ, dest);
	}
}
