package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.constant.VoidValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class IfStatement extends AbstractValue
{
	protected IValue	condition;
	protected IValue	then;
	protected IValue	elseThen;
	
	// Metadata
	private IType commonType;
	
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
	public boolean isResolved()
	{
		return this.commonType != null && this.commonType.isResolved();
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
				return this.commonType = Types.combine(this.then.getType(), this.elseThen.getType());
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
			return this;
		}
		
		this.commonType = type;
		IValue value = IType.convertValue(this.then, type, typeContext, markers, context);
		if (value == null)
		{
			Util.createTypeError(markers, this.then, type, typeContext, "if.then.type");
		}
		else
		{
			this.then = value;
		}
		
		if (this.elseThen != null)
		{
			value = IType.convertValue(this.elseThen, type, typeContext, markers, context);
			if (value == null)
			{
				Util.createTypeError(markers, this.elseThen, type, typeContext, "if.else.type");
			}
			else
			{
				this.elseThen = value;
			}
		}
		
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
		return !(this.elseThen != null && !this.elseThen.isType(type));
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (this.elseThen == null)
		{
			return this.then.getTypeMatch(type);
		}
		
		float f1 = this.then.getTypeMatch(type);
		float f2 = this.elseThen.getTypeMatch(type);
		return f1 == 0 || f2 == 0 ? 0 : (f1 + f2) / 2;
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
			this.then.resolveTypes(markers, context);
		}
		
		if (this.elseThen != null)
		{
			this.elseThen.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		if (this.then != null)
		{
			this.then.resolveStatement(context, markers);
		}
		
		if (this.elseThen != null)
		{
			this.elseThen.resolveStatement(context, markers);
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
			IValue condition1 = this.condition.withType(Types.BOOLEAN, Types.BOOLEAN, markers, context);
			if (condition1 == null)
			{
				Marker marker = I18n.createMarker(this.condition.getPosition(), "if.condition.type");
				marker.addInfo(I18n.getString("value.type", this.condition.getType()));
				markers.add(marker);
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
			markers.add(I18n.createMarker(this.position, "if.condition.invalid"));
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
		if (this.condition != null)
		{
			if (this.condition.valueTag() == BOOLEAN)
			{
				if (this.condition.booleanValue())
				{
					// Condition is true -> Return the action
					return this.then.foldConstants();
				}
				else if (this.elseThen != null)
				{
					// Condition is false, else clause exists -> Return else
					// clause
					return this.elseThen.foldConstants();
				}
				else
				{
					// Condition is false, no else clause -> Return empty
					// statement (VoidValue)
					return new VoidValue(this.position);
				}
			}
			this.condition = this.condition.foldConstants();
		}
		
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.condition != null)
		{
			this.condition = this.condition.cleanup(context, compilableList);
		}
		
		if (this.then != null)
		{
			this.then = this.then.cleanup(context, compilableList);
		}
		if (this.elseThen != null)
		{
			this.elseThen = this.elseThen.cleanup(context, compilableList);
		}
		
		if (this.condition.valueTag() == BOOLEAN)
		{
			return this.condition.booleanValue() ? this.then : this.elseThen;
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label elseStart = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label elseEnd = new dyvil.tools.asm.Label();
		Object commonFrameType = this.commonType.getFrameType();
		
		// Condition
		this.condition.writeInvJump(writer, elseStart);
		// If Block
		this.then.writeExpression(writer, this.commonType);
		
		if (!writer.hasReturn())
		{
			writer.getFrame().set(commonFrameType);
			writer.writeJumpInsn(Opcodes.GOTO, elseEnd);
		}
		
		writer.writeTargetLabel(elseStart);
		
		// Else Block
		if (this.elseThen == null)
		{
			this.commonType.writeDefaultValue(writer);
		}
		else
		{
			this.elseThen.writeExpression(writer, this.commonType);
		}
		
		if (!writer.hasReturn())
		{
			writer.getFrame().set(commonFrameType);
		}
		
		writer.writeTargetLabel(elseEnd);
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
		
		dyvil.tools.asm.Label elseStart = new dyvil.tools.asm.Label();
		
		if (this.elseThen != null)
		{
			dyvil.tools.asm.Label elseEnd = new dyvil.tools.asm.Label();
			// Condition
			this.condition.writeInvJump(writer, elseStart);
			// If Block
			this.then.writeStatement(writer);
			writer.writeJumpInsn(Opcodes.GOTO, elseEnd);
			writer.writeTargetLabel(elseStart);
			// Else Block
			this.elseThen.writeStatement(writer);
			writer.writeTargetLabel(elseEnd);
		}
		else
		{
			// Condition
			this.condition.writeInvJump(writer, elseStart);
			// If Block
			this.then.writeStatement(writer);
			writer.writeTargetLabel(elseStart);
		}
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
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
				buffer.append(Formatting.Statements.ifElse);
				this.elseThen.toString(prefix, buffer);
			}
		}
	}
}
