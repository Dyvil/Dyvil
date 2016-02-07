package dyvil.tools.dpf.converter;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.reflect.Modifiers;
import dyvil.tools.dpf.visitor.*;
import dyvil.tools.parsing.Name;

@DyvilModifiers(Modifiers.OBJECT_CLASS)
public final class IgnoreVisitor implements NodeVisitor, ValueVisitor, ListVisitor, MapVisitor, StringInterpolationVisitor, BuilderVisitor
{
	public static final IgnoreVisitor instance = new IgnoreVisitor();

	@Override
	public ValueVisitor visitParameter(Name name)
	{
		return this;
	}

	@Override
	public NodeVisitor visitNode()
	{
		return this;
	}

	@Override
	public ValueVisitor visitElement()
	{
		return this;
	}

	@Override
	public ValueVisitor visitKey()
	{
		return this;
	}

	@Override
	public void visitStringPart(String string)
	{

	}

	@Override
	public ValueVisitor visitValue()
	{
		return this;
	}

	@Override
	public NodeVisitor visitNode(Name name)
	{
		return this;
	}

	@Override
	public ValueVisitor visitProperty(Name name)
	{
		return this;
	}

	@Override
	public NodeVisitor visitNodeAccess(Name name)
	{
		return this;
	}

	@Override
	public void visitEnd()
	{

	}

	@Override
	public void visitInt(int value)
	{

	}

	@Override
	public void visitLong(long value)
	{

	}

	@Override
	public void visitFloat(float value)
	{

	}

	@Override
	public void visitDouble(double value)
	{

	}

	@Override
	public void visitString(String value)
	{

	}

	@Override
	public StringInterpolationVisitor visitStringInterpolation()
	{
		return this;
	}

	@Override
	public void visitName(Name name)
	{

	}

	@Override
	public ValueVisitor visitValueAccess(Name name)
	{
		return this;
	}

	@Override
	public ListVisitor visitList()
	{
		return this;
	}

	@Override
	public MapVisitor visitMap()
	{
		return this;
	}

	@Override
	public BuilderVisitor visitBuilder(Name name)
	{
		return this;
	}
}
