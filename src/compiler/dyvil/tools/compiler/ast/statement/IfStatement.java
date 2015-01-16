package dyvil.tools.compiler.ast.statement;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IStatement;
import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.BooleanValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class IfStatement extends ASTNode implements IStatement
{
	private IValue	condition;
	private IValue	then;
	private IValue	elseThen;
	
	public IfStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	public void setCondition(IValue condition)
	{
		this.condition = condition;
	}
	
	public void setThen(IValue then)
	{
		this.then = then;
	}
	
	public void setElse(IValue elseThen)
	{
		this.elseThen = elseThen;
	}
	
	public IValue getCondition()
	{
		return this.condition;
	}
	
	public IValue getThen()
	{
		return this.then;
	}
	
	public IValue getElse()
	{
		return this.elseThen;
	}
	
	@Override
	public IType getType()
	{
		return this.then == null ? this.condition.getType() : this.then.getType();
	}
	
	@Override
	public int getValueType()
	{
		return IF;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.condition.resolveTypes(markers, context);
		if (this.then != null)
		{
			this.then.resolveTypes(markers, context);
		}
		if (this.elseThen != null)
		{
			this.elseThen.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		this.condition = this.condition.resolve(markers, context);
		if (this.then != null)
		{
			this.then = this.then.resolve(markers, context);
		}
		if (this.elseThen != null)
		{
			this.elseThen = this.elseThen.resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		this.condition.check(markers, context);
		if (!Type.isSuperType(Type.BOOLEAN, this.condition.getType()))
		{
			markers.add(new SemanticError(this.position, "The condition of an if statement has to evaluate to a boolean value."));
		}
		
		if (this.then != null)
		{
			this.then.check(markers, context);
		}
		if (this.elseThen != null)
		{
			this.elseThen.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.condition.isConstant())
		{
			int type = this.condition.getValueType();
			if (type == BOOLEAN)
			{
				if (((BooleanValue) this.condition).value)
				{
					return this.then;
				}
				else
				{
					return this.elseThen;
				}
			}
			return this;
		}
		
		this.condition = this.condition.foldConstants();
		if (this.then != null)
		{
			this.then = this.then.foldConstants();
		}
		if (this.elseThen != null)
		{
			this.elseThen = this.elseThen.foldConstants();
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.then == null)
		{
			this.condition.writeExpression(writer);
		}
		if (this.elseThen != null)
		{
			Label ifEnd = new Label();
			Label elseEnd = new Label();
			
			// Condition
			this.condition.writeExpression(writer);
			writer.visitJumpInsn(Opcodes.IFEQ, ifEnd);
			// If Block
			this.then.writeExpression(writer);
			writer.visitJumpInsn(Opcodes.GOTO, elseEnd);
			writer.visitLabel(ifEnd);
			// Else Block
			this.elseThen.writeExpression(writer);
			writer.visitLabel(elseEnd);
		}
		else
		{
			Label ifEnd = new Label();
			
			// Condition
			this.condition.writeJump(writer, ifEnd);
			// If Block
			this.then.writeExpression(writer);
			writer.visitLabel(ifEnd);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.elseThen != null)
		{
			Label ifEnd = new Label();
			Label elseEnd = new Label();
			
			// Condition
			this.condition.writeJump(writer, ifEnd);
			// If Block
			this.then.writeStatement(writer);
			writer.visitJumpInsn(Opcodes.GOTO, elseEnd);
			writer.visitLabel(ifEnd);
			// Else Block
			this.elseThen.writeStatement(writer);
			writer.visitLabel(elseEnd);
		}
		else
		{
			Label ifEnd = new Label();
			
			// Condition
			this.condition.writeJump(writer, ifEnd);
			// If Block
			this.then.writeStatement(writer);
			writer.visitLabel(ifEnd);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.ifStart);
		this.condition.toString(prefix, buffer);
		buffer.append(Formatting.Statements.ifEnd).append(' ');
		
		if (this.then != null)
		{
			this.then.toString(prefix, buffer);
		}
		
		if (this.elseThen != null)
		{
			if (this.then instanceof IStatement)
			{
				buffer.append('\n').append(prefix);
			}
			else
			{
				buffer.append(' ');
			}
			
			buffer.append(Formatting.Statements.ifElse);
			this.elseThen.toString(prefix, buffer);
		}
	}
}
