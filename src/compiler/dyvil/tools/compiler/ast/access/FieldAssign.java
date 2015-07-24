package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class FieldAssign extends ASTNode implements IValue, INamed, IValued
{
	public Name name;
	
	public IValue	instance;
	public IValue	value;
	
	public IDataMember field;
	
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
	public int valueTag()
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
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.VOID)
		{
			return this;
		}
		
		IValue value1 = this.value.withType(type, typeContext, markers, context);
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
		return type == Types.VOID || this.value.isType(type);
	}
	
	@Override
	public float getTypeMatch(IType type)
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
		
		if (!ICall.privateAccess(context, this.instance))
		{
			Name name = Name.getQualified(this.name.qualified + "_$eq");
			IArguments arg = new SingleArgument(this.value);
			IMethod m = ICall.resolveMethod(context, this.instance, name, arg);
			if (m != null)
			{
				MethodCall mc = new MethodCall(this.position, this.instance, name);
				mc.arguments = arg;
				mc.method = m;
				mc.checkArguments(markers, context);
				return mc;
			}
		}
		
		this.field = ICall.resolveField(context, this.instance, this.name);
		
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
		
		if (this.field != null)
		{
			this.field = this.field.capture(context);
			this.instance = this.field.checkAccess(markers, this.position, this.instance, context);
			this.value = this.field.checkAssign(markers, context, this.position, this.instance, this.value);
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
		
		this.value.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.instance != null)
		{
			this.instance = this.instance.foldConstants();
		}
		this.value = this.value.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.cleanup(context, compilableList);
		}
		this.value = this.value.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		if (this.value == null)
		{
			return;
		}
		
		if (this.instance == null)
		{
			this.value.writeExpression(writer);
			writer.writeInsn(Opcodes.AUTO_DUP);
		}
		else
		{
			this.instance.writeExpression(writer);
			this.value.writeExpression(writer);
			writer.writeInsn(Opcodes.AUTO_DUP_X1);
		}
		this.field.writeSet(writer, null, null, this.getLineNumber());
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		if (this.value == null)
		{
			return;
		}
		
		this.field.writeSet(writer, this.instance, this.value, this.getLineNumber());
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
			this.value.toString(prefix, buffer);
		}
	}
}
