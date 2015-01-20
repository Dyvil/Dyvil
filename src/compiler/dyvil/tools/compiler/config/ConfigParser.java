package dyvil.tools.compiler.config;

import java.io.File;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ConfigParser extends Parser
{
	public static final int		KEY		= 0;
	public static final int		VALUE	= 1;
	public static final int		ARRAY	= 2;
	
	protected CompilerConfig	config;
	
	private String				key;
	
	public ConfigParser(CompilerConfig config)
	{
		this.config = config;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == KEY)
		{
			if (type == Tokens.EQUALS)
			{
				this.mode = VALUE;
				return true;
			}
			if (ParserUtil.isIdentifier(type))
			{
				this.key = token.value();
				return true;
			}
		}
		else if (this.mode == VALUE)
		{
			if (type == Tokens.OPEN_SQUARE_BRACKET)
			{
				this.mode = ARRAY;
				return true;
			}
			
			this.setProperty(this.key, token.object());
			this.mode = KEY;
			this.key = null;
			return true;
		}
		else if (this.mode == ARRAY)
		{
			if (type == Tokens.CLOSE_SQUARE_BRACKET)
			{
				this.mode = KEY;
				this.key = null;
				return true;
			}
			
			this.setProperty(this.key, token.object());
			return true;
		}
		return false;
	}
	
	private void setProperty(String name, Object property)
	{
		switch (name)
		{
		case "jar_name":
			this.config.jarName = (String) property;
			break;
		case "jar_vendor":
			this.config.jarVendor = (String) property;
			break;
		case "jar_version":
			this.config.jarVersion = (String) property;
			break;
		case "jar_format":
			this.config.jarNameFormat = (String) property;
		case "source_dir":
			this.config.sourceDir = new File((String) property);
			break;
		case "output_dir":
			this.config.outputDir = new File((String) property);
			break;
		case "main_type":
			this.config.mainType = (String) property;
			break;
		case "include":
			this.config.includeFile((String) property);
			break;
		case "exclude":
			this.config.excludeFile((String) property);
			break;
		}
	}
}
