package dyvil.tools.compiler.ast.statement;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class WhileStatement extends ASTNode implements IStatement
{
	private IValue	condition;
	private IValue	then;
	
	public WhileStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}
	
	public IValue getCondition()
	{
		return this.condition;
	}
	
	public void setThen(IValue then)
	{
		this.then = then;
	}
	
	public IValue getThen()
	{
		return this.then;
	}
	
	@Override
	public int getValueType()
	{
		return WHILE;
	}
	
	@Override
	public IType getType()
	{
		return Type.VOID;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.classEquals(Type.VOID);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.condition.resolveTypes(markers, context);
		this.then.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
		}
		if (this.then != null)
		{
			this.then = this.then.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.condition != null)
		{
			if (this.condition.requireType(Type.BOOLEAN))
			{
				SemanticError error = new SemanticError(this.condition.getPosition(), "The condition of a while statement has to evaluate to a boolean value.");
				error.addInfo("Condition Type: " + this.condition.getType());
				markers.add(error);
			}
			this.condition.check(markers, context);
		}
		else
		{
			markers.add(new SyntaxError(this.position, "Invalid while condition"));
		}
		if (this.then != null)
		{
			this.then.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.condition != null)
		{
			this.condition = this.condition.foldConstants();
		}
		if (this.then != null)
		{
			this.then = this.then.foldConstants();
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.then == null)
		{
			this.condition.writeStatement(writer);
		}
		
		Label start = new Label();
		start.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
		Label end = new Label();
		end.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
		
		// Condition
		writer.visitLabel(start);
		this.condition.writeJump(writer, end);
		// While Block
		this.then.writeStatement(writer);
		writer.visitJumpInsn(Opcodes.GOTO, start);
		writer.visitLabel(end);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.whileStart);
		this.condition.toString(prefix, buffer);
		buffer.append(Formatting.Statements.whileEnd);
		
		if (this.then != null)
		{
			this.then.toString(prefix, buffer);
		}
	}
}
