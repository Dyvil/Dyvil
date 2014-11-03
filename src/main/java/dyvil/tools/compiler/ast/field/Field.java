package dyvil.tools.compiler.ast.field;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Modifiers;

public class Field extends Member implements IField
{
	private IValue	value;
	
	public Field(IClass iclass)
	{
		super(iclass);
	}
	
	public Field(IClass iclass, String name)
	{
		super(iclass, name);
	}
	
	public Field(IClass iclass, String name, Type type)
	{
		super(iclass, name, type);
	}
	
	public Field(IClass iclass, String name, Type type, int modifiers)
	{
		super(iclass, name, type, modifiers);
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
	public String getDescription()
	{
		return this.type.getInternalName();
	}
	
	@Override
	public String getSignature()
	{
		return this.type.getSignature();
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		// TODO static fields initial value
		FieldVisitor visitor = writer.visitField(this.modifiers, this.name, this.getDescription(), this.type.getSignature(), null);
	}
	
	@Override
	public Field applyState(CompilerState state, IContext context)
	{
		this.type = this.type.applyState(state, context);
		
		if (this.value != null)
		{
			this.value = this.value.applyState(state, context);
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		buffer.append(Modifiers.FIELD.toString(this.modifiers));
		this.type.toString("", buffer);
		buffer.append(' ');
		
		if (Formatting.Field.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName);
		}
		else
		{
			buffer.append(this.name);
		}
		
		IValue value = this.value;
		if (value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			value.toString("", buffer);
		}
		buffer.append(';');
	}
}
