package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.CompilerState;


public interface IField extends IASTObject, INamed, ITyped, IModified, IAnnotatable, IValued
{
	@Override
	public IField applyState(CompilerState state);
}
