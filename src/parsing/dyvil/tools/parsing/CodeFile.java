package dyvil.tools.parsing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CodeFile extends File
{
	private static final long serialVersionUID = 130988432718494125L;
	
	private String code;
	
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
	
	public void load()
	{
		try
		{
			this.code = new String(Files.readAllBytes(this.toPath()), StandardCharsets.UTF_8);
		}
		catch (IOException ex)
		{
			// TODO Don't do this
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
