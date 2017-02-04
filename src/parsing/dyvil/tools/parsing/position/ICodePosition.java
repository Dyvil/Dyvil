package dyvil.tools.parsing.position;

import dyvil.annotation.internal.NonNull;

public interface ICodePosition extends Comparable<ICodePosition>
{
	@NonNull ICodePosition ORIGIN = new CodePosition(1, 0, 1);

	static @NonNull ICodePosition before(@NonNull ICodePosition next)
	{
		final int startLine = next.startLine();
		int startIndex = next.startColumn();
		if (startIndex == 0)
		{
			startIndex++;
		}
		return new CodePosition(startLine, startLine, startIndex - 1, startIndex);
	}

	static @NonNull ICodePosition after(@NonNull ICodePosition prev)
	{
		final int endLine = prev.endLine();
		final int endIndex = prev.endColumn();
		return new CodePosition(endLine, endLine, endIndex, endIndex + 1);
	}

	static @NonNull ICodePosition between(@NonNull ICodePosition start, @NonNull ICodePosition end)
	{
		int startIndex = start.endColumn();
		int endIndex = end.startColumn();
		if (startIndex == endIndex)
		{
			startIndex--;
			endIndex++;
		}
		return new CodePosition(start.endLine(), end.startLine(), startIndex, endIndex);
	}

	int startLine();

	int endLine();

	int startColumn();

	int endColumn();

	default @NonNull ICodePosition raw()
	{
		return new CodePosition(this.startLine(), this.endLine(), this.startColumn(), this.endColumn());
	}

	@NonNull
	default ICodePosition to(@NonNull ICodePosition end)
	{
		return new CodePosition(this.startLine(), end.endLine(), this.startColumn(), end.endColumn());
	}

	@Override
	default int compareTo(@NonNull ICodePosition o)
	{
		int byLine = Integer.compare(this.startLine(), o.startLine());
		if (byLine != 0)
		{
			return byLine;
		}
		return Integer.compare(this.startColumn(), o.startColumn());
	}

	@Override
	boolean equals(Object obj);

	@Override
	int hashCode();

	default boolean isBefore(@NonNull ICodePosition position)
	{
		return this.compareTo(position) < 0;
	}

	default boolean isAfter(@NonNull ICodePosition position)
	{
		return this.compareTo(position) > 0;
	}
}
