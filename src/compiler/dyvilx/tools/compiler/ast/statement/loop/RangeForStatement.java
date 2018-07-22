package dyvilx.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;

public class RangeForStatement extends ForEachStatement
{
	private static final int INT       = 0;
	private static final int LONG      = 1;
	private static final int FLOAT     = 2;
	private static final int DOUBLE    = 3;
	private static final int RANGEABLE = 4;

	private final IType elementType;

	public RangeForStatement(SourcePosition position, IVariable var, IType elementType)
	{
		super(position, var);

		this.elementType = elementType;
	}

	public static boolean isRangeOperator(MethodCall methodCall)
	{
		final Name name = methodCall.getName();
		return (name == Names.dotdot || name == Names.dotdotlt) // name is .. or ..<
			       && methodCall.getReceiver() != null // has receiver
			       && methodCall.getArguments().size() == 1 // has exactly one argument
			       ;//&& Types.isSuperType(LazyFields.RANGE, methodCall.getType()); // return type <: dyvil.collection.Range
	}

	public static boolean isHalfOpen(MethodCall rangeOperator)
	{
		return rangeOperator.getName() == Names.dotdotlt;
	}

	public static IValue getStartValue(MethodCall rangeOperator)
	{
		return rangeOperator.getReceiver();
	}

	public static IValue getEndValue(MethodCall rangeOperator)
	{
		return rangeOperator.getArguments().getFirst();
	}

	public static IType getElementType(MethodCall range)
	{
		return Types.combine(range.getReceiver().getType(), range.getArguments().getFirst().getType());
	}

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		// Determine the kind (int, long, float, double, Rangeable) of the range to fasten up compilation.
		byte kind = RANGEABLE;
		boolean boxed = false;

		final int lineNumber = this.lineNumber();

		final IVariable var = this.variable;
		final IType varType = var.getType();

		final MethodCall rangeOperator = (MethodCall) var.getValue();
		final IValue startValue = getStartValue(rangeOperator);
		final IValue endValue = getEndValue(rangeOperator);
		final IType elementType = this.elementType;
		final boolean halfOpen = isHalfOpen(rangeOperator);

		if (elementType.isPrimitive())
		{
			switch (elementType.getTypecode())
			{
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.CHAR_CODE:
			case PrimitiveType.INT_CODE:
				kind = INT;
				break;
			case PrimitiveType.LONG_CODE:
				kind = LONG;
				break;
			case PrimitiveType.FLOAT_CODE:
				kind = FLOAT;
				break;
			case PrimitiveType.DOUBLE_CODE:
				kind = DOUBLE;
				break;
			}

			if (!varType.isPrimitive())
			{
				boxed = true;
			}
		}

		dyvilx.tools.asm.Label startLabel = this.startLabel.getTarget();
		dyvilx.tools.asm.Label updateLabel = this.updateLabel.getTarget();
		dyvilx.tools.asm.Label endLabel = this.endLabel.getTarget();
		dyvilx.tools.asm.Label scopeLabel = new dyvilx.tools.asm.Label();
		writer.visitLabel(scopeLabel);

		// Write the start value and store it in the variable.
		startValue.writeExpression(writer, elementType);

		final int counterVarIndex, varIndex;

		if (boxed)
		{
			// Create two variables, the counter variable and the user-visible loop variable

			writer.visitInsn(Opcodes.AUTO_DUP);
			counterVarIndex = writer.localCount();
			writer.visitVarInsn(elementType.getStoreOpcode(), counterVarIndex);

			elementType.writeCast(writer, varType, lineNumber);
			var.writeInit(writer, null);

			varIndex = var.getLocalIndex();
		}
		else
		{
			// Use the loop variable as the counter variable

			var.writeInit(writer, null);

			varIndex = counterVarIndex = var.getLocalIndex();
		}

		endValue.writeExpression(writer, elementType);

		final int endVarIndex = writer.localCount();
		writer.visitVarInsn(elementType.getStoreOpcode(), endVarIndex);

		writer.visitTargetLabel(startLabel);

