package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

import static dyvil.reflect.Opcodes.*;

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
		for (int i : this.opcodes)
		{
			switch (i)
			{
			case LOAD_0:
				IntrinsicData.writeArgument(writer, method, 0, instance, arguments);
				continue;
			case LOAD_1:
				IntrinsicData.writeArgument(writer, method, 0, instance, arguments);
				continue;
			case LOAD_2:
				IntrinsicData.writeArgument(writer, method, 0, instance, arguments);
				continue;
			}
			
			writer.writeInsn(i, lineNumber);
		}
	}
	
	@Override
	public void writeIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		this.writeIntrinsic(writer, instance, arguments, lineNumber);
		writer.writeJumpInsn(Opcodes.IFEQ, dest);
	}
	
	@Override
	public void writeInvIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		this.writeIntrinsic(writer, instance, arguments, lineNumber);
		writer.writeJumpInsn(Opcodes.IFNE, dest);
	}
}
