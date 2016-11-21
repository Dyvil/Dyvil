package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.generic.CodeTypeParameter;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class TypeParameterParser extends Parser implements ITypeConsumer
{
	public static final int ANNOTATIONS = 0;
	public static final int VARIANCE    = 1;
	public static final int NAME        = 2;
	public static final int TYPE_BOUNDS = 3;

	private static final int BOUND_MASK  = 0b11;
	private static final int UPPER_BOUND = 1;
	private static final int LOWER_BOUND = 2;

	protected ITypeParametric typeParameterized;

	private int flags;
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
			switch (type)
			{
			case DyvilSymbols.AT:
				final IAnnotation annotation = new Annotation();
				this.addAnnotation(annotation);
				pm.pushParser(new AnnotationParser(annotation));
				return;
			case DyvilKeywords.TYPE:
				// type IDENTIFIER
				// type +IDENTIFIER
				// type -IDENTIFIER
				this.mode = VARIANCE;
				return;
			case BaseSymbols.SEMICOLON:
				if (token.isInferred())
				{
					return;
				}
			}
			if (TypeParser.isGenericEnd(token, type))
			{
				pm.popParser(true);
				return;
			}
			// Fallthrough
		case VARIANCE:
		{
			if (!ParserUtil.isIdentifier(type))
			{
				pm.report(token, "type_parameter.identifier");
				return;
			}

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
		case NAME:
			if (ParserUtil.isIdentifier(type))
			{
				this.createTypeParameter(token, this.variance);
				return;
			}
			pm.report(token, "type_parameter.identifier");
			return;
		case TYPE_BOUNDS:
			if (ParserUtil.isTerminator(type) || TypeParser.isGenericEnd(token, type))
			{
				if (this.typeParameter != null)
				{
					this.typeParameterized.addTypeParameter(this.typeParameter);
				}
				pm.popParser(true);
				return;
			}

			switch (type)
			{
			case DyvilKeywords.EXTENDS:
			case BaseSymbols.COLON:
				// type T: Super
				// type T extends Super
				pm.pushParser(this.newTypeParser());
				this.setBoundMode(UPPER_BOUND);
				return;
			case DyvilKeywords.SUPER:
				pm.pushParser(this.newTypeParser());
				this.setBoundMode(UPPER_BOUND);
				return;
			}

			if (this.typeParameter != null)
			{
				this.typeParameterized.addTypeParameter(this.typeParameter);
			}
			pm.popParser(true);
			pm.report(token, "type_parameter.bound.invalid");
		}
	}

	private int getBoundMode()
	{
		return (this.flags & BOUND_MASK);
	}

	private void setBoundMode(int mode)
	{
		this.flags = (this.flags & ~BOUND_MASK) | mode;
	}

	private TypeParser newTypeParser()
	{
		// All Type Parameters are within Angle Brackets
		return new TypeParser(this).withFlags(TypeParser.CLOSE_ANGLE);
	}

	private void createTypeParameter(IToken token, Variance variance)
	{
		this.typeParameter = new CodeTypeParameter(token.raw(), this.typeParameterized, token.nameValue(), variance);
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
		switch (this.getBoundMode())
		{
		case UPPER_BOUND:
			this.typeParameter.setUpperBound(type);
			return;
		case LOWER_BOUND:
			this.typeParameter.setLowerBound(type);
		}
	}
}
