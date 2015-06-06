package dyvil.tools.compiler.ast.statement.foreach;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class ArrayForStatement extends ForEachStatement
{
	public static final Name	$index	= Name.getQualified("$index");
	public static final Name	$length	= Name.getQualified("$length");
	public static final Name	$array	= Name.getQualified("$array");
	
	protected Variable			indexVar;
	protected Variable			lengthVar;
	protected Variable			arrayVar;
	
	public ArrayForStatement(Variable var, IValue action)
	{
		this(var, action, var.value.getType());
	}
	
	public ArrayForStatement(Variable var, IValue action, IType arrayType)
	{
		super(var, action);
		
		Variable temp = new Variable();
		temp.type = Types.INT;
		temp.name = $index;
		this.indexVar = temp;
		
		temp = new Variable();
		temp.type = Types.INT;
		temp.name = $length;
		this.lengthVar = temp;
		
		temp = new Variable();
		temp.type = var.value.getType();
		temp.name = $array;
		this.arrayVar = temp;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		if (name == this.variable.name)
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
		
		return this.context.resolveField(name);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		org.objectweb.asm.Label startLabel = this.startLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label updateLabel = this.updateLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label endLabel = this.endLabel.target = new org.objectweb.asm.Label();
		
		Variable var = this.variable;
		Variable arrayVar = this.arrayVar;
		Variable indexVar = this.indexVar;
		Variable lengthVar = this.lengthVar;
		
		org.objectweb.asm.Label scopeLabel = new org.objectweb.asm.Label();
		writer.writeLabel(scopeLabel);
		
		// Load the array
		var.value.writeExpression(writer);
		
		// Local Variables
		int locals = writer.localCount();
		writer.writeInsn(Opcodes.DUP);
		arrayVar.writeInit(writer, null);
		// Load the length
		writer.writeInsn(Opcodes.ARRAYLENGTH);
		writer.writeInsn(Opcodes.DUP);
		lengthVar.writeInit(writer, null);
		// Set index to 0
		writer.writeLDC(0);
		indexVar.writeInit(writer, null);
		
		// Initial Boundary Check - if the length is 0, skip the loop
		writer.writeJumpInsn(Opcodes.IFEQ, endLabel);
		writer.writeTargetLabel(startLabel);
		
		// Load the element
		arrayVar.writeGet(writer, null);
		indexVar.writeGet(writer, null);
		writer.writeInsn(var.type.getArrayLoadOpcode());
		var.writeInit(writer, null);
		
		// Action
		if (this.action != null)
		{
			this.action.writeStatement(writer);
		}
		
		writer.writeLabel(updateLabel);
		// Increase index
		writer.writeIINC(indexVar.index, 1);
		// Boundary Check
		indexVar.writeGet(writer, null);
		lengthVar.writeGet(writer, null);
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
