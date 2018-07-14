package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IInitializer;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.method.MethodWriterImpl;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class TraitMetadata extends InterfaceMetadata
{
	public static final String INIT_NAME = "trait$init";

	public TraitMetadata(IClass theClass)
	{
		super(theClass);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.TRAIT;
	}

	@Override
	protected void processInitializer(IInitializer initializer, MarkerList markers)
	{
		// No error
	}

	@Override
	protected void processPropertyInitializer(SourcePosition position, MarkerList markers)
	{
		// No error
	}

	@Override
	protected void processField(IField field, MarkerList markers)
	{
		if (!field.hasModifier(Modifiers.STATIC) || !field.hasModifier(Modifiers.FINAL))
		{
			markers.add(Markers.semantic(field.getPosition(), "trait.field.warning", field.getName()));
		}

		super.processField(field, markers);
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final String internalName = this.theClass.getInternalName();
		final MethodWriter initWriter = new MethodWriterImpl(writer, writer.visitMethod(
			Modifiers.PUBLIC | Modifiers.STATIC, INIT_NAME, "(L" + internalName + ";)V", null, null));

		initWriter.visitCode();
		initWriter.setLocalType(0, internalName);

		this.theClass.writeClassInit(initWriter);

		initWriter.visitInsn(Opcodes.RETURN);
		initWriter.visitEnd();
	}
}
