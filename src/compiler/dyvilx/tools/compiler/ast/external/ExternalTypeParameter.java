package dyvilx.tools.compiler.ast.external;

import dyvil.annotation.Reified;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeParametric;
import dyvilx.tools.compiler.ast.generic.TypeParameter;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.structure.RootPackage;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.parsing.marker.MarkerList;

import java.util.Arrays;

public class ExternalTypeParameter extends TypeParameter
{
	// =============== Constants ===============

	private static final int UPPER_BOUND  = 1;
	private static final int LOWER_BOUND  = 2;
	private static final int REIFIED_KIND = 4;

	// =============== Fields ===============

	private byte resolved;

	// =============== Constructors ===============

	public ExternalTypeParameter(ITypeParametric generic)
	{
		super(generic);
	}

	public ExternalTypeParameter(ITypeParametric generic, Name name)
	{
		super(generic, name);
	}

	// =============== Properties ===============

	// --------------- Position ---------------

	@Override
	public SourcePosition getPosition()
	{
		return null;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	// --------------- Attributes ---------------

	@Override
	public AttributeList getAttributes()
	{
		this.resolveAnnotations();
		return super.getAttributes();
	}

	private void resolveAnnotations()
	{
		this.attributes.resolveTypes(null, RootPackage.rootPackage, this);
	}

	// --------------- Reification ---------------

	@Override
	public Reified.Type getReifiedKind()
	{
		if ((this.resolved & REIFIED_KIND) == 0)
		{
			this.resolved |= REIFIED_KIND;
			this.computeReifiedKind();
		}
		return this.reifiedKind;
	}

	// --------------- Upper Bound ---------------

	@Override
	public IType getUpperBound()
	{
		final IType upperBound = super.getUpperBound();
		if ((this.resolved & UPPER_BOUND) != 0 || upperBound == null)
		{
			return upperBound;
		}

		this.resolved |= UPPER_BOUND;
		final IType resolved = upperBound.resolveType(null, this.generic.getTypeParameterContext());
		this.setUpperBound(resolved);
		return resolved;
	}

	@Override
	public IType[] getUpperBounds()
	{
		IType[] upperBounds = super.getUpperBounds();
		if ((this.resolved & UPPER_BOUND) != 0 || upperBounds == null)
		{
			return upperBounds;
		}

		this.resolved |= UPPER_BOUND;

		final IContext typeParameterContext = this.generic.getTypeParameterContext();
		final IType[] resolvedUpperBounds = Arrays.stream(upperBounds)
		                                          .map(t -> t.resolveType(null, typeParameterContext))
		                                          .toArray(IType[]::new);

		this.setUpperBounds(resolvedUpperBounds);
		return resolvedUpperBounds;
	}

	// --------------- Lower Bound ---------------

	@Override
	public IType getLowerBound()
	{
		if ((this.resolved & LOWER_BOUND) == 0)
		{
			this.resolved |= LOWER_BOUND;

			if (this.lowerBound != null)
			{
				this.lowerBound = this.lowerBound.resolveType(null, RootPackage.rootPackage);
			}
		}
		return this.lowerBound;
	}

	// =============== Methods ===============

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	// --------------- Diagnostic Phases ---------------

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	// --------------- Compilation Phases ---------------

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
	}

	// --------------- Compilation ---------------

	@Override
	public void writeParameter(MethodWriter writer) throws BytecodeException
	{
	}

	// --------------- Decompilation ---------------

	@Override
	public void addBoundAnnotation(Annotation annotation, int index, TypePath typePath)
	{
		final IType [] upperBounds = super.getUpperBounds();
		assert (this.resolved & UPPER_BOUND) == 0;

		upperBounds[index] = IType.withAnnotation(upperBounds[index], annotation, typePath);

		// reset cache
		this.setUpperBounds(upperBounds);
	}
}
