package com.assessment.hierarchy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HierarchyFilterTest {

    private static Hierarchy hierarchy(int[] nodeIds, int[] depths) {
        return new ArrayBasedHierarchy(nodeIds, depths);
    }

    private static void assertHierarchyEquals(Hierarchy expected, Hierarchy actual) {
        assertEquals(expected.formatString(), actual.formatString());
    }

    @Test
    void assignmentExample() {
        Hierarchy unfiltered = hierarchy(
            new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},
            new int[]{0, 1, 2, 3, 1, 0, 1, 0, 1, 1, 2}
        );
        Hierarchy expected = hierarchy(
            new int[]{1, 2, 5, 8, 10, 11},
            new int[]{0, 1, 1, 0, 1, 2}
        );
        assertHierarchyEquals(expected, HierarchyFilter.filter(unfiltered, id -> id % 3 != 0));
    }

    @Test
    void nullHierarchyThrowsNpe() {
        assertThrows(NullPointerException.class,
                () -> HierarchyFilter.filter(null, id -> true));
    }

    @Test
    void nullPredicateThrowsNpe() {
        Hierarchy input = hierarchy(new int[]{1}, new int[]{0});
        assertThrows(NullPointerException.class,
                () -> HierarchyFilter.filter(input, null));
    }

    @Test
    void emptyHierarchyYieldsEmptyResult() {
        Hierarchy empty = hierarchy(new int[0], new int[0]);
        assertHierarchyEquals(empty, HierarchyFilter.filter(empty, id -> true));
    }

    @Test
    void predicateAlwaysTruePreservesEverything() {
        Hierarchy input = hierarchy(
            new int[]{1, 2, 3, 4, 5},
            new int[]{0, 1, 2, 1, 0}
        );
        assertHierarchyEquals(input, HierarchyFilter.filter(input, id -> true));
    }

    @Test
    void predicateAlwaysFalseDropsEverything() {
        Hierarchy input = hierarchy(
            new int[]{1, 2, 3, 4, 5},
            new int[]{0, 1, 2, 1, 0}
        );
        Hierarchy expected = hierarchy(new int[0], new int[0]);
        assertHierarchyEquals(expected, HierarchyFilter.filter(input, id -> false));
    }

    @Test
    void rootFailingDropsEntireTreeEvenIfDescendantsWouldPass() {
        Hierarchy input = hierarchy(
            new int[]{1, 2, 3},
            new int[]{0, 1, 2}
        );
        Hierarchy expected = hierarchy(new int[0], new int[0]);
        assertHierarchyEquals(expected, HierarchyFilter.filter(input, id -> id != 1));
    }

    @Test
    void failingRootDoesNotAffectSiblingTree() {
        // forest:
        //   1
        //   - 2
        //   3
        //   - 4
        Hierarchy input = hierarchy(
            new int[]{1, 2, 3, 4},
            new int[]{0, 1, 0, 1}
        );
        Hierarchy expected = hierarchy(
            new int[]{3, 4},
            new int[]{0, 1}
        );
        assertHierarchyEquals(expected, HierarchyFilter.filter(input, id -> id != 1));
    }

    @Test
    void deepChainTruncatesAtFirstFailure() {
        // 1 - 2 - 3 - 4 - 5
        Hierarchy input = hierarchy(
            new int[]{1, 2, 3, 4, 5},
            new int[]{0, 1, 2, 3, 4}
        );
        Hierarchy expected = hierarchy(
            new int[]{1, 2},
            new int[]{0, 1}
        );
        assertHierarchyEquals(expected, HierarchyFilter.filter(input, id -> id != 3));
    }


    @Test
    void droppingOneSubtreeDoesNotAffectSiblingSubtree() {
        // 1
        // - 2
        // - - 3
        // - 4
        // - - 5
        Hierarchy input = hierarchy(
            new int[]{1, 2, 3, 4, 5},
            new int[]{0, 1, 2, 1, 2}
        );
        Hierarchy expected = hierarchy(
            new int[]{1, 4, 5},
            new int[]{0, 1, 2}
        );
        assertHierarchyEquals(expected, HierarchyFilter.filter(input, id -> id != 2));
    }

    @Test
    void resultIsAFreshInstance() {
        Hierarchy input = hierarchy(new int[]{1}, new int[]{0});
        Hierarchy result = HierarchyFilter.filter(input, id -> true);
        assertNotSame(input, result);
    }

}
