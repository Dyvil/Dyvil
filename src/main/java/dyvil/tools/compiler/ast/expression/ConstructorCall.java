package dyvil.tools.compiler.ast.expression;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
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
	protected IType		type;
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
	public IAccess applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.type = this.type.resolve(context);
		}
		else if (state == CompilerState.RESOLVE)
		{
			Util.applyState(this.arguments, state, context);
			if (!this.resolve(context, null))
			{
				state.addMarker(new SemanticError(this.position, "The constructor could not be resolved"));
			}
			return this;
		}
		else if (state == CompilerState.CHECK)
		{
			IClass iclass = this.type.getTheClass();
			if (iclass.hasModifier(Modifiers.INTERFACE_CLASS))
			{
				state.addMarker(new SemanticError(this.position, "The interface '" + iclass.getName() + "' cannot be instantiated"));
			}
			else if (iclass.hasModifier(Modifiers.ABSTRACT))
			{
				state.addMarker(new SemanticError(this.position, "The abstract class '" + iclass.getName() + "' cannot be instantiated"));
			}
			else if (this.method != null)
			{
				this.method.checkArguments(state, null, this.arguments);
				
				byte access = context.getAccessibility(this.method);
				if ((access & IContext.READ_ACCESS) == 0)
				{
					state.addMarker(new SemanticError(this.position, "The constructor cannot be invoked since it is not visible"));
				}
			}
		}
		
		Util.applyState(this.arguments, state, context);
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
		if (this.isCustom)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKESPECIAL;
			
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
		writer.visitMethodInsn(opcode, owner, name, desc, false);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label label)
	{
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
