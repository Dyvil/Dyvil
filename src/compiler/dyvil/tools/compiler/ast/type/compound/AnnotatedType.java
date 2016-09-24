package dyvil.tools.compiler.ast.type.compound;

import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeDelegate;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class AnnotatedType extends TypeDelegate
{
	private IAnnotation annotation;

	public AnnotatedType()
	{
	}

	public AnnotatedType(IAnnotation annotation)
	{
		this.annotation = annotation;
	}

	public AnnotatedType(IType type, IAnnotation annotation)
	{
		this.type = type;
		this.annotation = annotation;
	}

	@Override
	public int typeTag()
	{
		return ANNOTATED;
	}

	@Override
	protected IType wrap(IType type)
	{
		return new AnnotatedType(type, this.annotation);
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		if (this.type == null)
		{
			markers.add(Markers.semanticError(this.annotation.getPosition(), "type.annotated.invalid"));
			this.type = Types.UNKNOWN;
		}
		else
		{
			this.type = this.type.resolveType(markers, context);
		}

		this.annotation.resolveTypes(markers, context);

		final IType withAnnotation = this.type.withAnnotation(this.annotation);
		if (withAnnotation != null)
		{
			return withAnnotation;
		}
		return this;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		this.annotation.resolve(markers, context);
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		this.type.checkType(markers, context, position);
		this.annotation.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
		this.annotation.check(markers, context, ElementType.TYPE_USE);
	}

	@Override
	public void foldConstants()
	{
		this.type.foldConstants();
		this.annotation.foldConstants();
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
		this.annotation.cleanup(context, compilableList);
	}

	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		if (this.annotation.getType().getTheClass() == type)
		{
			return this.annotation;
		}

		return this.type.getAnnotation(type);
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		TypePath path = TypePath.fromString(typePath);
		IType type = this.type;

		this.annotation.write(visitor, typeRef, path);

		// Ensure that we don't create the TypePath object multiple times by
		// checking for multiple annotations on the same type
		while (type.typeTag() == ANNOTATED)
		{
			AnnotatedType t = (AnnotatedType) type;
			t.annotation.write(visitor, typeRef, path);
			type = t.type;
		}

		type.writeAnnotations(visitor, typeRef, typePath);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.type, out);
		this.annotation.write(out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
		this.annotation.read(in);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.annotation.toString(prefix, buffer);
		if (this.type != null)
		{
			buffer.append(' ');
			this.type.toString(prefix, buffer);
		}
	}
}
