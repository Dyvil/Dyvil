package dyvil.tools.compiler.ast.statement.foreach;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.operator.RangeOperator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.statement.ILoop;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.statement.Label;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

import static dyvil.tools.compiler.ast.statement.ForStatement.$forEnd;
import static dyvil.tools.compiler.ast.statement.ForStatement.$forStart;
import static dyvil.tools.compiler.ast.statement.ForStatement.$forUpdate;

public class ForEachStatement implements IStatement, IContext, ILoop
{
	protected transient IContext	context;
	protected IStatement			parent;
	
	public Variable					variable;
	public IValue					action;
	
	protected Label					startLabel;
	protected Label					updateLabel;
	protected Label					endLabel;
	
	public ForEachStatement(Variable var, IValue action)
	{
		this.startLabel = new Label($forStart);
		this.updateLabel = new Label($forUpdate);
		this.endLabel = new Label($forEnd);
		
		this.variable = var;
		this.action = action;
	}
	
	@Override
	public int valueTag()
	{
		return FOR;
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
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IDyvilHeader getHeader()
	{
		return this.context.getHeader();
	}
	
	@Override
	public IClass getThisClass()
	{
		return this.context.getThisClass();
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.context.resolveTypeVariable(name);
	}
	
	@Override
	public IField resolveField(Name name)
	{
		if (this.variable.name == name)
		{
			return this.variable;
		}
		
		return this.context.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return this.context.handleException(type);
	}
	
	@Override
	public byte getVisibility(IMember member)
	{
		return this.context.getVisibility(member);
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
		
		return this.parent == null ? null : this.parent.resolveLabel(name);
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
			if (this.action.isStatement())
			{
				((IStatement) this.action).setParent(this);
			}
			
			this.action.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.context = context;
		
		IType varType = this.variable.type;
		IValue value = this.variable.value;
		this.variable.value = value = value.resolve(markers, context);
		
		if (value.valueTag() == IValue.RANGE_OPERATOR)
		{
			RangeOperator ro = (RangeOperator) value;
			IValue value1 = ro.value1;
			IValue value2 = ro.value2;
			IType rangeType = ro.getElementType();
			
			if (varType == Types.UNKNOWN)
			{
				if (rangeType == Types.UNKNOWN)
				{
					rangeType = Types.findCommonSuperType(value1.getType(), value2.getType());
				}
				
				this.variable.type = varType = rangeType;
				if (varType == Types.UNKNOWN)
				{
					markers.add(this.variable.position, "for.variable.infer", this.variable.name);
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
			
			return new RangeForStatement(this.variable, value1, value2, this.action == null ? null : this.action.resolve(markers, this));
		}
		
		IType valueType = value.getType();
		if (valueType.isArrayType())
		{
			if (varType == Types.UNKNOWN)
			{
				this.variable.type = varType = valueType.getElementType();
				if (varType == Types.UNKNOWN)
				{
					markers.add(this.variable.getPosition(), "for.variable.infer", this.variable.name);
				}
			}
			else if (!varType.classEquals(valueType.getElementType()))
			{
				Marker marker = markers.create(value.getPosition(), "for.array.type");
				marker.addInfo("Array Type: " + valueType);
				marker.addInfo("Variable Type: " + varType);
			}
			
			return new ArrayForStatement(this.variable, this.action == null ? null : this.action.resolve(markers, this), valueType);
		}
		if (Types.ITERABLE.isSuperTypeOf(valueType))
		{
			IType iterableType = valueType.resolveType(IterableForStatement.ITERABLE_TYPE);
			if (varType == Types.UNKNOWN)
			{
				this.variable.type = varType = iterableType;
				if (varType == Types.UNKNOWN)
				{
					markers.add(this.variable.position, "for.variable.infer", this.variable.name);
				}
			}
			else if (!varType.isSuperTypeOf(iterableType))
			{
				Marker m = markers.create(value.getPosition(), "for.iterable.type");
				m.addInfo("Iterable Type: " + iterableType);
				m.addInfo("Variable Type: " + varType);
			}
			
			return new IterableForStatement(this.variable, this.action == null ? null : this.action.resolve(markers, this), valueType, iterableType);
		}
		if (Types.STRING.isSuperTypeOf(valueType))
		{
			if (varType == Types.UNKNOWN)
			{
				this.variable.type = varType = Types.CHAR;
			}
			else if (!varType.classEquals(Types.CHAR))
			{
				Marker marker = markers.create(value.getPosition(), "for.string.type");
				marker.addInfo("Variable Type: " + varType);
			}
			
			return new StringForStatement(this.variable, this.action == null ? null : this.action.resolve(markers, this));
		}
		
		Marker m = markers.create(this.variable.position, "for.invalid");
		m.addInfo("Variable Type: " + varType);
		m.addInfo("Value Type: " + valueType);
		
		if (this.action != null)
		{
			this.action = this.action.resolve(markers, this);
		}
		
		this.context = null;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.variable != null)
		{
			this.variable.value.checkTypes(markers, context);
		}
		this.context = context;
		if (this.action != null)
		{
			this.action.checkTypes(markers, this);
		}
		this.context = null;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.context = context;
		
		if (this.variable != null)
		{
			this.variable.value.check(markers, context);
		}
		if (this.action != null)
		{
			this.action.check(markers, this);
		}
		
		this.context = null;
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
		this.variable.type.toString(prefix, buffer);
		buffer.append(' ').append(this.variable.name).append(Formatting.Statements.forEachSeperator);
		this.variable.value.toString(prefix, buffer);
		buffer.append(Formatting.Statements.forEnd);
		
		if (this.action != null)
		{
			this.action.toString(prefix, buffer);
		}
	}
}
