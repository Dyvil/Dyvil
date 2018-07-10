package dyvilx.tools.compiler.parser.header;

import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IImportConsumer;
import dyvilx.tools.compiler.ast.expression.operator.Operator;
import dyvilx.tools.compiler.ast.header.HeaderDeclaration;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.header.PackageDeclaration;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.imports.KindedImport;
import dyvilx.tools.compiler.ast.type.alias.TypeAlias;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.annotation.ModifierParser;
import dyvilx.tools.compiler.parser.classes.AbstractMemberParser;
import dyvilx.tools.compiler.parser.classes.ClassDeclarationParser;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class SourceFileParser extends AbstractMemberParser
{
	protected static final int PACKAGE   = 0;
	protected static final int SEPARATOR = 1;
	protected static final int IMPORT    = 2;
	protected static final int CLASS     = 3;

	// Flags

	public static final int NO_CLASSES  = 1;
	public static final int ONE_ELEMENT = 2;

	protected IHeaderUnit unit;
	protected int         flags;

	public SourceFileParser(IHeaderUnit unit)
	{
		this.unit = unit;
		// this.mode = PACKAGE;
	}

	public SourceFileParser withFlags(int flags)
	{
		this.flags |= flags;
		return this;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case SEPARATOR:
			if (this.unit.classCount() > 0)
			{
				// any classes -> only allow classes from here
				this.mode = CLASS;
			}
			else if (this.unit.importCount() > 0 || this.unit.typeAliasCount() > 0 || this.unit.operatorCount() > 0
			         || this.unit.getHeaderDeclaration() != null)
			{
				// any imports, type aliases, operators or header declarations -> don't allow any package declarations
				this.mode = IMPORT;
			}
			else
			{
				// nothing defined yet -> allow a package declaration
				this.mode = PACKAGE;
			}

			if (!checkEnd(pm, type))
			{
				pm.report(token, "header.separator");
				pm.reparse();
			}
			return;
		case PACKAGE:
			if (type == DyvilKeywords.PACKAGE)
			{
				PackageDeclaration pack = new PackageDeclaration(token.raw());
				this.unit.setPackageDeclaration(pack);
				pm.pushParser(new PackageParser(pack));
				this.mode = SEPARATOR;
				return;
			}
			// Fallthrough
		case IMPORT:
			switch (type)
			{
			case DyvilKeywords.IMPORT:
				pm.pushParser(new ImportParser(this.importConsumer(token)));
				this.mode = SEPARATOR;
				return;
			case DyvilKeywords.USING:
				pm.pushParser(new ImportParser(this.importConsumer(token), KindedImport.USING_DECLARATION));
				this.mode = SEPARATOR;
				return;
			case DyvilKeywords.OPERATOR:
				pm.pushParser(new OperatorParser(this.unit, Operator.INFIX), true);
				this.mode = SEPARATOR;
				return;
			case DyvilKeywords.PREFIX:
			case DyvilKeywords.POSTFIX:
			case DyvilKeywords.INFIX:
				if (token.next().type() == DyvilKeywords.OPERATOR //
				    || token.next().type() == DyvilKeywords.POSTFIX
				       && token.next().next().type() == DyvilKeywords.OPERATOR)
				{
					pm.pushParser(new OperatorParser(this.unit), true);
					this.mode = SEPARATOR;
					return;
				}

				break; // parse as modifier (in 'case CLASS' via fallthrough)
			case DyvilKeywords.TYPE:
				pm.pushParser(new TypeAliasParser(this.unit, new TypeAlias()));
				this.mode = SEPARATOR;
				return;
			case DyvilKeywords.HEADER:
				final IToken next = token.next();
				if (!Tokens.isIdentifier(next.type()))
				{
					this.attributes = new AttributeList();
					pm.report(next, "header.declaration.identifier");
					return;
				}

				pm.skip();
				if (this.unit.getHeaderDeclaration() != null)
				{
					this.attributes = new AttributeList();
					pm.report(token, "header.declaration.duplicate");
					this.mode = SEPARATOR;
					return;
				}

				final HeaderDeclaration declaration = new HeaderDeclaration(this.unit, next.raw(), next.nameValue(),
				                                                            this.attributes);
				this.unit.setHeaderDeclaration(declaration);
				this.attributes = new AttributeList(); // reset
				this.mode = SEPARATOR;
				return;
			}
			// Fallthrough
		case CLASS:
			final int classType;
			if ((classType = ModifierParser.parseClassTypeModifier(token, pm)) >= 0)
			{
				if ((this.flags & NO_CLASSES) != 0)
				{
					pm.report(token, "header.class");
				}

				this.attributes.addFlag(classType);
				pm.pushParser(new ClassDeclarationParser(this.unit, this.attributes));
				this.attributes = new AttributeList(); // reset
				this.mode = SEPARATOR;
				return;
			}
			if (this.parseAttribute(pm, token))
			{
				return;
			}
		}

		if (!checkEnd(pm, type))
		{
			pm.report(Markers.syntaxError(token, "header.element.invalid", token.toString()));
		}
	}

	private static boolean checkEnd(IParserManager pm, int type)
	{
		switch (type)
		{
		case Tokens.EOF:
			pm.popParser();
			return true;
		case BaseSymbols.SEMICOLON:
			return true;
		}
		return false;
	}

	private IImportConsumer importConsumer(IToken token)
	{
		return im -> {
			final ImportDeclaration declaration = new ImportDeclaration(token.raw());
			declaration.setImport(im);
			this.unit.addImport(declaration);
		};
	}

	@Override
	public boolean reportErrors()
	{
		return this.mode == SEPARATOR && super.reportErrors();
	}
}
