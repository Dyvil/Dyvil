package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;
import java.util.Iterator;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.IMember;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public class Field extends Member implements IField, IContext
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
	
	public Field(IClass iclass, String name, IType type)
	{
		super(iclass, name, type);
	}
	
	public Field(IClass iclass, String name, IType type, int modifiers, List<Annotation> annotations)
	{
		super(iclass, name, type, modifiers, annotations);
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
		if (!processAnnotation(annotation))
		{
			annotation.target = ElementType.FIELD;
			this.annotations.add(annotation);
		}
	}
	
	private boolean processAnnotation(Annotation annotation)
	{
		String name = annotation.type.qualifiedName;
		if ("dyvil.lang.annotation.lazy".equals(name))
		{
			this.modifiers |= Modifiers.LAZY;
			return true;
		}
		return false;
	}
	
	@Override
	public Field applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.type = this.type.applyState(state, context);
		}
		else if (state == CompilerState.RESOLVE)
		{
			if (this.value != null)
			{
				this.value = this.value.applyState(state, this);
			}
			
			for (Iterator<Annotation> iterator = this.annotations.iterator(); iterator.hasNext();)
			{
				Annotation a = iterator.next();
				if (this.processAnnotation(a))
				{
					iterator.remove();
					continue;
				}
				
				a.applyState(state, context);
			}
			return this;
		}
		else if (state == CompilerState.CHECK)
		{
			if (this.value != null && !this.value.requireType(this.type))
			{
				state.addMarker(new SemanticError(this.value.getPosition(), "The value of the field '" + this.name + "' is incompatible with the field type " + this.type));
			}
		}
		
		if (this.value != null)
		{
			this.value = this.value.applyState(state, this);
		}
		Util.applyState(this.annotations, state, context);
		return this;
	}
	
	@Override
	public boolean isStatic()
	{
		return (this.modifiers & Modifiers.STATIC) != 0;
	}
	
	@Override
	public IType getThisType()
	{
		return this.theClass.getThisType();
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.theClass.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
	{
		if (name.equals(this.name))
		{
			return new FieldMatch(this, 1);
		}
		return this.theClass.resolveField(context, name);
	}
	
	@Override
	public MethodMatch resolveMethod(IContext returnType, String name, IType... argumentTypes)
	{
		return this.theClass.resolveMethod(returnType, name, argumentTypes);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		IClass iclass = member.getTheClass();
		if (iclass == null)
		{
			return READ_WRITE_ACCESS;
		}
		if ((this.modifiers & Modifiers.STATIC) != 0 && iclass == this.theClass && !member.hasModifier(Modifiers.STATIC))
		{
			return STATIC;
		}
		return this.theClass.getAccessibility(member);
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
	public void writeGet(MethodWriter writer)
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
		
		String owner = this.theClass.getInternalName();
		String name = this.name;
		String desc = this.type.getExtendedName();
		writer.visitFieldInsn(opcode, owner, name, desc, this.type);
	}
	
	@Override
	public void writeSet(MethodWriter writer)
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
		
		String owner = this.theClass.getInternalName();
		String name = this.name;
		String desc = this.type.getExtendedName();
		writer.visitFieldInsn(opcode, owner, name, desc, this.type);
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
