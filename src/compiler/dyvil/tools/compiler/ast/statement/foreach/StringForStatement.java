package dyvil.tools.compiler.ast.statement.foreach;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class StringForStatement extends ForEachStatement
{
	public static final Name	$string	= Name.getQualified("$string");
	
	protected Variable			indexVar;
	protected Variable			lengthVar;
	protected Variable			stringVar;
	
	public StringForStatement(Variable var, IValue action)
	{
		super(var, action);
		
		Variable var1 = new Variable();
		var1.type = Types.INT;
		var1.name = ArrayForStatement.$index;
		this.indexVar = var1;
		
		var1 = new Variable();
		var1.type = Types.INT;
		var1.name = ArrayForStatement.$length;
		this.lengthVar = var1;
		
		var1 = new Variable();
		var1.type = Types.STRING;
		var1.name = $string;
		this.stringVar = var1;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.variable.name == name)
		{
			return this.variable;
		}
		if (name == ArrayForStatement.$index)
		{
			return this.indexVar;
		}
		if (name == ArrayForStatement.$length)
		{
			return this.lengthVar;
		}
		if (name == $string)
		{
			return this.stringVar;
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
		Variable stringVar = this.stringVar;
		Variable indexVar = this.indexVar;
		Variable lengthVar = this.lengthVar;
		int lineNumber = this.getLineNumber();
		
		org.objectweb.asm.Label scopeLabel = new org.objectweb.asm.Label();
		writer.writeLabel(scopeLabel);
		
		// Load the String
		var.value.writeExpression(writer);
		
		// Local Variables
		int locals = writer.localCount();
		writer.writeInsn(Opcodes.DUP);
		stringVar.writeInit(writer, null);
		// Get the length
		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
		writer.writeInsn(Opcodes.DUP);
		lengthVar.writeInit(writer, null);
		// Set index to 0
		writer.writeLDC(0);
		indexVar.writeInit(writer, null);
		
		// Initial Boundary Check - if the length is 0, skip the loop.
		writer.writeJumpInsn(Opcodes.IFEQ, endLabel);
		writer.writeTargetLabel(startLabel);
		
		// Get the char at the index
		stringVar.writeGet(writer, null, lineNumber);
		indexVar.writeGet(writer, null, lineNumber);
		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
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
		indexVar.writeGet(writer, null, lineNumber);
		lengthVar.writeGet(writer, null, lineNumber);
		writer.writeJumpInsn(Opcodes.IF_ICMPLT, startLabel);
		
		// Local Variables
		writer.resetLocals(locals);
		writer.writeLabel(endLabel);
		
		var.writeLocal(writer, scopeLabel, endLabel);
		indexVar.writeLocal(writer, scopeLabel, endLabel);
		lengthVar.writeLocal(writer, scopeLabel, endLabel);
		stringVar.writeLocal(writer, scopeLabel, endLabel);
	}
}
