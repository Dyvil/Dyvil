package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.CodeParameter;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.parsing.Name;

public class Intrinsics
{
	private static final CodeParameter STRINGS = new CodeParameter(Name.fromRaw("strings"),
	                                                               new ArrayType(Types.STRING));

	public static IntrinsicData readAnnotation(IMethod method, IAnnotation annotation)
	{
		IArguments arguments = annotation.getArguments();
		IValue value = arguments.getValue(0, Annotation.VALUE);
		if (value == null || value.valueTag() != IValue.ARRAY)
		{
			return null;
		}

		ArrayExpr values = (ArrayExpr) value;

		int length = values.valueCount();
		int insnCount = 0;

		int[] ints = new int[length];

		for (int i = 0; i < length; i++)
		{
			int opcode = values.getValue(i).intValue();
			ints[i] = opcode;

			insnCount++;

			if (Opcodes.isFieldOrMethodOpcode(opcode))
			{
				ints[i + 1] = values.getValue(i + 1).intValue();
				ints[i + 2] = values.getValue(i + 2).intValue();
				ints[i + 3] = values.getValue(i + 3).intValue();
				i += 3;
			}
			else if (Opcodes.isJumpOpcode(opcode) || Opcodes.isLoadOpcode(opcode) || Opcodes.isStoreOpcode(opcode)
				         || opcode == Opcodes.LDC || opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH)
			{
				ints[i + 1] = values.getValue(i + 1).intValue();
				i += 1;
			}
		}

		if (length > insnCount)
		{
			IValue stringValue = arguments.getValue(1, STRINGS);
			ArrayExpr strings = (ArrayExpr) stringValue;

			return readComplex(method, insnCount, ints, strings);
		}

		return new SimpleIntrinsicData(method, ints);
	}

	private static IntrinsicData readComplex(IMethod method, int insnCount, int[] ints, ArrayExpr stringArray)
	{
		String[] strings;

		if (stringArray != null)
		{
			// Convert string constants to an array
			final int stringCount = stringArray.valueCount();

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
		final boolean[] labels = new boolean[insnCount + 1];
		for (int i = 0, length = ints.length; i < length; i++)
		{
			final int opcode = ints[i];

			if (Opcodes.isFieldOrMethodOpcode(opcode))
			{
				i += 3;
			}
			else if (Opcodes.isJumpOpcode(opcode))
			{
				final int target = ints[i + 1];
				labels[target] = true;
				i += 1;
			}
		}

		return new SpecialIntrinsicData(method, ints, strings, labels);
	}
}
