package dyvil.tools.compiler.ast.imports;

import java.util.HashSet;
import java.util.Set;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class MultiImport extends PackageImport
{
	public Set<String>	classNames	= new HashSet();
	public Set<IClass>	classes;
	
	public MultiImport(ICodePosition position, String basePackage)
	{
		super(position, basePackage);
	}
	
	public void addClass(String name)
	{
		this.classNames.add(name);
	}
	
	public boolean containsClass(String name)
	{
		return this.classNames.contains(name);
	}
	
	@Override
	public MultiImport applyState(CompilerState state, IContext context)
	{
		super.applyState(state, context);
		if (state == CompilerState.RESOLVE_TYPES)
		{
			if (this.pack == null)
			{
				return this;
			}
			
			this.classes = new HashSet(this.classNames.size());
			for (String s : this.classNames)
			{
				IClass c = this.pack.resolveClass(s);
				if (c == null)
				{
					state.addMarker(new SemanticError(this.position, "'" + s + "' could not be resolved to a class"));
				}
				else
				{
					this.classes.add(c);
				}
			}
		}
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append("import ").append(this.packageName).append(Formatting.Import.multiImportStart);
		Util.listToString(this.classNames, Formatting.Import.multiImportSeperator, buffer);
		buffer.append(Formatting.Import.multiImportEnd).append(';');
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		for (IClass c : this.classes)
		{
			if (name.equals(c.getName()))
			{
				return c;
			}
		}
		return null;
	}
}
