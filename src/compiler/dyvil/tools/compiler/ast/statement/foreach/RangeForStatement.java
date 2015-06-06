package dyvil.tools.compiler.ast.statement.foreach;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.statement.ForStatement;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class RangeForStatement extends ForEachStatement
{
	public IValue	value1;
	public IValue	value2;
	
	private Variable startVar;
	private  Variable endVar;
	
	public RangeForStatement(Variable var, IValue value1, IValue value2, IValue action)
	{
		super(var, action);
		
		this.value1 = value1;
		this.value2 = value2;
		
		IType varType = var.getType();
		
		this.startVar = new Variable();
		this.startVar.name = ForStatement.$forStart;
		this.startVar.type = varType;
		
		this.endVar = new Variable();
		this.endVar.name = ForStatement.$forStart;
		this.endVar.type = varType;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IType rangeType = this.variable.type;
		IValue v = value1.withType(rangeType);
		if (v == null)
		{
			Marker marker = markers.create(value1.getPosition(), "for.range.type");
			marker.addInfo("Value Type: " + value1.getType());
			marker.addInfo("Variable Type: " + rangeType);
		}
		else
		{
			value1 = v;
		}
		
		v = value2.withType(rangeType);
		if (v == null)
		{
			Marker marker = markers.create(value2.getPosition(), "for.range.type");
			marker.addInfo("Value Type: " + value2.getType());
			marker.addInfo("Variable Type: " + rangeType);
		}
		else
		{
			value2 = v;
		}
		
		if (this.action != null)
		{
			this.context = context;
			this.action.checkTypes(markers, this);
			this.context = null;
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		// Determine the 'type' of the range to fasten up compilation.
		byte type = 5;
		IType rangeType = this.variable.type;
		if (rangeType.typeTag() == IType.PRIMITIVE_TYPE)
		{
			switch (((PrimitiveType) rangeType).typecode)
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
		else if (rangeType.classEquals(Types.STRING))
		{
			type = 4;
		}
		
		org.objectweb.asm.Label startLabel = this.startLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label updateLabel = this.updateLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label endLabel = this.endLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label scopeLabel = new org.objectweb.asm.Label();
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
		
		writer.writeLabel(updateLabel);
		switch (type) {
		case 0: // Integers
			writer.writeIINC(var.index, 1);
			writer.writeVarInsn(Opcodes.ILOAD, var.index);
			writer.writeVarInsn(Opcodes.ILOAD, endVar.index);
			writer.writeJumpInsn(Opcodes.IF_ICMPLE, startLabel);
			break;
		case 1: // Long
			writer.writeVarInsn(Opcodes.LLOAD, var.index);
			writer.writeLDC(1L);
			writer.writeInsn(Opcodes.LADD);
			writer.writeInsn(Opcodes.DUP2);
			writer.writeVarInsn(Opcodes.LSTORE, var.index);
			writer.writeVarInsn(Opcodes.LLOAD, endVar.index);
			writer.writeJumpInsn(Opcodes.IF_LCMPLE, startLabel);
			break;
		case 2: // Float
			writer.writeVarInsn(Opcodes.FLOAD, var.index);
			writer.writeLDC(1F);
			writer.writeInsn(Opcodes.FADD);
			writer.writeInsn(Opcodes.DUP);
			writer.writeVarInsn(Opcodes.FSTORE, var.index);
			writer.writeVarInsn(Opcodes.FLOAD, endVar.index);
			writer.writeJumpInsn(Opcodes.IF_FCMPLE, startLabel);
			break;
		case 3: // Double
			writer.writeVarInsn(Opcodes.DLOAD, var.index);
			writer.writeLDC(1D);
			writer.writeInsn(Opcodes.DADD);
			writer.writeInsn(Opcodes.DUP2);
			writer.writeVarInsn(Opcodes.DSTORE, var.index);
			writer.writeVarInsn(Opcodes.DLOAD, endVar.index);
			writer.writeJumpInsn(Opcodes.IF_DCMPLE, startLabel);
			break;
		case 4: // String
			writer.writeVarInsn(Opcodes.ALOAD, var.index);
			writer.writeVarInsn(Opcodes.ALOAD, endVar.index);
			writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/String;)I", false);
			writer.writeJumpInsn(Opcodes.IFGE, endLabel);
			writer.writeVarInsn(Opcodes.ALOAD, var.index);
			writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/collection/range/StringRange", "next", "(Ljava/lang/String;)Ljava/lang/String;", false);
			writer.writeVarInsn(Opcodes.ASTORE, var.index);
			writer.writeJumpInsn(Opcodes.GOTO, startLabel);
			break;
		case 5: // Ordered
			writer.writeVarInsn(Opcodes.ALOAD, var.index);
			writer.writeVarInsn(Opcodes.ALOAD, endVar.index);
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "dyvil/lang/Ordered", "$gt$eq", "(Ldyvil/lang/Ordered;)Z", true);
			writer.writeJumpInsn(Opcodes.IFNE, endLabel);
			writer.writeVarInsn(Opcodes.ALOAD, var.index);
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "dyvil/lang/Ordered", "next", "()Ldyvil/lang/Ordered;", true);
			writer.writeTypeInsn(Opcodes.CHECKCAST, rangeType.getInternalName());
			writer.writeVarInsn(Opcodes.ASTORE, var.index);
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
