package dyvil.tools.compiler.ast.access;

import java.util.Collections;
import java.util.List;

import dyvil.collections.SingleElementList;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.EnumValue;
import dyvil.tools.compiler.ast.value.ThisValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.AccessResolver;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.Modifiers;

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
		this.qualifiedName = Symbols.qualify(name);
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
			if (this.field.hasModifier(Modifiers.STATIC))
			{
				if (this.instance != null && (this.instance.getValueType() != CLASS_ACCESS))
				{
					markers.add(Markers.create(this.position, "access.field.static", this.name));
					this.instance = null;
				}
			}
			else if (this.instance == null && this.field instanceof Field)
			{
				markers.add(Markers.create(this.position, "access.field.unqualified", this.name));
				this.instance = new ThisValue(this.position, this.field.getTheClass().getType());
			}
			
			byte access = context.getAccessibility(this.field);
			if (access == IContext.STATIC)
			{
				markers.add(Markers.create(this.position, "access.field.instance", this.name));
			}
			else if (access == IContext.SEALED)
			{
				markers.add(Markers.create(this.position, "access.field.sealed", this.name));
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(Markers.create(this.position, "access.field.invisible", this.name));
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.field != null && this.field.hasModifier(Modifiers.CONST))
		{
			IValue v = this.field.getValue();
			return v != null && v.isConstant() ? v : this;
		}
		if (this.instance != null)
		{
			this.instance = this.instance.foldConstants();
		}
		return this;
	}
	
	private transient IValue	replacement;
	
	@Override
	public boolean resolve(IContext context, List<Marker> markers)
	{
		IField field = IAccess.resolveField(context, this.instance, this.qualifiedName);
		if (field != null)
		{
			if (field.isEnumConstant())
			{
				EnumValue enumValue = new EnumValue(this.position);
				enumValue.name = this.name;
				enumValue.qualifiedName = this.qualifiedName;
				enumValue.type = field.getType();
				this.replacement = enumValue;
				return false;
			}
			
			this.field = field;
			return true;
		}
		return false;
	}
	
	@Override
	public IValue resolve2(IContext context)
	{
		if (this.replacement != null)
		{
			return this.replacement;
		}
		
		IMethod method = IAccess.resolveMethod(context, this.instance, this.qualifiedName, Type.EMPTY_TYPES);
		if (method != null)
		{
			return this.toMethodCall(method);
		}
		
		return null;
	}
	
	public MethodCall toMethodCall(IMethod method)
	{
		MethodCall call = new MethodCall(this.position);
		call.instance = this.instance;
		call.name = this.name;
		call.qualifiedName = this.qualifiedName;
		call.method = method;
		call.dotless = this.dotless;
		call.isSugarCall = true;
		return call;
	}
	
	@Override
	public IAccess resolve3(IContext context, IAccess next)
	{
		IMethod method = IAccess.resolveMethod(context, this.instance, this.qualifiedName, new SingleElementList(next));
		if (method != null)
		{
			MethodCall call = new MethodCall(this.position, this.instance, this.name);
			call.addValue(next);
			call.method = method;
			call.dotless = this.dotless;
			call.isSugarCall = true;
			return call;
		}
		
		return null;
	}
	
	@Override
	public Marker getResolveError()
	{
		Marker marker = Markers.create(this.position, "resolve.method_field");
		marker.addInfo("Qualified Name: " + this.qualifiedName);
		marker.addInfo("Instance Type: " + this.instance.getType());
		return marker;
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
