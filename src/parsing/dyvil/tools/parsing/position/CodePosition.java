package dyvil.tools.parsing.position;

import dyvil.annotation.internal.NonNull;

public class CodePosition implements ICodePosition
{
	public int startLine;
	public int endLine;
	public int startColumn;
	public int endColumn;

	public CodePosition(int line, int startColumn, int endColumn)
	{
		this.startLine = line;
		this.endLine = line;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	public CodePosition(int startLine, int endLine, int startColumn, int endColumn)
	{
		this.startLine = startLine;
		this.endLine = endLine;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	@Override
	public int startLine()
	{
		return this.startLine;
	}

	@Override
	public int endLine()
	{
		return this.endLine;
	}

	@Override
	public int startColumn()
	{
		return this.startColumn;
	}

	@Override
	public int endColumn()
	{
		return this.endColumn;
	}

	@Override
	public @NonNull ICodePosition raw()
	{
		return this;
	}

	@Override
	public @NonNull ICodePosition to(@NonNull ICodePosition end)
	{
		return new CodePosition(this.startLine, end.endLine(), this.startColumn, end.endColumn());
	}

	@Override
	public @NonNull String toString()
	{
		return "CodePosition(lines: " + this.startLine + " .. " + this.endLine + ", columns: " + this.startColumn
			       + " .. " + this.endColumn + ")";
	}
}
