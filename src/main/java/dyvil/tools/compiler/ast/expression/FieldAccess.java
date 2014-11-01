package dyvil.tools.compiler.ast.expression;

import java.util.Collections;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.method.IMethod;
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

public class FieldAccess extends ASTObject implements IValue, INamed, IValued, IAccess
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
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
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
	public boolean resolve(IContext context)
	{
		this.field = context.resolveField(this.qualifiedName);
		return this.field != null;
	}
	
	@Override
	public IAccess resolve2(IContext context)
	{
		IMethod method = context.resolveMethod(this.qualifiedName, Type.EMPTY_TYPES);
		if (method != null)
		{
			MethodCall call = new MethodCall(this.position, this.instance, this.qualifiedName);
			call.method = method;
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
	
	@Override
	public void setValues(List<IValue> list)
	{}
	
	@Override
	public void setIsArray(boolean isArray)
	{}
	
	@Override
	public boolean isArray()
	{
		return false;
	}
	
	@Override
	public List<IValue> getValues()
	{
		return Collections.EMPTY_LIST;
	}
}
