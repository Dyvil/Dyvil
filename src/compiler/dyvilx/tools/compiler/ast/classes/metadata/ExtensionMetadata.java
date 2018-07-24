package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public class ExtensionMetadata implements IClassMetadata
{
	private final IClass theClass;

	public ExtensionMetadata(IClass theClass)
	{
		this.theClass = theClass;
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.CLASS;
	}

	// Phases

	@Override
	public void resolveTypesHeader(MarkerList markers, IContext context)
	{
		this.theClass.setPosition(this.theClass.getSuperType().getPosition());
		this.theClass.setName(
			Name.fromQualified("extension_" + this.theClass.getSuperType().getInternalName().replace('/', '_')));
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.theClass.setSuperType(Types.OBJECT);
		this.theClass.setTypeParameters(null);
	}

	// Compilation

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{

	}
}
