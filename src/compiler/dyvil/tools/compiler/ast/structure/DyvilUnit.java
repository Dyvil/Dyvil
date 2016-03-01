package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.consumer.IClassConsumer;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.ObjectFormat;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.classes.DyvilHeaderParser;
import dyvil.tools.compiler.parser.classes.DyvilUnitParser;
import dyvil.tools.compiler.sources.DyvilFileType;
import dyvil.tools.parsing.Name;

import java.io.File;

public class DyvilUnit extends DyvilHeader implements IClassConsumer
{
	private IClass[] classes = new IClass[1];
	private int classCount;
	private IClassCompilable[] innerClasses = new IClassCompilable[2];
	private int innerClassCount;
	
	public DyvilUnit(DyvilCompiler compiler, Package pack, File input, File output)
	{
		super(compiler, pack, input, output);
	}
	
	@Override
	public boolean isHeader()
	{
		return this.headerDeclaration != null;
	}
	
	@Override
	public int classCount()
	{
		return this.classCount;
	}
	
	@Override
	public void addClass(IClass iclass)
	{
		iclass.setHeader(this);

		int index = this.classCount++;
		if (index >= this.classes.length)
		{
			IClass[] temp = new IClass[this.classCount];
			System.arraycopy(this.classes, 0, temp, 0, this.classes.length);
			this.classes = temp;
		}

		this.classes[index] = iclass;
	}
	
	@Override
	public IClass getClass(int index)
	{
		return this.classes[index];
	}
	
	@Override
	public IClass getClass(Name name)
	{
		for (int i = 0; i < this.classCount; i++)
		{
			IClass c = this.classes[i];
			if (c.getName() == name)
			{
				return c;
			}
		}
		return null;
	}
	
	@Override
	public int innerClassCount()
	{
		return this.innerClassCount;
	}
	
	@Override
	public void addInnerClass(IClassCompilable iclass)
	{
		int index = this.innerClassCount++;
		if (index >= this.innerClasses.length)
		{
			IClassCompilable[] temp = new IClassCompilable[this.innerClassCount];
			System.arraycopy(this.innerClasses, 0, temp, 0, this.innerClasses.length);
			this.innerClasses = temp;
		}
		this.innerClasses[index] = iclass;
		
		iclass.setInnerIndex(this.getInternalName(), index);
	}
	
	@Override
	public IClassCompilable getInnerClass(int index)
	{
		return this.innerClasses[index];
	}
	
	@Override
	public void parseHeader()
	{
		ParserManager manager = new ParserManager(new DyvilHeaderParser(this, true), this.markers, this);
		manager.parse(this.tokens);
	}
	
	@Override
	public void parse()
	{
		ParserManager manager = new ParserManager(new DyvilUnitParser(this, true), this.markers, this);
		manager.parse(this.tokens);
		this.tokens = null;
	}
	
	@Override
	public void resolveTypes()
	{
		super.resolveTypes();
		
		for (int i = 0; i < this.classCount; i++)
		{
			IClass iclass = this.classes[i];
			if (iclass.getName() == null)
			{
				this.classes[i] = null;
				this.classCount = i;
				return;
			}
			this.classes[i].resolveTypes(this.markers, this);
		}
	}
	
	@Override
	public void resolve()
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].resolve(this.markers, this);
		}
	}
	
	@Override
	public void checkTypes()
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].checkTypes(this.markers, this);
		}
	}
	
	@Override
	public void check()
	{
		this.pack.check(this.packageDeclaration, this.markers);
		if (this.headerDeclaration != null)
		{
			this.headerDeclaration.check(this.markers);
		}
		
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].check(this.markers, this);
		}
	}
	
	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].foldConstants();
		}
	}
	
	@Override
	public void cleanup()
	{
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].cleanup(this, null);
		}
	}
	
	@Override
	protected boolean printMarkers()
	{
		return ICompilationUnit.printMarkers(this.compiler, this.markers, "Dyvil Unit", this.name, this.inputFile, this.code);
	}
	
	@Override
	public void compile()
	{
		if (this.printMarkers())
		{
			return;
		}
		
		if (this.headerDeclaration != null)
		{
			ObjectFormat.write(this.compiler, new File(this.outputDirectory, this.name.qualified + ".dyo"), this);
		}
		
		for (int i = 0; i < this.classCount; i++)
		{
			IClass iclass = this.classes[i];
			Name name = iclass.getName();
			String name1;
			if (name != this.name)
			{
				name1 = this.name.qualified + "$" + name.qualified + DyvilFileType.CLASS_EXTENSION;
			}
			else
			{
				name1 = name.qualified + DyvilFileType.CLASS_EXTENSION;
			}
			
			File file = new File(this.outputDirectory, name1);
			ClassWriter.compile(this.compiler, file, iclass);
			
			IClassBody body = iclass.getBody();
			if (body != null)
			{
				int len = body.classCount();
				for (int j = 0; j < len; j++)
				{
					IClass iclass1 = body.getClass(j);
					name1 = this.name.qualified + "$" + iclass1.getName().qualified + DyvilFileType.CLASS_EXTENSION;
					file = new File(this.outputDirectory, name1);
					ClassWriter.compile(this.compiler, file, iclass1);
				}
			}
		}
		
		for (int i = 0; i < this.innerClassCount; i++)
		{
			IClassCompilable iclass = this.innerClasses[i];
			String name = iclass.getFileName() + DyvilFileType.CLASS_EXTENSION;
			File file = new File(this.outputDirectory, name);
			ClassWriter.compile(this.compiler, file, iclass);
		}
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		// Own classes
		for (int i = 0; i < this.classCount; i++)
		{
			IClass c = this.classes[i];
			if (c.getName() == name)
			{
				return c;
			}
		}
		
		return super.resolveClass(name);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		for (int i = 0; i < this.classCount; i++)
		{
			this.classes[i].toString(prefix, buffer);
		}
	}
}
