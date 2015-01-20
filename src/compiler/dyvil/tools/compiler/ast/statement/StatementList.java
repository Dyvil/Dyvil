package dyvil.tools.compiler.ast.statement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.access.FieldAssign;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ValueList;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class StatementList extends ValueList implements IStatement, IContext
{
	private IContext				context;
	
	private int						variableCount;
	public Map<String, Variable>	variables	= new HashMap();
	public Label					start		= new Label();
	public Label					end			= new Label();
	
	public StatementList(ICodePosition position)
	{
		super(position);
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
		this.variableCount = context.getVariableCount();
		
		for (IValue v : this.values)
		{
			if (v.getValueType() == IValue.FIELD_ASSIGN)
			{
				FieldAssign fa = (FieldAssign) v;
				if (fa.type != null)
				{
					Variable var = new Variable(this.position);
					var.name = fa.name;
					var.qualifiedName = fa.qualifiedName;
					var.type = fa.type;
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
			IType type = this.getType();
			for (IValue v : this.values)
			{
				v.check(markers, context);
				
				if (v instanceof IStatement && !v.requireType(type))
				{
					SemanticError error = new SemanticError(v.getPosition(), "The returning type of the block is incompatible with the required type");
					error.addInfo("Block Type: " + type);
					error.addInfo("Returning Type: " + v.getType());
					markers.add(error);
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
	public MethodMatch resolveMethod(ITyped instance, String name, List<? extends ITyped> arguments)
	{
		return this.context.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, ITyped instance, String name, List<? extends ITyped> arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
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
		
		for (Entry<String, Variable> entry : this.variables.entrySet())
		{
			Variable var = entry.getValue();
			writer.visitLocalVariable(entry.getKey(), var.type, this.start, this.end, var.index);
		}
	}
}
