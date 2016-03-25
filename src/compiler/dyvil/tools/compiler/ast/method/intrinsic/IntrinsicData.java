package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IntrinsicData
{
	void writeIntrinsic(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber)
			throws BytecodeException;
	
	void writeIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber)
			throws BytecodeException;
	
	void writeInvIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber)
			throws BytecodeException;
	
	static IType writeArgument(MethodWriter writer, IMethod method, int index, IValue instance, IArguments arguments)
			throws BytecodeException
	{
		if (instance == null)
		{
			final IParameter parameter = method.getParameter(index);
			arguments.writeValue(index, parameter, writer);
			return parameter.getInternalType();
		}
		
		if (index == 0)
		{
			if (method.hasModifier(Modifiers.INFIX))
			{
				final IType internalParameterType = method.getParameter(0).getInternalType();
				instance.writeExpression(writer, internalParameterType);
				return internalParameterType;
			}

			final IType type = method.getEnclosingClass().getType();
			instance.writeExpression(writer, type);
			return type;
		}
		
		if (method.hasModifier(Modifiers.INFIX))
		{
			final IParameter parameter = method.getParameter(index);
			arguments.writeValue(index - 1, parameter, writer);
			return parameter.getInternalType();
		}

		final IParameter parameter = method.getParameter(index - 1);
		arguments.writeValue(index - 1, parameter, writer);
		return parameter.getInternalType();
	}
}
