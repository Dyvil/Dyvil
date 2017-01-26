package dyvil.tools.gensrc.parser;

import dyvil.tools.gensrc.ast.Util;
import dyvil.tools.gensrc.ast.directive.*;

import java.util.List;

public class Parser
{
	private static final int IF_BLOCK   = 1;
	private static final int ELSE_BLOCK = 2;
	private static final int FOR_BLOCK  = 3;

	private final List<String> lines;
	private final int endLine;

	public Parser(List<String> lines)
	{
		this.lines = lines;
		this.endLine = lines.size();
	}

	public DirectiveList parse()
	{
		DirectiveList list = new DirectiveList();
		this.parse(list, 0, 0);
		return list;
	}

	private int parse(DirectiveList list, int start, int block)
	{
		for (; start < this.endLine; start++)
		{
			final String line = this.lines.get(start);
			final int length = line.length();

			final int hashIndex;
			if (length < 2 || (hashIndex = Util.skipWhitespace(line, 0, length)) >= length
				    || line.charAt(hashIndex) != '#')
			{
				// no leading directive
				list.add(new ProcessedLine(line));

				continue;
			}

			final int directiveStart = hashIndex + 1;
			final int directiveEnd = Util.findIdentifierEnd(line, directiveStart, length);
			final String directive = line.substring(directiveStart, directiveEnd);

			switch (directive)
			{
			// ----- Simple Directives -----
			case "process":
				list.add(this.parseProcess(line, directiveEnd, length));
				continue;
			case "literal":
				list.add(this.parseLiteral(line, directiveEnd, length));
				continue;
			case "include":
				list.add(this.parseInclude(line, directiveEnd, length));
				continue;
			case "comment":
				continue;

				// ----- Replacement Directives -----
			case "define":
				list.add(this.parseDefine(line, directiveEnd, length, false));
				continue;
			case "local":
				list.add(this.parseDefine(line, directiveEnd, length, true));
				continue;
			case "undefine":
				list.add(this.parseUndefine(line, directiveEnd, length, false));
				continue;
			case "delete":
				list.add(this.parseUndefine(line, directiveEnd, length, true));
				continue;

				// ----- Scope Directives -----
			case "import":
				list.add(this.parseImport(line, directiveEnd, length));
				continue;

				// ----- if Directives -----
			case "if":
				start = this.parseIf(list, start, line, directiveEnd, length, IfDirective.MODE_IF);
				continue;
			case "ifdef":
				start = this.parseIf(list, start, line, directiveEnd, length, IfDirective.MODE_IFDEF);
				continue;
			case "ifndef":
				start = this.parseIf(list, start, line, directiveEnd, length, IfDirective.MODE_IFNDEF);
				continue;
			case "else":
				if (block == IF_BLOCK)
				{
					return start;
				}
				break;
			case "endif":
				if (block == IF_BLOCK || block == ELSE_BLOCK)
				{
					return start;
				}
				break;

			// ----- for Directives -----
			case "for":
				start = this.parseFor(list, start, line, directiveEnd, length);
				continue;
			case "foreach":
				start = this.parseForEach(list, start, line, directiveEnd, length);
				continue;
			case "endfor":
				if (block == FOR_BLOCK)
				{
					return start;
				}
				break;

			case "end":
				if (block != 0)
				{
					return start;
				}
				break;
			}

			// TODO Invalid Directive Error
		}

		return start;
	}

	private LiteralDirective parseLiteral(String line, int directiveEnd, int length)
	{
		return new LiteralDirective(Util.getArgument(line, directiveEnd, length));
	}

	private ProcessedLine parseProcess(String line, int directiveEnd, int length)
	{
		return new ProcessedLine(Util.getArgument(line, directiveEnd, length));
	}

	private DefineDirective parseDefine(String line, int directiveEnd, int length, boolean local)
	{
		final int keyStart = Util.skipWhitespace(line, directiveEnd, length);
		final int keyEnd = Util.findIdentifierEnd(line, keyStart, length);
		final String key = line.substring(keyStart, keyEnd);

		final int valueStart = Util.skipWhitespace(line, keyEnd, length);
		final String value = line.substring(valueStart, length);

		return new DefineDirective(local, key, value);
	}

	private UndefineDirective parseUndefine(String line, int directiveEnd, int length, boolean local)
	{
		final int keyStart = Util.skipWhitespace(line, directiveEnd, length);
		final int keyEnd = Util.findIdentifierEnd(line, keyStart, length);
		final String key = line.substring(keyStart, keyEnd);

		// TODO Handle extra text after key

		return new UndefineDirective(local, key);
	}

	private ImportDirective parseImport(String line, int directiveEnd, int length)
	{
		return new ImportDirective(Util.getArgument(line, directiveEnd, length));
	}

	private IncludeDirective parseInclude(String line, int directiveEnd, int length)
	{
		return new IncludeDirective(Util.getArgument(line, directiveEnd, length));
	}

	private int parseIf(DirectiveList list, int lineIndex, String line, int directiveEnd, int length, byte mode)
	{
		final IfDirective directive = new IfDirective(Util.getArgument(line, directiveEnd, length), mode);

		final DirectiveList thenBlock = new DirectiveList();
		final int thenEnd = this.parse(thenBlock, lineIndex + 1, IF_BLOCK);

		directive.setThenBlock(thenBlock);
		list.add(directive);

		if (!this.lines.get(thenEnd).contains("#else"))
		{
			return thenEnd;
		}

		final DirectiveList elseBlock = new DirectiveList();
		final int elseEnd = this.parse(elseBlock, thenEnd + 1, ELSE_BLOCK);

		directive.setElseBlock(elseBlock);

		return elseEnd;
	}

	private int parseFor(DirectiveList list, int lineIndex, String line, int directiveEnd, int length)
	{
		final String[] parts = Util.getArgument(line, directiveEnd, length).split("\\s*;\\s*");

		final String varName = parts.length > 0 ? parts[0] : "";
		final String start = parts.length > 1 ? parts[1] : "";
		final String end = parts.length > 2 ? parts[2] : "";

		final ForDirective directive = new ForDirective(varName, start, end);
		final DirectiveList action = new DirectiveList();

		final int endLine = this.parse(action, lineIndex + 1, FOR_BLOCK);

		directive.setAction(action);
		list.add(directive);

		return endLine;
	}

	private int parseForEach(DirectiveList list, int lineIndex, String line, int directiveEnd, int length)
	{
		final ForEachDirective directive = new ForEachDirective(Util.getArgument(line, directiveEnd, length));
		final DirectiveList action = new DirectiveList();

		final int end = this.parse(action, lineIndex + 1, FOR_BLOCK);

		directive.setAction(action);
		list.add(directive);

		return end;
	}
}
