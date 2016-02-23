package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.operator.RangeOperator;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
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
	
	protected IValue  value1;
	protected IValue  value2;
	protected boolean halfOpen;
	
	public RangeForStatement(ICodePosition position, Variable var, IValue value1, IValue value2, boolean halfOpen)
	{
		super(position, var);
		
		this.value1 = value1;
		this.value2 = value2;
		this.halfOpen = halfOpen;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		IType rangeType = this.variable.getType();
		IValue v = this.value1.withType(rangeType, typeContext, markers, context);
		if (v == null)
		{
			Marker marker = Markers.semantic(this.value1.getPosition(), "for.range.type");
			marker.addInfo(Markers.getSemantic("value.type", this.value1.getType()));
			marker.addInfo(Markers.getSemantic("variable.type", rangeType));
			markers.add(marker);
		}
		else
		{
			this.value1 = v;
		}
		
		v = this.value2.withType(rangeType, typeContext, markers, context);
		if (v == null)
		{
			Marker marker = Markers.semantic(this.value2.getPosition(), "for.range.type");
			marker.addInfo(Markers.getSemantic("value.type", this.value2.getType()));
			marker.addInfo(Markers.getSemantic("variable.type", rangeType));
			markers.add(marker);
		}
		else
		{
			this.value2 = v;
		}
		
		return super.withType(type, typeContext, markers, context);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		// Determine the 'type' of the range to fasten up compilation.
		byte type = RANGEABLE;
		IType rangeType = this.variable.getType();
		if (rangeType.isPrimitive())
		{
			switch (rangeType.getTypecode())
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
		}
		
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.writeLabel(scopeLabel);

		final int startVarIndex = writer.localCount();
		
		Variable var = this.variable;

		// Write the start value and store it in the variable.
		this.value1.writeExpression(writer, var.getType());
		writer.writeInsn(Opcodes.AUTO_DUP);
		var.writeInit(writer, null);

		final int endVarIndex = writer.localCount();

		this.value2.writeExpression(writer, var.getType());
		writer.writeVarInsn(var.getType().getStoreOpcode(), endVarIndex);
		
		writer.writeTargetLabel(startLabel);
		
		int varIndex = var.getLocalIndex();
		
		// Check the condition
		switch (type)
		{
		case INT:
			writer.writeVarInsn(Opcodes.ILOAD, varIndex);
			writer.writeVarInsn(Opcodes.ILOAD, endVarIndex);
			writer.writeJumpInsn(this.halfOpen ? Opcodes.IF_ICMPGE : Opcodes.IF_ICMPGT, endLabel);
			break;
		case LONG:
			writer.writeVarInsn(Opcodes.LLOAD, varIndex);
			writer.writeVarInsn(Opcodes.LLOAD, endVarIndex);
			writer.writeJumpInsn(this.halfOpen ? Opcodes.IF_LCMPGE : Opcodes.IF_LCMPGT, endLabel);
			break;
		case FLOAT:
			writer.writeVarInsn(Opcodes.FLOAD, varIndex);
			writer.writeVarInsn(Opcodes.FLOAD, endVarIndex);
			writer.writeJumpInsn(this.halfOpen ? Opcodes.IF_FCMPGE : Opcodes.IF_FCMPGT, endLabel);
			break;
		case DOUBLE:
			writer.writeVarInsn(Opcodes.DLOAD, varIndex);
			writer.writeVarInsn(Opcodes.DLOAD, endVarIndex);
			writer.writeJumpInsn(this.halfOpen ? Opcodes.IF_DCMPGE : Opcodes.IF_DCMPGT, endLabel);
			break;
		case RANGEABLE:
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeVarInsn(Opcodes.ALOAD, endVarIndex);
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
			writer.writeIINC(varIndex, 1);
			break;
		case LONG:
			writer.writeVarInsn(Opcodes.LLOAD, varIndex);
			writer.writeInsn(Opcodes.LCONST_1);
			writer.writeInsn(Opcodes.LADD);
			writer.writeVarInsn(Opcodes.LSTORE, varIndex);
			break;
		case FLOAT:
			writer.writeVarInsn(Opcodes.FLOAD, varIndex);
			writer.writeLDC(1F);
			writer.writeInsn(Opcodes.FCONST_1);
			writer.writeVarInsn(Opcodes.FSTORE, varIndex);
			break;
		case DOUBLE:
			writer.writeVarInsn(Opcodes.DLOAD, varIndex);
			writer.writeInsn(Opcodes.DCONST_1);
			writer.writeInsn(Opcodes.DADD);
			writer.writeVarInsn(Opcodes.DSTORE, varIndex);
			break;
		case RANGEABLE:
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "dyvil/collection/range/Rangeable", "next", "()Ldyvil/collection/range/Rangeable;",
			                       true);
			
			if (rangeType.getTheClass() != RangeOperator.LazyFields.RANGEABLE_CLASS)
			{
				RangeOperator.LazyFields.RANGEABLE.writeCast(writer, rangeType, this.getLineNumber());
			}
			
			writer.writeVarInsn(Opcodes.ASTORE, varIndex);
			break;
		}
		
		writer.writeJumpInsn(Opcodes.GOTO, startLabel);
		
		// Local Variables
		writer.resetLocals(startVarIndex);
		writer.writeLabel(endLabel);
		
		var.writeLocal(writer, scopeLabel, endLabel);
	}
}
