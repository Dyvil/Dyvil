package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class FieldAccess extends ASTNode implements IValue, INamed, IValued
{
	public IValue		instance;
	public Name			name;
	
	public boolean		dotless;
	
	public IDataMember	field;
	protected IType		type;
	
	public FieldAccess(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAccess(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
	}
	
	@Override
	public int valueTag()
	{
		return FIELD_ACCESS;
	}
	
	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			if (this.field == null)
			{
				return Types.UNKNOWN;
			}
			if (this.instance == null)
			{
				return this.type = this.field.getType();
			}
			return this.type = this.field.getType().getConcreteType(this.instance.getType()).getReturnType();
		}
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return IValue.autoBox(this, this.getType(), type);
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.field == null ? false : type.isSuperTypeOf(this.getType());
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.field == null)
		{
			return 0;
		}
		
		IType type1 = this.getType();
		if (type.equals(type1))
		{
			return 3;
		}
		if (type.isSuperTypeOf(type1))
		{
			return 2;
		}
		return 0;
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
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
	}
	
	protected IValue resolveFieldAccess(MarkerList markers, IContext context)
	{
		IDataMember field = ICall.resolveField(context, this.instance, this.name);
		if (field != null)
		{
			if (field.isEnumConstant())
			{
				EnumValue enumValue = new EnumValue(this.position);
				enumValue.name = this.name;
				enumValue.type = field.getType();
				return enumValue;
			}
			
			this.field = field;
			return this;
		}
		
		IMethod method = ICall.resolveMethod(context, this.instance, this.name, EmptyArguments.INSTANCE);
		if (method != null)
		{
			AbstractCall mc = this.toMethodCall(method);
			mc.checkArguments(markers, context);
			return mc;
		}
		
		if (this.instance == null)
		{
			IClass iclass = IContext.resolveClass(context, this.name);
			if (iclass != null)
			{
				return new ClassAccess(this.position, iclass.getType());
			}
		}
		
		return null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		
		IValue v = this.resolveFieldAccess(markers, context);
		if (v != null)
		{
			return v;
		}
		
		Marker marker = markers.create(this.position, "resolve.method_field", this.name.unqualified);
		marker.addInfo("Qualified Name: " + this.name.qualified);
		if (this.instance != null)
		{
			marker.addInfo("Instance Type: " + this.instance.getType());
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
		
		if (this.field != null)
		{
			this.instance = this.field.checkAccess(markers, this.position, this.instance, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		if (this.field != null)
		{
			this.field.checkAccess(markers, this.position, instance, context);
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
	
	public MethodCall toMethodCall(IMethod method)
	{
		MethodCall call = new MethodCall(this.position);
		call.instance = this.instance;
		call.name = this.name;
		call.method = method;
		call.dotless = this.dotless;
		call.arguments = EmptyArguments.INSTANCE;
		return call;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.field.writeGet(writer, this.instance);
		
		if (!this.type.isSuperTypeOf(this.field.getType()))
		{
			writer.writeTypeInsn(Opcodes.CHECKCAST, this.type.getInternalName());
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(this.field.getType().getReturnOpcode());
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
		
		buffer.append(this.name);
	}
}
