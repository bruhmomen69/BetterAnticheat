package better.anticheat.core.util.type.fastlist;

import it.unimi.dsi.fastutil.objects.*;

import java.io.Serial;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;

/**
 * ObjectArrayList that does not provide any order consistency guarantees
 *
 * @param <K>
 */
public class FastObjectArrayList<K> extends AbstractObjectList<K> implements RandomAccess, Cloneable, java.io.Serializable {
    /**
     * The initial default capacity of an array list.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 10;
    @Serial
    private static final long serialVersionUID = -7046029254386353131L;
    private static final Collector<Object, ?, FastObjectArrayList<Object>> TO_LIST_COLLECTOR = Collector.of(FastObjectArrayList::new, FastObjectArrayList::add, FastObjectArrayList::combine);
    /**
     * Whether the backing array was passed to {@code wrap()}. In this case, we must reallocate with the
     * same type of array.
     */
    protected final boolean wrapped;
    /**
     * The backing array.
     */
    protected transient K[] a;
    /**
     * The current actual size of the list (never greater than the backing-array length).
     */
    protected int size;

    /**
     * Creates a new array list using a given array.
     *
     * <p>
     * This constructor is only meant to be used by the wrapping methods.
     *
     * @param a the array that will be used to back this array list.
     */
    protected FastObjectArrayList(final K[] a, @SuppressWarnings("unused") boolean wrapped) {
        this.a = a;
        this.wrapped = wrapped;
    }

    /**
     * Creates a new array list with given capacity.
     *
     * @param capacity the initial capacity of the array list (may be 0).
     */
    public FastObjectArrayList(final int capacity) {
        initArrayFromCapacity(capacity);
        this.wrapped = false;
    }

    /**
     * Creates a new array list with {@link #DEFAULT_INITIAL_CAPACITY} capacity.
     */
    @SuppressWarnings("unchecked")
    public FastObjectArrayList() {
        a = (K[]) ObjectArrays.DEFAULT_EMPTY_ARRAY; // We delay allocation
        wrapped = false;
    }

    /**
     * Creates a new array list and fills it with a given collection.
     *
     * @param c a collection that will be used to fill the array list.
     */
    public FastObjectArrayList(final Collection<? extends K> c) {
        if (c instanceof FastObjectArrayList) {
            a = copyArrayFromSafe((FastObjectArrayList<? extends K>) c);
            size = a.length;
        } else {
            initArrayFromCapacity(c.size());
            if (c instanceof ObjectList) {
                ((ObjectList<? extends K>) c).getElements(0, a, 0, size = c.size());
            } else {
                size = ObjectIterators.unwrap(c.iterator(), a);
            }
        }
        this.wrapped = false;
    }

    /**
     * Creates a new array list and fills it with a given type-specific collection.
     *
     * @param c a type-specific collection that will be used to fill the array list.
     */
    public FastObjectArrayList(final ObjectCollection<? extends K> c) {
        if (c instanceof FastObjectArrayList) {
            a = copyArrayFromSafe((FastObjectArrayList<? extends K>) c);
            size = a.length;
        } else {
            initArrayFromCapacity(c.size());
            if (c instanceof ObjectList) {
                ((ObjectList<? extends K>) c).getElements(0, a, 0, size = c.size());
            } else {
                size = ObjectIterators.unwrap(c.iterator(), a);
            }
        }
        this.wrapped = false;
    }

    /**
     * Creates a new array list and fills it with a given type-specific list.
     *
     * @param l a type-specific list that will be used to fill the array list.
     */
    public FastObjectArrayList(final ObjectList<? extends K> l) {
        if (l instanceof FastObjectArrayList) {
            a = copyArrayFromSafe((FastObjectArrayList<? extends K>) l);
            size = a.length;
        } else {
            initArrayFromCapacity(l.size());
            l.getElements(0, a, 0, size = l.size());
        }
        this.wrapped = false;
    }

    /**
     * Creates a new array list and fills it with the elements of a given array.
     *
     * @param a an array whose elements will be used to fill the array list.
     */
    public FastObjectArrayList(final K[] a) {
        this(a, 0, a.length);
    }

