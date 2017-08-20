package dyvilx.tools.compiler.ast.method.intrinsic;

import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class CompilerIntrinsic implements IntrinsicData
{
	private final int compilerCode;

	public CompilerIntrinsic(int compilerCode)
	{
		this.compilerCode = compilerCode;
	}

	@Override
	public int getCompilerCode()
	{
		return this.compilerCode;
	}

	@Override
	public void writeIntrinsic(MethodWriter writer, IValue receiver, ArgumentList arguments, int lineNumber)
		throws BytecodeException
	{
	}

	@Override
	public void writeIntrinsic(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments, int lineNumber)
		throws BytecodeException
	{
	}

	@Override
	public void writeInvIntrinsic(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments,
		                             int lineNumber) throws BytecodeException
	{
	}
}
