package dyvil.tools.compiler.ast.statement;

import java.util.*;
import java.util.Map.Entry;

import dyvil.collections.ArrayIterator;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.access.FieldInitializer;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class StatementList extends ASTNode implements IStatement, IValueList, IContext
{
	private IValue[]			values	= new IValue[3];
	private int					valueCount;
	
	private Label[]				labels;
	private Map<Name, Variable>	variables;
	private IType				requiredType;
	
	private IContext			context;
	private IStatement			parent;
	
	public StatementList()
	{
	}
	
	public StatementList(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return STATEMENT_LIST;
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
	public boolean isPrimitive()
	{
		return this.requiredType.isPrimitive();
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Types.VOID || type == Types.UNKNOWN)
		{
			this.requiredType = Types.VOID;
			return this;
		}
		
		if (this.valueCount > 0 && this.values[this.valueCount - 1].isType(type))
		{
			this.requiredType = type;
			return this;
		}
		
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Types.VOID || type == Types.UNKNOWN)
		{
			return true;
		}
		
		return this.valueCount > 0 && this.values[this.valueCount - 1].isType(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
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
		return new ArrayIterator(this.values, this.valueCount);
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
		int i = this.valueCount++;
		System.arraycopy(this.values, index, this.values, index + 1, i - index + 1);
		this.values[index] = value;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.values[index];
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.context = context;
		for (int i = 0; i < this.valueCount; i++)
		{
			IValue v = this.values[i];
			if (v.isStatement())
			{
				((IStatement) v).setParent(this);
			}
			
			v.resolveTypes(markers, this);
		}
		this.context = null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.context = context;
		for (int i = 0; i < this.valueCount; i++)
		{
			IValue v1 = this.values[i];
			IValue v2 = v1.resolve(markers, this);
			if (v1 != v2)
			{
				this.values[i] = v2;
			}
			
			if (v2.getValueType() == IValue.VARIABLE)
			{
				if (this.variables == null)
				{
					this.variables = new IdentityHashMap();
				}
				
				FieldInitializer fi = (FieldInitializer) v2;
				Variable var = fi.variable;
				this.variables.put(var.name, var);
			}
		}
		this.context = null;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.valueCount == 0)
		{
			return;
		}
		
		if (this.requiredType == null)
		{
			this.requiredType = Types.VOID;
		}
		
		this.context = context;
		int len = this.valueCount - 1;
		for (int i = 0; i < len; i++)
		{
			IValue v = this.values[i];
			IValue v1 = v.withType(Types.VOID);
			if (v1 == null)
			{
				Marker marker = markers.create(v.getPosition(), "statement.type");
				marker.addInfo("Returning Type: " + v.getType());
			}
			else
			{
				v = this.values[i] = v1;
			}
			
			v.checkTypes(markers, this);
		}
		
		IValue lastValue = this.values[len];
		IValue value1 = lastValue.withType(this.requiredType);
		if (value1 == null)
		{
			Marker marker = markers.create(lastValue.getPosition(), "block.type");
			marker.addInfo("Block Type: " + this.requiredType);
			marker.addInfo("Returning Type: " + lastValue.getType());
		}
		else
		{
			lastValue = this.values[len] = value1;
		}
		lastValue.checkTypes(markers, this);
		
		this.context = null;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.context = context;
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].check(markers, context);
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
			IValue v1 = this.values[i];
			IValue v2 = v1.foldConstants();
			if (v1 != v2)
			{
				this.values[i] = v2;
			}
		}
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
		if (this.variables != null)
		{
			IField field = this.variables.get(name);
			if (field != null)
			{
				return field;
			}
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
	public byte getAccessibility(IMember member)
	{
		return this.context.getAccessibility(member);
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
		
		return this.parent == null ? null : this.parent.resolveLabel(name);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.requiredType == Types.VOID)
		{
			this.writeStatement(writer);
			return;
		}
		
		org.objectweb.asm.Label start = new org.objectweb.asm.Label();
		org.objectweb.asm.Label end = new org.objectweb.asm.Label();
		
		writer.writeLabel(start);
		int count = writer.registerLocal();
		int len = this.valueCount - 1;
		
		if (this.labels == null)
		{
			for (int i = 0; i < len; i++)
			{
				this.values[i].writeStatement(writer);
			}
			this.values[len].writeExpression(writer);
		}
		else
		{
			for (int i = 0; i < len; i++)
			{
				Label l = this.labels[i];
				if (l != null)
				{
					writer.writeLabel(l.target);
				}
				
				this.values[i].writeStatement(writer);
			}
			
			Label l = this.labels[len];
			if (l != null)
			{
				writer.writeLabel(l.target);
			}
			
			this.values[len].writeExpression(writer);
		}
		
		writer.resetLocals(count);
		writer.writeLabel(end);
		
		if (this.variables == null)
		{
			return;
		}
		
		for (Entry<Name, Variable> entry : this.variables.entrySet())
		{
			Variable var = entry.getValue();
			writer.writeLocal(var.name.qualified, var.type.getExtendedName(), var.type.getSignature(), start, end, var.index);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		org.objectweb.asm.Label start = new org.objectweb.asm.Label();
		org.objectweb.asm.Label end = new org.objectweb.asm.Label();
		
		writer.writeLabel(start);
		int count = writer.registerLocal();
		
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
				Label l = this.labels[i];
				if (l != null)
				{
					writer.writeLabel(l.target);
				}
				
				this.values[i].writeStatement(writer);
			}
		}
		
		writer.resetLocals(count);
		writer.writeLabel(end);
		
		if (this.variables == null)
		{
			return;
		}
		
		for (Entry<Name, Variable> entry : this.variables.entrySet())
		{
			Variable var = entry.getValue();
			writer.writeLocal(var.name.qualified, var.type.getExtendedName(), var.type.getSignature(), start, end, var.index);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.valueCount == 0)
		{
			buffer.append(Formatting.Expression.emptyExpression);
		}
		else
		{
			buffer.append('\n').append(prefix).append('{').append('\n');
			String prefix1 = prefix + Formatting.Method.indent;
			IValue prev = null;
			
			for (int i = 0; i < this.valueCount; i++)
			{
				IValue value = this.values[i];
				buffer.append(prefix1);
				
				if (prev != null)
				{
					ICodePosition pos = value.getPosition();
					if (pos != null && pos.endLine() - prev.getPosition().startLine() > 0)
					{
						buffer.append('\n').append(prefix1);
					}
				}
				
				if (this.labels != null)
				{
					Label l = this.labels[i];
					if (l != null)
					{
						buffer.append(l.name).append(Formatting.Expression.labelSeperator);
					}
				}
				
				value.toString(prefix1, buffer);
				buffer.append(";\n");
				prev = value;
			}
			buffer.append(prefix).append('}');
		}
	}
}