    /**
     * Creates a new array list and fills it with the elements of a given array.
     *
     * @param a      an array whose elements will be used to fill the array list.
     * @param offset the first element to use.
     * @param length the number of elements to use.
     */
    public FastObjectArrayList(final K[] a, final int offset, final int length) {
        this(length);
        System.arraycopy(a, offset, this.a, 0, length);
        size = length;
    }

    /**
     * Creates a new array list and fills it with the elements returned by an iterator..
     *
     * @param i an iterator whose returned elements will fill the array list.
     */
    public FastObjectArrayList(final Iterator<? extends K> i) {
        this();
        while (i.hasNext()) this.add((i.next()));
    }

    /**
     * Creates a new array list and fills it with the elements returned by a type-specific iterator..
     *
     * @param i a type-specific iterator whose returned elements will fill the array list.
     */
    public FastObjectArrayList(final ObjectIterator<? extends K> i) {
        this();
        while (i.hasNext()) this.add(i.next());
    }

    /**
     * Ensures that the component type of the given array is the proper type. This is irrelevant for
     * primitive types, so it will just do a trivial copy. But for Reference types, you can have a
     * {@code String[]} masquerading as an {@code Object[]}, which is a case we need to prepare for
     * because we let the user give an array to use directly with {@link #wrap}.
     */
    @SuppressWarnings("unchecked")
    private static final <K> K[] copyArraySafe(K[] a, int length) {
        if (length == 0) return (K[]) ObjectArrays.EMPTY_ARRAY;
        return (K[]) Arrays.copyOf(a, length, Object[].class);
    }

    private static final <K> K[] copyArrayFromSafe(FastObjectArrayList<K> l) {
        return copyArraySafe(l.a, l.size);
    }

    /**
     * Wraps a given array into an array list of given size.
     *
     * <p>
     * Note it is guaranteed that the type of the array returned by {@link #elements()} will be the same
     * (see the comments in the class documentation).
     *
     * @param a      an array to wrap.
     * @param length the length of the resulting array list.
     * @return a new array list of the given size, wrapping the given array.
     */
    public static <K> FastObjectArrayList<K> wrap(final K[] a, final int length) {
        if (length > a.length)
            throw new IllegalArgumentException("The specified length (" + length + ") is greater than the array size (" + a.length + ")");
        final FastObjectArrayList<K> l = new FastObjectArrayList<>(a, true);
        l.size = length;
        return l;
    }

    /**
     * Wraps a given array into an array list.
     *
     * <p>
     * Note it is guaranteed that the type of the array returned by {@link #elements()} will be the same
     * (see the comments in the class documentation).
     *
     * @param a an array to wrap.
     * @return a new array list wrapping the given array.
     */
    public static <K> FastObjectArrayList<K> wrap(final K[] a) {
        return wrap(a, a.length);
    }

    /**
     * Creates a new empty array list.
     *
     * @return a new empty array list.
     */
    public static <K> FastObjectArrayList<K> of() {
        return new FastObjectArrayList<>();
    }

    /**
     * Creates an array list using an array of elements.
     *
     * @param init a the array the will become the new backing array of the array list.
     * @return a new array list backed by the given array.
     * @see #wrap
     */
    @SafeVarargs
    public static <K> FastObjectArrayList<K> of(final K... init) {
        return wrap(init);
    }

    /**
     * Returns a {@link Collector} that collects a {@code Stream}'s elements into a new ArrayList.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <K> Collector<K, ?, FastObjectArrayList<K>> toList() {
        return (Collector) TO_LIST_COLLECTOR;
    }

    public Object[] getRawArray() {
        return a;
    }

    @SuppressWarnings("unchecked")
    private void initArrayFromCapacity(final int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
        if (capacity == 0) a = (K[]) ObjectArrays.EMPTY_ARRAY;
        else a = (K[]) new Object[capacity];
    }

    /**
     * Returns the backing array of this list.
     *
     * <p>
     * If this array list was created by wrapping a given array, it is guaranteed that the type of the
     * returned array will be the same. Otherwise, the returned array will be of type {@link Object
     * Object[]} (in spite of the declared return type).
     *
     * <p>
     * <strong>Warning</strong>: This behaviour may cause (unfathomable) run-time errors if a method
     * expects an array actually of type {@code K[]}, but this methods returns an array of type
     * {@link Object Object[]}.
     *
     * @return the backing array.
     */
    public K[] elements() {
        return a;
    }

