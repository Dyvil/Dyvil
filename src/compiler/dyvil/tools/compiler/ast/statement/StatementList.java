package dyvil.tools.compiler.ast.statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dyvil.tools.compiler.ast.access.FieldInitializer;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.ValueList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class StatementList extends ValueList implements IStatement, IContext
{
	private IContext				context;
	private IStatement				parent;
	
	public Map<String, Variable>	variables	= new HashMap();
	
	public Label[]					labels;
	
	public StatementList(ICodePosition position)
	{
		super(position);
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
		if (this.isArray)
		{
			return false;
		}
		return this.requiredType.isPrimitive();
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Type.VOID || type == Type.NONE)
		{
			this.elementType = this.requiredType = Type.VOID;
			return this;
		}
		
		if (this.valueCount > 0 && this.values[this.valueCount - 1].isType(type))
		{
			this.elementType = this.requiredType = type;
			return this;
		}
		
		if (type.isArrayType())
		{
			return new ValueList(this.position, this.values, this.valueCount, type, type.getElementType());
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Type.VOID || type == Type.NONE)
		{
			return true;
		}
		
		if (this.valueCount > 0 && this.values[this.valueCount - 1].isType(type))
		{
			return true;
		}
		
		if (type.isArrayType())
		{
			return super.isType(type);
		}
		return false;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type == Type.VOID || type == Type.NONE)
		{
			return 3;
		}
		
		if (this.valueCount > 0)
		{
			int m = this.values[this.valueCount - 1].getTypeMatch(type);
			if (m > 0)
			{
				return m;
			}
		}
		
		if (type.isArrayType())
		{
			return super.getTypeMatch(type);
		}
		return 0;
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
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.isArray)
		{
			for (int i = 0; i < this.valueCount; i++)
			{
				this.values[i].resolveTypes(markers, context);
			}
			return;
		}
		
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
		if (this.isArray)
		{
			// Convert this to a simpler ValueList for performance
			return new ValueList(this.position, this.values, this.valueCount, this.requiredType, this.elementType).resolve(markers, context);
		}
		
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
				FieldInitializer fi = (FieldInitializer) v2;
				Variable var = fi.variable;
				this.variables.put(var.qualifiedName, var);
			}
		}
		this.context = null;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		// this.isArray has already been checked in resolve
		
		if (this.valueCount == 0)
		{
			return;
		}
		
		if (this.requiredType == null)
		{
			this.elementType = this.requiredType = Type.VOID;
		}
		
		this.context = context;
		int len = this.valueCount - 1;
		for (int i = 0; i < len; i++)
		{
			IValue v = this.values[i];
			IValue v1 = v.withType(Type.VOID);
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
		// this.isArray has already been checked in resolve
		
		this.context = context;
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].check(markers, context);
		}
		this.context = null;
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
		IField field = this.variables.get(name);
		if (field != null)
		{
			return new FieldMatch(field, 1);
		}
		
		return this.context.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		return this.context.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public MethodMatch resolveConstructor(IArguments arguments)
	{
		return this.context.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, IArguments arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.context.getAccessibility(member);
	}
	
	@Override
	public Label resolveLabel(String name)
	{
		if (this.labels != null)
		{
			for (Label label : this.labels)
			{
				if (label != null && name.equals(label.name))
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
		if (this.requiredType == Type.VOID)
		{
			this.writeStatement(writer);
			return;
		}
		
		org.objectweb.asm.Label start = new org.objectweb.asm.Label();
		org.objectweb.asm.Label end = new org.objectweb.asm.Label();
		
		writer.writeLabel(start);
		int count = writer.localCount();
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
					writer.writeFrameLabel(l.target);
				}
				
				this.values[i].writeStatement(writer);
			}
			
			Label l = this.labels[len];
			if (l != null)
			{
				writer.writeFrameLabel(l.target);
			}
			
			this.values[len].writeExpression(writer);
		}
		
		writer.resetLocals(count);
		writer.writeLabel(end);
		
		for (Entry<String, Variable> entry : this.variables.entrySet())
		{
			Variable var = entry.getValue();
			writer.writeLocal(var.qualifiedName, var.type.getExtendedName(), var.type.getSignature(), start, end, var.index);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		org.objectweb.asm.Label start = new org.objectweb.asm.Label();
		org.objectweb.asm.Label end = new org.objectweb.asm.Label();
		
		writer.writeLabel(start);
		int count = writer.localCount();
		
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
					writer.writeFrameLabel(l.target);
				}
				
				this.values[i].writeStatement(writer);
			}
		}
		
		writer.resetLocals(count);
		writer.writeLabel(end);
		
		for (Entry<String, Variable> entry : this.variables.entrySet())
		{
			Variable var = entry.getValue();
			writer.writeLocal(var.qualifiedName, var.type.getExtendedName(), var.type.getSignature(), start, end, var.index);
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
			buffer.append('{').append('\n');
			String prefix1 = prefix + Formatting.Method.indent;
			IValue prev = null;
			
			for (int i = 0; i < this.valueCount; i++)
			{
				IValue value = this.values[i];
				buffer.append(prefix1);
				
				if (prev != null)
				{
					ICodePosition pos = value.getPosition();
					if (pos != null && pos.endLine() - prev.getPosition().startLine() > 1)
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
