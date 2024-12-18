import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

public class CustomHashMap<K, V> implements CustomMap<K, V> {
  private static final int DEFAULT_CAPACITY = 4;
  private static final int MAX_CAPACITY = 1 << 30;
  private int currentCapacity;
  private static final float DEFAULT_LOAD_FACTOR = 0.75f;
  private float loadThreshold;
  private int elementCount;
  private LinkedList<Node<K, V>>[] buckets;

  public CustomHashMap() {
    this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
  }

  public CustomHashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  public CustomHashMap(int initialCapacity, float loadThreshold) {
    this.currentCapacity = (initialCapacity > MAX_CAPACITY) ? MAX_CAPACITY : closestPowerOfTwo(initialCapacity);
    this.loadThreshold = loadThreshold;
    buckets = new LinkedList[currentCapacity];
  }

  @Override
  public void clear() {
    elementCount = 0;
    resetBuckets();
  }

  @Override
  public boolean containsKey(K key) {
    int index = hashIndex(key.hashCode());
    if (buckets[index] != null) {
      for (Node<K, V> node : buckets[index]) {
        if (node.getKey().equals(key)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean containsValue(V value) {
    for (int i = 0; i < currentCapacity; i++) {
      if (buckets[i] != null) {
        for (Node<K, V> node : buckets[i]) {
          if (node.getValue().equals(value)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public Set<CustomMap.Entry<K, V>> entrySet() {
    Set<CustomMap.Entry<K, V>> entries = new HashSet<>();
    for (LinkedList<Node<K, V>> bucket : buckets) {
      if (bucket != null) {
        entries.addAll(bucket);
      }
    }
    return entries;
  }

  @Override
  public V get(K key) {
    int index = hashIndex(key.hashCode());
    if (buckets[index] != null) {
      for (Node<K, V> node : buckets[index]) {
        if (node.getKey().equals(key)) {
          return node.getValue();
        }
      }
    }
    return null;
  }

  @Override
  public boolean isEmpty() {
    return elementCount == 0;
  }

  @Override
  public Set<K> keySet() {
    Set<K> keys = new HashSet<>();
    for (LinkedList<Node<K, V>> bucket : buckets) {
      if (bucket != null) {
        for (Node<K, V> node : bucket) {
          keys.add(node.getKey());
        }
      }
    }
    return keys;
  }

  @Override
  public V put(K key, V value) {
    if (get(key) != null) {
      int index = hashIndex(key.hashCode());
      for (Node<K, V> node : buckets[index]) {
        if (node.getKey().equals(key)) {
          V oldValue = node.getValue();
          node.setValue(value);
          return oldValue;
        }
      }
    }

    if (elementCount >= currentCapacity * loadThreshold) {
      if (currentCapacity == MAX_CAPACITY) {
        throw new RuntimeException("Exceeded maximum capacity");
      }
      resize();
    }

    int index = hashIndex(key.hashCode());
    if (buckets[index] == null) {
      buckets[index] = new LinkedList<>();
    }

    buckets[index].add(new Node<>(key, value));
    elementCount++;
    return value;
  }

  @Override
  public void remove(K key) {
    int index = hashIndex(key.hashCode());
    if (buckets[index] != null) {
      buckets[index].removeIf(node -> node.getKey().equals(key));
      elementCount--;
    }
  }

  @Override
  public int size() {
    return elementCount;
  }

  @Override
  public Set<V> values() {
    Set<V> values = new HashSet<>();
    for (LinkedList<Node<K, V>> bucket : buckets) {
      if (bucket != null) {
        for (Node<K, V> node : bucket) {
          values.add(node.getValue());
        }
      }
    }
    return values;
  }

  private int hashIndex(int hashCode) {
    return hashFunction(hashCode) & (currentCapacity - 1);
  }

  private static int hashFunction(int hash) {
    hash ^= (hash >>> 20) ^ (hash >>> 12);
    return hash ^ (hash >>> 7) ^ (hash >>> 4);
  }

  private int closestPowerOfTwo(int capacity) {
    int power = 1;
    while (power < capacity) {
      power <<= 1;
    }
    return power;
  }

  private void resetBuckets() {
    for (int i = 0; i < currentCapacity; i++) {
      if (buckets[i] != null) {
        buckets[i].clear();
      }
    }
  }

  private void resize() {
    Set<Node<K, V>> entries = new HashSet<>();
    for (LinkedList<Node<K, V>> bucket : buckets) {
      if (bucket != null) {
        entries.addAll(bucket);
      }
    }
    currentCapacity <<= 1;
    buckets = new LinkedList[currentCapacity];
    elementCount = 0;
    for (Node<K, V> node : entries) {
      put(node.getKey(), node.getValue());
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("[");
    for (LinkedList<Node<K, V>> bucket : buckets) {
      if (bucket != null) {
        for (Node<K, V> node : bucket) {
          result.append(node).append(", ");
        }
      }
    }
    if (result.length() > 1) {
      result.setLength(result.length() - 2);
    }
    result.append("]");
    return result.toString();
  }

  private static class Node<K, V> implements CustomMap.Entry<K, V> {
    private K key;
    private V value;

    public Node(K key, V value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }

    public void setValue(V value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return key + "=" + value;
    }
  }
}
