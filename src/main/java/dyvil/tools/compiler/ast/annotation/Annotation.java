package dyvil.tools.compiler.ast.annotation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;

public class Annotation extends ASTNode implements ITyped
{
	public Type					type;
	public Map<String, IValue>	parameters	= new HashMap();
	
	private String				name;
	
	public Annotation(ICodePosition position, Type type)
	{
		this.position = position;
		this.name = type.name;
		this.type = type;
	}
	
	public Annotation(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
		this.type = new Type(name, position);
	}
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	public void addValue(String key, IValue value)
	{
		this.parameters.put(key, value);
	}
	
	public IValue getValue(String key)
	{
		return this.parameters.get(key);
	}
	
	@Override
	public Annotation applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.type = this.type.resolve(context);
			if (!this.type.isResolved())
			{
				state.addMarker(new SemanticError(this.position, "'" + this.name + "' could not be resolved to a type"));
			}
		}
		else if (state == CompilerState.CHECK)
		{
			if (!this.type.theClass.hasModifier(Modifiers.ANNOTATION))
			{
				state.addMarker(new SemanticError(this.position, "The type '" + this.name + "' is not an annotation type"));
			}
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		if (!this.parameters.isEmpty())
		{
			buffer.append(Formatting.Method.parametersStart);
			
			Iterator<Entry<String, IValue>> iterator = this.parameters.entrySet().iterator();
			while (true)
			{
				Entry<String, IValue> e = iterator.next();
				buffer.append(e.getKey()).append(Formatting.Field.keyValueSeperator);
				e.getValue().toString("", buffer);
				if (iterator.hasNext())
				{
					buffer.append(Formatting.Method.parameterSeperator);
					
				}
				else
				{
					break;
				}
			}
			
			buffer.append(Formatting.Method.parametersEnd);
		}
	}
}
