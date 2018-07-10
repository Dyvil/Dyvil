package dyvilx.tools.compiler.ast.classes.metadata;

import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeParametric;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class ExtensionMetadata implements IClassMetadata, ITypeParametric
{
	private IType extendedType;

	public IType getExtendedType()
	{
		return this.extendedType;
	}

	public void setExtendedType(IType type)
	{
		this.extendedType = type;
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.CLASS;
	}

	// Type Parametric

	@Override
	public boolean isTypeParametric()
	{
		return false;
	}

	@Override
	public TypeParameterList getTypeParameters()
	{
		return null;
	}

	@Override
	public IContext getTypeParameterContext()
	{
		return null;
	}

	// Compilation

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{

	}
}
