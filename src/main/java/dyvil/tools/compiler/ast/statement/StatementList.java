package dyvil.tools.compiler.ast.statement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.IVariableList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.ValueList;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
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
	public boolean requireType(Type type)
	{
		this.requiredType = type;
		if (type == Type.VOID)
		{
			return true;
		}
		return super.requireType(type);
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		int len = this.values.size();
		if (state == CompilerState.FOLD_CONSTANTS)
		{
			if (len == 1)
			{
				return this.values.get(0);
			}
		}
		else if (state == CompilerState.RESOLVE)
		{
			this.context = context;
			IVariableList variableList = context instanceof IVariableList ? (IVariableList) context : null;
			this.context = context;
			ListIterator<IValue> iterator = this.values.listIterator();
			while (iterator.hasNext())
			{
				IValue v = iterator.next();
				if (v == null)
				{
					iterator.remove();
					continue;
				}
				iterator.set(v.applyState(state, this));
				
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
					variableList.addVariable((Variable) assign.field);
				}
			}
		}
		else if (state == CompilerState.CHECK)
		{
			Type type = this.requiredType;
			if (this.isArray)
			{
				type.arrayDimensions--;
				for (IValue value : this.values)
				{
					if (!value.requireType(type))
					{
						state.addMarker(new SemanticError(value.getPosition(), "The array value is incompatible with the required type " + type));
					}
					
					value.applyState(state, context);
				}
				type.arrayDimensions++;
			}
			else
			{
				for (IValue v : this.values)
				{
					if (v instanceof IStatement)
					{
						if (!v.requireType(type))
						{
							state.addMarker(new SemanticError(v.getPosition(), "The returning type of the block is incompatible with the required type " + type));
						}
					}
					
					v.applyState(state, context);
				}
			}
		}
		else
		{
			this.context = context;
			ListIterator<IValue> iterator = this.values.listIterator();
			while (iterator.hasNext())
			{
				IValue v = iterator.next();
				if (v == null)
				{
					iterator.remove();
					continue;
				}
				iterator.set(v.applyState(state, this));
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
	public Type getThisType()
	{
		return this.context.getThisType();
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
	public MethodMatch resolveMethod(IContext context, String name, Type... argumentTypes)
	{
		return this.context.resolveMethod(context, name, argumentTypes);
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.isArray)
		{
			super.writeExpression(writer);
			return;
		}
		
		if (this.requiredType == Type.VOID)
		{
			this.writeStatement(writer);
			return;
		}
		
		writer.visitLabel(this.start);
		if (!this.values.isEmpty())
		{
			Iterator<IValue> iterator = this.values.iterator();
			while (true)
			{
				IValue v = iterator.next();
				if (iterator.hasNext())
				{
					v.writeStatement(writer);
				}
				else
				{
					v.writeExpression(writer);
					break;
				}
			}
		}
		writer.visitLabel(this.end);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		writer.visitLabel(this.start);
		for (IValue v : this.values)
		{
			v.writeStatement(writer);
		}
		writer.visitLabel(this.end);
	}
}
