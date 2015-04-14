package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class Variable extends Member implements IVariable
{
	public int		index;
	public IValue	value;
	
	public Variable(ICodePosition position)
	{
		this.position = position;
	}
	
	public Variable(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}
	
	public Variable(ICodePosition position, Name name, IType type)
	{
		this.name = name;
		this.type = type;
		this.position = position;
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
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	@Override
	public int getIndex()
	{
		return this.index;
	}
	
	@Override
	public boolean addRawAnnotation(String type)
	{
		switch (type)
		{
		case "dyvil/annotation/lazy":
			this.modifiers |= Modifiers.LAZY;
			return false;
		}
		return true;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.LOCAL_VARIABLE;
	}
	
	@Override
	public String getDescription()
	{
		return this.type.getExtendedName();
	}
	
	@Override
	public String getSignature()
	{
		return this.type.getSignature();
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
		
		if (this.type == Types.UNKNOWN)
		{
			this.type = this.value.getType();
			if (this.type == Types.UNKNOWN)
			{
				markers.add(this.position, "variable.type.infer", this.name.unqualified);
			}
			return;
		}
		
		IValue value1 = this.value.withType(this.type);
		if (value1 == null)
		{
			Marker marker = markers.create(this.position, "variable.type", this.name.unqualified);
			marker.addInfo("Variable Type: " + this.type);
			marker.addInfo("Value Type: " + this.value.getType());
		}
		else
		{
			this.value = value1;
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		this.value.check(markers, context);
	}
	
	@Override
	public void foldConstants()
	{
		super.foldConstants();
		
		this.value = this.value.foldConstants();
	}
	
	@Override
	public void write(ClassWriter writer)
	{
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance)
	{
		writer.writeVarInsn(this.type.getLoadOpcode(), this.index);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value)
	{
		if (value != null)
		{
			value.writeExpression(writer);
		}
		
		writer.writeVarInsn(this.type.getStoreOpcode(), this.index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString("", buffer);
		buffer.append(' ').append(this.name);
		
		if (this.value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			this.value.toString(prefix, buffer);
		}
	}
}
