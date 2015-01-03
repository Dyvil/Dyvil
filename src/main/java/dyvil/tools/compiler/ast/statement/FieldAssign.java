package dyvil.tools.compiler.ast.statement;

import java.util.Collections;
import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.value.ThisValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Symbols;

public class FieldAssign extends ASTNode implements INamed, IValued, IAccess
{
	protected String	name;
	protected String	qualifiedName;
	
	public boolean		initializer;
	
	public IValue		instance;
	public IField		field;
	public IValue		value;
	
	public FieldAssign(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAssign(ICodePosition position, String name, IValue instance)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return this.field.getType();
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.qualifiedName = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.qualifiedName;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.qualifiedName.equals(name);
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
	}
	
	@Override
	public List<IValue> getValues()
	{
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return null;
	}
	
	@Override
	public void addValue(IValue value)
	{
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
	}
	
	@Override
	public void setArray(boolean array)
	{
	}
	
	@Override
	public boolean isArray()
	{
		return false;
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			if (this.initializer)
			{
				this.field.applyState(state, context);
			}
			else if (this.instance != null)
			{
				this.instance.applyState(state, context);
			}
		}
		else if (state == CompilerState.RESOLVE)
		{
			if (!this.initializer)
			{
				FieldMatch match;
				if (this.instance == null)
				{
					match = context.resolveField(null, this.qualifiedName);
				}
				else
				{
					match = this.instance.getType().resolveField(context, this.qualifiedName);
				}
				
				if (match != null)
				{
					this.field = match.theField;
				}
				else
				{
					state.addMarker(new SemanticError(this.position, "'" + this.name + "' could not be resolved to a field"));
				}
			}
		}
		else if (state == CompilerState.CHECK)
		{
			if (this.value instanceof ThisValue)
			{
				state.addMarker(new SyntaxError(this.position, "Cannot assign a value to 'this'"));
			}
			else if (this.field != null)
			{
				IType type = this.field.getType();
				if (!this.value.requireType(type))
				{
					state.addMarker(new SemanticError(this.value.getPosition(), "The type of the assigned value is incompatible with the required type " + type));
				}
				
				if (!this.initializer)
				{
					if (this.field.hasModifier(Modifiers.FINAL))
					{
						state.addMarker(new SemanticError(this.position, "The final field '" + this.name + "' cannot be assigned"));
					}
					
					byte access = context.getAccessibility(this.field);
					if (access == IContext.STATIC)
					{
						state.addMarker(new SemanticError(this.position, "The instance field '" + this.name + "' cannot be assigned from a static context"));
					}
					else if ((access & IContext.WRITE_ACCESS) == 0)
					{
						state.addMarker(new SemanticError(this.position, "The field '" + this.name + "' cannot be assigned since it is not visible"));
					}
				}
			}
		}
		
		if (this.value != null)
		{
			this.value = this.value.applyState(state, context);
		}
		return this;
	}
	
	@Override
	public boolean resolve(IContext context, IContext context1)
	{
		if (this.field != null)
		{
			return true;
		}
		
		FieldMatch f = context1.resolveField(null, this.qualifiedName);
		if (f != null)
		{
			this.field = f.theField;
			return true;
		}
		return false;
	}
	
	@Override
	public IAccess resolve2(IContext context, IContext context1)
	{
		return null;
	}
	
	@Override
	public IAccess resolve3(IContext context, IAccess next)
	{
		return null;
	}
	
	@Override
	public Marker getResolveError()
	{
		return new SemanticError(this.position, "'" + this.name + "' could not be resolved to a field");
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.value == null)
		{
			return;
		}
		
		if (this.instance != null)
		{
			this.instance.writeExpression(writer);
		}
		this.value.writeExpression(writer);
		writer.visitInsn(Opcodes.DUP, this.value.getType());
		this.field.writeSet(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.value == null)
		{
			return;
		}
		
		if (this.instance != null)
		{
			this.instance.writeExpression(writer);
		}
		this.value.writeExpression(writer);
		this.field.writeSet(writer);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label label)
	{
		// TODO
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.initializer)
		{
			this.field.toString("", buffer);
		}
		else
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append('.');
			}
			
			buffer.append(this.name);
		}
		
		if (this.value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			this.value.toString("", buffer);
		}
	}
}