    // Collector wants a function that returns the collection being added to.
    FastObjectArrayList<K> combine(FastObjectArrayList<? extends K> toAddFrom) {
        addAll(toAddFrom);
        return this;
    }

    /**
     * Ensures that this array list can contain the given number of entries without resizing.
     *
     * @param capacity the new minimum capacity for this array list.
     */
    @SuppressWarnings("unchecked")
    public void ensureCapacity(final int capacity) {
        if (capacity <= a.length || (a == ObjectArrays.DEFAULT_EMPTY_ARRAY && capacity <= DEFAULT_INITIAL_CAPACITY))
            return;
        if (wrapped) a = ObjectArrays.ensureCapacity(a, capacity, size);
        else {
            if (capacity > a.length) {
                final Object[] t = new Object[capacity];
                System.arraycopy(a, 0, t, 0, size);
                a = (K[]) t;
            }
        }
        assert size <= a.length;
    }

    /**
     * Grows this array list, ensuring that it can contain the given number of entries without resizing,
     * and in case increasing the current capacity at least by a factor of 50%.
     *
     * @param capacity the new minimum capacity for this array list.
     */
    @SuppressWarnings("unchecked")
    private void grow(int capacity) {
        if (capacity <= a.length) return;
        if (a != ObjectArrays.DEFAULT_EMPTY_ARRAY)
            capacity = (int) Math.max(Math.min((long) a.length + (a.length >> 1), it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE), capacity);
        else if (capacity < DEFAULT_INITIAL_CAPACITY) capacity = DEFAULT_INITIAL_CAPACITY;
        if (wrapped) a = ObjectArrays.forceCapacity(a, capacity, size);
        else {
            final Object[] t = new Object[capacity];
            System.arraycopy(a, 0, t, 0, size);
            a = (K[]) t;
        }
        assert size <= a.length;
    }

    @Override
    public void add(final int index, final K k) {
        ensureIndex(index);
        grow(size + 1);
        if (index != size) System.arraycopy(a, index, a, index + 1, size - index);
        a[index] = k;
        size++;
        assert size <= a.length;
    }

    @Override
    public boolean add(final K k) {
        grow(size + 1);
        a[size++] = k;
        assert size <= a.length;
        return true;
    }

    @Override
    public K remove(final int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        final K old = a[index];
        size--;
        if (index != size) System.arraycopy(a, index + 1, a, index, size - index);
        a[size] = null;
        return old;
    }

