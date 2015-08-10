package dyvil.tools.compiler.ast.statement.foreach;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.statement.ForStatement;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class RangeForStatement extends ForEachStatement
{
	public IValue	value1;
	public IValue	value2;
	
	private Variable	startVar;
	private Variable	endVar;
	
	public RangeForStatement(ICodePosition position, Variable var, IValue value1, IValue value2, IValue action)
	{
		super(position, var, action);
		
		this.value1 = value1;
		this.value2 = value2;
		
		IType varType = var.getType();
		
		this.startVar = new Variable(ForStatement.$forStart, varType);
		
		this.endVar = new Variable(ForStatement.$forEnd, varType);
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		IType rangeType = this.variable.getType();
		IValue v = this.value1.withType(rangeType, typeContext, markers, context);
		if (v == null)
		{
			Marker marker = markers.create(this.value1.getPosition(), "for.range.type");
			marker.addInfo("Value Type: " + this.value1.getType());
			marker.addInfo("Variable Type: " + rangeType);
		}
		else
		{
			this.value1 = v;
		}
		
		v = this.value2.withType(rangeType, typeContext, markers, context);
		if (v == null)
		{
			Marker marker = markers.create(this.value2.getPosition(), "for.range.type");
			marker.addInfo("Value Type: " + this.value2.getType());
			marker.addInfo("Variable Type: " + rangeType);
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
		byte type = 5;
		IType rangeType = this.variable.getType();
		if (rangeType.isPrimitive())
		{
			switch (rangeType.getTypecode())
			{
			case ClassFormat.T_BYTE:
			case ClassFormat.T_SHORT:
			case ClassFormat.T_CHAR:
			case ClassFormat.T_INT:
				type = 0;
				break;
			case ClassFormat.T_LONG:
				type = 1;
				break;
			case ClassFormat.T_FLOAT:
				type = 2;
				break;
			case ClassFormat.T_DOUBLE:
				type = 3;
				break;
			}
		}
		else if (rangeType.getTheClass() == Types.STRING_CLASS)
		{
			type = 4;
		}
		
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.writeLabel(scopeLabel);
		
		Variable var = this.variable;
		Variable endVar = this.endVar;
		Variable startVar = this.startVar;
		
		int locals = writer.localCount();
		
		// Write the start value and store it in the variable.
		this.value1.writeExpression(writer);
		writer.writeInsn(Opcodes.AUTO_DUP);
		startVar.writeInit(writer, null);
		var.writeInit(writer, null);
		
		this.value2.writeExpression(writer);
		endVar.writeInit(writer, null);
		
		// Jump to boundary check
		writer.writeTargetLabel(startLabel);
		
		// Action
		if (this.action != null)
		{
			this.action.writeStatement(writer);
		}
		
		// Increment / Next and Boundary Check
		
		int varIndex = var.getIndex();
		int endIndex = endVar.getIndex();
		
		writer.writeLabel(updateLabel);
		switch (type)
		{
		case 0: // Integers
			writer.writeIINC(varIndex, 1);
			writer.writeVarInsn(Opcodes.ILOAD, varIndex);
			writer.writeVarInsn(Opcodes.ILOAD, endIndex);
			writer.writeJumpInsn(Opcodes.IF_ICMPLE, startLabel);
			break;
		case 1: // Long
			writer.writeVarInsn(Opcodes.LLOAD, varIndex);
			writer.writeLDC(1L);
			writer.writeInsn(Opcodes.LADD);
			writer.writeInsn(Opcodes.DUP2);
			writer.writeVarInsn(Opcodes.LSTORE, varIndex);
			writer.writeVarInsn(Opcodes.LLOAD, endIndex);
			writer.writeJumpInsn(Opcodes.IF_LCMPLE, startLabel);
			break;
		case 2: // Float
			writer.writeVarInsn(Opcodes.FLOAD, varIndex);
			writer.writeLDC(1F);
			writer.writeInsn(Opcodes.FADD);
			writer.writeInsn(Opcodes.DUP);
			writer.writeVarInsn(Opcodes.FSTORE, varIndex);
			writer.writeVarInsn(Opcodes.FLOAD, endIndex);
			writer.writeJumpInsn(Opcodes.IF_FCMPLE, startLabel);
			break;
		case 3: // Double
			writer.writeVarInsn(Opcodes.DLOAD, varIndex);
			writer.writeLDC(1D);
			writer.writeInsn(Opcodes.DADD);
			writer.writeInsn(Opcodes.DUP2);
			writer.writeVarInsn(Opcodes.DSTORE, varIndex);
			writer.writeVarInsn(Opcodes.DLOAD, endIndex);
			writer.writeJumpInsn(Opcodes.IF_DCMPLE, startLabel);
			break;
		case 4: // String
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeVarInsn(Opcodes.ALOAD, endIndex);
			writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/String;)I", false);
			writer.writeJumpInsn(Opcodes.IFGE, endLabel);
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/collection/range/StringRange", "next", "(Ljava/lang/String;)Ljava/lang/String;", false);
			writer.writeVarInsn(Opcodes.ASTORE, varIndex);
			writer.writeJumpInsn(Opcodes.GOTO, startLabel);
			break;
		case 5: // Ordered
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeVarInsn(Opcodes.ALOAD, endIndex);
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "dyvil/lang/Ordered", "$gt$eq", "(Ldyvil/lang/Ordered;)Z", true);
			writer.writeJumpInsn(Opcodes.IFNE, endLabel);
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "dyvil/lang/Ordered", "next", "()Ldyvil/lang/Ordered;", true);
			writer.writeTypeInsn(Opcodes.CHECKCAST, rangeType.getInternalName());
			writer.writeVarInsn(Opcodes.ASTORE, varIndex);
			writer.writeJumpInsn(Opcodes.GOTO, startLabel);
			break;
		}
		
		// Local Variables
		writer.resetLocals(locals);
		writer.writeLabel(endLabel);
		
		var.writeLocal(writer, scopeLabel, endLabel);
		startVar.writeLocal(writer, scopeLabel, endLabel);
		endVar.writeLocal(writer, scopeLabel, endLabel);
	}
}
