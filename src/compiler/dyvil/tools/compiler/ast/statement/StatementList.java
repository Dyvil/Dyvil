package dyvil.tools.compiler.ast.statement;

import dyvil.collection.List;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.collection.mutable.ArrayList;
import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvil.tools.compiler.ast.context.*;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.expression.access.MethodCall;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.util.Iterator;

public class StatementList implements IValue, IValueList, IDefaultContext, ILabelContext
{
	private static final TypeChecker.MarkerSupplier RETURN_MARKER_SUPPLIER = TypeChecker
		                                                                         .markerSupplier("statementlist.return",
		                                                                                         "type.expected",
		                                                                                         "return.type");
	protected SourcePosition position;

	protected IValue[] values;
	protected int      valueCount;
	protected Label[]  labels;

	// Metadata
	protected List<IVariable> variables;
	protected List<IMethod>   methods;
	protected IType           returnType;

	public StatementList()
	{
		this.values = new IValue[3];
	}

	public StatementList(SourcePosition position)
	{
		this.position = position;
		this.values = new IValue[3];
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
	public int valueTag()
	{
		return STATEMENT_LIST;
	}

	@Override
	public boolean isStatement()
	{
		return true;
	}

	@Override
	public boolean isUsableAsStatement()
	{
		return true;
	}

	@Override
	public boolean isResolved()
	{
		return this.valueCount == 0 || this.values[this.valueCount - 1].isResolved();
	}

	@Override
	public IType getType()
	{
		if (this.returnType != null)
		{
			return this.returnType;
		}
		if (this.valueCount == 0)
		{
			return this.returnType = Types.VOID;
		}
		return this.returnType = this.values[this.valueCount - 1].getType();
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.valueCount <= 0)
		{
			return Types.isVoid(type) ? this : null;
		}

		context = context.push(this);

		final IValue value = this.values[this.valueCount - 1];
		if (Types.isVoid(type) && !value.isStatement())
		{
			final IValue applyStatementCall = resolveApplyStatement(markers, context, value);
			if (applyStatementCall != null)
			{
				this.returnType = type;
				this.values[this.valueCount - 1] = applyStatementCall;
				return this;
			}
		}

		final IValue typed = TypeChecker
			                     .convertValue(value, type, typeContext, markers, context, RETURN_MARKER_SUPPLIER);

		context.pop();

		if (typed != null)
		{
			this.values[this.valueCount - 1] = typed;
			this.returnType = typed.getType();
		}

		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		if (this.valueCount > 0)
		{
			return this.values[this.valueCount - 1].isType(type);
		}
		return Types.isVoid(type);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (this.valueCount <= 0)
		{
			return MISMATCH;
		}
		return this.values[this.valueCount - 1].getTypeMatch(type, implicitContext);
	}

	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator<>(this.values, this.valueCount);
	}

	@Override
	public int size()
	{
		return this.valueCount;
	}

	@Override
	public boolean isEmpty()
	{
		return this.valueCount == 0;
	}

	@Override
	public void set(int index, IValue value)
	{
		this.values[index] = value;
	}

	@Override
	public void add(IValue value)
	{
		final int index = this.valueCount++;
		if (index >= this.values.length)
		{
			final IValue[] temp = new IValue[index + 1];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
	}

	@Override
	public void add(Name name, IValue value)
	{
		final int index = this.valueCount;
		this.add(value);

		final Label label = new Label(name, value);

		if (this.labels == null)
		{
			this.labels = new Label[index + 1];
			this.labels[index] = label;
			return;
		}
		if (index >= this.labels.length)
		{
			final Label[] temp = new Label[index + 1];
			System.arraycopy(this.labels, 0, temp, 0, this.labels.length);
			this.labels = temp;
		}
		this.labels[index] = label;
	}

	@Override
	public IValue get(int index)
	{
		return this.values[index];
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.variables == null)
		{
			return null;
		}

		// Intentionally start at the last variable
		for (int i = this.variables.size() - 1; i >= 0; i--)
		{
			final IVariable variable = this.variables.get(i);
			if (variable.getName() == name)
			{
				return variable;
			}
		}

		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		if (this.methods == null)
		{
			return;
		}

		for (IMethod method : this.methods)
		{
			method.checkMatch(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if (this.methods == null)
		{
			return;
		}

		for (IMethod method : this.methods)
		{
			method.checkImplicitMatch(list, value, targetType);
		}
	}

	@Override
	public Label resolveLabel(Name name)
	{
		if (this.labels != null)
		{
			for (Label label : this.labels)
			{
				if (label != null && name == label.name)
				{
					return label;
				}
			}
		}

		return null;
	}

	@Override
	public Label getContinueLabel()
	{
		return null;
	}

	@Override
	public Label getBreakLabel()
	{
		return null;
	}

	@Override
	public boolean isMember(IVariable variable)
	{
		return this.variables != null && this.variables.contains(variable);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		// We don't push this context because Statement Lists can't define any types (yet)

		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].resolveTypes(markers, context);
		}
	}

	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		final ILabelContext labelContext = new CombiningLabelContext(this, context);
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].resolveStatement(labelContext, markers);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.valueCount <= 0)
		{
			return this;
		}

		context = context.push(this);

		// Resolve and check all values
		final int lastIndex = this.valueCount - 1;
		for (int i = 0; i < this.valueCount; i++)
		{
			final IValue resolvedValue = this.values[i] = this.values[i].resolve(markers, context);
			final int valueTag = resolvedValue.valueTag();

			if (valueTag == IValue.VARIABLE)
			{
				this.addVariable((VariableStatement) resolvedValue, markers, context);
				continue;
			}
			if (valueTag == IValue.MEMBER_STATEMENT)
			{
				this.addMethod((MemberStatement) resolvedValue, markers);
				continue;
			}

			if (i == lastIndex)
			{
				break;
			}

			// Try to resolve an applyStatement method

			if (!resolvedValue.isStatement())
			{
				final IValue applyStatementCall = resolveApplyStatement(markers, context, resolvedValue);
				if (applyStatementCall != null)
				{
					this.values[i] = applyStatementCall;
					continue;
				}
			}

			this.values[i] = IStatement.checkStatement(markers, context, resolvedValue, "statementlist.statement");
		}

		context.pop();

		return this;
	}

	private static IValue resolveApplyStatement(MarkerList markers, IContext context, IValue resolvedValue)
	{
		final ArgumentList arguments = new ArgumentList(resolvedValue);

		final IValue implicitValue = context.resolveImplicit(null);
		if (implicitValue != null)
		{
			final IValue call = resolveApplyStatement(markers, context, arguments, implicitValue);
			if (call != null)
			{
				return call;
			}
		}

		return resolveApplyStatement(markers, context, arguments, null);
	}

	private static IValue resolveApplyStatement(MarkerList markers, IContext context, ArgumentList arguments,
		                                           IValue receiver)
	{
		final MethodCall call = new MethodCall(arguments.getFirst().getPosition(), receiver, Names.applyStatement, arguments);
		return call.resolveCall(markers, context, false);
	}

	protected void addVariable(VariableStatement initializer, MarkerList markers, IContext context)
	{
		final IVariable variable = initializer.variable;
		final Name variableName = variable.getName();

		// Additional Semantic Analysis

		// Uninitialized Variables
		if (variable.getValue() == null)
		{
			variable.setValue(variable.getType().getDefaultValue());
			markers.add(Markers.semanticError(variable.getPosition(), "variable.uninitialized", variableName));
		}

		// Variable Name Shadowing
		final IDataMember dataMember = context.resolveField(variableName);
		if (dataMember != null && dataMember.isLocal())
		{
			markers.add(Markers.semantic(initializer.getPosition(), "variable.shadow", variableName));
		}

		// Actually add the Variable to the List (this has to happen after checking for shadowed variables)

		this.addVariable(variable);
	}

	public void addVariable(IVariable variable)
	{
		if (this.variables == null)
		{
			this.variables = new ArrayList<>();
		}
		this.variables.add(variable);
	}

	protected void addMethod(MemberStatement memberStatement, MarkerList markers)
	{
		final IClassMember member = memberStatement.member;
		final MemberKind memberKind = member.getKind();
		if (memberKind != MemberKind.METHOD)
		{
			markers.add(
				Markers.semantic(member.getPosition(), "statementlist.declaration.invalid", Util.memberNamed(member)));

			return;
		}

		final IMethod method = (IMethod) member;

		if (this.methods == null)
		{
			this.methods = new ArrayList<>();
			this.methods.add(method);
			return;
		}

		final Name methodName = method.getName();
		final int parameterCount = method.getParameters().size();
		final String desc = method.getDescriptor();
		for (IMethod candidate : this.methods)
		{
			if (candidate.getName() == methodName // same name
				    && candidate.getParameters().size() == parameterCount && candidate.getDescriptor().equals(desc))
			{
				markers.add(Markers.semanticError(memberStatement.getPosition(), "method.duplicate", methodName, desc));
			}
		}

		this.methods.add(method);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].checkTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		context = context.push(this);

		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].check(markers, context);
		}

		context.pop();
	}

	@Override
	public IValue foldConstants()
	{
		if (this.valueCount == 1)
		{
			return this.values[0].foldConstants();
		}

		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].cleanup(compilableList, classCompilableList);
		}

		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].cleanup(compilableList, classCompilableList);
		}
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		int statementCount = this.valueCount - 1;
		if (statementCount < 0)
		{
			return;
		}

		dyvil.tools.asm.Label start = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label end = new dyvil.tools.asm.Label();

		writer.visitLabel(start);
		int localCount = writer.localCount();

		if (this.labels == null)
		{
			// Write all statements except the last one
			for (int i = 0; i < statementCount; i++)
			{
				this.values[i].writeExpression(writer, Types.VOID);
			}

			// Write the last expression
			this.values[statementCount].writeExpression(writer, type);
		}
		else
		{
			final int labelCount = this.labels.length;
			Label label;

			// Write all statements except the last one
			for (int i = 0; i < statementCount; i++)
			{
				if (i < labelCount && (label = this.labels[i]) != null)
				{
					writer.visitLabel(label.getTarget());
				}

				this.values[i].writeExpression(writer, Types.VOID);
			}

			// Write last expression (and label)
			if (statementCount < labelCount && (label = this.labels[statementCount]) != null)
			{
				writer.visitLabel(label.getTarget());
			}

			this.values[statementCount].writeExpression(writer, type);
		}

		writer.resetLocals(localCount);
		writer.visitLabel(end);

		if (this.variables == null)
		{
			return;
		}

		for (IVariable variable : this.variables)
		{
			variable.writeLocal(writer, start, end);
		}
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.valueCount == 0)
		{
			if (Formatting.getBoolean("statement.empty.newline"))
			{
				buffer.append('{').append('\n').append(prefix).append('}');
			}
			else if (Formatting.getBoolean("statement.empty.space_between"))
			{
				buffer.append("{ }");
			}
			else
			{
				buffer.append("{}");
			}
			return;
		}

		buffer.append('{').append('\n');

		String indentedPrefix = Formatting.getIndent("statement.indent", prefix);
		int prevLine = 0;
		Label label;

		for (int i = 0; i < this.valueCount; i++)
		{
			IValue value = this.values[i];
			SourcePosition pos = value.getPosition();
			buffer.append(indentedPrefix);

			if (pos != null)
			{
				if (pos.startLine() - prevLine > 1 && i > 0)
				{
					buffer.append('\n').append(indentedPrefix);
				}
				prevLine = pos.endLine();
			}

			if (this.labels != null && i < this.labels.length && (label = this.labels[i]) != null)
			{
				buffer.append("label ");
				buffer.append(label.name);

				if (Formatting.getBoolean("label.separator.space_before"))
				{
					buffer.append(' ');
				}
				buffer.append(':');
				if (Formatting.getBoolean("label.separator.newline_after"))
				{
					buffer.append('\n').append(indentedPrefix);
				}
				else if (Formatting.getBoolean("label.separator.newline_after"))
				{
					buffer.append(' ');
				}
			}

			value.toString(indentedPrefix, buffer);

			if (Formatting.getBoolean("statement.semicolon"))
			{
				buffer.append(';');
			}
			buffer.append('\n');
		}

		buffer.append(prefix).append('}');
	}
}
