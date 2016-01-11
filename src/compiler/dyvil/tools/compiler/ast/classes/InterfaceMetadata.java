package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

public class InterfaceMetadata implements IClassMetadata
{
	private final IClass theClass;

	public InterfaceMetadata(IClass theClass)
	{
		this.theClass = theClass;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
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

		// Make fields public and static
		for (int i = 0, count = classBody.fieldCount(); i < count; i++)
		{
			processField(classBody.getField(i));
		}

		// Make non-static methods public and abstract
		for (int i = 0, count = classBody.methodCount(); i < count; i++)
		{
			processMethod(classBody.getMethod(i));
		}
	}

	protected static void processMethod(IMethod method)
	{
		if (!method.hasModifier(Modifiers.STATIC))
		{
			method.getModifiers().addIntModifier(Modifiers.PUBLIC | Modifiers.ABSTRACT);
		}
	}

	protected static void processField(IField field)
	{
		if (field.isField())
		{
			field.getModifiers().addIntModifier(Modifiers.PUBLIC | Modifiers.STATIC | Modifiers.FINAL);
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}
}
