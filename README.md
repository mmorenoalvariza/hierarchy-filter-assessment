# Senior Developer Assessment

Solution for a two-task take-home assessment:

1. **Task 1** — implement `HierarchyFilter.filter()` for a forest-shaped data structure stored
   as two parallel arrays in DFS-traversal order, plus comprehensive JUnit 5 tests.
2. **Task 2** — review the `SimpleCache` implementation for production readiness in a
   high-concurrency environment. The review lives in [`SimpleCache.md`](SimpleCache.md).

The original assignment had multiple classes in the same file. I split them into one class per file under a
package.

## Task 1 — Algorithm

Because the input is in DFS order, a single linear pass is sufficient. The implementation
keeps a small auxiliary array `keptAt[d]` recording, for each depth `d`, whether the most
recent ancestor at that depth was kept. When inspecting a node at depth `d`, the entire
ancestor chain has been kept iff its parent at depth `d - 1` was kept. When `d == 0`
the node is a root and has no ancestors.

## Task 1 — Assumptions

- The input `Hierarchy` is well-formed. The implementation does not validate it defensively.
- Passing `null` for either argument throws a `NullPointerException`.
- The result is always a new `ArrayBasedHierarchy`. The input is not mutated.

## Task 2

See [`SimpleCache.md`](SimpleCache.md).
