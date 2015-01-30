package dyvil.tools.compiler.ast.statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.access.FieldAssign;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.ValueList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class StatementList extends ValueList implements IStatement, IContext
{
	private IContext				context;
	private IStatement				parent;
	
	private int						variableCount;
	public Map<String, Variable>	variables	= new HashMap();
	
	public Label					start;
	public Label					end;
	
	public Map<String, Label>		labels;
	public Map<IValue, String>		valueLabels;
	
	public StatementList(ICodePosition position)
	{
		super(position);
		
		this.start = new Label();
		this.end = new Label();
	}
	
	@Override
	public void addLabel(String name, IValue value)
	{
		if (this.labels == null)
		{
			this.labels = new HashMap();
			this.valueLabels = new HashMap();
		}
		
		Label label = new Label();
		label.info = value;
		this.labels.put(name, label);
		this.valueLabels.put(value, name);
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
	public IValue withType(IType type)
	{
		this.requiredType = type;
		if (type == Type.VOID)
		{
			return this;
		}
		return super.withType(type);
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Type.VOID || type == Type.NONE || super.isType(type);
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.isArray)
		{
			for (IValue v : this.values)
			{
				v.resolveTypes(markers, context);
			}
		}
		
		this.context = context;
		this.variableCount = context.getVariableCount();
		
		for (IValue v : this.values)
		{
			if (v.isStatement())
			{
				((IStatement) v).setParent(this);
			}
			else if (v.getValueType() == IValue.FIELD_ASSIGN)
			{
				FieldAssign fa = (FieldAssign) v;
				if (fa.type != null)
				{
					Variable var = new Variable(this.position);
					var.name = fa.name;
					var.qualifiedName = fa.qualifiedName;
					var.type = fa.type;
					var.value = fa.value;
					var.index = this.variableCount++;
					fa.field = var;
					this.variables.put(fa.qualifiedName, var);
				}
			}
			
			v.resolveTypes(markers, this);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		if (this.isArray)
		{
			// Convert this to a simpler ValueList for performance
			return new ValueList(this.position, this.values, this.requiredType, this.elementType).resolve(markers, context);
		}
		
		this.context = context;
		
		int len = this.values.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.values.get(i);
			IValue v2 = v1.resolve(markers, this);
			if (v1 != v2)
			{
				this.values.set(i, v2);
			}
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.isArray)
		{
			super.check(markers, context);
		}
		else
		{
			IType type = this.requiredType;
			for (IValue v : this.values)
			{
				v.check(markers, this);
				
				if (v.getValueType() == RETURN && v.withType(type) == null)
				{
					Marker marker = Markers.create(v.getPosition(), "return.type");
					marker.addInfo("Block Type: " + type);
					marker.addInfo("Returning Type: " + v.getType());
					markers.add(marker);
				}
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		int len = this.values.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.values.get(i);
			IValue v2 = v1.foldConstants();
			if (v1 != v2)
			{
				this.values.set(i, v2);
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
	public int getVariableCount()
	{
		return this.variableCount;
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
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		return this.context.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
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
		if (this.labels != null)
		{
			Label label = this.labels.get(name);
			if (label != null)
			{
				return label;
			}
		}
		
		return this.parent == null ? null : this.parent.resolveLabel(name);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.writeStatement(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.visitLabel2(this.start);
		
		// Write variable types
		for (Entry<String, Variable> entry : this.variables.entrySet())
		{
			Variable var = entry.getValue();
			writer.addLocal(var.index, var.type);
		}
		
		String label;
		for (IValue v : this.values)
		{
			if (this.valueLabels != null && (label = this.valueLabels.get(v)) != null)
			{
				writer.visitLabel(this.labels.get(label));
			}
			
			v.writeStatement(writer);
		}
		writer.visitLabel2(this.end);
		
		int count = 0;
		for (Entry<String, Variable> entry : this.variables.entrySet())
		{
			Variable var = entry.getValue();
			writer.visitLocalVariable(var.qualifiedName, var.type.getExtendedName(), var.type.getSignature(), this.start, this.end, var.index);
			count++;
		}
		writer.removeLocals(count);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.values.size();
		if (len == 0)
		{
			buffer.append(Formatting.Expression.emptyExpression);
		}
		else
		{
			buffer.append('{').append('\n');
			String label;
			String prefix1 = prefix + Formatting.Method.indent;
			for (IValue value : this.values)
			{
				buffer.append(prefix1);
				
				if (this.valueLabels != null && (label = this.valueLabels.get(value)) != null)
				{
					buffer.append(label).append(Formatting.Expression.labelSeperator);
				}
				value.toString(prefix1, buffer);
				buffer.append(";\n");
			}
			buffer.append(prefix).append('}');
		}
	}
}
