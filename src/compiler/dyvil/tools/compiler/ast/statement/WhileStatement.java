package dyvil.tools.compiler.ast.statement;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class WhileStatement extends ASTNode implements IStatement, ILoop
{
	private IValue		condition;
	private IValue		then;
	
	private IStatement	parent;
	
	public Label		start;
	public Label		end;
	
	public WhileStatement(ICodePosition position)
	{
		this.position = position;
		
		this.start = new Label();
		this.start.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
		this.end = new Label();
		this.end.info = MethodWriter.JUMP_INSTRUCTION_TARGET;
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
	public Label getStartLabel()
	{
		return this.start;
	}
	
	@Override
	public Label getEndLabel()
	{
		return this.end;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.condition.resolveTypes(markers, context);
		
		if (this.then.isStatement())
		{
			((IStatement) this.then).setParent(this);
			this.then.resolveTypes(markers, context);
		}
		else if (this.then != null)
		{
			this.then.resolveTypes(markers, context);
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
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.condition != null)
		{
			if (!this.condition.requireType(Type.BOOLEAN))
			{
				Marker marker = Markers.create(this.condition.getPosition(), "while.condition.type");
				marker.addInfo("Condition Type: " + this.condition.getType());
				markers.add(marker);
			}
			this.condition.check(markers, context);
		}
		else
		{
			markers.add(new SyntaxError(this.position, "while.condition.invalid"));
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
	public Label resolveLabel(String name)
	{
		if ("$whileStart".equals(name))
		{
			return this.start;
		}
		else if ("$whileEnd".equals(name))
		{
			return this.end;
		}
		
		return this.parent == null ? null : this.parent.resolveLabel(name);
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
		if (this.condition != null)
		{
			this.condition.toString(prefix, buffer);
		}
		buffer.append(Formatting.Statements.whileEnd);
		
		if (this.then != null)
		{
			if (this.then.isStatement())
			{
				buffer.append('\n').append(prefix);
				this.then.toString(prefix, buffer);
			}
			else
			{
				buffer.append(' ');
				this.then.toString(prefix, buffer);
			}
		}
	}
}
