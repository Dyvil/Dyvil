package dyvilx.tools.compiler.ast.statement.loop;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Formattable;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.CombiningLabelContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.context.ILabelContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.MethodCall;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.statement.IStatement;
import dyvilx.tools.compiler.ast.statement.control.Label;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import static dyvilx.tools.compiler.ast.statement.loop.ForStatement.*;

public class ForEachStatement implements IForStatement, IDefaultContext
{
	protected SourcePosition position;

	protected IVariable variable;
	protected IValue    action;

	// Metadata
	protected Label startLabel;
	protected Label updateLabel;
	protected Label endLabel;

	public ForEachStatement(SourcePosition position, IVariable var)
	{
		this.position = position;
		this.variable = var;

		this.startLabel = new Label($forStart);
		this.updateLabel = new Label($forUpdate);
		this.endLabel = new Label($forEnd);
	}

	public ForEachStatement(SourcePosition position, IVariable var, IValue action)
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
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public IVariable getVariable()
	{
		return this.variable;
	}

	@Override
	public void setVariable(IVariable variable)
	{
		this.variable = variable;
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
	public boolean isMember(IVariable variable)
	{
		return variable == this.variable;
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

		if (value.valueTag() == IValue.METHOD_CALL)
		{
			final MethodCall rangeOperator = (MethodCall) value;
			if (RangeForStatement.isRangeOperator(rangeOperator))
			{
				final IType elementType = RangeForStatement.getElementType(rangeOperator);

				if (varType == Types.UNKNOWN)
				{
					this.inferVariableType(markers, elementType);
				}
				else if (!Types.isAssignable(varType, elementType))
				{
					final Marker marker = Markers.semanticError(value.getPosition(), "for.range.type");
					marker.addInfo(Markers.getSemantic("range.type", valueType));
					marker.addInfo(Markers.getSemantic("variable.type", varType));
					markers.add(marker);
				}

				final RangeForStatement rangeForStatement = new RangeForStatement(this.position, this.variable,
				                                                                  elementType);
				rangeForStatement.resolveAction(this.action, markers, context);
				return rangeForStatement;
			}
		}
		final ArrayType arrayType = valueType.extract(ArrayType.class);
		if (arrayType != null)
		{
			if (varType == Types.UNKNOWN)
			{
				this.inferVariableType(markers, arrayType.getElementType());
			}
			else if (!Types.isAssignable(varType, arrayType.getElementType()))
			{
				final Marker marker = Markers.semanticError(value.getPosition(), "for.array.type");
				marker.addInfo(Markers.getSemantic("array.type", valueType));
				marker.addInfo(Markers.getSemantic("variable.type", varType));
				markers.add(marker);
			}

			final ArrayForStatement arrayForStatement = new ArrayForStatement(this.position, this.variable, arrayType);
			arrayForStatement.resolveAction(this.action, markers, context);
			return arrayForStatement;
		}
		if (Types.isAssignable(IterableForStatement.LazyFields.ITERATOR, valueType))
		{
			return this.toIteratorLoop(markers, context, varType, value, valueType);
		}
		if (Types.isAssignable(IterableForStatement.LazyFields.ITERABLE, valueType))
		{
			return this.toIterable(markers, context, varType, value, valueType);
		}
		if (Types.isAssignable(Types.STRING, valueType))
		{
			return this.toStringLoop(markers, context, varType, value);
		}

		final Marker marker = Markers.semanticError(this.variable.getPosition(), "for.each.invalid");
		marker.addInfo(Markers.getSemantic("value.type", valueType));
		markers.add(marker);

		this.resolveAction(this.action, markers, context);

		return this;
	}

	@NonNull
	public IValue toIteratorLoop(MarkerList markers, IContext context, IType varType, IValue value, IType valueType)
	{
		final IType iteratorType = Types.resolveTypeSafely(valueType, IterableForStatement.LazyFields.ITERATOR_TYPE);
		if (varType == Types.UNKNOWN)
		{
			this.inferVariableType(markers, iteratorType);
		}
		else if (!Types.isAssignable(varType, iteratorType))
		{
			final Marker marker = Markers.semanticError(value.getPosition(), "for.iterator.type");
			marker.addInfo(Markers.getSemantic("iterator.type", iteratorType));
			marker.addInfo(Markers.getSemantic("variable.type", varType));
			markers.add(marker);
		}

		this.variable.setValue(
			TypeChecker.convertValue(value, IterableForStatement.LazyFields.ITERATOR, null, markers, context, null));
		final IterableForStatement iterableForStatement = new IterableForStatement(this.position, this.variable, true);
		iterableForStatement.resolveAction(this.action, markers, context);
		return iterableForStatement;
	}

	@NonNull
	public IValue toStringLoop(MarkerList markers, IContext context, IType varType, IValue value)
	{
		if (varType == Types.UNKNOWN)
		{
			this.variable.setType(Types.CHAR);
		}
		else if (!Types.isAssignable(varType, Types.CHAR))
		{
			final Marker marker = Markers.semanticError(value.getPosition(), "for.string.type");
			marker.addInfo(Markers.getSemantic("variable.type", varType));
			markers.add(marker);
		}

		this.variable.setValue(TypeChecker.convertValue(value, Types.STRING, null, markers, context, null));
		final StringForStatement stringForStatement = new StringForStatement(this.position, this.variable);
		stringForStatement.resolveAction(this.action, markers, context);
		return stringForStatement;
	}

	@NonNull
	public IValue toIterable(MarkerList markers, IContext context, IType varType, IValue value, IType valueType)
	{
		final IType iterableType = Types.resolveTypeSafely(valueType, IterableForStatement.LazyFields.ITERABLE_TYPE);
		if (varType == Types.UNKNOWN)
		{
			this.inferVariableType(markers, iterableType);
		}
		else if (!Types.isAssignable(varType, iterableType))
		{
			final Marker marker = Markers.semanticError(value.getPosition(), "for.iterable.type");
			marker.addInfo(Markers.getSemantic("iterable.type", iterableType));
			marker.addInfo(Markers.getSemantic("variable.type", varType));
			markers.add(marker);
		}

		this.variable.setValue(
			TypeChecker.convertValue(value, IterableForStatement.LazyFields.ITERABLE, null, markers, context, null));
		final IterableForStatement iterableForStatement = new IterableForStatement(this.position, this.variable, false);
		iterableForStatement.resolveAction(this.action, markers, context);
		return iterableForStatement;
	}

	public void inferVariableType(MarkerList markers, IType type)
	{
		if (type != Types.UNKNOWN)
		{
			this.variable.setType(type);
			return;
		}
		markers.add(Markers.semantic(this.variable.getPosition(), "for.variable.infer", this.variable.getName()));
	}

	protected void resolveAction(IValue action, MarkerList markers, IContext context)
	{
		if (action == null)
		{
			return;
		}

		context = context.push(this);

		this.action = action.resolve(markers, context);
		this.action = IStatement.checkStatement(markers, context, this.action, "for.action.type");

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
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.variable.cleanup(compilableList, classCompilableList);
		if (this.action != null)
		{
			this.action = this.action.cleanup(compilableList, classCompilableList);
		}

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
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append("for");
		Formatting.appendSeparator(buffer, "for.open_paren", '(');

		buffer.append(this.variable.getName());
		Formatting.appendSeparator(buffer, "field.type_ascription", ':');
		this.variable.getType().toString(indent, buffer);

		Formatting.appendSeparator(buffer, "for.each.separator", "<-");

		this.variable.getValue().toString(indent, buffer);

		if (Formatting.getBoolean("for.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');

		if (this.action != null && !Util.formatStatementList(indent, buffer, this.action))
		{
			String actionPrefix = Formatting.getIndent("for.indent", indent);
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
