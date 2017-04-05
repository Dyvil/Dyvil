package dyvil.tools.compiler.ast.method.intrinsic;

import dyvil.annotation.Intrinsic;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.intrinsic.*;
import dyvil.tools.compiler.ast.expression.optional.NullCoalescingOperator;
import dyvil.tools.compiler.ast.expression.optional.OptionalChainOperator;
import dyvil.tools.compiler.ast.expression.optional.OptionalUnwrapOperator;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.ParameterList;
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
			final ParameterList params = Types.INTRINSIC_CLASS.getParameters();
			VALUE = params.get(0);
			STRINGS = params.get(1);
			COMPILER_CODE = params.get(2);
		}
	}

	public static IValue getOperator(int code, IValue lhs, ArgumentList arguments)
	{
		switch (code)
		{
		// Boolean
		case Intrinsic.BOOLEAN_NOT:
			return new NotOperator(arguments.getFirstValue());
		case Intrinsic.BOOLEAN_OR:
			return new OrOperator(lhs, arguments.getFirstValue());
		case Intrinsic.BOOLEAN_AND:
			return new AndOperator(lhs, arguments.getFirstValue());
		// Arrays
		case Intrinsic.ARRAY_SPREAD:
			return new VarargsOperator(lhs);
		// Optionals
		case Intrinsic.OPTIONAL_UNWRAP:
			return new OptionalUnwrapOperator(lhs, false);
		case Intrinsic.FORCE_UNWRAP:
			return new OptionalUnwrapOperator(lhs, true);
		case Intrinsic.OPTIONAL_CHAIN:
			return new OptionalChainOperator(lhs);
		case Intrinsic.NULL_COALESCING:
			return new NullCoalescingOperator(lhs, arguments.getFirstValue());
		// Strings
		case Intrinsic.STRING_CONCAT:
			return StringConcatExpr.apply(lhs, arguments.getFirstValue());
		// Increment / Decrement
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
		final ArgumentList arguments = annotation.getArguments();
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

		final ArrayExpr valueArray = (ArrayExpr) value;
		final ArgumentList values = valueArray.getValues();

		final int size = values.size();
		int insnCount = 0;

		int[] ints = new int[size];

		for (int i = 0; i < size; i++)
		{
			int opcode = values.get(i).intValue();
			ints[i] = opcode;

			insnCount++;

			if (Opcodes.isFieldOrMethodOpcode(opcode))
			{
				ints[i + 1] = values.get(i + 1).intValue();
				ints[i + 2] = values.get(i + 2).intValue();
				ints[i + 3] = values.get(i + 3).intValue();
				i += 3;
			}
			else if (Opcodes.isJumpOpcode(opcode) || Opcodes.isLoadOpcode(opcode) || Opcodes.isStoreOpcode(opcode)
				         || opcode == Opcodes.LDC || opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH)
			{
				ints[i + 1] = values.get(i + 1).intValue();
				i += 1;
			}
		}

		if (size > insnCount)
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
			final ArgumentList values = stringArray.getValues();
			// Convert string constants to an array
			final int stringCount = values.size();

			strings = new String[stringCount];
			for (int i = 0; i < stringCount; i++)
			{
				strings[i] = values.get(i).stringValue();
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
