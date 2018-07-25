package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.classes.ClassBody;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
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
	public void resolveTypesBody(MarkerList markers, IContext context)
	{
		final ClassBody body = this.theClass.getBody();

		for (IField field : body.fields())
		{
			markers.add(Markers.semanticError(field.getPosition(), "extension.field.invalid"));
		}

		for (IProperty property : body.properties())
		{
			markers.add(Markers.semanticError(property.getPosition(), "extension.property.invalid"));
		}

		final IType superType = this.theClass.getSuperType();
		for (IMethod method : body.methods())
		{
			method.setThisType(superType);
			method.getAttributes().addFlag(Modifiers.EXTENSION);
		}
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
