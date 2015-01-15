package dyvil.tools.compiler.ast.statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.expression.ValueList;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class StatementList extends ValueList implements IStatement, IContext
{
	private IContext			context;
	
	public Map<String, IField>	variables	= new HashMap();
	public Label				start		= new Label();
	public Label				end			= new Label();
	
	public StatementList(ICodePosition position)
	{
		super(position);
	}
	
	public void addStatement(IStatement statement)
	{
		this.values.add(statement);
	}
	
	@Override
	public boolean requireType(IType type)
	{
		this.requiredType = type;
		if (type == Type.VOID)
		{
			return true;
		}
		return super.requireType(type);
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.context = context;
		IVariableList variableList = context instanceof IVariableList ? (IVariableList) context : null;
		
		for (IValue v : this.values)
		{
			v.resolveTypes(markers, context);
			
			if (!(v instanceof FieldAssign))
			{
				continue;
			}
			
			FieldAssign assign = (FieldAssign) v;
			if (!assign.initializer)
			{
				continue;
			}
			
			Variable var = (Variable) assign.field;
			var.start = this.start;
			var.end = this.end;
			
			this.variables.put(assign.qualifiedName, assign.field);
			
			if (variableList != null)
			{
				variableList.addVariable(var);
			}
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
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
			IType type = this.getElementType();
			for (IValue value : this.values)
			{
				value.check(markers, context);
				
				if (!value.requireType(type))
				{
					markers.add(new SemanticError(value.getPosition(), "The array value is incompatible with the required type " + type));
				}
			}
		}
		else
		{
			IType type = this.getType();
			for (IValue v : this.values)
			{
				v.check(markers, context);
				
				if (v instanceof IStatement && !v.requireType(type))
				{
					markers.add(new SemanticError(v.getPosition(), "The returning type of the block is incompatible with the required type " + type));
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
	public FieldMatch resolveField(IContext context, String name)
	{
		IField field = this.variables.get(name);
		if (field != null)
		{
			return new FieldMatch(field, 1);
		}
		
		return this.context.resolveField(context, name);
	}
	
	@Override
	public MethodMatch resolveMethod(IContext context, String name, IType[] argumentTypes)
	{
		return this.context.resolveMethod(context, name, argumentTypes);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IType type, String name, IType[] argumentTypes)
	{
		this.context.getMethodMatches(list, type, name, argumentTypes);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.context.getAccessibility(member);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.isArray)
		{
			super.writeExpression(writer);
			return;
		}
		
		this.writeStatement(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.isArray)
		{
			super.writeExpression(writer);
			return;
		}
		
		writer.visitLabel(this.start);
		for (IValue v : this.values)
		{
			v.writeStatement(writer);
		}
		writer.visitLabel(this.end);
	}
}
