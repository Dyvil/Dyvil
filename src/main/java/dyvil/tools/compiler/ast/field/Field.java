package dyvil.tools.compiler.ast.field;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.annotation.Annotation;
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
		return this.type.getExtendedName();
	}
	
	@Override
	public String getSignature()
	{
		return this.type.getSignature();
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		if ("dyvil.lang.annotation.lazy".equals(annotation.name))
		{
			this.modifiers |= Modifiers.LAZY;
		}
		else
		{
			this.annotations.add(annotation);
		}
	}
	
	@Override
	public Field applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.type = this.type.applyState(state, context);
		}
		
		if (this.value != null)
		{
			this.value = this.value.applyState(state, context);
		}
		return this;
	}
	
	@Override
	public void write(ClassWriter writer)
	{
		writer.visitField(this.modifiers & 0xFFFF, this.name, this.getDescription(), this.type.getSignature(), null);
		
		if ((this.modifiers & Modifiers.LAZY) != 0)
		{
			writer.visitAnnotation("Ldyvil/lang/annotation/lazy;", true);
		}
	}
	
	@Override
	public void writeGet(MethodVisitor visitor)
	{
		int opcode;
		if ((this.modifiers & Modifiers.STATIC) == Modifiers.STATIC)
		{
			opcode = Opcodes.GETSTATIC;
		}
		else
		{
			opcode = Opcodes.GETFIELD;
		}
		
		String owner = this.getTheClass().getInternalName();
		String name = this.name;
		String desc = this.type.getExtendedName();
		visitor.visitFieldInsn(opcode, owner, name, desc);
	}
	
	@Override
	public void writeSet(MethodVisitor visitor)
	{
		int opcode;
		if ((this.modifiers & Modifiers.STATIC) == Modifiers.STATIC)
		{
			opcode = Opcodes.PUTSTATIC;
		}
		else
		{
			opcode = Opcodes.PUTFIELD;
		}
		
		String owner = this.getTheClass().getInternalName();
		String name = this.name;
		String desc = this.type.getExtendedName();
		visitor.visitFieldInsn(opcode, owner, name, desc);
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
