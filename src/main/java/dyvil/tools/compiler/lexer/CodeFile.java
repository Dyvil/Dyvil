package dyvil.tools.compiler.lexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CodeFile extends File
{
	private static final long	serialVersionUID	= 130988432718494125L;
	
	private String				code;
	
	public CodeFile(File parent, String child)
	{
		super(parent, child);
	}
	
	public CodeFile(String parent, String child)
	{
		super(parent, child);
	}
	
	public CodeFile(String pathname)
	{
		super(pathname);
	}
	
	@Override
	public boolean isDirectory()
	{
		return false;
	}
	
	public void load()
	{
		try
		{
			this.code = new String(Files.readAllBytes(this.toPath()));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public String getCode()
	{
		if (this.code == null)
		{
			this.load();
		}
		return this.code;
	}
	
	public int getLength()
	{
		if (this.code == null)
		{
			this.load();
		}
		return this.code.length();
	}
}
