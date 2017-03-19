package dyvil.tools.compiler.ast.external;

import dyvil.annotation.Reified;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.generic.TypeParameter;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.RootPackage;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.compound.IntersectionType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ExternalTypeParameter extends TypeParameter
{
	private static final int UPPER_BOUND = 1;
	private static final int LOWER_BOUND = 2;
	private static final int ANNOTATIONS = 4;

	private int upperBoundCount;
	private int resolved;

	// shared constructor
	{
		this.upperBounds = new IType[3];
	}

	public ExternalTypeParameter(ITypeParametric generic)
	{
		super(generic);
	}

	public ExternalTypeParameter(ITypeParametric generic, Name name)
	{
		super(generic, name);
	}

	@Override
	public ICodePosition getPosition()
	{
		return null;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
	}

	@Override
	public IParameter getReifyParameter()
	{
		return null;
	}

	@Override
	public Reified.Type getReifiedKind()
	{
		if (this.reifiedKind != null)
		{
			return this.reifiedKind;
		}

		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}

		return super.getReifiedKind();
	}

	@Override
	public AnnotationList getAnnotations()
	{
		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}

		return super.getAnnotations();
	}

	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		if ((this.resolved & ANNOTATIONS) == 0)
		{
			this.resolveAnnotations();
		}

		return super.getAnnotation(type);
	}

	private void resolveAnnotations()
	{
		this.resolved |= ANNOTATIONS;

		if (this.annotations == null)
		{
			return;
		}
		this.annotations.resolveTypes(null, RootPackage.rootPackage, this);
		this.computeReifiedKind();
	}

	@Override
	public IType getUpperBound()
	{
		if (this.upperBound != null)
		{
			return this.upperBound;
		}

		if ((this.resolved & UPPER_BOUND) == 0)
		{
			this.resolved |= UPPER_BOUND;
			for (int i = 0; i < this.upperBoundCount; i++)
			{
				this.upperBounds[i] = this.upperBounds[i].resolveType(null, RootPackage.rootPackage);
			}
		}
		return this.upperBound = createUpperBound(this.upperBounds, 0, this.upperBoundCount);
	}

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

	/**
	 * Creates a balanced tree for the slice of the given array
	 *
	 * @param upperBounds
	 * 	the upper bounds array
	 * @param start
	 * 	the start index
	 * @param count
	 * 	the number of elements
	 *
	 * @return a balanced tree of {@link IntersectionType}s
	 */
	private static IType createUpperBound(IType[] upperBounds, int start, int count)
	{
		if (count == 1)
		{
			return upperBounds[start];
		}

		final int halfCount = count / 2;
		return new IntersectionType(createUpperBound(upperBounds, start, halfCount),
		                            createUpperBound(upperBounds, start + halfCount, count - halfCount));
	}

	public void addUpperBound(IType bound)
	{
		this.upperBound = null;

		final int index = this.upperBoundCount++;
		if (index == 0)
		{
			// no need to resize the array, it has definitely a bigger capacity than zero

			this.erasure = bound;
			this.upperBounds[0] = bound;
			return;
		}

		if (index >= this.upperBounds.length)
		{
			IType[] temp = new IType[index + 1];
			System.arraycopy(this.upperBounds, 0, temp, 0, index);
			this.upperBounds = temp;
		}

		this.upperBounds[index] = bound;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
	}

	@Override
	public void addBoundAnnotation(IAnnotation annotation, int index, TypePath typePath)
	{
		this.upperBound = null;
		this.upperBounds[index] = IType.withAnnotation(this.upperBounds[index], annotation, typePath);
	}

	@Override
	public void writeParameter(MethodWriter writer) throws BytecodeException
	{
	}
}
