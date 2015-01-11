package dyvil.tools.compiler.ast.expression;

import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public class ConstructorCall extends Call implements ITyped
{
	public IType		type;
	protected boolean	isCustom;
	
	public ConstructorCall(ICodePosition position)
	{
		super(position);
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public int getValueType()
	{
		return CONSTRUCTOR_CALL;
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
	}
	
	@Override
	public void setName(String name)
	{
	}
	
	@Override
	public String getName()
	{
		return "new";
	}
	
	@Override
	public void setQualifiedName(String name)
	{
	}
	
	@Override
	public String getQualifiedName()
	{
		return "<init>";
	}
	
	@Override
	public boolean isName(String name)
	{
		return "<init>".equals(name);
	}
	
	@Override
	public void setValue(IValue value)
	{
	}
	
	@Override
	public IValue getValue()
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
		this.type = this.type.resolve(context);
		if (!this.type.isResolved())
		{
			markers.add(new SemanticError(this.type.getPosition(), "'" + this.type + "' could not be resolved to a type"));
		}
		
		for (IValue v : this.arguments)
		{
			v.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		int len = this.arguments.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.arguments.get(i);
			IValue v2 = v1.resolve(markers, context);
			if (v1 != v2)
			{
				this.arguments.set(i, v2);
			}
		}
		
		if (!this.resolve(context, null))
		{
			markers.add(new SemanticError(this.position, "The constructor could not be resolved"));
		}
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		for (IValue v : this.arguments)
		{
			v.check(markers, context);
		}
		
		IClass iclass = this.type.getTheClass();
		if (iclass.hasModifier(Modifiers.INTERFACE_CLASS))
		{
			markers.add(new SemanticError(this.position, "The interface '" + iclass.getName() + "' cannot be instantiated"));
		}
		else if (iclass.hasModifier(Modifiers.ABSTRACT))
		{
			markers.add(new SemanticError(this.position, "The abstract class '" + iclass.getName() + "' cannot be instantiated"));
		}
		else if (this.method != null)
		{
			this.method.checkArguments(markers, null, this.arguments);
			
			byte access = context.getAccessibility(this.method);
			if (access == IContext.SEALED)
			{
				markers.add(new SemanticError(this.position, "The sealed constructor cannot be invoked because it is private to it's library"));
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(new SemanticError(this.position, "The constructor cannot be invoked because it is not visible"));
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		int len = this.arguments.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.arguments.get(i);
			IValue v2 = v1.foldConstants();
			if (v1 != v2)
			{
				this.arguments.set(i, v2);
			}
		}
		return this;
	}
	
	@Override
	public boolean resolve(IContext context, IContext context1)
	{
		if (!this.type.isResolved())
		{
			return false;
		}
		
		MethodMatch match = this.type.resolveMethod(context1, "new", this.getTypes());
		if (match != null)
		{
			this.method = match.theMethod;
			this.isCustom = true;
			return true;
		}
		
		match = this.type.resolveMethod(context1, "<init>", this.getTypes());
		if (match != null)
		{
			this.method = match.theMethod;
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
		if (!this.type.isResolved())
		{
			return new SemanticError(this.type.getPosition(), "'" + this.type + "' could not be resolved to a type");
		}
		return new SemanticError(this.position, "'' could not be resolved to a constructor");
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		int opcode;
		int args = this.arguments.size();
		if (this.isCustom)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKESPECIAL;
			args++;
			
			writer.visitTypeInsn(Opcodes.NEW, this.type);
			writer.visitInsn(Opcodes.DUP, this.type);
		}
		
		for (IValue arg : this.arguments)
		{
			arg.writeExpression(writer);
		}
		
		String owner = this.method.getTheClass().getInternalName();
		String name = this.method.getName();
		String desc = this.method.getDescriptor();
		writer.visitMethodInsn(opcode, owner, name, desc, false, args, null);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString("", buffer);
		if (this.isSugarCall && !Formatting.Method.useJavaFormat)
		{
			this.arguments.get(0).toString("", buffer);
		}
		else
		{
			Util.parametersToString(this.arguments, buffer, true);
		}
	}
}
