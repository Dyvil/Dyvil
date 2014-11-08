package dyvil.tools.compiler.ast.expression;

import java.util.Collections;
import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.ThisValue;
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
	protected IValue	instance;
	protected String	name;
	protected String	qualifiedName;
	
	protected boolean	dotless;
	
	public IField		field;
	
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
	public Type getType()
	{
		return this.field.getType();
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
	{}

	@Override
	public void setValue(int index, IValue value)
	{}

	@Override
	public void addValue(IValue value)
	{}

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
	{}

	@Override
	public boolean isArray()
	{
		return false;
	}

	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE)
		{
			return AccessResolver.resolve(context, this);
		}
		else if (state == CompilerState.CHECK)
		{
			if (this.field.hasModifier(Modifiers.STATIC) && this.instance instanceof ThisValue)
			{
				state.addMarker(new Warning(this.position, "'" + this.qualifiedName + "' is a static field and should be accessed in a static way"));
				this.instance = null;
			}
		}
		else if (this.instance != null)
		{
			this.instance = this.instance.applyState(state, context);
		}
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
			MethodCall call = new MethodCall(this.position, this.instance, this.qualifiedName);
			call.method = match.theMethod;
			call.isSugarCall = true;
			return call;
		}
		return this;
	}
	
	@Override
	public Marker getResolveError()
	{
		return new SemanticError(this.position, "'" + this.qualifiedName + "' could not be resolved to a field");
	}
	
	@Override
	public void write(MethodVisitor visitor)
	{
		if (this.instance != null)
		{
			this.instance.write(visitor);
		}
		
		this.field.writeGet(visitor);
	}
	
	@Override
	public void writeJump(MethodVisitor visitor, Label label)
	{
		// TODO
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.dotless && !Formatting.Field.convertSugarAccess)
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append(Formatting.Field.sugarAccessStart);
			}
			
			buffer.append(this.qualifiedName);
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
		}
	}
}
