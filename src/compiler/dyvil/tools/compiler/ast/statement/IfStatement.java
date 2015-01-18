package dyvil.tools.compiler.ast.statement;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.BooleanValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class IfStatement extends ASTNode implements IStatement
{
	private IValue	condition;
	private IValue	then;
	private IValue	elseThen;
	
	private IType	commonType;
	
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
		if (this.commonType != null)
		{
			return this.commonType;
		}
		
		if (this.then != null)
		{
			if (this.elseThen != null)
			{
				return this.commonType = Type.findCommonSuperType(this.then.getType(), this.elseThen.getType());
			}
			return this.commonType = this.then.getType();
		}
		return this.commonType = Type.VOID;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (this.commonType != null)
		{
			return Type.isSuperType(type, this.commonType);
		}
		
		if (this.then != null)
		{
			if (this.elseThen != null)
			{
				if (this.then.isType(type) && this.elseThen.isType(type))
				{
					this.commonType = type;
					return true;
				}
			}
			
			return this.then.isType(type);
		}
		
		return type.classEquals(Type.VOID);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.isType(type))
		{
			return 2;
		}
		else if (type.classEquals(Type.OBJECT))
		{
			return 1;
		}
		return 0;
	}
	
	@Override
	public int getValueType()
	{
		return IF;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
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
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, context);
		}
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
		if (this.condition != null)
		{
			if (!this.condition.requireType(Type.BOOLEAN))
			{
				SemanticError error = new SemanticError(this.condition.getPosition(), "The condition of an if statement has to evaluate to a boolean value.");
				error.addInfo("Condition Type: " + this.condition.getType());
				markers.add(error);
			}
			this.condition.check(markers, context);
		}
		else
		{
			markers.add(new SyntaxError(this.position, "Invalid if condition"));
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
		if (this.condition == null)
		{
			return this;
		}
		
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
			ifEnd.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
			Label elseEnd = new Label();
			elseEnd.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
			
			// Condition
			this.condition.writeJump(writer, ifEnd);
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
			ifEnd.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
			
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
			ifEnd.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
			Label elseEnd = new Label();
			elseEnd.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
			
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
			ifEnd.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
			
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
		if (this.condition != null)
		{
			this.condition.toString(prefix, buffer);
		}
		buffer.append(Formatting.Statements.ifEnd);
		
		if (this.then instanceof IStatement)
		{
			buffer.append('\n').append(prefix);
			this.then.toString(prefix, buffer);
		}
		else
		{
			buffer.append(' ');
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
			
			if (this.elseThen instanceof IStatement)
			{
				buffer.append('\n').append(prefix);
			}
			else
			{
				buffer.append(' ');
			}
			
			this.elseThen.toString(prefix, buffer);
		}
	}
}
