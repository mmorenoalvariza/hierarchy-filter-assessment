# Code Review — `SimpleCache<K, V>`

## Code under review

```java
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCache<K, V> {
    private final ConcurrentHashMap<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final long ttlMs = 60000; // 1 minute

    public static class CacheEntry<V> {
        private final V value;
        private final long timestamp;

        public CacheEntry(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public V getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
    }

    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null) {
            if (System.currentTimeMillis() - entry.getTimestamp() < ttlMs) {
                return entry.getValue();
            }
        }
        return null;
    }

    public int size() {
        return cache.size();
    }
}
```

## PR Review commets:

1. **Memory leak — expired entries are never evicted.** `get()` returns `null` for an expired
   entry but leaves it in the underlying map. Under hundreds of writes/sec, the map grows unbounded 
   over time, increasing heap
   usage, until an eventual `OutOfMemoryError`.
2. **`size()` is misleading.** It returns the count of *all* entries in the map, including
   already-expired ones.
3. There is no `LRU/LFU/size-based eviction`.
4. **`System.currentTimeMillis()` is not suited for this task, use `System.nanoTime()`.
5. **TTL is hardcoded.** So, it cannot be overridden per instance, it will need a code change and
   redeploy to update it. It should come from an Env variable or remote config server.
6. **No protection against the "thundering herd".** When a hot key expires,
   the dozens of concurrent threads reading it will all observe `null` simultaneously, and
   every one of them will independently hit the upstream system (DB, downstream
   service, etc.) to repopulate the value.
7. **No `remove` / `invalidate` API.** When the application knows a value has become stale
   (e.g. after writing through a related update), there is no way to evict it. The caller
   has to wait up to the full TTL for the cache to refresh on its own.
8. **No metrics / observability.** The cache exposes no hit rate, miss rate, eviction count,
   load latency, or effective live size. In a high-throughput cache, these are essential to
   tune capacity and TTL, and to diagnose performance regressions. **Impact:** no real way to
   know how the cache is performing
