package dyvil.tools.gensrc.ast.scope;

import dyvil.tools.gensrc.ast.Specialization;

import java.io.File;

public class TemplateScope extends LazyScope
{
	public final File sourceFile;

	public TemplateScope(File sourceFile, Specialization spec)
	{
		super(null);
		this.importFrom(spec);
		this.sourceFile = sourceFile;
	}

	@Override
	public File getSourceFile()
	{
		return this.sourceFile;
	}

	@Override
	public Scope getGlobalParent()
	{
		return this;
	}
}
