package dyvil.tools.compiler.config;

import java.io.File;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ConfigParser extends Parser
{
	public static final int		KEY		= 1;
	public static final int		EQUALS	= 2;
	public static final int		VALUE	= 4;
	public static final int		ARRAY	= 8;
	
	protected CompilerConfig	config;
	
	private String				key;
	
	public ConfigParser(CompilerConfig config)
	{
		this.config = config;
		this.mode = KEY;
	}
	
	@Override
	public void reset()
	{
		this.mode = KEY;
		this.key = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == KEY)
		{
			if (ParserUtil.isIdentifier(type))
			{
				this.mode = EQUALS;
				this.key = token.text();
				return;
			}
			throw new SyntaxError(token, "Invalid Property - Name expected");
		}
		if (this.mode == EQUALS)
		{
			if (type == Tokens.EQUALS)
			{
				this.mode = VALUE;
				return;
			}
			throw new SyntaxError(token, "Invalid Property - '=' expected");
		}
		else if (this.mode == VALUE)
		{
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				this.mode = ARRAY;
				return;
			}
			
			this.setProperty(this.key, token);
			this.mode = KEY;
			this.key = null;
			return;
		}
		else if (this.mode == ARRAY)
		{
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				this.mode = KEY;
				this.key = null;
				return;
			}
			
			this.setProperty(this.key, token);
			return;
		}
	}
	
	private void setProperty(String name, IToken token)
	{
		switch (name)
		{
		case "jar_name":
			this.config.jarName = token.stringValue();
			return;
		case "jar_vendor":
			this.config.jarVendor = token.stringValue();
			return;
		case "jar_version":
			this.config.jarVersion = token.stringValue();
			return;
		case "jar_format":
			this.config.jarNameFormat = token.stringValue();
		case "source_dir":
			this.config.sourceDir = new File(token.stringValue());
			return;
		case "output_dir":
			this.config.outputDir = new File(token.stringValue());
			return;
		case "main_type":
			this.config.mainType = token.stringValue();
			return;
		case "main_args":
			this.config.mainArgs.add(token.stringValue());
			return;
		case "include":
			this.config.includeFile(token.stringValue());
			return;
		case "exclude":
			this.config.excludeFile(token.stringValue());
			return;
		}
	}
}
