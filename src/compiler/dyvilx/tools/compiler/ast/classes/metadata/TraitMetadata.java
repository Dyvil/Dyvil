package dyvilx.tools.compiler.ast.classes.metadata;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IInitializer;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.method.MethodWriterImpl;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class TraitMetadata extends InterfaceMetadata
{
	public static final String INIT_NAME = "trait$init";

	public TraitMetadata(IClass theClass)
	{
		super(theClass);
	}

	/**
	 * Returns all inherited traits of the given class that are not also inherited by the super-class, in the order in which they are initialized.
	 *
	 * @param currentClass
	 * 	the class
	 *
	 * @return all inherited traits except those already inherited by the super-class
	 */
	public static Set<IClass> getNewTraits(IClass currentClass)
	{
		final TypeList interfaces = currentClass.getInterfaces();
		if (interfaces.size() <= 0)
		{
			// don't bother with classes that don't implement anything directly
			return Collections.emptySet();
		}

		// collect all directly and transitively inherited traits
		final Set<IClass> interfaceTraits = new LinkedHashSet<>();
		fillTraitsFromInterfaces(currentClass, interfaceTraits);

		if (interfaceTraits.isEmpty())
		{
			// don't bother with classes that don't implement any traits directly
			return Collections.emptySet();
		}

		// collect all directly and transitively inherited traits of the super-class
		final Set<IClass> superClassTraits = new LinkedHashSet<>();
		fillTraitsFromSuperClass(currentClass, superClassTraits);

		// return only those traits that are directly or transitively inherited, except those from the super-class
		interfaceTraits.removeAll(superClassTraits);
		return interfaceTraits;
	}

	/**
	 * Returns all inherited traits of the given class, in the order in which they are initialized.
	 *
	 * @param currentClass
	 * 	the class
	 *
	 * @return all inherited traits
	 */
	public static Set<IClass> getAllTraits(IClass currentClass)
	{
		final Set<IClass> traits = new LinkedHashSet<>();
		fillTraits(currentClass, traits);
		return traits;
	}

	private static void fillTraits(IClass currentClass, Set<IClass> traitClasses)
	{
		fillTraitsFromSuperClass(currentClass, traitClasses);
		fillTraitsFromInterfaces(currentClass, traitClasses);
	}

	private static void fillTraitsFromSuperClass(IClass currentClass, Set<IClass> traitClasses)
	{
		final IType superType = currentClass.getSuperType();
		final IClass superClass;
		if (superType != null && (superClass = superType.getTheClass()) != null)
		{
			fillTraits(superClass, traitClasses);
		}
	}

	private static void fillTraitsFromInterfaces(IClass currentClass, Set<IClass> traitClasses)
	{
		for (IType type : currentClass.getInterfaces())
		{
			final IClass interfaceClass = type.getTheClass();
			if (interfaceClass == null)
			{
				continue;
			}

			fillTraits(interfaceClass, traitClasses);

			if (interfaceClass.hasModifier(Modifiers.TRAIT_CLASS))
			{
				traitClasses.add(interfaceClass);
			}
		}
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
