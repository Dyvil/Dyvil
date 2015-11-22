package dyvil.tools.compiler.ast.statement;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.tools.compiler.ast.context.CombiningContext;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.context.ILabelContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.statement.control.Label;
import dyvil.tools.compiler.ast.statement.loop.ILoop;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.util.Iterator;

public class StatementList implements IValue, IValueList, IDefaultContext, ILabelContext
{
	protected ICodePosition position;
	
	protected IValue[] values = new IValue[3];
	protected int     valueCount;
	protected Label[] labels;
	
	// Metadata
	private   Map<Name, Variable> variables;
	protected IType               returnType;
	
	public StatementList()
	{
	}
	
	public StatementList(ICodePosition position)
	{
		this.position = position;
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
	
	@Override
	public void expandPosition(ICodePosition position)
	{
		this.position = this.position.to(position);
	}
	
	@Override
	public int valueTag()
	{
		return STATEMENT_LIST;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.returnType != null && this.returnType.isPrimitive();
	}
	
	@Override
	public boolean isResolved()
	{
		return this.returnType != null && this.returnType.isResolved();
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
		if (this.valueCount > 0)
		{
			IValue typed = this.values[this.valueCount - 1]
					.withType(type, typeContext, markers, new CombiningContext(this, context));
			if (typed != null)
			{
				this.values[this.valueCount - 1] = typed;
				this.returnType = typed.getType();
				return this;
			}
		}
		
		return type == Types.VOID ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (this.valueCount > 0)
		{
			return this.values[this.valueCount - 1].isType(type);
		}
		return type == Types.VOID;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (this.valueCount > 0)
		{
			return this.values[this.valueCount - 1].getTypeMatch(type);
		}
		return 0;
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator<>(this.values, this.valueCount);
	}
	
	@Override
	public int valueCount()
	{
		return this.valueCount;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.valueCount == 0;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.values[index] = value;
	}
	
	@Override
	public void addValue(IValue value)
	{
		int index = this.valueCount++;
		if (index >= this.values.length)
		{
			IValue[] temp = new IValue[this.valueCount];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
	}
	
	@Override
	public void addValue(IValue value, Label label)
	{
		int index = this.valueCount++;
		if (this.valueCount > this.values.length)
		{
			IValue[] temp = new IValue[this.valueCount];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
		
		if (this.labels == null)
		{
			this.labels = new Label[index + 1];
			this.labels[index] = label;
			return;
		}
		if (index >= this.labels.length)
		{
			Label[] temp = new Label[index + 1];
			System.arraycopy(this.labels, 0, temp, 0, this.labels.length);
			this.labels = temp;
		}
		this.labels[index] = label;
	}
	
	@Override
	public void addValue(int index, IValue value)
	{
		IValue[] temp = new IValue[++this.valueCount];
		System.arraycopy(this.values, 0, temp, 0, index);
		temp[index] = value;
		System.arraycopy(this.values, index, temp, index + 1, this.valueCount - index - 1);
		this.values = temp;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.values[index];
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.variables != null)
		{
			IDataMember field = this.variables.get(name);
			if (field != null)
			{
				return field;
			}
		}
		
		return null;
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
	public ILoop getEnclosingLoop()
	{
		return null;
	}
	
	@Override
	public boolean isMember(IVariable variable)
	{
		return this.variables.containsValue(variable);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolveStatement(ILabelContext context, MarkerList markers)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].resolveStatement(context, markers);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.valueCount <= 0)
		{
			return this;
		}
		
		IContext combinedContext = new CombiningContext(this, context);

		// Resolve and check all values except the last one
		int len = this.valueCount - 1;
		for (int i = 0; i < len; i++)
		{
			IValue resolved = this.values[i] = this.values[i].resolve(markers, combinedContext);
			
			if (resolved.valueTag() == IValue.VARIABLE)
			{
				this.addVariable(resolved);
			}
			
			IValue typed = resolved.withType(Types.VOID, Types.VOID, markers, combinedContext);
			if (typed == null)
			{
				Marker marker = I18n.createMarker(resolved.getPosition(), "statementlist.statement");
				marker.addInfo(I18n.getString("return.type", resolved.getType()));
				markers.add(marker);
			}
			else
			{
				this.values[i] = typed;
			}
		}

		// Resolved the last value
		this.values[len] = this.values[len].resolve(markers, combinedContext);
		
		return this;
	}
	
	protected void addVariable(IValue value)
	{
		if (this.variables == null)
		{
			this.variables = new IdentityHashMap<>();
		}
		
		Variable var = ((FieldInitializer) value).variable;
		this.variables.put(var.getName(), var);
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IContext combinedContext = this.variables == null ? context : new CombiningContext(this, context);
		
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].checkTypes(markers, combinedContext);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		IContext context1 = this.variables == null ? context : new CombiningContext(this, context);
		
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].check(markers, context1);
		}
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].cleanup(context, compilableList);
		}
		
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].cleanup(context, compilableList);
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		int statementCount = this.valueCount - 1;
		if (statementCount < 0)
		{
			return;
		}
		
		dyvil.tools.asm.Label start = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label end = new dyvil.tools.asm.Label();
		
		writer.writeLabel(start);
		int localCount = writer.localCount();
		
		if (this.labels == null)
		{
			// Write all statements except the last one
			for (int i = 0; i < statementCount; i++)
			{
				this.values[i].writeStatement(writer);
			}

			// Write the last expression
			this.values[statementCount].writeExpression(writer, this.returnType);
		}
		else
		{
			// Write all statements except the last one
			for (int i = 0; i < statementCount; i++)
			{
				Label label = this.labels[i];
				if (label != null)
				{
					writer.writeLabel(label.target);
				}
				
				this.values[i].writeStatement(writer);
			}

			// Write last expression (and label)
			Label label = this.labels[statementCount];
			if (label != null)
			{
				writer.writeLabel(label.target);
			}
			
			this.values[statementCount].writeExpression(writer);
		}
		
		writer.resetLocals(localCount);
		writer.writeLabel(end);
		
		if (this.variables == null)
		{
			return;
		}
		
		for (Entry<Name, Variable> entry : this.variables)
		{
			Variable var = entry.getValue();
			var.writeLocal(writer, start, end);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label start = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label end = new dyvil.tools.asm.Label();
		
		writer.writeLabel(start);
		int localCount = writer.localCount();
		
		if (this.labels == null)
		{
			for (int i = 0; i < this.valueCount; i++)
			{
				this.values[i].writeStatement(writer);
			}
		}
		else
		{
			for (int i = 0; i < this.valueCount; i++)
			{
				Label label = this.labels[i];
				if (label != null)
				{
					writer.writeLabel(label.target);
				}
				
				this.values[i].writeStatement(writer);
			}
		}
		
		writer.resetLocals(localCount);
		writer.writeLabel(end);
		
		if (this.variables == null)
		{
			return;
		}
		
		for (Entry<Name, Variable> entry : this.variables)
		{
			entry.getValue().writeLocal(writer, start, end);
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
		Label label = null;
		
		for (int i = 0; i < this.valueCount; i++)
		{
			IValue value = this.values[i];
			ICodePosition pos = value.getPosition();
			buffer.append(indentedPrefix);

			if (pos != null)
			{
				if (pos.startLine() - prevLine > 1 && i > 0)
				{
					buffer.append('\n').append(indentedPrefix);
				}
				prevLine = pos.endLine();
			}
			
			if (this.labels != null && (label = this.labels[i]) != null)
			{
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
