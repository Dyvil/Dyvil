package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.type.ArrayType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.parsing.Name;

public class Intrinsics
{
	private static final MethodParameter STRINGS = new MethodParameter(Name.getQualified("strings"), new ArrayType(Types.STRING));
	
	public static IntrinsicData readAnnotation(IMethod method, IAnnotation annotation)
	{
		IArguments arguments = annotation.getArguments();
		IValue value = arguments.getValue(0, Annotation.VALUE);
		if (value.valueTag() != IValue.ARRAY)
		{
			return null;
		}
		
		IValue strings = arguments.getValue(1, STRINGS);
		
		Array values = (Array) value;
		
		int length = values.valueCount();
		int count = 0;
		boolean complex = false;
		
		for (int i = 0; i < length; i++)
		{
			value = values.getValue(i);
			int opcode = value.intValue();
			count++;
			
			if (Opcodes.isFieldOrMethodOpcode(opcode))
			{
				i += 3;
				complex = true;
			}
			else if (Opcodes.isJumpOpcode(opcode))
			{
				i += 1;
				complex = true;
			}
		}
		
		if (complex)
		{
			// return readComplex(method, values, strings);
		}
		
		int[] ints = new int[count];
		for (int i = 0; i < length; i++)
		{
			ints[i] = values.getValue(i).intValue();
		}
		return new SimpleIntrinsicData(method, ints);
	}
}
