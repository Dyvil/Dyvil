package dyvil.tools.compiler.ast.expression;

import java.util.Collections;
import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.marker.Warning;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.AccessResolver;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Symbols;

public class FieldAccess extends ASTNode implements IValue, INamed, IValued, IAccess
{
	public IValue	instance;
	public String	name;
	public String	qualifiedName;
	
	public boolean	dotless;
	
	public IField	field;
	
	public FieldAccess(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAccess(ICodePosition position, IValue instance, String name)
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
		if (this.field == null)
		{
			return null;
		}
		return this.field.getType();
	}
	
	@Override
	public int getValueType()
	{
		return FIELD_ACCESS;
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
	public void setQualifiedName(String name)
	{
		this.qualifiedName = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
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
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
	}
	
	@Override
	public void addValue(IValue value)
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
	public void setArray(boolean array)
	{
	}
	
	@Override
	public boolean isArray()
	{
		return false;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		return AccessResolver.resolve(markers, context, this);
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		if (this.field != null)
		{
			if (this.instance != null && this.field.hasModifier(Modifiers.STATIC) && this.instance.getValueType() == IValue.THIS)
			{
				markers.add(new Warning(this.position, "'" + this.name + "' is a static field and should be accessed in a static way"));
				this.instance = null;
			}
			
			byte access = context.getAccessibility(this.field);
			if (access == IContext.STATIC)
			{
				markers.add(new SemanticError(this.position, "The instance field '" + this.name + "' cannot be accessed from a static context"));
			}
			else if (access == IContext.SEALED)
			{
				markers.add(new SemanticError(this.position, "The sealed field '" + this.name + "' cannot be accessed because it is private to it's library"));
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(new SemanticError(this.position, "The field '" + this.name + "' cannot be accessed because it is not visible"));
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.field.hasModifier(Modifiers.CONST))
		{
			IValue v = this.field.getValue();
			if (v != null && v.isConstant())
			{
				return v;
			}
		}
		this.instance = this.instance.foldConstants();
		return this;
	}
	
	@Override
	public boolean resolve(IContext context, IContext context1)
	{
		FieldMatch f = context.resolveField(context1, this.qualifiedName);
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
		MethodMatch match = context.resolveMethod(context1, this.qualifiedName, Type.EMPTY_TYPES);
		if (match != null)
		{
			MethodCall call = new MethodCall(this.position, this.instance, this.name);
			call.method = match.theMethod;
			call.dotless = this.dotless;
			call.isSugarCall = true;
			return call;
		}
		
		return null;
	}
	
	@Override
	public IAccess resolve3(IContext context, IAccess next)
	{
		MethodMatch m = context.resolveMethod(null, this.qualifiedName, next.getType());
		if (m != null)
		{
			MethodCall call = new MethodCall(this.position, this.instance, this.name);
			call.addValue(next);
			call.method = m.theMethod;
			call.dotless = this.dotless;
			call.isSugarCall = true;
			return call;
		}
		
		return null;
	}
	
	@Override
	public Marker getResolveError()
	{
		return new SemanticError(this.position, "'" + this.qualifiedName + "' could not be resolved to a field");
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.instance != null)
		{
			this.instance.writeExpression(writer);
		}
		
		this.field.writeGet(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		if (this.instance != null)
		{
			this.instance.writeExpression(writer);
		}
		
		this.field.writeGet(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString("", buffer);
			if (this.dotless && !Formatting.Field.useJavaFormat)
			{
				buffer.append(Formatting.Field.dotlessSeperator);
			}
			else
			{
				buffer.append('.');
			}
		}
		
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName);
		}
		else
		{
			buffer.append(this.name);
		}
	}
}
