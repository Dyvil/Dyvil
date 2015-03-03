package dyvil.tools.compiler.ast.access;

import java.util.List;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.ast.value.ThisValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.AccessResolver;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.Util;

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
	public int getValueType()
	{
		return FIELD_ACCESS;
	}
	
	@Override
	public IType getType()
	{
		return this.field == null ? Type.NONE : this.field.getType();
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.field == null ? false : Type.isSuperType(type, this.field.getType());
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.field == null)
		{
			return 0;
		}
		
		IType type1 = this.field.getType();
		if (type.equals(type1))
		{
			return 3;
		}
		else if (Type.isSuperType(type, type1))
		{
			return 2;
		}
		return 0;
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
	public void setArguments(IArguments arguments)
	{
	}
	
	@Override
	public IArguments getArguments()
	{
		return Util.EMPTY_VALUES;
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
				if (this.instance != null && this.instance.getValueType() != CLASS_ACCESS)
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
			
			if (this.field.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(Markers.create(this.position, "access.field.deprecated", this.name));
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
	public boolean isResolved()
	{
		if (this.field == null)
		{
			return false;
		}
		
		if (this.instance != null && !(this.field instanceof Field))
		{
			return false;
		}
		return true;
	}
	
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
			
			if (this.instance != null && !(field instanceof Field))
			{
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
		
		IMethod method = IAccess.resolveMethod(context, this.instance, this.qualifiedName, Util.EMPTY_VALUES);
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
		call.arguments = Util.EMPTY_VALUES;
		return call;
	}
	
	@Override
	public IAccess resolve3(IContext context, IAccess next)
	{
		IArguments arguments = new SingleArgument(next);
		IMethod method = IAccess.resolveMethod(context, this.instance, this.qualifiedName, arguments);
		if (method != null)
		{
			MethodCall call = new MethodCall(this.position);
			call.instance = this.instance;
			call.name = this.name;
			call.qualifiedName = this.qualifiedName;
			call.method = method;
			call.dotless = this.dotless;
			call.arguments = arguments;
			return call;
		}
		
		return null;
	}
	
	@Override
	public Marker getResolveError()
	{
		Marker marker = Markers.create(this.position, "resolve.method_field", this.name);
		marker.addInfo("Qualified Name: " + this.qualifiedName);
		if (this.instance != null)
		{
			marker.addInfo("Instance Type: " + this.instance.getType());
		}
		return marker;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.field.writeGet(writer, this.instance);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.field.writeGet(writer, this.instance);
		writer.visitInsn(this.field.getType().getReturnOpcode());
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
