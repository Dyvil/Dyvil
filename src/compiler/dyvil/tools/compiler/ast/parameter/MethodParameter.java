package dyvil.tools.compiler.ast.parameter;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class MethodParameter extends Parameter
{
	public ICallableMember	method;
	
	public MethodParameter()
	{
	}
	
	public MethodParameter(Name name)
	{
		this.name = name;
	}
	
	public MethodParameter(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}
	
	@Override
	public boolean isField()
	{
		return false;
	}
	
	@Override
	public boolean isVariable()
	{
		return true;
	}
	
	@Override
	public void setMethod(ICallableMember method)
	{
		this.method = method;
	}
	
	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		return instance;
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue)
	{
		if ((this.modifiers & Modifiers.FINAL) != 0)
		{
			markers.add(position, "parameter.assign.final", this.name.unqualified);
		}
		
		IValue value1 = newValue.withType(this.type, null, markers, context);
		if (value1 == null)
		{
			Marker marker = markers.create(newValue.getPosition(), "parameter.assign.type", this.name.unqualified);
			marker.addInfo("Parameter Type: " + this.type);
			marker.addInfo("Value Type: " + newValue.getType());
		}
		else
		{
			newValue = value1;
		}
		
		return newValue;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.resolve(markers, context);
			
			IValue value1 = this.defaultValue.withType(this.type, null, markers, context);
			if (value1 == null)
			{
				Marker marker = markers.create(this.defaultValue.getPosition(), "parameter.type", this.name.unqualified);
				marker.addInfo("Parameter Type: " + this.type);
				marker.addInfo("Value Type: " + this.defaultValue.getType());
			}
			else
			{
				this.defaultValue = value1;
			}
			return;
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue.check(markers, context);
		}
		
		if (this.type == Types.VOID)
		{
			markers.add(this.position, "parameter.type.void");
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (this.defaultValue == null)
		{
			return;
		}
		
		// Copy the access modifiers and add the STATIC modifier
		int modifiers = this.method.getModifiers() & Modifiers.ACCESS_MODIFIERS | Modifiers.STATIC;
		String name = "parDefault$" + this.method.getName().qualified + "$" + this.index;
		String desc = "()" + this.type.getExtendedName();
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, name, desc, null, null));
		mw.begin();
		this.defaultValue.writeExpression(mw);
		mw.end(this.type);
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		writer.registerParameter(this.index, this.name.qualified, this.type, 0);
		
		if ((this.modifiers & Modifiers.VAR) != 0)
		{
			writer.addParameterAnnotation(this.index, "Ldyvil/annotation/var;", true);
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(writer, this.index);
		}
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance) throws BytecodeException
	{
		writer.writeVarInsn(this.type.getLoadOpcode(), this.index + writer.inlineOffset());
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value) throws BytecodeException
	{
		if (value != null)
		{
			value.writeExpression(writer);
		}
		writer.writeVarInsn(this.type.getStoreOpcode(), this.index + writer.inlineOffset());
	}
}
