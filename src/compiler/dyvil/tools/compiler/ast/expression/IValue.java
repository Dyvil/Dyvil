package dyvil.tools.compiler.ast.expression;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.ClassOperator;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IValue extends IASTNode, ITyped
{
	int	VOID				= 0;
	int	NULL				= 1;
	int	NIL					= 2;
	int	WILDCARD			= 3;
	int	BOOLEAN				= 4;
	int	BYTE				= 5;
	int	SHORT				= 6;
	int	CHAR				= 7;
	int	INT					= 8;
	int	LONG				= 9;
	int	FLOAT				= 10;
	int	DOUBLE				= 11;
	int	STRING				= 12;
	int	FORMAT_STRING		= 13;
	int	STRINGBUILDER		= 14;
	
	int	THIS				= 16;
	int	SUPER				= 17;
	
	int	STATEMENT_LIST		= 24;
	int	ARRAY				= 25;
	
	int	CLASS_ACCESS		= 32;
	int	ENUM				= 33;
	int	FIELD_ACCESS		= 34;
	int	FIELD_ASSIGN		= 35;
	int	METHOD_CALL			= 36;
	int	APPLY_METHOD_CALL	= 37;
	int	UPDATE_METHOD_CALL	= 38;
	int	CONSTRUCTOR_CALL	= 39;
	int	INITIALIZER_CALL	= 40;
	int	VARIABLE			= 41;
	int	NESTED_METHOD		= 42;
	
	int	CAST_OPERATOR		= 48;
	int	ISOF_OPERATOR		= 49;
	int	SWAP_OPERATOR		= 50;
	int	BOOLEAN_AND			= 51;
	int	BOOLEAN_OR			= 52;
	int	BOOLEAN_NOT			= 53;
	int	CLASS_OPERATOR		= 54;
	int	TYPE_OPERATOR		= 55;
	int	NULLCHECK			= 56;
	int	RANGE_OPERATOR		= 57;
	
	int	TUPLE				= 64;
	int	CASE_STATEMENT		= 65;
	int	MATCH				= 66;
	int	LAMBDA				= 67;
	int	PARTIAL_FUNCTION	= 68;
	int	BYTECODE			= 69;
	
	int	RETURN				= 70;
	int	IF					= 71;
	int	SWITCH				= 72;
	int	FOR					= 73;
	int	WHILE				= 74;
	int	DO_WHILE			= 75;
	int	TRY					= 76;
	int	THROW				= 77;
	int	SYNCHRONIZED		= 78;
	
	int	BREAK				= 79;
	int	CONTINUE			= 80;
	int	GOTO				= 81;
	
	// Special Types only used by the compiler
	int	CAPSULATED			= 128;
	int	BOXED				= 129;
	
	public int valueTag();
	
	public static boolean isNumeric(int tag)
	{
		return tag >= BYTE && tag <= DOUBLE;
	}
	
	public default boolean isConstant()
	{
		return false;
	}
	
	public default boolean isStatement()
	{
		return false;
	}
	
	public default boolean isPrimitive()
	{
		return this.getType().isPrimitive();
	}
	
	@Override
	public IType getType();
	
	@Override
	public default void setType(IType type)
	{
	}
	
	public default IValue withType(IType type)
	{
		IType thisType = this.getType();
		if (thisType == null)
		{
			return null;
		}
		if (!type.isSuperTypeOf(thisType))
		{
			return null;
		}
		
		boolean primitive = this.isPrimitive();
		if (primitive != type.isPrimitive())
		{
			// Box Primitive -> Object
			if (primitive)
			{
				return new BoxedValue(this, thisType.getBoxMethod());
			}
			// Unbox Object -> Primitive
			return new BoxedValue(this, type.getUnboxMethod());
		}
		return this;
	}
	
	@Override
	public boolean isType(IType type);
	
	public int getTypeMatch(IType type);
	
	public void resolveTypes(MarkerList markers, IContext context);
	
	public IValue resolve(MarkerList markers, IContext context);
	
	public void checkTypes(MarkerList markers, IContext context);
	
	public void check(MarkerList markers, IContext context);
	
	public IValue foldConstants();
	
	// Compilation
	
	/**
	 * Writes this {@link IValue} to the given {@link MethodWriter}
	 * {@code writer} as an expression. That means that this element remains as
	 * the first element of the stack.
	 * 
	 * @param visitor
	 * @throws BytecodeException
	 *             TODO
	 */
	public void writeExpression(MethodWriter writer) throws BytecodeException;
	
	/**
	 * Writes this {@link IValue} to the given {@link MethodWriter}
	 * {@code writer} as a statement. That means that this element is removed
	 * from the stack.
	 * 
	 * @param writer
	 * @throws BytecodeException
	 *             TODO
	 */
	public void writeStatement(MethodWriter writer) throws BytecodeException;
	
	public default void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeJumpInsn(Opcodes.IFNE, dest);
	}
	
	/**
	 * Writes this {@link IValue} to the given {@link MethodWriter}
	 * {@code writer} as a jump expression to the given {@link Label}
	 * {@code dest}. By default, this calls
	 * {@link #writeExpression(MethodWriter)} and then writes an
	 * {@link Opcodes#IFEQ IFEQ} instruction pointing to {@code dest}. That
	 * means the JVM would jump to {@code dest} if the current value on the
	 * stack equals {@code 0}.
	 * 
	 * @param writer
	 * @param dest
	 * @throws BytecodeException
	 *             TODO
	 */
	public default void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeJumpInsn(Opcodes.IFEQ, dest);
	}
	
	public default int stringSize()
	{
		return 20;
	}
	
	public default boolean toStringBuilder(StringBuilder builder)
	{
		return false;
	}
	
	public default Object toObject()
	{
		return null;
	}
	
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
			Array valueList = new Array(null);
			valueList.requiredType = new ArrayType(Types.INT);
			valueList.elementType = Types.INT;
			for (int i : (int[]) o)
			{
				valueList.addValue(new IntValue(i));
			}
			return valueList;
		}
		else if (c == long[].class)
		{
			Array valueList = new Array();
			valueList.requiredType = new ArrayType(Types.LONG);
			valueList.elementType = Types.LONG;
			for (long l : (long[]) o)
			{
				valueList.addValue(new LongValue(l));
			}
			return valueList;
		}
		else if (c == float[].class)
		{
			Array valueList = new Array();
			valueList.requiredType = new ArrayType(Types.FLOAT);
			valueList.elementType = Types.FLOAT;
			for (float f : (float[]) o)
			{
				valueList.addValue(new FloatValue(f));
			}
			return valueList;
		}
		else if (c == double[].class)
		{
			Array valueList = new Array();
			valueList.requiredType = new ArrayType(Types.DOUBLE);
			valueList.elementType = Types.DOUBLE;
			for (double d : (double[]) o)
			{
				valueList.addValue(new DoubleValue(d));
			}
			return valueList;
		}
		else if (c == org.objectweb.asm.Type.class)
		{
			org.objectweb.asm.Type type = (org.objectweb.asm.Type) o;
			IType itype = new Type(Name.getQualified(type.getClassName()));
			return new ClassOperator(itype);
		}
		return null;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer);
}
