package dyvilx.tools.compiler.ast.type.raw;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.structure.RootPackage;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
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
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		buffer.append('L').append(this.getInternalName()).append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitLdcInsn(this.thePackage.getInternalName());
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/Type", "apply",
		                       "(Ljava/lang/String;)Ldyvil/reflect/types/Type;", true);
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
		this.thePackage = RootPackage.rootPackage.resolveGlobalPackage(internal);
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
}
