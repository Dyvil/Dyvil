package dyvil.tools.compiler.ast.statement;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ForStatement extends ASTNode implements IStatement, IContext, ILoop
{
	private IContext	context;
	private IStatement	parent;
	
	public Variable		variable;
	
	public IValue		condition;
	public IValue		update;
	
	public boolean		isForeach;
	
	public IValue		then;
	
	protected Label		startLabel;
	protected Label		updateLabel;
	protected Label		endLabel;
	
	public ForStatement(ICodePosition position)
	{
		this.position = position;
		
		this.startLabel = new Label();
		this.updateLabel = new Label();
		this.endLabel = new Label();
	}
	
	@Override
	public int getValueType()
	{
		return FOR;
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
	
	@Override
	public void setType(IType type)
	{
		this.variable = new Variable(type.getPosition());
		this.variable.type = type;
	}
	
	@Override
	public Label getContinueLabel()
	{
		return this.updateLabel;
	}
	
	@Override
	public Label getBreakLabel()
	{
		return this.endLabel;
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
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.resolveTypes(markers, context);
		}
		if (this.condition != null)
		{
			this.condition.resolveTypes(markers, context);
		}
		if (this.update != null)
		{
			this.update.resolveTypes(markers, context);
		}
		
		if (this.then != null)
		{
			this.then.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		this.context = context;
		
		if (this.variable != null)
		{
			this.variable.index = context.getVariableCount();
			this.variable.resolve(markers, context);
		}
		if (this.condition != null)
		{
			this.condition = this.condition.resolve(markers, this);
		}
		if (this.update != null)
		{
			this.update = this.update.resolve(markers, this);
		}
		
		if (this.then != null)
		{
			this.then = this.then.resolve(markers, this);
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		this.context = context;
		
		if (this.variable != null)
		{
			this.variable.check(markers, context);
		}
		if (this.condition != null)
		{
			this.condition.check(markers, this);
			
			if (!this.condition.requireType(Type.BOOLEAN))
			{
				Marker marker = Markers.create(this.condition.getPosition(), "for.condition.type");
				marker.addInfo("Condition Type: " + this.condition.getType());
				markers.add(marker);
			}
		}
		if (this.update != null)
		{
			this.update.check(markers, this);
		}
		
		if (this.then != null)
		{
			this.then.check(markers, this);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IType getThisType()
	{
		return this.context.getThisType();
	}
	
	@Override
	public int getVariableCount()
	{
		return this.variable != null ? 1 : 0;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		if (this.variable.isName(name))
		{
			return new FieldMatch(this.variable, 1);
		}
		
		return this.context.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(ITyped instance, String name, List<? extends ITyped> arguments)
	{
		return this.context.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, ITyped instance, String name, List<? extends ITyped> arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.context.getAccessibility(member);
	}
	
	@Override
	public Label resolveLabel(String name)
	{
		if ("$forStart".equals(name))
		{
			return this.startLabel;
		}
		else if ("$forUpdate".equals(name))
		{
			return this.updateLabel;
		}
		else if ("$forEnd".equals(name))
		{
			return this.endLabel;
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
		Variable var = this.variable;
		if (var != null)
		{
			writer.addLocal(var.type);
			var.value.writeExpression(writer);
			var.writeSet(writer);
		}
		
		writer.visitLabel(this.startLabel);
		if (this.condition != null)
		{
			this.condition.writeJump(writer, this.endLabel);
		}
		if (this.then != null)
		{
			this.then.writeStatement(writer);
		}
		writer.visitLabel(this.updateLabel);
		if (this.update != null)
		{
			this.update.writeStatement(writer);
		}
		writer.visitJumpInsn(Opcodes.GOTO, this.startLabel);
		writer.visitLabel(this.endLabel);
		
		if (var != null)
		{
			writer.visitLocalVariable(var.qualifiedName, var.type.getExtendedName(), var.type.getSignature(), this.startLabel, this.endLabel, var.index);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.forStart);
		if (this.isForeach)
		{
			this.variable.type.toString(prefix, buffer);
			buffer.append(' ').append(this.variable.name).append(Formatting.Field.keyValueSeperator).append(' ');
			this.variable.value.toString(prefix, buffer);
		}
		else
		{
			if (this.variable != null)
			{
				this.variable.toString(prefix, buffer);
			}
			buffer.append(';');
			if (this.condition != null)
			{
				buffer.append(' ');
				this.condition.toString(prefix, buffer);
			}
			buffer.append(';');
			if (this.update != null)
			{
				buffer.append(' ');
				this.update.toString(prefix, buffer);
			}
		}
		buffer.append(Formatting.Statements.forEnd);
		
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
