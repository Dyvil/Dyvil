package dyvil.tools.compiler.ast.access;

import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.Modifiers;

public class FieldAssign extends ASTNode implements IValue, INamed, IValued
{
	public String	name;
	public String	qualifiedName;
	
	public IType	type;
	
	public IValue	instance;
	public IField	field;
	public IValue	value;
	
	public FieldAssign(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAssign(ICodePosition position, String name, IValue instance)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
	}
	
	@Override
	public IType getType()
	{
		return this.field == null ? Type.NONE : this.field.getType();
	}
	
	@Override
	public boolean requireType(IType type)
	{
		return type == Type.VOID || type.equals(Type.ANY) || Type.isSuperType(type, this.field.getType());
	}
	
	@Override
	public int getValueType()
	{
		return FIELD_ASSIGN;
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
		this.qualifiedName = qualifiedName;
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
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.type != null)
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
	public IValue resolve(List<Marker> markers, IContext context)
	{
		if (this.type != null)
		{
			this.field.resolve(markers, context);
		}
		else
		{
			FieldMatch match = null;
			if (this.instance == null)
			{
				match = context.resolveField(this.qualifiedName);
			}
			else
			{
				IType type = this.instance.getType();
				if (type != null)
				{
					match = type.resolveField(this.qualifiedName);
				}
			}
			
			if (match != null)
			{
				this.field = match.theField;
			}
			else
			{
				markers.add(Markers.create(this.position, "resolve.field", this.field));
			}
			
			if (this.value != null)
			{
				this.value = this.value.resolve(markers, context);
			}
		}
		
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.type != null)
		{
			this.field.check(markers, context);
			return;
		}
		
		if (this.value.getValueType() == IValue.THIS)
		{
			markers.add(new SyntaxError(this.position, "access.this.assign"));
		}
		
		if (this.field == null)
		{
			return;
		}
		
		IType type = this.field.getType();
		if (!this.value.requireType(type))
		{
			Marker marker = Markers.create(this.value.getPosition(), "access.assign.type", this.name);
			marker.addInfo("Field Type: " + type);
			IType vtype = this.value.getType();
			marker.addInfo("Value Type: " + (vtype == null ? "unknown" : vtype));
			markers.add(marker);
		}
		
		if (this.field.hasModifier(Modifiers.FINAL))
		{
			markers.add(Markers.create(this.position, "access.final.field", this.name));
		}
		if (this.field.hasModifier(Modifiers.DEPRECATED))
		{
			markers.add(Markers.create(this.position, "access.field.deprecated", this.name));
		}
		
		byte access = context.getAccessibility(this.field);
		if (access == IContext.STATIC)
		{
			markers.add(Markers.create(this.position, "access.static.field", this.name));
		}
		else if (access == IContext.SEALED)
		{
			markers.add(Markers.create(this.position, "access.sealed.field", this.name));
		}
		else if ((access & IContext.WRITE_ACCESS) == 0)
		{
			markers.add(Markers.create(this.position, "access.invisible.field", this.name));
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.type != null)
		{
			this.field.foldConstants();
		}
		else
		{
			this.value = this.value.foldConstants();
		}
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
		writer.visitInsn(Opcodes.DUP);
		this.field.writeSet(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.type != null)
		{
			this.field.getValue().writeExpression(writer);
			this.field.writeSet(writer);
			return;
		}
		
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
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.type != null)
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
			
			if (this.value != null)
			{
				buffer.append(Formatting.Field.keyValueSeperator);
				Formatting.appendValue(this.value, prefix, buffer);
			}
		}
	}
}
