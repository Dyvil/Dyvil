package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class CaseClassPattern extends Pattern implements IPatternList
{
	protected IType type;
	protected IPattern[] patterns = new IPattern[2];
	protected int patternCount;

	// Metadata
	private IMethod[] getterMethods;
	
	public CaseClassPattern(ICodePosition position)
	{
		this.position = position;
	}

	public CaseClassPattern(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}

	@Override
	public int getPatternType()
	{
		return CASE_CLASS;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		if (!type.isSuperTypeOf(this.type))
		{
			return null;
		}
		
		final IClass caseClass = this.type.getTheClass();
		if (caseClass == null)
		{
			return this; // skip
		}
		
		final int paramCount = caseClass.parameterCount();
		if (this.patternCount != paramCount)
		{
			final Marker marker = MarkerMessages
					.createMarker(this.position, "pattern.class.count", this.type.toString());
			marker.addInfo(MarkerMessages.getMarker("pattern.class.count.pattern", this.patternCount));
			marker.addInfo(MarkerMessages.getMarker("pattern.class.count.class", paramCount));
			markers.add(marker);
			return this;
		}
		
		for (int i = 0; i < paramCount; i++)
		{
			final IPattern pattern = this.patterns[i];
			final IParameter param = caseClass.getParameter(i);

			if (param.getAccessLevel() != Modifiers.PUBLIC)
			{
				this.checkMethodAccess(markers, caseClass, param, i, paramCount, pattern);
			}

			final IType paramType = param.getType().getConcreteType(type);
			final IPattern typedPattern = pattern.withType(paramType, markers);
			
			if (typedPattern == null)
			{
				final Marker marker = MarkerMessages.createMarker(this.position, "pattern.class.type", param.getName());
				marker.addInfo(MarkerMessages.getMarker("pattern.type", pattern.getType()));
				marker.addInfo(MarkerMessages.getMarker("classparameter.type", paramType));
				markers.add(marker);
			}
			else
			{
				this.patterns[i] = typedPattern;
			}
		}
		
		if (type.classEquals(this.type))
		{
			// No additional type check required
			return this;
		}
		return new TypeCheckPattern(this, type, this.type);
	}

	public void checkMethodAccess(MarkerList markers, IClass caseClass, IParameter param, int paramIndex, int paramCount, IPattern pattern)
	{
		final IMethod accessMethod = IContext.resolveMethod(caseClass, null, param.getName(), null); // find by name
		if (accessMethod != null)
		{
			if (this.getterMethods == null)
			{
				this.getterMethods = new IMethod[paramCount];
			}

			this.getterMethods[paramIndex] = accessMethod;
		}
		else
		{
			Marker marker = MarkerMessages.createError(pattern.getPosition(), "pattern.class.access", param.getName());
			marker.addInfo(MarkerMessages.getMarker("pattern.class.access.type", caseClass.getFullName()));
			markers.add(marker);
		}
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(this.type);
	}
	
	@Override
	public int patternCount()
	{
		return 0;
	}
	
	@Override
	public void setPattern(int index, IPattern pattern)
	{
		this.patterns[index] = pattern;
	}
	
	@Override
	public void addPattern(IPattern pattern)
	{
		final int index = this.patternCount++;
		if (index >= this.patterns.length)
		{
			final IPattern[] temp = new IPattern[index + 1];
			System.arraycopy(this.patterns, 0, temp, 0, this.patterns.length);
			this.patterns = temp;
		}
		this.patterns[index] = pattern;
	}
	
	@Override
	public IPattern getPattern(int index)
	{
		return this.patterns[index];
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.patternCount; i++)
		{
			final IDataMember field = this.patterns[i].resolveField(name);
			if (field != null)
			{
				return field;
			}
		}
		return null;
	}
	
	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		
		for (int i = 0; i < this.patternCount; i++)
		{
			this.patterns[i] = this.patterns[i].resolve(markers, context);
		}
		
		return this;
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex < 0)
		{
			varIndex = writer.localCount();
			writer.writeVarInsn(Opcodes.ASTORE, varIndex);
		}
		
		final IClass caseClass = this.type.getTheClass();
		final int lineNumber = this.getLineNumber();

		for (int i = 0; i < this.patternCount; i++)
		{
			if (this.patterns[i].getPatternType() == WILDCARD)
			{
				// Skip wildcard patterns
				continue;
			}

			// Load the instance
			writer.writeVarInsn(Opcodes.ALOAD, varIndex);

			final IType patternType = this.patterns[i].getType();
			final IMethod method = this.getterMethods[i];

			if (method != null)
			{
				// Invoke the getter method
				method.writeInvoke(writer, null, EmptyArguments.INSTANCE, ITypeContext.DEFAULT, lineNumber);
				method.getType().writeCast(writer, patternType, lineNumber);
			}
			else
			{
				final IDataMember field = caseClass.getParameter(i);

				// Get the field value
				field.writeGet(writer, null, lineNumber);
				field.getType().writeCast(writer, patternType, lineNumber);
			}

			// Check the pattern
			this.patterns[i].writeInvJump(writer, -1, elseLabel);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');

		Util.astToString(prefix, this.patterns, this.patternCount, Formatting.getSeparator("parameters.separator", ','),
		                 buffer);

		if (Formatting.getBoolean("parameters.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');
	}
}
