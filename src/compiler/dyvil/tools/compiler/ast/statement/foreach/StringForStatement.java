package dyvil.tools.compiler.ast.statement.foreach;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class StringForStatement extends ForEachStatement
{
	public static final Name $string = Name.getQualified("$string");
	
	protected Variable	indexVar;
	protected Variable	lengthVar;
	protected Variable	stringVar;
	
	public StringForStatement(ICodePosition position, Variable var, IValue action)
	{
		super(position, var, action);
		
		this.indexVar = new Variable(ArrayForStatement.$index, Types.INT);
		this.lengthVar = new Variable(ArrayForStatement.$length, Types.INT);
		this.stringVar = new Variable($string, Types.STRING);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.variable.getName() == name)
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
		return null;
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		
		Variable var = this.variable;
		Variable stringVar = this.stringVar;
		Variable indexVar = this.indexVar;
		Variable lengthVar = this.lengthVar;
		int lineNumber = this.getLineNumber();
		
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.writeLabel(scopeLabel);
		
		// Load the String
		var.getValue().writeExpression(writer, var.getType());
		
		// Local Variables
		int locals = writer.localCount();
		writer.writeInsn(Opcodes.DUP);
		stringVar.writeInit(writer, null);
		// Get the length
		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
		writer.writeInsn(Opcodes.DUP);
		lengthVar.writeInit(writer, null);
		
		// Initial Boundary Check - if the length is 0, skip the loop.
		writer.writeJumpInsn(Opcodes.IFEQ, endLabel);
		
		// Set index to 0
		writer.writeLDC(0);
		indexVar.writeInit(writer, null);
		
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
		// Increment index
		writer.writeIINC(indexVar.getIndex(), 1);
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
