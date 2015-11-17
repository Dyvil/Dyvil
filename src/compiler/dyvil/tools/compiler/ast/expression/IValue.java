package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.operator.ClassOperator;
import dyvil.tools.compiler.ast.reference.IReference;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.ArrayType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

public interface IValue extends IASTNode, ITyped
{
	// --- Expression IDs ---
	
	// Literals
	int	VOID			= 0;
	int	NULL			= 1;
	int	NIL				= 2;
	int	WILDCARD		= 3;
	int	BOOLEAN			= 4;
	int	BYTE			= 5;
	int	SHORT			= 6;
	int	CHAR			= 7;
	int	INT				= 8;
	int	LONG			= 9;
	int	FLOAT			= 10;
	int	DOUBLE			= 11;
	int	STRING			= 12;
	int	STRING_INTERPOLATION	= 13;
	
	// Compound Constructs
	int	STATEMENT_LIST	= 32;
	int	BYTECODE		= 33;
	int	TUPLE			= 34;
	int	ARRAY			= 35;
	int	MAP				= 36;
	int	ANNOTATION		= 37;
	
	// Basic Language Constructs
	int	THIS	= 64;
	int	SUPER	= 65;
	
	int	CAST_OPERATOR		= 66;
	int	ISOF_OPERATOR		= 67;
	int	CASE_STATEMENT		= 68;
	int	MATCH				= 69;
	int	LAMBDA				= 70;
	int	PARTIAL_FUNCTION	= 71;
	
	// Access and Invocation
	int	CLASS_ACCESS	= 96;
	int	FIELD_ACCESS	= 97;
	int	ENUM_ACCESS		= 98;
	int	METHOD_CALL		= 99;
	int	APPLY_CALL		= 100;
	int	UPDATE_CALL		= 101;
	int	SUBSCRIPT_GET	= 102;
	int	SUBSCRIPT_SET	= 103;
	
	// Special Invocation
	int	CONSTRUCTOR_CALL	= 112;
	int	INITIALIZER_CALL	= 113;
	
	// Assignments
	int	FIELD_ASSIGN	= 120;
	int	COMPOUND_CALL	= 121;
	
	// Special Operators and Intrinsics
	int	SWAP_OPERATOR	= 128;
	int	BOOLEAN_AND		= 129;
	int	BOOLEAN_OR		= 130;
	int	BOOLEAN_NOT		= 131;
	int	CLASS_OPERATOR	= 132;
	int	TYPE_OPERATOR	= 133;
	int	NULLCHECK		= 134;
	int	RANGE_OPERATOR	= 135;
	int	STRINGBUILDER	= 136;
	
	// Basic Control Statements
	int	RETURN			= 192;
	int	IF				= 193;
	int	SWITCH			= 194;
	int	FOR				= 195;
	int	WHILE			= 196;
	int	DO_WHILE		= 197;
	int	TRY				= 198;
	int	THROW			= 199;
	int	SYNCHRONIZED	= 200;
	
	// Jump Statements
	int	BREAK		= 214;
	int	CONTINUE	= 215;
	int	GOTO		= 216;
	
	// Pseudo-Expressions
	int	VARIABLE		= 232;
	int	NESTED_METHOD	= 233;
	
	// Special Types only used by the compiler
	int	REFERENCE			= 240;
	int	LITERAL_CONVERSION	= 241;
	
	// --- Other Constants ---
	
	float CONVERSION_MATCH = 1000F;
	
	int valueTag();
	
	static boolean isNumeric(int tag)
	{
		return tag >= BYTE && tag <= DOUBLE;
	}
	
	default boolean isConstant()
	{
		return false;
	}
	
	default boolean isConstantOrField()
	{
		return this.isConstant();
	}
	
	default boolean isPrimitive()
	{
		return this.getType().isPrimitive();
	}
	
	default IReference toReference()
	{
		return null;
	}
	
	boolean isResolved();
	
	@Override
	IType getType();
	
	@Override
	default void setType(IType type)
	{
	}
	
	IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context);
	
	@Override
	default boolean isType(IType type)
	{
		return type.isSuperTypeOf(this.getType());
	}
	
	/**
	 * Returns how much the type of this value 'matches' the given type.
	 * {@code 1} indicates a perfect match, while {@code 0} marks incompatible
	 * types. A higher value means that the value is less suitable for the type.
	 * 
	 * @param type
	 *            the type to match
	 * @return the subtyping distance
	 */
	default float getTypeMatch(IType type)
	{
		return type.getSubTypeDistance(this.getType());
	}
	
	void resolveTypes(MarkerList markers, IContext context);
	
	default void resolveStatement(ILabelContext context, MarkerList markers)
	{
	
	}
	
	IValue resolve(MarkerList markers, IContext context);
	
	void checkTypes(MarkerList markers, IContext context);
	
	void check(MarkerList markers, IContext context);
	
	IValue foldConstants();
	
	IValue cleanup(IContext context, IClassCompilableList compilableList);
	
	default IValue toConstant(MarkerList markers)
	{
		return null;
	}
	
	default int stringSize()
	{
		return 20;
	}
	
	default boolean toStringBuilder(StringBuilder builder)
	{
		return false;
	}
	
	static IValue fromObject(Object o)
	{
		if (o == null)
		{
			return new NullValue();
		}
		Class c = o.getClass();
		if (c == Character.class)
		{
			return new CharValue(null, o.toString(), true);
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
			ArrayExpr valueList = new ArrayExpr(null);
			valueList.arrayType = new ArrayType(Types.INT);
			valueList.elementType = Types.INT;
			for (int i : (int[]) o)
			{
				valueList.addValue(new IntValue(i));
			}
			return valueList;
		}
		else if (c == long[].class)
		{
			ArrayExpr valueList = new ArrayExpr();
			valueList.arrayType = new ArrayType(Types.LONG);
			valueList.elementType = Types.LONG;
			for (long l : (long[]) o)
			{
				valueList.addValue(new LongValue(l));
			}
			return valueList;
		}
		else if (c == float[].class)
		{
			ArrayExpr valueList = new ArrayExpr();
			valueList.arrayType = new ArrayType(Types.FLOAT);
			valueList.elementType = Types.FLOAT;
			for (float f : (float[]) o)
			{
				valueList.addValue(new FloatValue(f));
			}
			return valueList;
		}
		else if (c == double[].class)
		{
			ArrayExpr valueList = new ArrayExpr();
			valueList.arrayType = new ArrayType(Types.DOUBLE);
			valueList.elementType = Types.DOUBLE;
			for (double d : (double[]) o)
			{
				valueList.addValue(new DoubleValue(d));
			}
			return valueList;
		}
		else if (c == dyvil.tools.asm.Type.class)
		{
			dyvil.tools.asm.Type type = (dyvil.tools.asm.Type) o;
			return new ClassOperator(Types.fromASMType(type));
		}
		return null;
	}
	
	default Object toObject()
	{
		return null;
	}
	
	default boolean booleanValue()
	{
		return false;
	}
	
	default int intValue()
	{
		return 0;
	}
	
	default long longValue()
	{
		return 0L;
	}
	
	default float floatValue()
	{
		return 0F;
	}
	
	default double doubleValue()
	{
		return 0D;
	}
	
	default String stringValue()
	{
		return null;
	}
	
	@Override
	void toString(String prefix, StringBuilder buffer);
	
	// Compilation
	
	default void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.writeExpression(writer);
		this.getType().writeCast(writer, type, this.getLineNumber());
	}
	
	/**
	 * Writes this {@link IValue} to the given {@link MethodWriter}
	 * {@code writer} as an expression. That means that this element remains as
	 * the first element of the stack.
	 * 
	 * @param visitor
	 * @throws BytecodeException
	 */
	void writeExpression(MethodWriter writer) throws BytecodeException;
	
	/**
	 * Writes this {@link IValue} to the given {@link MethodWriter}
	 * {@code writer} as a statement. That means that this element is removed
	 * from the stack.
	 * 
	 * @param writer
	 * @throws BytecodeException
	 */
	void writeStatement(MethodWriter writer) throws BytecodeException;
	
	default void writeJump(MethodWriter writer, Label dest) throws BytecodeException
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
	 */
	default void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeJumpInsn(Opcodes.IFEQ, dest);
	}
	
	default void writeAnnotationValue(AnnotationVisitor visitor, String key)
	{
		visitor.visit(key, this.toObject());
	}
}
