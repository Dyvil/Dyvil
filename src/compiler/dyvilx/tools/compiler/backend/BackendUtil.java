package dyvilx.tools.compiler.backend;

import dyvilx.tools.compiler.backend.exception.BytecodeException;

import static dyvil.reflect.Opcodes.*;
import static dyvilx.tools.compiler.backend.ClassFormat.DOUBLE;
import static dyvilx.tools.compiler.backend.ClassFormat.LONG;

public abstract class BackendUtil
{
	private BackendUtil()
	{
		// no instances
	}

	static boolean twoWord(Object type)
	{
		return type == LONG || type == DOUBLE;
	}

	static void swap(MethodWriterImpl writer) throws BytecodeException
	{
		Object t1 = writer.frame.pop();
		Object t2 = writer.frame.pop();

		writer.frame.push(t1);
		writer.frame.push(t2);

		if (twoWord(t2))
		{
			if (twoWord(t1))
			{
				// { value4, value3 }, { value2, value1 } ->
				// { value2, value1 }, { value4, value3 }
				writer.mv.visitInsn(DUP2_X2);
				writer.mv.visitInsn(POP2);
			}
			else
			{
				// { value3, value2 }, value1 ->
				// value1, { value3, value2 }
				writer.mv.visitInsn(DUP_X2);
				writer.mv.visitInsn(POP);
			}
		}
		else
		{
			if (twoWord(t1))
			{
				// value3, { value2, value1 } ->
				// { value2, value1 }, value3
				writer.mv.visitInsn(DUP2_X1);
				writer.mv.visitInsn(POP2);
			}
			else
			{
				// value2, value1 -> value1, value1
				writer.mv.visitInsn(SWAP);
			}
		}
	}

	static void pop(MethodWriterImpl writer) throws BytecodeException
	{
		Object t = writer.frame.pop();

		if (twoWord(t))
		{
			// { value2, value1 } ->
			writer.mv.visitInsn(POP2);
		}
		else
		{
			// value1 ->
			writer.mv.visitInsn(POP);
		}
	}

	static void dup(MethodWriterImpl writer)
	{
		Object t = writer.frame.peek();
		writer.frame.push(t);
		if (twoWord(t))
		{
			// { value2, value1 } -> { value2, value1 }, { value2, value1 }
			writer.mv.visitInsn(DUP2);
		}
		else
		{
			// value1 -> value1, value1
			writer.mv.visitInsn(DUP);
		}
	}

	static void dupX1(MethodWriterImpl writer) throws BytecodeException
	{
		Object t1 = writer.frame.pop();
		Object t2 = writer.frame.pop();

		writer.frame.push(t1);
		writer.frame.push(t2);
		writer.frame.push(t1);
		if (twoWord(t1))
		{
			if (twoWord(t2))
			{
				// { value4, value3 }, { value2, value1 } ->
				// { value2, value1 }, { value4, value3 }, { value2, value1 }
				writer.mv.visitInsn(DUP2_X2);
			}
			else
			{
				// value3, { value2, value1 } ->
				// { value2, value1 }, value3, { value2, value1 }
				writer.mv.visitInsn(DUP2_X1);
			}
		}
		else
		{
			if (twoWord(t2))
			{
				// { value3, value2 }, value1 ->
				// value1, { value3, value2 }, value1
				writer.mv.visitInsn(DUP_X2);
			}
			else
			{
				// value2, value1 -> value1, value2, value1
				writer.mv.visitInsn(DUP_X1);
			}
		}
	}
}
