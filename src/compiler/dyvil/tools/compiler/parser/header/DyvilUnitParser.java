package dyvil.tools.compiler.parser.header;

import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.parser.classes.ClassDeclarationParser;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class DyvilUnitParser extends DyvilHeaderParser
{
	private static final int CLASS = 4;
	
	public DyvilUnitParser(IHeaderUnit unit)
	{
		super(unit);
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (type)
		{
		case Tokens.EOF:
			pm.popParser();
			// Fallthrough
		case BaseSymbols.SEMICOLON:
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
			if (this.parseImport(pm, token, type))
			{
				return;
			}
			// Fallthrough
		case CLASS:
			final int classType;
			if ((classType = ModifierUtil.parseClassTypeModifier(token, pm)) >= 0)
			{
				if (this.modifiers == null)
				{
					this.modifiers = new ModifierList();
				}

				this.modifiers.addIntModifier(classType);
				pm.pushParser(new ClassDeclarationParser(this.unit, this.modifiers, this.annotations));
				this.modifiers = null;
				this.annotations = null;
				return;
			}
			if (this.parseMetadata(pm, token, type))
			{
				this.mode = CLASS;
				return;
			}
		}

		reportInvalidElement(pm, token);
	}
}
