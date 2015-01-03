package dyvil.tools.compiler.ast.api;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.expression.ValueList;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.*;
import dyvil.tools.compiler.bytecode.MethodWriter;

public interface IValue extends IASTNode
{
	public boolean isConstant();
	
	public IType getType();
	
	public default Object toObject()
	{
		throw new UnsupportedOperationException();
	}
	
	public default boolean requireType(IType type)
	{
		return Type.isSuperType(type, this.getType());
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context);
	
	// Compilation
	
	/**
	 * Writes this {@link IValue} to the given {@link MethodWriter}
	 * {@code writer} as an expression. That means that this element remains as
	 * the first element of the stack.
	 * 
	 * @param visitor
	 */
	public void writeExpression(MethodWriter writer);
	
	/**
	 * Writes this {@link IValue} to the given {@link MethodWriter}
	 * {@code writer} as a statement. That means that this element is removed
	 * from the stack.
	 * 
	 * @param writer
	 */
	public void writeStatement(MethodWriter writer);
	
	public void writeJump(MethodWriter writer, Label label);
	
	public static IValue fromObject(Object o)
	{
		if (o == null)
		{
			return new NullValue();
		}
		Class c = o.getClass();
		if (c == Character.class)
		{
			return new CharValue((Character) o);
		}
		else if (c == Integer.class)
		{
			return new IntValue((Integer) o);
		}
		else if (c == Long.class)
		{
			return new LongValue((Long) o);
		}
		else if (c == Float.class)
		{
			return new FloatValue((Float) o);
		}
		else if (c == Double.class)
		{
			return new DoubleValue((Double) o);
		}
		else if (c == String.class)
		{
			return new StringValue((String) o);
		}
		else if (c == int[].class)
		{
			ValueList valueList = new ValueList(null, true);
			for (int i : (int[]) o)
			{
				valueList.addValue(new IntValue(i));
			}
			return valueList;
		}
		else if (c == long[].class)
		{
			ValueList valueList = new ValueList(null, true);
			for (long l : (long[]) o)
			{
				valueList.addValue(new LongValue(l));
			}
			return valueList;
		}
		else if (c == float[].class)
		{
			ValueList valueList = new ValueList(null, true);
			for (float f : (float[]) o)
			{
				valueList.addValue(new FloatValue(f));
			}
			return valueList;
		}
		else if (c == double[].class)
		{
			ValueList valueList = new ValueList(null, true);
			for (double d : (double[]) o)
			{
				valueList.addValue(new DoubleValue(d));
			}
			return valueList;
		}
		return null;
	}
}
