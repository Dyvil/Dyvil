package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.generic.TypeParameter;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.token.IToken;

public final class TypeParameterParser extends Parser implements ITyped
{
	public static final int ANNOTATIONS = 0;
	public static final int NAME        = 1;
	public static final int TYPE_BOUNDS = 2;
	
	public static final int UPPER = 1;
	public static final int LOWER = 2;
	
	protected ITypeParametric typeParameterized;
	
	private byte boundMode;
	private Variance variance = Variance.INVARIANT;
	private ITypeParameter typeParameter;
	private AnnotationList annotationList;
	
	public TypeParameterParser(ITypeParametric typeParameterized)
	{
		this.typeParameterized = typeParameterized;
		// this.mode = ANNOTATIONS; // pointless assignment to 0
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case ANNOTATIONS:
			if (type == DyvilSymbols.AT)
			{
				final IAnnotation annotation = new Annotation();
				this.addAnnotation(annotation);
				pm.pushParser(new AnnotationParser(annotation));
				return;
			}
			if (ParserUtil.isIdentifier(type))
			{
				final Name name = token.nameValue();
				if (ParserUtil.isIdentifier(token.next().type()))
				{
					if (name == Names.plus)
					{
						this.mode = NAME;
						this.variance = Variance.COVARIANT;
						return;
					}
					if (name == Names.minus)
					{
						this.mode = NAME;
						this.variance = Variance.CONTRAVARIANT;
						return;
					}
				}

				this.createTypeParameter(token, this.variance);
				return;
			}
			pm.report(token, "typeparameter.identifier");
			return;
		case NAME:
			if (ParserUtil.isIdentifier(type))
			{
				this.createTypeParameter(token, this.variance);
				return;
			}
			pm.report(token, "typeparameter.identifier");
			return;
		case TYPE_BOUNDS:
			if (ParserUtil.isTerminator(type))
			{
				if (this.typeParameter != null)
				{
					this.typeParameterized.addTypeParameter(this.typeParameter);
				}
				pm.popParser(true);
				return;
			}
			
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				if (this.boundMode == 0)
				{
					if (name == Names.gtcolon) // >: - Lower Bound
					{
						pm.pushParser(pm.newTypeParser(this));
						this.boundMode = LOWER;
						return;
					}
					if (name == Names.ltcolon) // <: - Upper Bounds
					{
						pm.pushParser(pm.newTypeParser(this));
						this.boundMode = UPPER;
						return;
					}
				}
				else if (this.boundMode == UPPER)
				{
					if (name == Names.amp)
					{
						pm.pushParser(pm.newTypeParser(this));
						return;
					}
				}
			}

			if (this.typeParameter != null)
			{
				this.typeParameterized.addTypeParameter(this.typeParameter);
			}
			pm.popParser(true);
			pm.report(token, "typeparameter.bound.invalid");
			return;
		}
	}

	private void createTypeParameter(IToken token, Variance variance)
	{
		this.typeParameter = new TypeParameter(token, this.typeParameterized, token.nameValue(), variance);
		this.typeParameter.setAnnotations(this.annotationList);
		this.mode = TYPE_BOUNDS;
	}

	private void addAnnotation(IAnnotation annotion)
	{
		if (this.annotationList == null)
		{
			this.annotationList = new AnnotationList();
		}
		this.annotationList.addAnnotation(annotion);
	}
	
	@Override
	public void setType(IType type)
	{
		if (this.boundMode == UPPER)
		{
			this.typeParameter.addUpperBound(type);
		}
		else if (this.boundMode == LOWER)
		{
			this.typeParameter.setLowerBound(type);
		}
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
}
