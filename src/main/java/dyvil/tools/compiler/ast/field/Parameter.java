package dyvil.tools.compiler.ast.field;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class Parameter extends Member implements IField
{
	public int		index;
	private char	seperator;
	
	public Parameter()
	{
		super(null);
	}
	
	public Parameter(int index, String name, Type type, int modifiers)
	{
		this(index, name, type, modifiers, ',');
	}
	
	public Parameter(int index, String name, Type type, int modifiers, char seperator)
	{
		super(null, name, type, modifiers);
		this.index = index;
		this.seperator = seperator;
	}
	
	@Override
	public void setValue(IValue value)
	{}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	public void setSeperator(char seperator)
	{
		this.seperator = seperator;
	}
	
	public char getSeperator()
	{
		return this.seperator;
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
	public Parameter applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.RESOLVE_TYPES)
		{
			this.type = this.type.resolve(context);
		}
		return this;
	}
	
	@Override
	public void write(ClassWriter writer)
	{}
	
	@Override
	public void writeGet(MethodVisitor visitor)
	{
		visitor.visitVarInsn(Opcodes.ALOAD, this.index);
	}
	
	@Override
	public void writeSet(MethodVisitor visitor)
	{
		visitor.visitVarInsn(Opcodes.ASTORE, this.index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString("", buffer);
		buffer.append(' ').append(this.name);
	}
}
