package dyvilx.tools.compiler.parser.header;

import dyvilx.tools.compiler.ast.annotation.Annotation;
import dyvilx.tools.compiler.ast.annotation.AnnotationList;
import dyvilx.tools.compiler.ast.consumer.IImportConsumer;
import dyvilx.tools.compiler.ast.header.*;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.imports.KindedImport;
import dyvilx.tools.compiler.ast.modifiers.Modifier;
import dyvilx.tools.compiler.ast.modifiers.ModifierList;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.expression.operator.Operator;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.type.alias.TypeAlias;
import dyvilx.tools.compiler.parser.ParserUtil;
import dyvilx.tools.compiler.parser.annotation.AnnotationParser;
import dyvilx.tools.compiler.parser.annotation.ModifierParser;
import dyvilx.tools.compiler.transform.DyvilKeywords;
import dyvilx.tools.compiler.transform.DyvilSymbols;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvil.lang.Name;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class DyvilHeaderParser extends Parser
{
	protected static final int PACKAGE  = 1;
	protected static final int IMPORT   = 2;
	protected static final int METADATA = 4;

	// Flags

	private static final int METADATA_FLAG = 1;
	public static final  int ONE_ELEMENT   = 2;

	// -----

	protected IHeaderUnit unit;

	// Parser data
	protected ModifierSet    modifiers;
	protected AnnotationList annotations;

	protected int flags;

	public DyvilHeaderParser(IHeaderUnit unit)
	{
		this.unit = unit;
		this.mode = PACKAGE;
	}

	public DyvilHeaderParser withFlags(int flags)
	{
		this.flags |= flags;
		return this;
	}

	protected boolean parsePackage(IParserManager pm, IToken token, int type)
	{
		if (type == DyvilKeywords.PACKAGE)
		{
			PackageDeclaration pack = new PackageDeclaration(token.raw());
			this.unit.setPackageDeclaration(pack);
			pm.pushParser(new PackageParser(pack));
			return true;
		}
		return false;
	}

	private IImportConsumer importConsumer(IToken token)
	{
		final ImportDeclaration declaration = new ImportDeclaration(token.raw());
		return im ->
		{
			declaration.setImport(im);
			this.unit.addImport(declaration);
		};
	}

	protected boolean parseImport(IParserManager pm, IToken token, int type)
	{
		switch (type)
		{
		case DyvilKeywords.IMPORT:
			pm.pushParser(new ImportParser(this.importConsumer(token)));
			return true;
		case DyvilKeywords.USING:
			pm.pushParser(new ImportParser(this.importConsumer(token), KindedImport.USING_DECLARATION));
			return true;
		case DyvilKeywords.OPERATOR:
			pm.pushParser(new OperatorParser(this.unit, Operator.INFIX), true);
			return true;
		case DyvilKeywords.PREFIX:
		case DyvilKeywords.POSTFIX:
		case DyvilKeywords.INFIX:
			if (token.next().type() == DyvilKeywords.OPERATOR)
			{
				final OperatorParser operatorParser = new OperatorParser(this.unit);
				// Parse this token so the OperatorParser correctly detects the type (prefix, postfix, infix)
				operatorParser.parse(pm, token);
				pm.pushParser(operatorParser);
				return true;
			}

			return false;
		case DyvilKeywords.TYPE:
		{
			TypeAlias typeAlias = new TypeAlias();
			pm.pushParser(new TypeAliasParser(this.unit, typeAlias));
			return true;
		}
		}
		return false;
	}

	protected boolean parseMetadata(IParserManager pm, IToken token, int type)
	{
		Modifier modifier;
		if ((modifier = ModifierParser.parseModifier(token, pm)) != null)
		{
			if (this.modifiers == null)
			{
				this.modifiers = new ModifierList();
			}

			this.modifiers.addModifier(modifier);
			return true;
		}
		if (type == DyvilSymbols.AT && token.next().type() != DyvilKeywords.INTERFACE)
		{
			this.parseAnnotation(pm, token);
			return true;
		}
		if (type == DyvilKeywords.HEADER)
		{
			IToken next = token.next();
			if (ParserUtil.isIdentifier(next.type()))
			{
				pm.skip();
				if (this.unit.getHeaderDeclaration() != null)
				{
					pm.report(token, "header.declaration.duplicate");
					return true;
				}

				Name name = next.nameValue();
				this.unit.setHeaderDeclaration(
					new HeaderDeclaration(this.unit, next.raw(), name, this.modifiers, this.annotations));
				this.modifiers = null;
				this.annotations = null;
				this.flags &= ~METADATA_FLAG;
				this.mode = IMPORT;
				return true;
			}
		}
		return false;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (type)
		{
		case Tokens.EOF:
			if (hasModifiers(this.modifiers, this.annotations))
			{
				pm.report(token, "header.element");
			}
			pm.popParser();
			return;
		case BaseSymbols.SEMICOLON:
			if ((this.flags & ONE_ELEMENT) != 0)
			{
				pm.popParser(true);
			}
			return;
		}

		switch (this.mode)
		{
		case PACKAGE:
			if (this.parsePackage(pm, token, type))
			{
				this.mode = IMPORT;
				return;
			}
			// Fallthrough
		case IMPORT:
			this.mode = IMPORT;
			if (this.parseImport(pm, token, type))
			{
				return;
			}
			// Fallthrough
		case METADATA:
			if (this.mode != METADATA)
			{
				this.flags |= METADATA_FLAG;
				this.mode = METADATA;
			}
			if (this.parseMetadata(pm, token, type))
			{
				return;
			}
		}

		reportInvalidElement(pm, token);
	}

	public static boolean hasModifiers(ModifierSet modifiers, AnnotationList annotations)
	{
		return modifiers != null && !modifiers.isEmpty() || annotations != null && annotations.size() != 0;
	}

	protected static void reportInvalidElement(IParserManager pm, IToken token)
	{
		pm.report(Markers.syntaxError(token, "header.element.invalid", token.toString()));
	}

	private void parseAnnotation(IParserManager pm, IToken token)
	{
		if (this.annotations == null)
		{
			this.annotations = new AnnotationList();
		}

		final Annotation annotation = new Annotation(token.raw());
		this.annotations.add(annotation);
		pm.pushParser(new AnnotationParser(annotation));
	}

	@Override
	public boolean reportErrors()
	{
		return this.mode > PACKAGE && (this.flags & METADATA_FLAG) == 0;
	}
}