		// Check the condition
		switch (kind)
		{
		case INT:
			writer.visitVarInsn(Opcodes.ILOAD, counterVarIndex);
			writer.visitVarInsn(Opcodes.ILOAD, endVarIndex);
			writer.visitJumpInsn(halfOpen ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPGT, endLabel);
			break;
		case LONG:
			writer.visitVarInsn(Opcodes.LLOAD, counterVarIndex);
			writer.visitVarInsn(Opcodes.LLOAD, endVarIndex);
			writer.visitJumpInsn(halfOpen ? Opcodes.IF_LCMPGE : Opcodes.IF_LCMPGT, endLabel);
			break;
		case FLOAT:
			writer.visitVarInsn(Opcodes.FLOAD, counterVarIndex);
			writer.visitVarInsn(Opcodes.FLOAD, endVarIndex);
			writer.visitJumpInsn(halfOpen ? Opcodes.IF_FCMPGE : Opcodes.IF_FCMPGT, endLabel);
			break;
		case DOUBLE:
			writer.visitVarInsn(Opcodes.DLOAD, counterVarIndex);
			writer.visitVarInsn(Opcodes.DLOAD, endVarIndex);
			writer.visitJumpInsn(halfOpen ? Opcodes.IF_DCMPGE : Opcodes.IF_DCMPGT, endLabel);
			break;
		case RANGEABLE:
			writer.visitVarInsn(Opcodes.ALOAD, counterVarIndex);
			writer.visitVarInsn(Opcodes.ALOAD, endVarIndex);
			writer.visitLineNumber(lineNumber);
			writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, "dyvil/collection/range/Rangeable", "compareTo",
			                       "(Ldyvil/collection/range/Rangeable;)I", true);
			writer.visitJumpInsn(halfOpen ? Opcodes.IFGE : Opcodes.IFGT, endLabel);
			break;
		}

		// Action
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}

		// Increment
		writer.visitLabel(updateLabel);
		switch (kind)
		{
		case INT:
			writer.visitIincInsn(counterVarIndex, 1);

			if (boxed)
			{
				writer.visitVarInsn(Opcodes.ILOAD, counterVarIndex);
				elementType.writeCast(writer, varType, lineNumber);
				writer.visitVarInsn(varType.getStoreOpcode(), varIndex);
			}
			break;
		case LONG:
			writer.visitVarInsn(Opcodes.LLOAD, counterVarIndex);
			writer.visitInsn(Opcodes.LCONST_1);
			writer.visitInsn(Opcodes.LADD);

			if (boxed)
			{
				writer.visitInsn(Opcodes.DUP2);
				elementType.writeCast(writer, varType, lineNumber);
				writer.visitVarInsn(varType.getStoreOpcode(), varIndex);
			}

			writer.visitVarInsn(Opcodes.LSTORE, counterVarIndex);
			break;
		case FLOAT:
			writer.visitVarInsn(Opcodes.FLOAD, counterVarIndex);
			writer.visitInsn(Opcodes.FCONST_1);
			writer.visitInsn(Opcodes.FADD);

			if (boxed)
			{
				writer.visitInsn(Opcodes.DUP);
				elementType.writeCast(writer, varType, lineNumber);
				writer.visitVarInsn(varType.getStoreOpcode(), varIndex);
			}

			writer.visitVarInsn(Opcodes.FSTORE, counterVarIndex);
			break;
		case DOUBLE:
			writer.visitVarInsn(Opcodes.DLOAD, counterVarIndex);
			writer.visitInsn(Opcodes.DCONST_1);
			writer.visitInsn(Opcodes.DADD);

			if (boxed)
			{
				writer.visitInsn(Opcodes.DUP2);
				elementType.writeCast(writer, varType, lineNumber);
				writer.visitVarInsn(varType.getStoreOpcode(), varIndex);
			}

			writer.visitVarInsn(Opcodes.DSTORE, counterVarIndex);
			break;
		case RANGEABLE:
			writer.visitVarInsn(Opcodes.ALOAD, counterVarIndex);
			writer.visitLineNumber(lineNumber);
			writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, "dyvil/collection/range/Rangeable", "next",
			                       "()Ldyvil/collection/range/Rangeable;", true);

			if (elementType.getTheClass() != LazyFields.RANGEABLE_CLASS)
			{
				LazyFields.RANGEABLE.writeCast(writer, elementType, lineNumber);
			}

			assert !boxed;

			writer.visitVarInsn(Opcodes.ASTORE, counterVarIndex);
			break;
		}

		writer.visitJumpInsn(Opcodes.GOTO, startLabel);

		// Local Variables
		writer.resetLocals(counterVarIndex);
		writer.visitLabel(endLabel);

		var.writeLocal(writer, scopeLabel, endLabel);
	}

	public static final class LazyFields
	{
		public static final IClass RANGEABLE_CLASS = Package.dyvilCollectionRange.resolveClass("Rangeable");
		public static final IType  RANGEABLE       = RANGEABLE_CLASS.getClassType();
	}
}
