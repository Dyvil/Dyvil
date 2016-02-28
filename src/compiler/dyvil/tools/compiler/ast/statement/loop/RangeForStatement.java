package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.operator.RangeOperator;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class RangeForStatement extends ForEachStatement
{
	private static final int INT = 0, LONG = 1, FLOAT = 2, DOUBLE = 3;
	private static final int RANGEABLE = 4;
	
	protected IValue  startValue;
	protected IValue  endValue;
	protected boolean halfOpen;
	
	public RangeForStatement(ICodePosition position, Variable var, IValue startValue, IValue endValue, boolean halfOpen)
	{
		super(position, var);
		
		this.startValue = startValue;
		this.endValue = endValue;
		this.halfOpen = halfOpen;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IType elementType = this.variable.getType();

		final IValue typedStartValue = this.startValue.withType(elementType, typeContext, markers, context);
		if (typedStartValue != null)
		{
			this.startValue = typedStartValue;
		}
		else if (this.endValue.isResolved())
		{
			final Marker marker = Markers.semantic(this.startValue.getPosition(), "for.range.type");
			marker.addInfo(Markers.getSemantic("value.type", this.startValue.getType()));
			marker.addInfo(Markers.getSemantic("variable.type", elementType));
			markers.add(marker);
		}
		
		final IValue typedEndValue = this.endValue.withType(elementType, typeContext, markers, context);
		if (typedEndValue != null)
		{
			this.endValue = typedEndValue;
		}
		else if (this.endValue.isResolved())
		{
			final Marker marker = Markers.semantic(this.endValue.getPosition(), "for.range.type");
			marker.addInfo(Markers.getSemantic("value.type", this.endValue.getType()));
			marker.addInfo(Markers.getSemantic("variable.type", elementType));
			markers.add(marker);
		}
		
		return super.withType(type, typeContext, markers, context);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		// Determine the 'type' of the range to fasten up compilation.
		byte type = RANGEABLE;
		boolean boxed = false;

		final Variable var = this.variable;
		final int lineNumber = this.getLineNumber();

		final IType varType = var.getType();
		final IType elementType = PrimitiveType.getPrimitiveType(varType);
		if (elementType.isPrimitive())
		{
			switch (elementType.getTypecode())
			{
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.CHAR_CODE:
			case PrimitiveType.INT_CODE:
				type = INT;
				break;
			case PrimitiveType.LONG_CODE:
				type = LONG;
				break;
			case PrimitiveType.FLOAT_CODE:
				type = FLOAT;
				break;
			case PrimitiveType.DOUBLE_CODE:
				type = DOUBLE;
				break;
			}

			if (!varType.isPrimitive())
			{
				boxed = true;
			}
		}
		
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.writeLabel(scopeLabel);

		// Write the start value and store it in the variable.
		this.startValue.writeExpression(writer, elementType);
		writer.writeInsn(Opcodes.AUTO_DUP);

		final int counterVarIndex, varIndex;

		if (boxed)
		{
			// Create two variables, the counter variable and the user-visible loop variable

			writer.writeInsn(Opcodes.AUTO_DUP);
			counterVarIndex = writer.localCount();
			writer.writeVarInsn(elementType.getStoreOpcode(), counterVarIndex);

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

		this.endValue.writeExpression(writer, elementType);

		final int endVarIndex = writer.localCount();
		writer.writeVarInsn(elementType.getStoreOpcode(), endVarIndex);

		writer.writeTargetLabel(startLabel);

		// Check the condition
		switch (type)
		{
		case INT:
			writer.writeVarInsn(Opcodes.ILOAD, counterVarIndex);
			writer.writeVarInsn(Opcodes.ILOAD, endVarIndex);
			writer.writeJumpInsn(this.halfOpen ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPGT, endLabel);
			break;
		case LONG:
			writer.writeVarInsn(Opcodes.LLOAD, counterVarIndex);
			writer.writeVarInsn(Opcodes.LLOAD, endVarIndex);
			writer.writeJumpInsn(this.halfOpen ? Opcodes.IF_LCMPGE : Opcodes.IF_LCMPGT, endLabel);
			break;
		case FLOAT:
			writer.writeVarInsn(Opcodes.FLOAD, counterVarIndex);
			writer.writeVarInsn(Opcodes.FLOAD, endVarIndex);
			writer.writeJumpInsn(this.halfOpen ? Opcodes.IF_FCMPGE : Opcodes.IF_FCMPGT, endLabel);
			break;
		case DOUBLE:
			writer.writeVarInsn(Opcodes.DLOAD, counterVarIndex);
			writer.writeVarInsn(Opcodes.DLOAD, endVarIndex);
			writer.writeJumpInsn(this.halfOpen ? Opcodes.IF_DCMPGE : Opcodes.IF_DCMPGT, endLabel);
			break;
		case RANGEABLE:
			writer.writeVarInsn(Opcodes.ALOAD, counterVarIndex);
			writer.writeVarInsn(Opcodes.ALOAD, endVarIndex);
			writer.writeLineNumber(lineNumber);
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "dyvil/collection/range/Rangeable", "compareTo",
			                       "(Ldyvil/collection/range/Rangeable;)I", true);
			writer.writeJumpInsn(this.halfOpen ? Opcodes.IFGE : Opcodes.IFGT, endLabel);
			break;
		}
		
		// Action
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}
		
		// Increment
		writer.writeLabel(updateLabel);
		switch (type)
		{
		case INT:
			writer.writeIINC(counterVarIndex, 1);

			if (boxed)
			{
				writer.writeVarInsn(Opcodes.ILOAD, counterVarIndex);
				elementType.writeCast(writer, varType, lineNumber);
				writer.writeVarInsn(varType.getStoreOpcode(), varIndex);
			}
			break;
		case LONG:
			writer.writeVarInsn(Opcodes.LLOAD, counterVarIndex);
			writer.writeInsn(Opcodes.LCONST_1);
			writer.writeInsn(Opcodes.LADD);

			if (boxed)
			{
				writer.writeInsn(Opcodes.DUP2);
				elementType.writeCast(writer, varType, lineNumber);
				writer.writeVarInsn(varType.getStoreOpcode(), varIndex);
			}

			writer.writeVarInsn(Opcodes.LSTORE, counterVarIndex);
			break;
		case FLOAT:
			writer.writeVarInsn(Opcodes.FLOAD, counterVarIndex);
			writer.writeInsn(Opcodes.FCONST_1);
			writer.writeInsn(Opcodes.FADD);

			if (boxed)
			{
				writer.writeInsn(Opcodes.DUP);
				elementType.writeCast(writer, varType, lineNumber);
				writer.writeVarInsn(varType.getStoreOpcode(), varIndex);
			}

			writer.writeVarInsn(Opcodes.FSTORE, counterVarIndex);
			break;
		case DOUBLE:
			writer.writeVarInsn(Opcodes.DLOAD, counterVarIndex);
			writer.writeInsn(Opcodes.DCONST_1);
			writer.writeInsn(Opcodes.DADD);

			if (boxed)
			{
				writer.writeInsn(Opcodes.DUP2);
				elementType.writeCast(writer, varType, lineNumber);
				writer.writeVarInsn(varType.getStoreOpcode(), varIndex);
			}

			writer.writeVarInsn(Opcodes.DSTORE, counterVarIndex);
			break;
		case RANGEABLE:
			writer.writeVarInsn(Opcodes.ALOAD, counterVarIndex);
			writer.writeLineNumber(lineNumber);
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "dyvil/collection/range/Rangeable", "next",
			                       "()Ldyvil/collection/range/Rangeable;", true);
			
			if (elementType.getTheClass() != RangeOperator.LazyFields.RANGEABLE_CLASS)
			{
				RangeOperator.LazyFields.RANGEABLE.writeCast(writer, elementType, lineNumber);
			}

			assert !boxed;
			
			writer.writeVarInsn(Opcodes.ASTORE, counterVarIndex);
			break;
		}
		
		writer.writeJumpInsn(Opcodes.GOTO, startLabel);
		
		// Local Variables
		writer.resetLocals(counterVarIndex);
		writer.writeLabel(endLabel);
		
		var.writeLocal(writer, scopeLabel, endLabel);
	}
}
