package dyvilx.tools.compiler.ast.constructor;

import dyvil.reflect.Modifiers;
import dyvilx.tools.compiler.ast.annotation.AnnotationList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.lang.annotation.ElementType;

public class Initializer extends Member implements IInitializer
{
	protected IValue value;

	// Metadata
	protected IClass enclosingClass;

	public Initializer(SourcePosition position, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, Names.init, Types.VOID, modifiers, annotations);
	}

	@Override
	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.CONSTRUCTOR;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		if (this.value != null)
		{
			final IValue resolved = this.value.resolve(markers, context);
			this.value = TypeChecker.convertValue(resolved, Types.VOID, Types.VOID, markers, context,
			                                      TypeChecker.markerSupplier("initializer.type"));
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);

		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);

		if (this.value != null)
		{
			this.value.check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		if (this.value != null)
		{
			this.value.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

		if (this.value != null)
		{
			this.value = this.value.cleanup(compilableList, classCompilableList);
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		// done in writeInit / writeStaticInit
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
		if (!this.modifiers.hasIntModifier(Modifiers.STATIC))
		//  ^ not
		{
			this.value.writeExpression(writer, Types.VOID);
		}
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		if (this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			this.value.writeExpression(writer, Types.VOID);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		buffer.append("init");

		if (this.value == null || Util.formatStatementList(prefix, buffer, this.value))
		{
			return;
		}

		buffer.append(' ');
		this.value.toString(prefix, buffer);
	}
}
