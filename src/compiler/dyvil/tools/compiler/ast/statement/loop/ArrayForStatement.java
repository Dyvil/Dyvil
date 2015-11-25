package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public class ArrayForStatement extends ForEachStatement
{
	public static final Name	$index	= Name.getQualified("$index");
	public static final Name	$length	= Name.getQualified("$length");
	public static final Name	$array	= Name.getQualified("$array");
	
	protected Variable	indexVar;
	protected Variable	lengthVar;
	protected Variable	arrayVar;
	
	protected IMethod boxMethod;
	
	public ArrayForStatement(ICodePosition position, Variable var)
	{
		this(position, var, var.getValue().getType());
	}
	
	public ArrayForStatement(ICodePosition position, Variable var, IType arrayType)
	{
		super(position, var);
		
		this.indexVar = new Variable($index, Types.INT);
		this.lengthVar = new Variable($length, Types.INT);
		this.arrayVar = new Variable($array, arrayType);
		
		IType elementType = arrayType.getElementType();
		IType varType = var.getType();
		boolean primitive = varType.isPrimitive();
		if (primitive != elementType.isPrimitive())
		{
			if (primitive)
			{
				this.boxMethod = varType.getUnboxMethod();
			}
			else
			{
				this.boxMethod = elementType.getBoxMethod();
			}
		}
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (name == this.variable.getName())
		{
			return this.variable;
		}
		
		if (name == $index)
		{
			return this.indexVar;
		}
		if (name == $length)
		{
			return this.lengthVar;
		}
		if (name == $array)
		{
			return this.arrayVar;
		}
		
		return null;
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		
		Variable var = this.variable;
		Variable arrayVar = this.arrayVar;
		Variable indexVar = this.indexVar;
		Variable lengthVar = this.lengthVar;
		int lineNumber = this.getLineNumber();
		
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.writeLabel(scopeLabel);
		
		// Load the array
		var.getValue().writeExpression(writer, null);
		
		// Local Variables
		int locals = writer.localCount();
		writer.writeInsn(Opcodes.DUP);
		arrayVar.writeInit(writer, null);
		// Load the length
		writer.writeLineNumber(lineNumber);
		writer.writeInsn(Opcodes.ARRAYLENGTH);
		writer.writeInsn(Opcodes.DUP);
		lengthVar.writeInit(writer, null);
		
		// Initial Boundary Check - if the length is 0, skip the loop
		writer.writeJumpInsn(Opcodes.IFEQ, endLabel);
		
		// Set index to 0
		writer.writeLDC(0);
		indexVar.writeInit(writer, null);
		
		writer.writeTargetLabel(startLabel);
		
		// Load the element
		arrayVar.writeGet(writer, null, lineNumber);
		indexVar.writeGet(writer, null, lineNumber);
		writer.writeLineNumber(lineNumber);
		writer.writeInsn(arrayVar.getType().getElementType().getArrayLoadOpcode());
		// Auto(un)boxing
		if (this.boxMethod != null)
		{
			this.boxMethod.writeCall(writer, null, EmptyArguments.INSTANCE, null, lineNumber);
		}
		// Store variable
		var.writeInit(writer, null);
		
		// Action
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}
		
		writer.writeLabel(updateLabel);
		// Increment index
		writer.writeIINC(indexVar.getLocalIndex(), 1);
		// Boundary Check
		indexVar.writeGet(writer, null, lineNumber);
		lengthVar.writeGet(writer, null, lineNumber);
		writer.writeJumpInsn(Opcodes.IF_ICMPLT, startLabel);
		
		// Local Variables
		writer.resetLocals(locals);
		writer.writeLabel(endLabel);
		
		var.writeLocal(writer, scopeLabel, endLabel);
		indexVar.writeLocal(writer, scopeLabel, endLabel);
		lengthVar.writeLocal(writer, scopeLabel, endLabel);
		arrayVar.writeLocal(writer, scopeLabel, endLabel);
	}
}
