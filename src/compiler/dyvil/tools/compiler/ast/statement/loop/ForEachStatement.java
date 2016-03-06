package dyvil.tools.compiler.ast.statement.loop;

import dyvil.tools.compiler.ast.context.CombiningLabelContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.operator.RangeOperator;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import static dyvil.tools.compiler.ast.statement.loop.ForStatement.*;

public class ForEachStatement implements IStatement, IDefaultContext, ILoop
{
	protected ICodePosition position;

	protected Variable variable;
	protected IValue   action;

	// Metadata
	protected Label startLabel;
	protected Label updateLabel;
	protected Label endLabel;

	public ForEachStatement(ICodePosition position, Variable var)
	{
		this.position = position;
		this.variable = var;

		this.startLabel = new Label($forStart);
		this.updateLabel = new Label($forUpdate);
		this.endLabel = new Label($forEnd);
	}

	public ForEachStatement(ICodePosition position, Variable var, IValue action)
	{
		this(position, var);
		this.action = action;
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
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	public void setVariable(Variable variable)
	{
		this.variable = variable;
	}

	public Variable getVariable()
	{
		return this.variable;
	}

	@Override
	public void setAction(IValue action)
	{
		this.action = action;
	}

	@Override
	public IValue getAction()
	{
		return this.action;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type != Types.VOID)
		{
			return null;
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

	private IValue resolveValue(MarkerList markers, IContext context)
	{
		IValue value = this.variable.getValue().resolve(markers, context);

		IType valueType = value.getType();

		value = TypeChecker.convertValue(value, valueType, valueType, markers, context,
		                                 TypeChecker.markerSupplier("for.each.type"));

		this.variable.setValue(value);
		return value;
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		IType varType = this.variable.getType();
		final IValue value = this.resolveValue(markers, context);
		final IType valueType = value.getType();

		if (value.valueTag() == IValue.RANGE_OPERATOR)
		{
			final RangeOperator rangeOperator = (RangeOperator) value;

			if (varType == Types.UNKNOWN)
			{
				this.variable.setType(varType = rangeOperator.getElementType());
				if (varType == Types.UNKNOWN)
				{
					markers.add(Markers.semantic(this.variable.getPosition(), "for.variable.infer", this.variable.getName()));
				}
			}
			else if (!varType.isSuperTypeOf(rangeOperator.getElementType()))
			{
				final Marker marker = Markers.semantic(value.getPosition(), "for.range.type");
				marker.addInfo(Markers.getSemantic("range.type", valueType));
				marker.addInfo(Markers.getSemantic("variable.type", varType));
				markers.add(marker);
			}

			final RangeForStatement rangeForStatement = new RangeForStatement(this.position, this.variable);
			rangeForStatement.resolveAction(this.action, markers, context);
			return rangeForStatement;
		}
		if (valueType.isArrayType())
		{
			if (varType == Types.UNKNOWN)
			{
				this.variable.setType(varType = valueType.getElementType());
				if (varType == Types.UNKNOWN)
				{
					markers.add(Markers.semantic(this.variable.getPosition(), "for.variable.infer", this.variable.getName()));
				}
			}
			else if (!varType.isSuperTypeOf(valueType.getElementType()))
			{
				final Marker marker = Markers.semantic(value.getPosition(), "for.array.type");
				marker.addInfo(Markers.getSemantic("array.type", valueType));
				marker.addInfo(Markers.getSemantic("variable.type", varType));
				markers.add(marker);
			}

			final ArrayForStatement arrayForStatement = new ArrayForStatement(this.position, this.variable, valueType);
			arrayForStatement.resolveAction(this.action, markers, context);
			return arrayForStatement;
		}
		if (Types.ITERABLE.isSuperTypeOf(valueType))
		{
			final IType iterableType = valueType.resolveTypeSafely(IterableForStatement.ITERABLE_TYPE).getReturnType();
			if (varType == Types.UNKNOWN)
			{
				this.variable.setType(varType = iterableType);
				if (varType == Types.UNKNOWN)
				{
					markers.add(
						Markers.semantic(this.variable.getPosition(), "for.variable.infer", this.variable.getName()));
				}
			}
			else if (!varType.isSuperTypeOf(iterableType))
			{
				final Marker marker = Markers.semantic(value.getPosition(), "for.iterable.type");
				marker.addInfo(Markers.getSemantic("iterable.type", iterableType));
				marker.addInfo(Markers.getSemantic("variable.type", varType));
				markers.add(marker);
			}

			final IterableForStatement iterableForStatement = new IterableForStatement(this.position, this.variable);
			iterableForStatement.resolveAction(this.action, markers, context);
			return iterableForStatement;
		}
		if (Types.STRING.isSuperTypeOf(valueType))
		{
			if (varType == Types.UNKNOWN)
			{
				this.variable.setType(Types.CHAR);
			}
			else if (!varType.isSuperTypeOf(Types.CHAR))
			{
				final Marker marker = Markers.semantic(value.getPosition(), "for.string.type");
				marker.addInfo(Markers.getSemantic("variable.type", varType));
				markers.add(marker);
			}

			final StringForStatement stringForStatement = new StringForStatement(this.position, this.variable);
			stringForStatement.resolveAction(this.action, markers, context);
			return stringForStatement;
		}

		final Marker marker = Markers.semantic(this.variable.getPosition(), "for.each.invalid");
		marker.addInfo(Markers.getSemantic("variable.type", varType));
		marker.addInfo(Markers.getSemantic("value.type", valueType));
		markers.add(marker);

		this.resolveAction(this.action, markers, context);

		return this;
	}

	protected void resolveAction(IValue action, MarkerList markers, IContext context)
	{
		if (action == null)
		{
			return;
		}

		context = context.push(this);

		this.action = action.resolve(markers, context);

		final IValue typedAction = this.action.withType(Types.VOID, Types.VOID, markers, context);
		if (typedAction != null && typedAction.isUsableAsStatement())
		{
			this.action = typedAction;
		}
		else if (this.action.isResolved())
		{
			final Marker marker = Markers.semanticError(this.action.getPosition(), "for.action.type");
			marker.addInfo(Markers.getSemantic("action.type", this.action.getType()));
			markers.add(marker);
		}

		context.pop();
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
			context = context.push(this);
			this.action.checkTypes(markers, context);
			context.pop();
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
			context = context.push(this);
			this.action.check(markers, context);
			context.pop();
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
		context = context.push(this);

		this.variable.cleanup(context, compilableList);
		if (this.action != null)
		{
			this.action = this.action.cleanup(context, compilableList);
		}

		context.pop();
		return this;
	}

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		throw new BytecodeException("Cannot compile invalid ForEach statement");
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("for");
		Formatting.appendSeparator(buffer, "for.open_paren", '(');

		this.variable.getType().toString(prefix, buffer);
		buffer.append(' ').append(this.variable.getName());

		Formatting.appendSeparator(buffer, "for.each.separator", ':');

		this.variable.getValue().toString(prefix, buffer);

		if (Formatting.getBoolean("for.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');

		if (this.action != null && !Util.formatStatementList(prefix, buffer, this.action))
		{
			String actionPrefix = Formatting.getIndent("for.indent", prefix);
			if (Formatting.getBoolean("for.close_paren.newline_after"))
			{
				buffer.append('\n').append(actionPrefix);
			}
			else if (Formatting.getBoolean("for.close_paren.space_after"))
			{
				buffer.append(' ');
			}

			this.action.toString(actionPrefix, buffer);
		}
	}
}
