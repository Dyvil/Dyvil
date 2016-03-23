package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.BaseModifiers;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class InterfaceMetadata implements IClassMetadata
{
	private final IClass theClass;

	public InterfaceMetadata(IClass theClass)
	{
		this.theClass = theClass;
	}

	@Override
	public void resolveTypesHeader(MarkerList markers, IContext context)
	{
		if (this.theClass instanceof ExternalClass)
		{
			return;
		}

		final IClassBody classBody = this.theClass.getBody();
		if (classBody == null)
		{
			return;
		}

		for (int i = 0, count = classBody.fieldCount(); i < count; i++)
		{
			this.processField(classBody.getField(i), markers);
		}

		for (int i = 0, count = classBody.methodCount(); i < count; i++)
		{
			this.processMethod(classBody.getMethod(i), markers);
		}

		for (int i = 0, count = classBody.propertyCount(); i < count; i++)
		{
			this.processProperty(classBody.getProperty(i), markers);
		}
	}

	protected void processMember(IMember member, MarkerList markers)
	{
		if (!member.hasModifier(Modifiers.PUBLIC))
		{
			// Make all members public
			member.getModifiers().addIntModifier(Modifiers.PUBLIC);
		}
		else if (member.getModifiers().hasModifier(BaseModifiers.PUBLIC))
		{
			markers.add(Markers.semantic(member.getPosition(), "interface.member.public", member.getName()));
		}
	}

	protected void processMethod(IMethod method, MarkerList markers)
	{
		this.processMember(method, markers);

		if (!method.hasModifier(Modifiers.STATIC) && method.getValue() == null)
		{
			// Make methods without an implementation abstract
			method.getModifiers().addIntModifier(Modifiers.ABSTRACT);
		}
	}

	protected void processField(IField field, MarkerList markers)
	{
		this.processMember(field, markers);

		if (field.isField())
		{
			field.getModifiers().addIntModifier(Modifiers.STATIC | Modifiers.FINAL);
		}
	}

	protected void processProperty(IProperty property, MarkerList markers)
	{
		this.processMember(property, markers);

		final IMethod getter = property.getGetter();
		if (getter != null)
		{
			this.processMethod(getter, markers);
		}

		final IMethod setter = property.getGetter();
		if (setter != null)
		{
			this.processMethod(setter, markers);
		}

		final ICodePosition initializerPosition = property.getInitializerPosition();
		if (initializerPosition != null)
		{
			this.processPropertyInitializer(initializerPosition, markers);
		}
	}

	protected void processPropertyInitializer(ICodePosition position, MarkerList markers)
	{
		markers.add(Markers.semanticError(position, "interface.property.initializer"));
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}
}
