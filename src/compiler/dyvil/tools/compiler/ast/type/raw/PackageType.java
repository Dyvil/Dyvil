package dyvil.tools.compiler.ast.type.raw;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.structure.RootPackage;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PackageType implements IRawType
{
	private Package thePackage;

	public PackageType()
	{
	}

	public PackageType(Package thePackage)
	{
		this.thePackage = thePackage;
	}

	@Override
	public int typeTag()
	{
		return PACKAGE;
	}

	@Override
	public Name getName()
	{
		return this.thePackage.getName();
	}

	@Override
	public IClass getTheClass()
	{
		return null;
	}

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
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public IClass resolveClass(Name name)
	{
		return this.thePackage.resolveClass(name);
	}

	@Override
	public Package resolvePackage(Name name)
	{
		return this.thePackage.resolvePackage(name);
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments)
	{
	}

	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}

	@Override
	public String getInternalName()
	{
		return this.thePackage.getInternalName();
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitLdcInsn(this.thePackage.getInternalName());
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/Type", "apply",
		                       "(Ljava/lang/String;)Ldyvilx/lang/model/type/Type;", true);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.thePackage.getInternalName());
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		final String internal = in.readUTF();
		this.thePackage = RootPackage.rootPackage.resolvePackageInternal(internal);
	}

	@Override
	public String toString()
	{
		return this.thePackage.getName().toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.thePackage.getName());
	}

	@Override
	public IType clone()
	{
		return new PackageType(this.thePackage);
	}
}
