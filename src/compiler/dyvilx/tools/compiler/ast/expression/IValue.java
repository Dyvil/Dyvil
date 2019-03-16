package dyvilx.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.AnnotationVisitor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IImplicitContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.expression.constant.*;
import dyvilx.tools.compiler.ast.expression.intrinsic.PopExpr;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.Typed;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.transform.SideEffectHelper;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public interface IValue extends ASTNode, Typed, WriteableExpression
{
	// --- Expression IDs ---

	int UNKNOWN = 0;

	// Literals
	int VOID                 = 1;
	int NULL                 = 2;
	int WILDCARD             = 4;
	int BOOLEAN              = 5;
	int BYTE                 = 6;
	int SHORT                = 7;
	int CHAR                 = 8;
	int INT                  = 9;
	int LONG                 = 10;
	int FLOAT                = 11;
	int DOUBLE               = 12;
	int STRING               = 13;
	int STRING_INTERPOLATION = 14;

	// Compound Constructs
	int STATEMENT_LIST = 32;
	int TUPLE          = 34;
	int ARRAY          = 35;
	int MAP            = 36;
	int ANNOTATION     = 37;

	// Basic Language Constructs
	int THIS  = 64;
	int SUPER = 65;

	int CAST_OPERATOR      = 66;
	int ISOF_OPERATOR      = 67;
	// int CASE_STATEMENT   = 68;
	int MATCH              = 69;
	int LAMBDA             = 70;
	int WILDCARD_PARAMETER = 71;

	// Access and Invocation
	int CLASS_ACCESS  = 96;
	int FIELD_ACCESS  = 97;
	int ENUM_ACCESS   = 98;
	int METHOD_CALL   = 99;
	int APPLY_CALL    = 100;
	int UPDATE_CALL   = 101;
	int SUBSCRIPT_GET = 102;
	int SUBSCRIPT_SET = 103;
	int BRACE_ACCESS  = 104;
	int PREFIX_CALL   = 105;

	// Special Invocation
	int CONSTRUCTOR_CALL = 112;
	int INITIALIZER_CALL = 113;

	// Assignments
	int FIELD_ASSIGN  = 120;
	int METHOD_ASSIGN = 121;

	// Special Operators and Intrinsics
	int BOOLEAN_AND       = 129;
	int BOOLEAN_OR        = 130;
	int BOOLEAN_NOT       = 131;
	int CLASS_OPERATOR    = 132;
	int TYPE_OPERATOR     = 133;
	int STRING_CONCAT     = 134;
	int INC               = 135;
	int COLON             = 136;
	int VARARGS_EXPANSION = 137;

	int OPTIONAL_UNWRAP = 150;
	int OPTIONAL_CHAIN  = 151;
	int NULL_COALESCING = 152;

	// Basic Control Statements
	int RETURN       = 192;
	int IF           = 193;
	// int SWITCH       = 194;
	int FOR          = 195;
	int WHILE        = 196;
	int DO_WHILE     = 197;
	int TRY          = 198;
	int THROW        = 199;
	int SYNCHRONIZED = 200;

	// Jump Statements
	int BREAK    = 214;
	int CONTINUE = 215;
	int GOTO     = 216;

	// Pseudo-Expressions
	int VARIABLE         = 232;
	int MEMBER_STATEMENT = 233;

	// Special Types only used by the compiler
	int REFERENCE          = 240;
	int LITERAL_CONVERSION = 241;
	int OPERATOR_CHAIN     = 242;
	int POP_EXPR           = 243;

	// --- Other Constants ---

	int MISMATCH                  = 0;
	int IMPLICIT_CONVERSION_MATCH = 1;
	int CONVERSION_MATCH          = 2;
	int SECONDARY_SUBTYPE_MATCH   = 3;
	int SECONDARY_MATCH           = 4;
	int SUBTYPE_MATCH             = 5;
	int EXACT_MATCH               = 6;

	int valueTag();

	static boolean isNumeric(int tag)
	{
		return tag >= BYTE && tag <= DOUBLE;
	}

	default boolean isConstant()
	{
		return false;
	}

	default boolean isAnnotationConstant()
	{
		return this.isConstant();
	}

	default boolean isConstantOrField()
	{
		return this.isConstant();
	}

	default boolean hasSideEffects()
	{
		return !this.isConstantOrField();
	}

	default boolean isStatement()
	{
		return Types.isVoid(this.getType());
	}

	default boolean isUsableAsStatement()
	{
		return this.isStatement();
	}

	boolean isResolved();

	default IValue toReferenceValue(MarkerList markers, IContext context)
	{
		return null;
	}

	default IValue toAssignment(IValue rhs, SourcePosition position)
	{
		return null;
	}

	default IValue toCompoundAssignment(IValue rhs, SourcePosition position, MarkerList markers, IContext context,
		SideEffectHelper helper)
	{
		return null;
	}

	// Subclass-specific methods

	default boolean isClassAccess()
	{
		return false;
	}

	default boolean isIgnoredClassAccess()
	{
		return false;
	}

	default IValue asIgnoredClassAccess()
	{
		if (!this.hasSideEffects())
		{
			return null;
		}
		return new PopExpr(this);
	}

	default boolean isPartialWildcard()
	{
		return false;
	}

	default IValue withLambdaParameter(IParameter parameter)
	{
		return null;
	}

	default boolean checkVarargs(boolean typeCheck)
	{
		return false;
	}

	default boolean isPolyExpression()
	{
		return false;
	}

	// Types

	@Override
	IType getType();

	@Override
	default void setType(IType type)
	{
	}

	default IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return this.isType(type) ? this : null;
	}

	@Override
	default boolean isType(IType type)
	{
		return Types.isSuperType(type, this.getType());
	}

	/**
	 * Returns how much the type of this value 'matches' the given type. {@code 1} indicates a perfect match, while
	 * {@code 0} marks incompatible types. A higher value means that the value is less suitable for the type. A negative
	 * value means an implicit conversion was applied.
	 *
	 * @param type
	 * 	the type to match
	 * @param implicitContext
	 * 	the context for implicit resolution
	 *
	 * @return the subtyping distance
	 */
	default int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		return Types.getTypeMatch(type, this.getType());
	}

	void resolveTypes(MarkerList markers, IContext context);

	default void resolveStatement(ILabelContext context, MarkerList markers)
	{
	}

	IValue resolve(MarkerList markers, IContext context);

	void checkTypes(MarkerList markers, IContext context);

	void check(MarkerList markers, IContext context);

	IValue foldConstants();

	IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);

	default IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		return null;
	}

	default Marker getAnnotationError()
	{
		return Markers.semanticError(this.getPosition(), "value.constant");
	}

	static IValue toAnnotationConstant(IValue value, MarkerList markers, IContext context)
	{
		final int depth = context.getCompilationContext().config.getMaxConstantDepth();
		final IValue constant = value.toAnnotationConstant(markers, context, depth);
		if (constant != null)
		{
			return constant;
		}

		final Marker marker = value.getAnnotationError();
		marker.addInfo(Markers.getSemantic("value.constant.depth", depth));
		markers.add(marker);
		return value;
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
		else if (c == Boolean.class)
		{
			return new BooleanValue((Boolean) o);
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
			final ArrayExpr valueList = new ArrayExpr();
			final ArgumentList values = valueList.getValues();
			valueList.setElementType(Types.INT);
			for (int i : (int[]) o)
			{
				values.add(new IntValue(i));
			}
			return valueList;
		}
		else if (c == long[].class)
		{
			final ArrayExpr valueList = new ArrayExpr();
			final ArgumentList values = valueList.getValues();
			valueList.setElementType(Types.LONG);
			for (long l : (long[]) o)
			{
				values.add(new LongValue(l));
			}
			return valueList;
		}
		else if (c == float[].class)
		{
			final ArrayExpr valueList = new ArrayExpr();
			final ArgumentList values = valueList.getValues();
			valueList.setElementType(Types.FLOAT);
			for (float f : (float[]) o)
			{
				values.add(new FloatValue(f));
			}
			return valueList;
		}
		else if (c == double[].class)
		{
			final ArrayExpr valueList = new ArrayExpr();
			final ArgumentList values = valueList.getValues();
			valueList.setElementType(Types.DOUBLE);
			for (double d : (double[]) o)
			{
				values.add(new DoubleValue(d));
			}
			return valueList;
		}
		else if (c == dyvilx.tools.asm.Type.class)
		{
			dyvilx.tools.asm.Type type = (dyvilx.tools.asm.Type) o;
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
	void toString(@NonNull String indent, @NonNull StringBuilder buffer);

	// Compilation

	@Override
	void writeExpression(MethodWriter writer, IType type) throws BytecodeException;

	default int writeStore(MethodWriter writer, IType type) throws BytecodeException
	{
		this.writeExpression(writer, type);
		final int localIndex = writer.localCount();

		if (type == null)
		{
			type = this.getType();
		}

		writer.visitVarInsn(type.getStoreOpcode(), localIndex);
		return localIndex;
	}

	default int writeStoreLoad(MethodWriter writer, IType type) throws BytecodeException
	{
		final int localIndex = this.writeStore(writer, type);
		writer.visitVarInsn(Opcodes.AUTO_LOAD, localIndex);
		return localIndex;
	}

	default void writeAnnotationValue(AnnotationVisitor visitor, String key)
	{
		final Object value = this.toObject();
		if (value != null)
		{
			visitor.visit(key, value);
		}
	}
}
