package dyvil.tools.compiler.ast.type;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ClassType implements IRawType
{
	public IClass theClass;
	
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
	public boolean isSameType(IType type)
	{
		return this.theClass == type.getTheClass() && this.isPrimitive() == type.isPrimitive();
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		return this.theClass == type.getTheClass() && !type.isPrimitive();
	}
	
	// Resolve
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		IClass iclass = this.theClass;
		if (iclass != null)
		{
			Deprecation.checkAnnotations(markers, this.getPosition(), iclass, "type");

			if (IContext.getVisibility(context, iclass) == IContext.INTERNAL)
			{
				markers.add(Markers.semantic(this.getPosition(), "type.access.internal", iclass.getName()));
			}
		}
	}
	
	// IContext
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return this.theClass == null ? null : this.theClass.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		if (this.theClass != null)
		{
			this.theClass.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
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
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/Type", "apply",
		                       "(Ljava/lang/String;)Ldyvilx/lang/model/type/Type;", true);
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.theClass.getInternalName());
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		String internal = in.readUTF();
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
	
	@Override
	public boolean equals(Object obj)
	{
		return this.isSameType((IType) obj);
	}
}
