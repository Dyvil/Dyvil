package dyvil.tools.compiler.ast.expression;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.AccessResolver;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Symbols;
import dyvil.tools.compiler.util.Util;

public class MethodCall extends Call implements INamed, IValued
{
	protected IValue	instance;
	protected String	name;
	protected String	qualifiedName;
	
	public MethodCall(ICodePosition position)
	{
		super(position);
	}
	
	public MethodCall(ICodePosition position, IValue instance, String name)
	{
		super(position);
		this.instance = instance;
		this.name = name;
		this.qualifiedName = Symbols.expand(name);
	}
	
	@Override
	public Type getType()
	{
		return this.method.getType();
	}
	
	@Override
	public void setName(String name)
	{
		this.qualifiedName = name;
	}
	
	@Override
	public String getName()
	{
		return this.qualifiedName;
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.isSugarCall)
		{
			this.arguments.add(value);
		}
		else
		{
			this.instance = value;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	@Override
	public void setIsArray(boolean isArray)
	{}
	
	@Override
	public boolean isArray()
	{
		return false;
	}
	
	@Override
	public IAccess applyState(CompilerState state, IContext context)
	{
		super.applyState(state, context);
		
		if (state == CompilerState.RESOLVE)
		{
			return AccessResolver.resolve(context, this);
		}
		else if (this.instance != null)
		{
			this.instance = this.instance.applyState(state, context);
		}
		
		return this;
	}
	
	@Override
	public boolean resolve(IContext context)
	{
		this.method = context.resolveMethod(this.qualifiedName, this.getTypes());
		return this.method != null;
	}
	
	@Override
	public IAccess resolve2(IContext context)
	{
		if (this.arguments.isEmpty())
		{
			IField field = context.resolveField(this.qualifiedName);
			if (field != null)
			{
				FieldAccess access = new FieldAccess(this.position, this.instance, this.qualifiedName);
				access.field = field;
				return access;
			}
		}
		return this;
	}
	
	@Override
	public Marker getResolveError()
	{
		return new SemanticError(this.position, "'" + this.qualifiedName + "' could not be resolved to a method");
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isSugarCall && !Formatting.Method.convertSugarCalls)
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append(Formatting.Method.sugarCallStart);
			}
			
			if (Formatting.Method.convertQualifiedNames)
			{
				buffer.append(this.qualifiedName);
			}
			else
			{
				buffer.append(this.name);
			}
			
			buffer.append(Formatting.Method.sugarCallEnd);
			this.arguments.get(0).toString("", buffer);
		}
		else
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append('.');
			}
			
			if (Formatting.Method.convertQualifiedNames)
			{
				buffer.append(this.qualifiedName);
			}
			else
			{
				buffer.append(this.name);
			}
			
			Util.parametersToString(this.arguments, buffer, !this.isSugarCall);
		}
	}
	
	@Override
	public void write(MethodVisitor visitor)
	{
		if (this.instance != null)
		{
			this.instance.write(visitor);
		}
		for (IValue arg : this.arguments)
		{
			arg.write(visitor);
		}
		
		// TODO super -> INVOKESPECIAL
		int opcode;
		if (this.method.hasModifier(Modifiers.STATIC))
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else if (this.method.getTheClass().hasModifier(Modifiers.INTERFACE_CLASS))
		{
			opcode = Opcodes.INVOKEINTERFACE;
		}
		else
		{
			opcode = Opcodes.INVOKEVIRTUAL;
		}
		
		String owner = this.method.getTheClass().getInternalName();
		String name = this.method.getName();
		String desc = this.method.getDescriptor();
		visitor.visitMethodInsn(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE);
	}
}