    @Override
    public K set(final int index, final K k) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        K old = a[index];
        a[index] = k;
        return old;
    }

    @Override
    public boolean addAll(int index, final Collection<? extends K> c) {
        if (c instanceof ObjectList) {
            return addAll(index, (ObjectList<? extends K>) c);
        }
        ensureIndex(index);
        int n = c.size();
        if (n == 0) return false;
        grow(size + n);
        System.arraycopy(a, index, a, index + n, size - index);
        final Iterator<? extends K> i = c.iterator();
        size += n;
        while (n-- != 0) a[index++] = i.next();
        assert size <= a.length;
        return true;
    }

    @Override
    public ObjectListIterator<K> listIterator(final int index) {
        if (index > size | (size == 0 & index > 0) | index < 0)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + size + ")");

        return new ObjectListIterator<K>() {
            int pos = index, last = -1;

            @Override
            public boolean hasNext() {
                return pos < size;
            }

            @Override
            public K next() {
                if (!hasNext()) throw new NoSuchElementException();
                return a[last = pos++];
            }

            @Override
            public void forEachRemaining(final Consumer<? super K> action) {
                while (pos < size) {
                    action.accept(a[last = pos++]);
                }
            }

            @Override
            public K previous() {
                if (!hasPrevious()) throw new NoSuchElementException();
                return a[last = --pos];
            }

            @Override
            public boolean hasPrevious() {
                return pos > 0;
            }

            @Override
            public int nextIndex() {
                return pos;
            }

            @Override
            public int previousIndex() {
                return pos - 1;
            }

            @Override
            public void set(K k) {
                if (last == -1) throw new IllegalStateException();
                FastObjectArrayList.this.set(last, k);
            }

            @Override
            public void add(K k) {
                FastObjectArrayList.this.add(pos++, k);
                last = -1;
            }

            @Override
            public void remove() {
                if (last == -1) throw new IllegalStateException();
                FastObjectArrayList.this.remove(last);
                /* If the last operation was a next(), we are removing an element *before* us, and we must decrease pos correspondingly. */
                if (last < pos) pos--;
                last = -1;
            }

            @Override
            public int back(int n) {
                if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                final int remaining = pos;
                if (n < remaining) {
                    pos -= n;
                } else {
                    n = remaining;
                    pos = 0;
                }
                last = pos;
                return n;
            }

            @Override
            public int skip(int n) {
                if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                final int remaining = size - pos;
                if (n < remaining) {
                    pos += n;
                } else {
                    n = remaining;
                    pos = size;
                }
                last = pos - 1;
                return n;
            }
        };
    }

    @Override
    public int indexOf(final Object k) {
        for (int i = 0; i < size; i++) if (Objects.equals(k, a[i])) return i;
        return -1;
    }

    @Override
    public int lastIndexOf(final Object k) {
        for (int i = size; i-- != 0; ) if (Objects.equals(k, a[i])) return i;
        return -1;
    }

    @Override
    public void size(final int size) {
        if (size > a.length) a = ObjectArrays.forceCapacity(a, size, this.size);
        if (size > this.size) Arrays.fill(a, this.size, size, (null));
        else Arrays.fill(a, size, this.size, (null));
        this.size = size;
    }

    @Override
    public ObjectList<K> subList(int from, int to) {
        if (from == 0 && to == size()) return this;
        ensureIndex(from);
        ensureIndex(to);
        if (from > to)
            throw new IndexOutOfBoundsException("Start index (" + from + ") is greater than end index (" + to + ")");
        return new SubList(from, to);
    }

    @Override
    public void forEach(final Consumer<? super K> action) {
        for (int i = 0; i < size; ++i) {
            action.accept(a[i]);
        }
    }

    /**
     * Removes elements of this type-specific list using optimized system calls.
     *
     * @param from the start index (inclusive).
     * @param to   the end index (exclusive).
     */
    @Override
    public void removeElements(final int from, final int to) {
        it.unimi.dsi.fastutil.Arrays.ensureFromTo(size, from, to);
        System.arraycopy(a, to, a, from, size - to);
        size -= (to - from);
        int i = to - from;
        while (i-- != 0) a[size + i] = null;
    }

    /**
     * Adds elements to this type-specific list using optimized system calls.
     *
     * @param index  the index at which to add elements.
     * @param a      the array containing the elements.
     * @param offset the offset of the first element to add.
     * @param length the number of elements to add.
     */
    @Override
    public void addElements(final int index, final K[] a, final int offset, final int length) {
        ensureIndex(index);
        ObjectArrays.ensureOffsetLength(a, offset, length);
        grow(size + length);
        System.arraycopy(this.a, index, this.a, index + length, size - index);
        System.arraycopy(a, offset, this.a, index, length);
        size += length;
    }

    /**
     * Copies element of this type-specific list into the given array using optimized system calls.
     *
     * @param from   the start index (inclusive).
     * @param a      the destination array.
     * @param offset the offset into the destination array where to store the first element copied.
     * @param length the number of elements to be copied.
     */
    @Override
    public void getElements(final int from, final Object[] a, final int offset, final int length) {
        ObjectArrays.ensureOffsetLength(a, offset, length);
        System.arraycopy(this.a, from, a, offset, length);
    }

    /**
     * Sets elements to this type-specific list using optimized system calls.
     *
     * @param index  the index at which to start setting elements.
     * @param a      the array containing the elements.
     * @param offset the offset of the first element to add.
     * @param length the number of elements to add.
     */
    @Override
    public void setElements(final int index, final K[] a, final int offset, final int length) {
        ensureIndex(index);
        ObjectArrays.ensureOffsetLength(a, offset, length);
        if (index + length > size)
            throw new IndexOutOfBoundsException("End index (" + (index + length) + ") is greater than list size (" + size + ")");
        System.arraycopy(a, offset, this.a, index, length);
    }

    @Override
    public void clear() {
        Arrays.fill(a, 0, size, null);
        size = 0;
        assert size <= a.length;
    }

    @Override
    public Object[] toArray() {
        final int size = size();
        // A subtle part of the spec says the returned array must be Object[] exactly.
        if (size == 0) return it.unimi.dsi.fastutil.objects.ObjectArrays.EMPTY_ARRAY;
        return Arrays.copyOf(a, size, Object[].class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        if (a == null) {
            a = (T[]) new Object[size()];
        } else if (a.length < size()) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
        }
        System.arraycopy(this.a, 0, a, 0, size());
        if (a.length > size()) {
            a[size()] = null;
        }
        return a;
    }

    @SuppressWarnings({"unchecked", "unlikely-arg-type"})
    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof List)) return false;
        if (o instanceof FastObjectArrayList) {
            // Safe cast because we are only going to take elements from other list, never give them
            return equals((FastObjectArrayList<K>) o);
        }
        if (o instanceof FastObjectArrayList.SubList) {
            // Safe cast because we are only going to take elements from other list, never give them
            // Sublist has an optimized sub-array based comparison, reuse that.
            return o.equals(this);
        }
        return super.equals(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(final List<? extends K> l) {
        if (l instanceof FastObjectArrayList) {
            return compareTo((FastObjectArrayList<? extends K>) l);
        }
        if (l instanceof FastObjectArrayList.SubList) {
            // Must negate because we are inverting the order of the comparison.
            return -((SubList) l).compareTo(this);
        }
        return super.compareTo(l);
    }

    @Override
    public K get(final int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        return a[index];
    }

    public int indexOfExact(final Object k) {
        for (int i = 0; i < size; i++) if (k == a[i]) return i;
        return -1;
    }

    private K removeExact(final int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        final K old = a[index];

        size--;

        if (index != size) {
            a[index] = a[size];
        }

        a[size] = null;
        return old;
    }

    public boolean removeExact(final Object k) {
        int index = indexOfExact(k);
        if (index == -1) return false;
        removeExact(index);
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean remove(final Object k) {
        int index = indexOf(k);
        if (index == -1) return false;
        remove(index);
        assert size <= a.length;
        return true;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        final Object[] a = this.a;
        int j = 0;
        for (int i = 0; i < size; i++) if (!c.contains(a[i])) a[j++] = a[i];
        Arrays.fill(a, j, size, null);
        final boolean modified = size != j;
        size = j;
        return modified;
    }

    /**
     * Trims this array list so that the capacity is equal to the size.
     *
     * @see ArrayList#trimToSize()
     */
    public void trim() {
        trim(0);
    }

    /**
     * Trims the backing array if it is too large.
     * <p>
     * If the current array length is smaller than or equal to {@code n}, this method does nothing.
     * Otherwise, it trims the array length to the maximum between {@code n} and {@link #size()}.
     *
     * <p>
     * This method is useful when reusing lists. {@linkplain #clear() Clearing a list} leaves the array
     * length untouched. If you are reusing a list many times, you can call this method with a typical
     * size to avoid keeping around a very large array just because of a few large transient lists.
     *
     * @param n the threshold for the trimming.
     */
    @SuppressWarnings("unchecked")
    public void trim(final int n) {
        // TODO: use Arrays.trim() and preserve type only if necessary
        if (n >= a.length || size == a.length) return;
        final K[] t = (K[]) new Object[Math.max(n, size)];
        System.arraycopy(a, 0, t, 0, size);
        a = t;
        assert size <= a.length;
    }

    @Override
    public boolean removeIf(final Predicate<? super K> filter) {
        final K[] a = this.a;
        int j = 0;
        for (int i = 0; i < size; i++) if (!filter.test(a[i])) a[j++] = a[i];
        Arrays.fill(a, j, size, null);
        final boolean modified = size != j;
        size = j;
        return modified;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The returned spliterator is late-binding; it will track structural changes after the current
     * index, up until the first {@link java.util.Spliterator#trySplit() trySplit()}, at which point the
     * maximum index will be fixed. <br>
     * Structural changes before the current index or after the first
     * {@link java.util.Spliterator#trySplit() trySplit()} will result in unspecified behavior.
     */
    @Override
    public ObjectSpliterator<K> spliterator() {
        // If it wasn't for the possibility of the list being expanded or shrunk,
        // we could return SPLITERATORS.wrap(a, 0, size).
        return new Spliterator();
    }

    @Override
    public boolean addAll(final int index, final ObjectList<? extends K> l) {
        ensureIndex(index);
        final int n = l.size();
        if (n == 0) return false;
        grow(size + n);
        System.arraycopy(a, index, a, index + n, size - index);
        l.getElements(0, a, index, n);
        size += n;
        assert size <= a.length;
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void sort(final Comparator<? super K> comp) {
        if (comp == null) {
            ObjectArrays.stableSort(a, 0, size);
        } else {
            ObjectArrays.stableSort(a, 0, size, comp);
        }
    }

    @Override
    public void unstableSort(final Comparator<? super K> comp) {
        if (comp == null) {
            ObjectArrays.unstableSort(a, 0, size);
        } else {
            ObjectArrays.unstableSort(a, 0, size, comp);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public FastObjectArrayList<K> clone() {
        FastObjectArrayList<K> cloned = null;
        // Test for fastpath we can do if exactly an ArrayList
        if (getClass() == FastObjectArrayList.class) {
            // Preserve backwards compatibility and make new list have Object[] even if it was wrapped from some
            // subclass.
            cloned = new FastObjectArrayList<>(copyArraySafe(a, size), false);
            cloned.size = size;
        } else {
            try {
                cloned = (FastObjectArrayList<K>) super.clone();
            } catch (CloneNotSupportedException err) {
                // Can't happen
                throw new InternalError(err);
            }
            // Preserve backwards compatibility and make new list have Object[] even if it was wrapped from some
            // subclass.
            cloned.a = copyArraySafe(a, size);
            // We can't clear cloned.wrapped because it is final.
        }
        return cloned;
    }

    /**
     * Compares this type-specific array list to another one.
     *
     * @param l a type-specific array list.
     * @return true if the argument contains the same elements of this type-specific array list.
     * @apiNote This method exists only for sake of efficiency. The implementation inherited from the
     * abstract implementation would already work.
     */
    public boolean equals(final FastObjectArrayList<K> l) {
        // TODO When minimum version of Java becomes Java 9, use the Arrays.equals which takes bounds, which
        // is vectorized.
        if (l == this) return true;
        int s = size();
        if (s != l.size()) return false;
        final K[] a1 = a;
        final K[] a2 = l.a;
        if (a1 == a2 && s == l.size()) return true;
        while (s-- != 0) if (!Objects.equals(a1[s], a2[s])) return false;
        return true;
    }

    /**
     * Compares this array list to another array list.
     *
     * @param l an array list.
     * @return a negative integer, zero, or a positive integer as this list is lexicographically less
     * than, equal to, or greater than the argument.
     * @apiNote This method exists only for sake of efficiency. The implementation inherited from the
     * abstract implementation would already work.
     */
    @SuppressWarnings("unchecked")
    public int compareTo(final FastObjectArrayList<? extends K> l) {
        final int s1 = size(), s2 = l.size();
        final K[] a1 = a, a2 = l.a;
        // TODO When minimum version of Java becomes Java 9, use Arrays.compare, which vectorizes.
        K e1, e2;
        int r, i;
        for (i = 0; i < s1 && i < s2; i++) {
            e1 = a1[i];
            e2 = a2[i];
            if ((r = (((Comparable<K>) (e1)).compareTo(e2))) != 0) return r;
        }
        return i < s2 ? -1 : (i < s1 ? 1 : 0);
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
        for (int i = 0; i < size; i++) s.writeObject(a[i]);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        a = (K[]) new Object[size];
        for (int i = 0; i < size; i++) a[i] = (K) s.readObject();
    }

    private class SubList extends AbstractObjectList.ObjectRandomAccessSubList<K> {
        private static final long serialVersionUID = -3185226345314976296L;

        protected SubList(int from, int to) {
            super(FastObjectArrayList.this, from, to);
        }

        // Most of the inherited methods should be fine, but we can override a few of them for performance.
        // Needed because we can't access the parent class' instance variables directly in a different
        // instance of SubList.
        private K[] getParentArray() {
            return a;
        }

        @Override
        public K get(int i) {
            ensureRestrictedIndex(i);
            return a[i + from];
        }

        @Override
        public ObjectListIterator<K> listIterator(int index) {
            return new SubListIterator(index);
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new SubListSpliterator();
        }

        boolean contentsEquals(K[] otherA, int otherAFrom, int otherATo) {
            if (a == otherA && from == otherAFrom && to == otherATo) return true;
            if (otherATo - otherAFrom != size()) {
                return false;
            }
            int pos = from, otherPos = otherAFrom;
            // We have already assured that the two ranges are the same size, so we only need to check one
            // bound.
            // TODO When minimum version of Java becomes Java 9, use the Arrays.equals which takes bounds, which
            // is vectorized.
            // Make sure to split out the reference equality case when you do this.
            while (pos < to) if (!Objects.equals(a[pos++], otherA[otherPos++])) return false;
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null) return false;
            if (!(o instanceof List)) return false;
            if (o instanceof FastObjectArrayList) {
                @SuppressWarnings("unchecked")
                FastObjectArrayList<K> other = (FastObjectArrayList<K>) o;
                return contentsEquals(other.a, 0, other.size());
            }
            if (o instanceof FastObjectArrayList.SubList) {
                @SuppressWarnings("unchecked")
                SubList other = (SubList) o;
                return contentsEquals(other.getParentArray(), other.from, other.to);
            }
            return super.equals(o);
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compareTo(final List<? extends K> l) {
            if (l instanceof FastObjectArrayList) {
                @SuppressWarnings("unchecked")
                FastObjectArrayList<K> other = (FastObjectArrayList<K>) l;
                return contentsCompareTo(other.a, 0, other.size());
            }
            if (l instanceof FastObjectArrayList.SubList) {
                @SuppressWarnings("unchecked")
                SubList other = (SubList) l;
                return contentsCompareTo(other.getParentArray(), other.from, other.to);
            }
            return super.compareTo(l);
        }

        @SuppressWarnings("unchecked")
        int contentsCompareTo(K[] otherA, int otherAFrom, int otherATo) {
            // TODO When minimum version of Java becomes Java 9, use Arrays.compare, which vectorizes.
            K e1, e2;
            int r, i, j;
            for (i = from, j = otherAFrom; i < to && i < otherATo; i++, j++) {
                e1 = a[i];
                e2 = otherA[j];
                if ((r = (((Comparable<K>) (e1)).compareTo(e2))) != 0) return r;
            }
            return i < otherATo ? -1 : (i < to ? 1 : 0);
        }

        private final class SubListIterator extends ObjectIterators.AbstractIndexBasedListIterator<K> {
            // We are using pos == 0 to be 0 relative to SubList.from (meaning you need to do a[from + i] when
            // accessing array).
            SubListIterator(int index) {
                super(0, index);
            }

            @Override
            protected K get(int i) {
                return a[from + i];
            }

            @Override
            protected void remove(int i) {
                SubList.this.remove(i);
            }

            @Override
            protected int getMaxPos() {
                return to - from;
            }

            @Override
            public K next() {
                if (!hasNext()) throw new NoSuchElementException();
                return a[from + (lastReturned = pos++)];
            }

            @Override
            public void forEachRemaining(final Consumer<? super K> action) {
                final int max = to - from;
                while (pos < max) {
                    action.accept(a[from + (lastReturned = pos++)]);
                }
            }

            @Override
            protected void add(int i, K k) {
                SubList.this.add(i, k);
            }

            @Override
            protected void set(int i, K k) {
                SubList.this.set(i, k);
            }

            @Override
            public K previous() {
                if (!hasPrevious()) throw new NoSuchElementException();
                return a[from + (lastReturned = --pos)];
            }
        }

        private final class SubListSpliterator extends ObjectSpliterators.LateBindingSizeIndexBasedSpliterator<K> {
            // We are using pos == 0 to be 0 relative to real array 0
            SubListSpliterator() {
                super(from);
            }

            private SubListSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            protected int getMaxPosFromBackingStore() {
                return to;
            }

            @Override
            protected K get(int i) {
                return a[i];
            }

            @Override
            protected SubListSpliterator makeForSplit(int pos, int maxPos) {
                return new SubListSpliterator(pos, maxPos);
            }

            @Override
            public boolean tryAdvance(final Consumer<? super K> action) {
                if (pos >= getMaxPos()) return false;
                action.accept(a[pos++]);
                return true;
            }

            @Override
            public void forEachRemaining(final Consumer<? super K> action) {
                final int max = getMaxPos();
                while (pos < max) {
                    action.accept(a[pos++]);
                }
            }
        }
        // We don't override subList as we want AbstractList's "sub-sublist" nesting handling,
        // which would be tricky to do here.
        // TODO Do override it so array access isn't sent through N indirections.
        // This will likely mean making this class static.
    }

    // If you update this, you will probably want to update ArraySet as well
    private final class Spliterator implements ObjectSpliterator<K> {
        // Until we split, we will track the size of the list.
        // Once we split, then we stop updating on structural modifications.
        // Aka, size is late-binding.
        boolean hasSplit = false;
        int pos, max;

        public Spliterator() {
            this(0, FastObjectArrayList.this.size, false);
        }

        private Spliterator(int pos, int max, boolean hasSplit) {
            assert pos <= max : "pos " + pos + " must be <= max " + max;
            this.pos = pos;
            this.max = max;
            this.hasSplit = hasSplit;
        }

        private int getWorkingMax() {
            return hasSplit ? max : FastObjectArrayList.this.size;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super K> action) {
            if (pos >= getWorkingMax()) return false;
            action.accept(a[pos++]);
            return true;
        }

        @Override
        public void forEachRemaining(final Consumer<? super K> action) {
            for (final int max = getWorkingMax(); pos < max; ++pos) {
                action.accept(a[pos]);
            }
        }

        @Override
        public long estimateSize() {
            return getWorkingMax() - pos;
        }

        @Override
        public int characteristics() {
            return ObjectSpliterators.LIST_SPLITERATOR_CHARACTERISTICS;
        }

        @Override
        public long skip(long n) {
            if (n < 0) throw new IllegalArgumentException("Argument must be nonnegative: " + n);
            final int max = getWorkingMax();
            if (pos >= max) return 0;
            final int remaining = max - pos;
            if (n < remaining) {
                pos = it.unimi.dsi.fastutil.SafeMath.safeLongToInt(pos + n);
                return n;
            }
            n = remaining;
            pos = max;
            return n;
        }

        @Override
        public ObjectSpliterator<K> trySplit() {
            final int max = getWorkingMax();
            int retLen = (max - pos) >> 1;
            if (retLen <= 1) return null;
            // Update instance max with the last seen list size (if needed) before continuing
            this.max = max;
            int myNewPos = pos + retLen;
            int retMax = myNewPos;
            int oldPos = pos;
            this.pos = myNewPos;
            this.hasSplit = true;
            return new Spliterator(oldPos, retMax, true);
        }
    }
}
