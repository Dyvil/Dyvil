package dyvil.util;

import dyvil.annotation.mutating;
import dyvil.collection.*;

/**
 * Marker interface that marks types as <i>immutable</i>. This interface is
 * primarily designed to be used in conjunction with the {@link mutating}
 * annotation, which allows compile-time checking mutating accesses to immutable
 * types, especially {@link Collection collections}.
 * 
 * @see mutating
 * @see Collection
 * @see ImmutableCollection
 * @see ImmutableList
 * @see ImmutableMap
 * @see ImmutableMatrix
 * @author Clashsoft
 * @version 1.0
 */
public interface Immutable
{
}
