package dyvilx.tools.compiler.phase;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.header.ICompilationUnit;
import dyvilx.tools.compiler.lang.I18n;

public class PrintPhase implements ICompilerPhase
{
	private final ICompilerPhase predecessor;

	public PrintPhase(ICompilerPhase predecessor)
	{
		this.predecessor = predecessor;
	}

	@Override
	public String getName()
	{
		return "PRINT";
	}

	@Override
	public int getID()
	{
		return this.predecessor.getID() + 1;
	}

	@Override
	public void apply(DyvilCompiler compiler)
	{
		compiler.log(I18n.get("phase.syntax_trees", this.predecessor.getName()));
		for (ICompilationUnit unit : compiler.fileFinder.units)
		{
			try
			{
				compiler.log(unit.getFileSource() + ":\n" + unit.toString());
			}
			catch (Throwable throwable)
			{
				compiler.error(I18n.get("phase.syntax_trees.error", unit.getFileSource()), throwable);
			}
		}
	}

	@Override
	public String toString()
	{
		return "PRINT[" + this.predecessor.getName() + "]";
	}
}
