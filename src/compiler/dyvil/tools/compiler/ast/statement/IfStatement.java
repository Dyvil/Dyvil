package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.BooleanValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class IfStatement extends ASTNode implements IStatement
{
	public IValue		condition;
	public IValue		then;
	public IValue		elseThen;
	
	private IType		commonType;
	
	private IStatement	parent;
	
	public IfStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return IF;
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
	public void setParent(IStatement parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IStatement getParent()
	{
		return this.parent;
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
				return this.commonType = Types.findCommonSuperType(this.then.getType(), this.elseThen.getType());
			}
			return this.commonType = this.then.getType();
		}
		return this.commonType = Types.VOID;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.then == null)
		{
			return null;
		}
		
		IValue then1 = this.then.withType(type, typeContext, markers, context);
		if (then1 == null)
		{
			return null;
		}
		this.then = then1;
		
		if (this.elseThen != null)
		{
			then1 = this.elseThen.withType(type, typeContext, markers, context);
			if (then1 == null)
			{
				return null;
			}
			this.elseThen = then1;
		}
		
		this.commonType = type;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Types.VOID)
		{
			return true;
		}
		if (this.then != null && !this.then.isType(type))
		{
			return false;
		}
		if (this.elseThen != null && !this.elseThen.isType(type))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return this.isType(type) ? 3 : 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
		
		if (this.then != null)
		{
			if (this.then.isStatement())
			{
				((IStatement) this.then).setParent(this);
			}
			this.then.resolveTypes(markers, context);
		}
		
		if (this.elseThen != null)
		{
			if (this.elseThen.isStatement())
			{
				((IStatement) this.elseThen).setParent(this);
			}
			
			this.elseThen.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
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
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			IValue condition1 = this.condition.withType(Types.BOOLEAN, null, markers, context);
			if (condition1 == null)
			{
				Marker marker = markers.create(this.condition.getPosition(), "if.condition.type");
				marker.addInfo("Condition Type: " + this.condition.getType());
			}
			else
			{
				this.condition = condition1;
			}
			
			this.condition.checkTypes(markers, context);
		}
		if (this.then != null)
		{
			this.then.checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.condition != null)
		{
			this.condition.check(markers, context);
		}
		else
		{
			markers.add(this.position, "if.condition.invalid");
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
			if (this.condition.valueTag() == BOOLEAN)
			{
				return ((BooleanValue) this.condition).value ? this.then : this.elseThen;
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
	public Label resolveLabel(Name name)
	{
		return this.parent == null ? null : this.parent.resolveLabel(name);
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		org.objectweb.asm.Label elseStart = new org.objectweb.asm.Label();
		org.objectweb.asm.Label elseEnd = new org.objectweb.asm.Label();
		Object commonFrameType = this.commonType.getFrameType();
		
		// Condition
		this.condition.writeInvJump(writer, elseStart);
		// If Block
		this.then.writeExpression(writer);
		writer.getFrame().set(commonFrameType);
		writer.writeJumpInsn(Opcodes.GOTO, elseEnd);
		writer.writeLabel(elseStart);
		// Else Block
		if (this.elseThen == null)
		{
			this.commonType.writeDefaultValue(writer);
		}
		else
		{
			this.elseThen.writeExpression(writer);
		}
		writer.getFrame().set(commonFrameType);
		writer.writeLabel(elseEnd);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		if (this.then == null)
		{
			this.condition.writeExpression(writer);
			writer.writeInsn(Opcodes.POP);
			return;
		}
		
		org.objectweb.asm.Label elseStart = new org.objectweb.asm.Label();
		
		if (this.elseThen != null)
		{
			org.objectweb.asm.Label elseEnd = new org.objectweb.asm.Label();
			// Condition
			this.condition.writeInvJump(writer, elseStart);
			// If Block
			this.then.writeStatement(writer);
			writer.writeJumpInsn(Opcodes.GOTO, elseEnd);
			writer.writeLabel(elseStart);
			// Else Block
			this.elseThen.writeStatement(writer);
			writer.writeLabel(elseEnd);
		}
		else
		{
			// Condition
			this.condition.writeInvJump(writer, elseStart);
			// If Block
			this.then.writeStatement(writer);
			writer.writeLabel(elseStart);
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
		
		if (this.then != null)
		{
			this.then.toString(prefix, buffer);
			
			if (this.elseThen != null)
			{
				if (this.then.isStatement())
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
}
