package com.assessment.hierarchy;

import java.util.Arrays;
import java.util.function.IntPredicate;

/**
 * Filters a {@link Hierarchy} so that a node is present in the result iff its node ID
 * passes the predicate <em>and</em> all of its ancestors pass it as well.
 *
 * <p>If a node is dropped, every descendant of that node is also dropped, regardless of
 * whether their own IDs pass the predicate.
 */
public final class HierarchyFilter {

    private HierarchyFilter() {
    }

    /**
     * Returns a new {@link Hierarchy} containing only the nodes that satisfy the rule above.
     * The input is not mutated.
     *
     * <p>Runs in {@code O(n)} time and uses {@code O(n)} auxiliary space, where {@code n} is
     * the number of nodes in the input hierarchy.
     *
     * <p>Algorithm: a single pass over the DFS-ordered input is sufficient. A small auxiliary
     * array {@code keptAt} tracks, for each depth {@code d}, whether the most recent ancestor
     * at that depth was kept. For the node currently being inspected at depth {@code d}, the
     * full ancestor chain is kept if its parent at depth {@code d - 1} was kept (the parent's
     * inclusion already implies, by induction, that every ancestor above it was kept too).
     *
     * @throws NullPointerException if {@code hierarchy} or {@code nodeIdPredicate} is {@code null}
     */
    public static Hierarchy filter(Hierarchy hierarchy, IntPredicate nodeIdPredicate) {
        int n = hierarchy.size();
        int[] returnIds = new int[n];
        int[] returnDephs = new int[n];
        boolean[] keptAt = new boolean[n + 1];
        int returnSize = 0;

        for (int i = 0; i < n; i++) {
            int d = hierarchy.depth(i);
            int id = hierarchy.nodeId(i);
            boolean parentChainKept = d == 0 || keptAt[d - 1];
            boolean shouldKeep = parentChainKept && nodeIdPredicate.test(id);
            keptAt[d] = shouldKeep;
            if (shouldKeep) {
                returnIds[returnSize] = id;
                returnDephs[returnSize] = d;
                returnSize++;
            }
        }

        return new ArrayBasedHierarchy(
            Arrays.copyOf(returnIds, returnSize),
            Arrays.copyOf(returnDephs, returnSize)
        );
    }
}
