package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.annotation.Intrinsic;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.intrinsic.*;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.operator.VarargsOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.reference.ReferenceOperator;
import dyvil.tools.compiler.ast.type.builtin.Types;

public class Intrinsics
{
	private static class LazyFields
	{
		public static final IParameter VALUE;
		public static final IParameter STRINGS;
		public static final IParameter COMPILER_CODE;

		static
		{
			final IParameterList params = Types.INTRINSIC_CLASS.getParameterList();
			VALUE = params.get(0);
			STRINGS = params.get(1);
			COMPILER_CODE = params.get(2);
		}
	}

	public static IValue getOperator(int code, IValue lhs, IArguments arguments)
	{
		switch (code)
		{
		case Intrinsic.BOOLEAN_NOT:
			return new NotOperator(arguments.getFirstValue());
		case Intrinsic.BOOLEAN_OR:
			return new OrOperator(lhs, arguments.getFirstValue());
		case Intrinsic.BOOLEAN_AND:
			return new AndOperator(lhs, arguments.getFirstValue());
		case Intrinsic.REFERENCE:
			return new ReferenceOperator(arguments.getFirstValue());
		case Intrinsic.ARRAY_SPREAD:
			return new VarargsOperator(lhs);
		case Intrinsic.STRING_CONCAT:
			return StringConcatExpr.apply(lhs, arguments.getFirstValue());
		case Intrinsic.PRE_INCREMENT:
			return IncOperator.apply(arguments.getFirstValue(), 1, true);
		case Intrinsic.POST_INCREMENT:
			return IncOperator.apply(lhs, 1, false);
		case Intrinsic.PRE_DECREMENT:
			return IncOperator.apply(arguments.getFirstValue(), -1, true);
		case Intrinsic.POST_DECREMENT:
			return IncOperator.apply(lhs, -1, false);
		}
		return null;
	}

	public static IntrinsicData readAnnotation(IMethod method, IAnnotation annotation)
	{
		final IArguments arguments = annotation.getArguments();
		final IValue compilerCode = arguments.getValue(2, LazyFields.COMPILER_CODE);
		if (compilerCode != null && compilerCode.valueTag() == IValue.INT)
		{
			return new CompilerIntrinsic(compilerCode.intValue());
		}

		final IValue value = arguments.getValue(0, LazyFields.VALUE);
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
			IValue stringValue = arguments.getValue(1, LazyFields.STRINGS);
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
