package dyvil.tools.compiler.ast.statement.foreach;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.*;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.RangeOperator;
import dyvil.tools.compiler.ast.statement.ILoop;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import static dyvil.tools.compiler.ast.statement.ForStatement.$forEnd;
import static dyvil.tools.compiler.ast.statement.ForStatement.$forStart;
import static dyvil.tools.compiler.ast.statement.ForStatement.$forUpdate;

public class ForEachStatement implements IStatement, IDefaultContext, ILoop
{
	protected ICodePosition position;
	
	protected Variable	variable;
	protected IValue	action;
	
	// Metadata
	protected Label	startLabel;
	protected Label	updateLabel;
	protected Label	endLabel;
	
	public ForEachStatement(ICodePosition position, Variable var, IValue action)
	{
		this.position = position;
		this.variable = var;
		this.action = action;
		
		this.startLabel = new Label($forStart);
		this.updateLabel = new Label($forUpdate);
		this.endLabel = new Label($forEnd);
	}
	
	@Override
	public int valueTag()
	{
		return FOR;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type != Types.VOID)
		{
			return null;
		}
		
		if (this.action != null)
		{
			IValue action1 = this.action.withType(Types.VOID, typeContext, markers, context);
			if (action1 == null)
			{
				// TODO Handle error
			}
			else
			{
				this.action = action1;
			}
		}
		
		return this;
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
	public IDataMember resolveField(Name name)
	{
		if (this.variable.getName() == name)
		{
			return this.variable;
		}
		
		return null;
	}
	
	@Override
	public Label resolveLabel(Name name)
	{
		if (name == $forStart)
		{
			return this.startLabel;
		}
		if (name == $forUpdate)
		{
			return this.updateLabel;
		}
		if (name == $forEnd)
		{
			return this.endLabel;
		}
		
		return null;
	}
	
	@Override
	public ILoop getEnclosingLoop()
	{
		return this;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.resolveTypes(markers, context);
		}
		
		if (this.action != null)
		{
			this.action.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		if (this.action != null)
		{
			this.action.resolveStatement(new CombiningLabelContext(this, context), markers);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		IType varType = this.variable.getType();
		IValue value = this.variable.getValue().resolve(markers, context);
		this.variable.setValue(value);
		
		if (value.valueTag() == IValue.RANGE_OPERATOR)
		{
			RangeOperator ro = (RangeOperator) value;
			IValue value1 = ro.getFirstValue();
			IValue value2 = ro.getLastValue();
			IType rangeType = ro.getElementType();
			
			if (varType == Types.UNKNOWN)
			{
				if (rangeType == Types.UNKNOWN)
				{
					rangeType = Types.combine(value1.getType(), value2.getType());
				}
				
				this.variable.setType(varType = rangeType);
				if (varType == Types.UNKNOWN)
				{
					markers.add(this.variable.getPosition(), "for.variable.infer", this.variable.getName());
				}
			}
			else if (rangeType == Types.UNKNOWN)
			{
				rangeType = varType;
			}
			else if (!varType.isSuperTypeOf(rangeType))
			{
				Marker marker = markers.create(value1.getPosition(), "for.range.type");
				marker.addInfo("Range Type: " + rangeType);
				marker.addInfo("Variable Type: " + varType);
			}
			
			return new RangeForStatement(this.position, this.variable, value1, value2, this.resolveAction(markers, context));
		}
		
		IType valueType = value.getType();
		if (valueType.isArrayType())
		{
			if (varType == Types.UNKNOWN)
			{
				this.variable.setType(varType = valueType.getElementType());
				if (varType == Types.UNKNOWN)
				{
					markers.add(this.variable.getPosition(), "for.variable.infer", this.variable.getName());
				}
			}
			else if (!varType.classEquals(valueType.getElementType()))
			{
				Marker marker = markers.create(value.getPosition(), "for.array.type");
				marker.addInfo("Array Type: " + valueType);
				marker.addInfo("Variable Type: " + varType);
			}
			
			return new ArrayForStatement(this.position, this.variable, this.resolveAction(markers, context), valueType);
		}
		if (Types.ITERABLE.isSuperTypeOf(valueType))
		{
			IType iterableType = valueType.resolveType(IterableForStatement.ITERABLE_TYPE);
			if (varType == Types.UNKNOWN)
			{
				this.variable.setType(varType = iterableType);
				if (varType == Types.UNKNOWN)
				{
					markers.add(this.variable.getPosition(), "for.variable.infer", this.variable.getName());
				}
			}
			else if (!varType.isSuperTypeOf(iterableType))
			{
				Marker m = markers.create(value.getPosition(), "for.iterable.type");
				m.addInfo("Iterable Type: " + iterableType);
				m.addInfo("Variable Type: " + varType);
			}
			
			return new IterableForStatement(this.position, this.variable, this.resolveAction(markers, context), valueType, iterableType);
		}
		if (Types.STRING.isSuperTypeOf(valueType))
		{
			if (varType == Types.UNKNOWN)
			{
				this.variable.setType(varType = Types.CHAR);
			}
			else if (!varType.classEquals(Types.CHAR))
			{
				Marker marker = markers.create(value.getPosition(), "for.string.type");
				marker.addInfo("Variable Type: " + varType);
			}
			
			return new StringForStatement(this.position, this.variable, this.resolveAction(markers, context));
		}
		
		Marker m = markers.create(this.variable.getPosition(), "for.invalid");
		m.addInfo("Variable Type: " + varType);
		m.addInfo("Value Type: " + valueType);
		
		this.action = this.resolveAction(markers, context);
		
		return this;
	}
	
	private IValue resolveAction(MarkerList markers, IContext context)
	{
		return this.action == null ? this.action.resolve(markers, new CombiningContext(this, context)) : null;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.getValue().checkTypes(markers, context);
		}
		if (this.action != null)
		{
			this.action.checkTypes(markers, this);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.getValue().check(markers, context);
		}
		if (this.action != null)
		{
			this.action.check(markers, new CombiningContext(this, context));
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.variable.foldConstants();
		if (this.action != null)
		{
			this.action = this.action.foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.variable.cleanup(this, compilableList);
		if (this.action != null)
		{
			this.action = this.action.cleanup(new CombiningContext(this, context), compilableList);
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.writeStatement(writer);
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		throw new BytecodeException("Cannot compile invalid ForEach statement");
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Statements.forStart);
		this.variable.getType().toString(prefix, buffer);
		buffer.append(' ').append(this.variable.getName()).append(Formatting.Statements.forEachSeperator);
		this.variable.getValue().toString(prefix, buffer);
		buffer.append(Formatting.Statements.forEnd);
		
		if (this.action != null)
		{
			this.action.toString(prefix, buffer);
		}
	}
}
