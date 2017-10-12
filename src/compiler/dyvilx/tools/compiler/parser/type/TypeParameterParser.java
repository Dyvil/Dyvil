package dyvilx.tools.compiler.parser.type;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.CodeAnnotation;
import dyvilx.tools.compiler.ast.consumer.ITypeConsumer;
import dyvilx.tools.compiler.ast.generic.CodeTypeParameter;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.ITypeParametric;
import dyvilx.tools.compiler.ast.generic.Variance;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.annotation.AnnotationParser;
import dyvilx.tools.compiler.parser.classes.AbstractMemberParser;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public final class TypeParameterParser extends AbstractMemberParser implements ITypeConsumer
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
				final Annotation annotation = new CodeAnnotation(token.raw());
				this.attributes.add(annotation);
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
			if (!Tokens.isIdentifier(type))
			{
				pm.report(token, "type_parameter.identifier");
				return;
			}

			final Name name = token.nameValue();
			if (Tokens.isIdentifier(token.next().type()))
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
			if (Tokens.isIdentifier(type))
			{
				this.createTypeParameter(token, this.variance);
				return;
			}
			pm.report(token, "type_parameter.identifier");
			return;
		case TYPE_BOUNDS:
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
				this.setBoundMode(LOWER_BOUND);
				return;
			}

			if (BaseSymbols.isTerminator(type) || TypeParser.isGenericEnd(token, type))
			{
				if (this.typeParameter != null)
				{
					this.typeParameterized.getTypeParameters().add(this.typeParameter);
				}
				pm.popParser(true);
				return;
			}

			if (this.typeParameter != null)
			{
				this.typeParameterized.getTypeParameters().add(this.typeParameter);
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
		this.typeParameter = new CodeTypeParameter(token.raw(), this.typeParameterized, token.nameValue(), variance,
		                                           this.attributes);
		this.mode = TYPE_BOUNDS;
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
