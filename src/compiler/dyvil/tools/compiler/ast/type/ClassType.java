package dyvil.tools.compiler.ast.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.ast.structure.Package;

public class ClassType implements IType
{
	public IClass	theClass;
	
	public ClassType()
	{
		super();
	}
	public ClassType(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public int typeTag()
	{
		return CLASS;
	}
	
	// Names
	
	@Override
	public Name getName()
	{
		return this.theClass.getName();
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	// Super Type
	
	@Override
	public boolean equals(IType type)
	{
		return this.theClass == type.getTheClass();
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		return this.theClass == type.getTheClass();
	}
	
	// Resolve
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
	{
		return this;
	}
	
	// IContext
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return this.theClass == null ? null : this.theClass.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.theClass != null)
		{
			this.theClass.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		if (this.theClass != null)
		{
			this.theClass.getConstructorMatches(list, arguments);
		}
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return this.theClass == null ? null : this.theClass.getFunctionalMethod();
	}
	
	// Compilation
	
	@Override
	public String getInternalName()
	{
		return this.theClass.getInternalName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.theClass.getInternalName()).append(';');
	}
	
	@Override
	public String getSignature()
	{
		return null;
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append('L').append(this.theClass.getInternalName()).append(';');
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.theClass.getFullName());
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/lang/Type", "apply", "(Ljava/lang/String;)Ldyvil/lang/Type;", true);
	}
	
	@Override
	public void write(DataOutputStream dos) throws IOException
	{
		dos.writeUTF(this.theClass.getInternalName());
	}
	
	@Override
	public void read(DataInputStream dis) throws IOException
	{
		String internal = dis.readUTF();
		this.theClass = Package.rootPackage.resolveInternalClass(internal);
	}
	
	// Misc
	
	@Override
	public String toString()
	{
		return this.theClass.getFullName();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.theClass.getName());
	}
	
	@Override
	public ClassType clone()
	{
		ClassType t = new ClassType();
		t.theClass = this.theClass;
		return t;
	}
}
