package dyvil.tools.compiler.ast.statement;

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
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.ThisValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.AccessResolver;
import dyvil.tools.compiler.util.Symbols;

public class FieldAssign extends ASTNode implements INamed, IValued, IAccess
{
	protected String	name;
	protected String	qualifiedName;
	
	public boolean		initializer;
	
	public IValue		instance;
	public IField		field;
	protected IValue	value;
	
	public FieldAssign(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAssign(ICodePosition position, String name, IValue instance)
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
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public void setValues(List<IValue> list)
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
	public void addValue(IValue value)
	{}
	
	@Override
	public void setValue(int index, IValue value)
	{}
	
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
		if (state == CompilerState.RESOLVE_TYPES)
		{
			if (this.initializer)
			{
				this.field.applyState(state, context);
			}
		}
		else if (state == CompilerState.RESOLVE)
		{
			this.value.applyState(state, context);
			return AccessResolver.resolve(context, this);
		}
		else if (state == CompilerState.CHECK)
		{
			if (this.value instanceof ThisValue)
			{
				state.addMarker(new SyntaxError(this.position, "Cannot assign a value to 'this'"));
				this.value = null;
			}
		}
		
		if (this.value != null)
		{
			this.value = this.value.applyState(state, context);
		}
		return this;
	}
	
	@Override
	public boolean resolve(IContext context, IContext context1)
	{
		if (this.field != null)
		{
			return true;
		}
		
		FieldMatch f = context1.resolveField(null, this.qualifiedName);
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
		if (this.value != null)
		{
			if (this.instance != null)
			{
				this.instance.write(visitor);
			}
			
			this.value.write(visitor);
			
			this.field.writeSet(visitor);
		}
	}
	
	@Override
	public void writeJump(MethodVisitor visitor, Label label)
	{
		// TODO
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.initializer)
		{
			this.field.toString("", buffer);
		}
		else
		{
			if (this.instance != null)
			{
				this.instance.toString("", buffer);
				buffer.append('.');
			}
			
			buffer.append(this.name);
		}
		
		if (this.value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			this.value.toString("", buffer);
		}
	}
}
