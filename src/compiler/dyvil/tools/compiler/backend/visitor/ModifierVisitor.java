package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;

public class ModifierVisitor implements AnnotationVisitor
{
	private final ModifierSet modifierSet;

	public ModifierVisitor(ModifierSet modifierSet)
	{
		this.modifierSet = modifierSet;
	}

	@Override
	public void visit(String name, Object value)
	{
		if ("value".equals(name) && value instanceof Integer)
		{
			this.modifierSet.addIntModifier((int) value);
		}
	}

	@Override
	public void visitEnum(String name, String desc, String value)
	{
	}

	@Override
	public AnnotationVisitor visitAnnotation(String name, String desc)
	{
		return null;
	}

	@Override
	public AnnotationVisitor visitArray(String name)
	{
		return null;
	}

	@Override
	public void visitEnd()
	{
	}
}
