package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public interface IntrinsicData
{
	public void writeIntrinsic(MethodWriter writer, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException;
	
	public void writeIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException;
	
	public void writeInvIntrinsic(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException;
	
	public static void writeArgument(MethodWriter writer, IMethod method, int index, IValue instance, IArguments arguments) throws BytecodeException
	{
		if (instance == null)
		{
			arguments.writeValue(index, method.getParameter(index), writer);
			return;
		}
		
		if (index == 0)
		{
			if (method.hasModifier(Modifiers.INFIX))
			{
				instance.writeExpression(writer, method.getParameter(0).getType());
				return;
			}
			
			if (instance.isPrimitive())
			{
				instance.writeExpression(writer);
				return;
			}
			
			instance.writeExpression(writer, method.getTheClass().getType());
			return;
		}
		
		if (method.hasModifier(Modifiers.INFIX))
		{
			arguments.writeValue(index - 1, method.getParameter(index), writer);
			return;
		}
		
		arguments.writeValue(index - 1, method.getParameter(index - 1), writer);
	}
}
