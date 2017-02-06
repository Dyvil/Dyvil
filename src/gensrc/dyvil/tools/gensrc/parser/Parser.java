package dyvil.tools.gensrc.parser;

import dyvil.tools.gensrc.ast.Util;
import dyvil.tools.gensrc.ast.directive.*;
import dyvil.tools.gensrc.lang.I18n;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SemanticError;
import dyvil.tools.parsing.marker.Warning;
import dyvil.tools.parsing.position.CodePosition;
import dyvil.tools.parsing.source.Source;

public class Parser
{
	private static final int IF_BLOCK    = 1;
	private static final int ELSE_BLOCK  = 2;
	private static final int FOR_BLOCK   = 3;
	private static final int SCOPE_BLOCK = 4;

	private final Source     source;
	private final MarkerList markers;
	private final int        lineCount;

	public Parser(Source source, MarkerList markers)
	{
		this.source = source;
		this.lineCount = source.lineCount();
		this.markers = markers;
	}

	public DirectiveList parse()
	{
		DirectiveList list = new DirectiveList(this.lineCount);
		this.parse(list, 1, 0);
		return list;
	}

	private int parse(DirectiveList list, int lineNumber, int block)
	{
		for (; lineNumber < this.lineCount; lineNumber++)
		{
			final String line = this.source.getLine(lineNumber);
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
				list.add(this.parseInclude(line, lineNumber, hashIndex, directiveEnd, length));
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
				list.add(this.parseImport(line, lineNumber, hashIndex, directiveEnd, length));
				continue;
			case "scope":
				lineNumber = this.parseBlock(list, lineNumber);
				continue;
			case "endscope":

				if (block == SCOPE_BLOCK)
				{
					this.markers.add(new Warning(new CodePosition(lineNumber, directiveStart, directiveEnd),
					                             I18n.get("endscope.deprecated")));
					return lineNumber;
				}
				break;

			// ----- if Directives -----
			case "if":
				lineNumber = this.parseIf(list, lineNumber, line, directiveEnd, length, IfDirective.MODE_IF);
				continue;
			case "ifdef":
				lineNumber = this.parseIf(list, lineNumber, line, directiveEnd, length, IfDirective.MODE_IFDEF);
				continue;
			case "ifndef":
				lineNumber = this.parseIf(list, lineNumber, line, directiveEnd, length, IfDirective.MODE_IFNDEF);
				continue;
			case "else":
				if (block == IF_BLOCK)
				{
					return lineNumber;
				}
				break;
			case "endif":
				if (block == IF_BLOCK || block == ELSE_BLOCK)
				{
					this.markers.add(new Warning(new CodePosition(lineNumber, directiveStart, directiveEnd),
					                             I18n.get("endif.deprecated")));
					return lineNumber;
				}
				break;

			// ----- for Directives -----
			case "for":
				lineNumber = this.parseFor(list, lineNumber, line, hashIndex, directiveEnd, length);
				continue;
			case "foreach":
				lineNumber = this.parseForEach(list, lineNumber, line, hashIndex, directiveEnd, length);
				continue;
			case "endfor":
				if (block == FOR_BLOCK)
				{
					this.markers.add(new Warning(new CodePosition(lineNumber, directiveStart, directiveEnd),
					                             I18n.get("endfor.deprecated")));
					return lineNumber;
				}
				break;

			case "end":
				if (block != 0)
				{
					return lineNumber;
				}
				break;
			}

			this.markers.add(new SemanticError(new CodePosition(lineNumber, directiveStart, directiveEnd),
			                                   I18n.get("directive.invalid", directive)));
		}

		return lineNumber;
	}

	private int parseBlock(DirectiveList list, int start)
	{
		final ScopeDirective directive = new ScopeDirective();
		final DirectiveList block = new DirectiveList();

		final int end = this.parse(block, start + 1, SCOPE_BLOCK);

		directive.setBlock(block);
		list.add(directive);

		return end;
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

	private ImportDirective parseImport(String line, int lineNumber, int hashIndex, int directiveEnd, int length)
	{
		return new ImportDirective(new CodePosition(lineNumber, hashIndex, directiveEnd), Util.getArgument(line, directiveEnd, length));
	}

	private IncludeDirective parseInclude(String line, int lineNumber, int hashIndex, int directiveEnd, int length)
	{
		return new IncludeDirective(new CodePosition(lineNumber, hashIndex, directiveEnd), Util.getArgument(line, directiveEnd, length));
	}

	private int parseIf(DirectiveList list, int lineNumber, String line, int directiveEnd, int length, byte mode)
	{
		final IfDirective directive = new IfDirective(Util.getArgument(line, directiveEnd, length), mode);

		final DirectiveList thenBlock = new DirectiveList();
		final int thenEnd = this.parse(thenBlock, lineNumber + 1, IF_BLOCK);

		directive.setThenBlock(thenBlock);
		list.add(directive);

		if (!this.source.getLine(thenEnd).contains("#else"))
		{
			return thenEnd;
		}

		final DirectiveList elseBlock = new DirectiveList();
		final int elseEnd = this.parse(elseBlock, thenEnd + 1, ELSE_BLOCK);

		directive.setElseBlock(elseBlock);

		return elseEnd;
	}

	private int parseFor(DirectiveList list, int lineNumber, String line, int hashIndex, int directiveEnd, int length)
	{
		final CodePosition position = new CodePosition(lineNumber, hashIndex, directiveEnd);

		final String[] parts = Util.getArgument(line, directiveEnd, length).split("\\s*;\\s*");

		if (parts.length != 3)
		{
			this.markers
				.add(new SemanticError(position, I18n.get("for.syntax")));
			return this.parseBlock(list, lineNumber);
		}

		final String varName = parts[0];
		final String start = parts[1];
		final String end = parts[2];

		final ForDirective directive = new ForDirective(position, varName, start, end);
		final DirectiveList action = new DirectiveList();

		final int endLine = this.parse(action, lineNumber + 1, FOR_BLOCK);

		directive.setAction(action);
		list.add(directive);

		return endLine;
	}

	private int parseForEach(DirectiveList list, int lineNumber, String line, int hashIndex, int directiveEnd,
		                        int length)
	{
		final ForEachDirective directive = new ForEachDirective(new CodePosition(lineNumber, hashIndex, directiveEnd),
		                                                        Util.getArgument(line, directiveEnd, length));
		final DirectiveList action = new DirectiveList();

		final int end = this.parse(action, lineNumber + 1, FOR_BLOCK);

		directive.setAction(action);
		list.add(directive);

		return end;
	}
}
