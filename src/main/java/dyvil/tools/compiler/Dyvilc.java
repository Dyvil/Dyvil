package dyvil.tools.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.config.ConfigParser;

public class Dyvilc
{
	public static Dyvilc				instance;
	
	protected CompilerConfig			config;
	protected CompilerState				state;
	
	public Map<File, CompilationUnit>	compilationUnits	= new HashMap();
	
	public Dyvilc(CompilerConfig config)
	{
		this.config = config;
	}
	
	public static void main(String[] args)
	{
		File file = new File(args[0]);
		try
		{
			String s = new String(Files.readAllBytes(file.toPath()));
			
			CompilerConfig config = new CompilerConfig();
			CodeParser.instance.parse(new ConfigParser(config), s);
			
			instance = new Dyvilc(config);
			instance.compile();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void compile()
	{
		System.out.println("Compiling " + this.config.sourceDir.getAbsolutePath() + " to " + this.config.outputDir.getAbsolutePath());
		
		this.compile(this.config.sourceDir, this.config.outputDir);
		
		for (CompilationUnit unit : this.compilationUnits.values())
		{	
			unit.applyState(CompilerState.FOLD_CONSTANTS);
			
			System.out.println(unit);
		}
	}
	
	public void compile(File source, File output)
	{
		if (!source.exists())
		{
			System.out.println(source.getPath() + " does not exist.");
		}
		else if (source.isDirectory())
		{
			for (String s : source.list())
			{
				this.compile(new File(source, s), new File(output, s));
			}
		}
		else if (source.getName().endsWith(".dyvil"))
		{
			CompilationUnit unit = CodeParser.compilationUnit(readFile(source));
			this.compilationUnits.put(source, unit);
		}
		else
		{
			final List<IToken> tokens = new ArrayList();
			CodeParser.instance.parse(new Parser() {

				@Override
				public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
				{
					tokens.add(token);
					return true;
				}
				
			}, readFile(source));
			System.out.println(tokens);
		}
	}
	
	public static String readFile(File file)
	{
		try
		{
			return new String(Files.readAllBytes(file.toPath()));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
