package dyvil.tools.parsing.position;

import dyvil.annotation.internal.NonNull;

public interface ICodePosition
{
	@NonNull ICodePosition ORIGIN = new CodePosition(1, 0, 1);

	static @NonNull ICodePosition before(@NonNull ICodePosition next)
	{
		final int startLine = next.startLine();
		int startIndex = next.startIndex();
		if (startIndex == 0)
		{
			startIndex++;
		}
		return new CodePosition(startLine, startLine, startIndex - 1, startIndex);
	}

	static @NonNull ICodePosition after(@NonNull ICodePosition prev)
	{
		final int endLine = prev.endLine();
		final int endIndex = prev.endIndex();
		return new CodePosition(endLine, endLine, endIndex, endIndex + 1);
	}

	static @NonNull ICodePosition between(@NonNull ICodePosition start, @NonNull ICodePosition end)
	{
		int startIndex = start.endIndex();
		int endIndex = end.startIndex();
		if (startIndex == endIndex)
		{
			startIndex--;
			endIndex++;
		}
		return new CodePosition(start.endLine(), end.startLine(), startIndex, endIndex);
	}

	int startIndex();

	int endIndex();

	int startLine();

	int endLine();

	@NonNull ICodePosition raw();

	@NonNull ICodePosition to(@NonNull ICodePosition end);

	default boolean isBefore(@NonNull ICodePosition position)
	{
		return this.endIndex() < position.startIndex();
	}

	default boolean isAfter(@NonNull ICodePosition position)
	{
		return this.startIndex() > position.endIndex();
	}
}
