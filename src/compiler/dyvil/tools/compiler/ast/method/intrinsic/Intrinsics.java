package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
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
		
		ArrayExpr values = (ArrayExpr) value;
		
		int length = values.valueCount();
		int count = 0;
		
		int[] ints = new int[length];
		
		for (int i = 0; i < length; i++)
		{
			int opcode = values.getValue(i).intValue();
			ints[i] = opcode;
			
			count++;
			
			if (Opcodes.isFieldOrMethodOpcode(opcode))
			{
				ints[i + 1] = values.getValue(i + 1).intValue();
				ints[i + 2] = values.getValue(i + 2).intValue();
				ints[i + 3] = values.getValue(i + 3).intValue();
				i += 3;
			}
			else if (Opcodes.isJumpOpcode(opcode) || opcode == Opcodes.LDC || opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH)
			{
				ints[i + 1] = values.getValue(i + 1).intValue();
				i += 1;
			}
		}
		
		if (length > count)
		{
			IValue stringValue = arguments.getValue(1, STRINGS);
			ArrayExpr strings = (ArrayExpr) stringValue;
			
			return readComplex(method, count, ints, strings);
		}
		
		return new SimpleIntrinsicData(method, ints);
	}
	
	private static IntrinsicData readComplex(IMethod method, int count, int[] ints, ArrayExpr stringArray)
	{
		String[] strings;
		
		if (stringArray != null)
		{
			// Convert string constants to an array
			int stringCount = stringArray.valueCount();
			strings = new String[stringCount];
			for (int i = 0; i < stringCount; i++)
			{
				strings[i] = stringArray.getValue(i).stringValue();
			}
		}
		else
		{
			strings = null;
		}
		
		// Convert jump instructions into a label array
		int length = ints.length;
		Label[] labels = new Label[count];
		for (int i = 0; i < length; i++)
		{
			int opcode = ints[i];
			
			if (Opcodes.isFieldOrMethodOpcode(opcode))
			{
				i += 3;
			}
			else if (Opcodes.isJumpOpcode(opcode))
			{
				i += 1;
				int target = ints[i + 1];
				labels[target] = new Label();
			}
		}
		
		return new SpecialIntrinsicData(method, ints, strings, labels);
	}
}
