package dyvilx.tools.compiler.ast.method.intrinsic;

import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.parameter.ParameterList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

import static dyvil.reflect.Modifiers.INFIX;

public interface IntrinsicData
{
	default int getCompilerCode()
	{
		return 0;
	}

	void writeIntrinsic(MethodWriter writer, IValue receiver, ArgumentList arguments, int lineNumber)
		throws BytecodeException;

	void writeIntrinsic(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments, int lineNumber)
		throws BytecodeException;

	void writeInvIntrinsic(MethodWriter writer, Label dest, IValue receiver, ArgumentList arguments, int lineNumber)
		throws BytecodeException;

	static void writeInsn(MethodWriter writer, IMethod method, int insn, IValue receiver, ArgumentList arguments,
		                     int lineNumber) throws BytecodeException
	{
		if (insn < 0)
		{
			IntrinsicData.writeArgument(writer, method, ~insn, // = -insn+1
			                            receiver, arguments);
			return;
		}

		writer.visitInsnAtLine(insn, lineNumber);
	}

	static IType writeArgument(MethodWriter writer, IMethod method, int index, IValue receiver, ArgumentList arguments)
		throws BytecodeException
	{
		final ParameterList params = method.getParameters();

		if (receiver == null || receiver.isIgnoredClassAccess())
		{
			final IParameter parameter = params.get(index);
			arguments.writeValue(index, parameter, writer);
			return parameter.getCovariantType();
		}

		if (index == 0)
		{
			final IType type = method.hasModifier(INFIX) ? params.get(0).getCovariantType() : method.getReceiverType();
			receiver.writeExpression(writer, type);
			return type;
		}

		final IParameter parameter = params.get(method.hasModifier(INFIX) ? index : index - 1);
		arguments.writeValue(index - 1, parameter, writer);
		return parameter.getCovariantType();
	}
}
