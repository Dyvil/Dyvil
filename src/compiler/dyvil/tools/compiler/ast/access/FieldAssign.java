package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class FieldAssign extends ASTNode implements IValue, INamed, IValued
{
	public Name		name;
	
	public IValue	instance;
	public IValue	value;
	
	public IField	field;
	
	public FieldAssign(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAssign(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
	}
	
	@Override
	public int getValueType()
	{
		return FIELD_ASSIGN;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.field == null ? false : this.field.getType().isPrimitive();
	}
	
	@Override
	public IType getType()
	{
		return this.field == null ? Types.UNKNOWN : this.field.getType();
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type == Types.UNKNOWN || type == Types.VOID)
		{
			return this;
		}
		
		IValue value1 = this.value.withType(type);
		if (value1 == null)
		{
			return null;
		}
		this.value = value1;
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.value == null ? type == Types.UNKNOWN || type == Types.VOID : this.value.isType(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return this.value.getTypeMatch(type);
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
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
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.field != null)
		{
			this.field.resolveTypes(markers, context);
		}
		else
		{
			if (this.instance != null)
			{
				this.instance.resolveTypes(markers, context);
			}
			
			if (this.value != null)
			{
				this.value.resolveTypes(markers, context);
			}
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		
		this.field = IAccess.resolveField(context, this.instance, this.name);
		
		if (this.field == null)
		{
			Marker marker = markers.create(this.position, "resolve.field", this.name.unqualified);
			marker.addInfo("Qualified Name: " + this.name.qualified);
			if (this.instance != null)
			{
				marker.addInfo("Instance Type: " + this.instance.getType());
			}
			
		}
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.checkTypes(markers, context);
		}
		
		if (this.field == null)
		{
			return;
		}
		
		IType type = this.field.getType();
		IValue value1 = this.value.withType(type);
		if (value1 == null)
		{
			Marker marker = markers.create(this.value.getPosition(), "access.assign.type", this.name);
			marker.addInfo("Field Type: " + type);
			marker.addInfo("Value Type: " + this.value.getType());
		}
		else
		{
			this.value = value1;
		}
		
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		if (this.value.getValueType() == IValue.THIS)
		{
			markers.add(new SyntaxError(this.position, "access.this.assign"));
		}
		else
		{
			this.value.check(markers, context);
		}
		
		if (this.field == null)
		{
			return;
		}
		
		if (this.field.hasModifier(Modifiers.FINAL))
		{
			markers.add(this.position, "access.final.field", this.name);
		}
		if (this.field.hasModifier(Modifiers.DEPRECATED))
		{
			markers.add(this.position, "access.field.deprecated", this.name);
		}
		
		byte access = context.getAccessibility(this.field);
		if (access == IContext.STATIC)
		{
			markers.add(this.position, "access.static.field", this.name);
		}
		else if (access == IContext.SEALED)
		{
			markers.add(this.position, "access.sealed.field", this.name);
		}
		else if ((access & IContext.WRITE_ACCESS) == 0)
		{
			markers.add(this.position, "access.invisible.field", this.name);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		return this;
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
		writer.writeInsn(Opcodes.DUP);
		this.field.writeSet(writer, null, null);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.value == null)
		{
			return;
		}
		
		this.field.writeSet(writer, this.instance, this.value);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString("", buffer);
			buffer.append('.');
		}
		
		buffer.append(this.name);
		
		if (this.value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			Formatting.appendValue(this.value, prefix, buffer);
		}
	}
}
