package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.constructor.IInitializer;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;

public class TraitMetadata extends InterfaceMetadata
{
	public TraitMetadata(IClass theClass)
	{
		super(theClass);
	}

	@Override
	protected void processInitializer(IInitializer initializer, MarkerList markers)
	{
		// No error
	}

	@Override
	protected void processField(IField field, MarkerList markers)
	{
		this.processMember(field, markers);

		if (!field.hasModifier(Modifiers.STATIC) || !field.hasModifier(Modifiers.FINAL))
		{
			field.getModifiers().addIntModifier(Modifiers.STATIC | Modifiers.FINAL);
			markers.add(Markers.semanticWarning(field.getPosition(), "trait.field.warning", field.getName()));
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final String internalName = this.theClass.getInternalName();
		final MethodWriter initWriter = new MethodWriterImpl(writer, writer.visitMethod(
			Modifiers.PUBLIC | Modifiers.STATIC, "<traitinit>", "(L" + internalName + ";)V", null, null));

		initWriter.visitCode();
		initWriter.setLocalType(0, internalName);

		this.theClass.writeInit(initWriter);

		initWriter.visitInsn(Opcodes.RETURN);
		initWriter.visitEnd();
	}
}
