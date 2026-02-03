package net.minecraft.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

import java.lang.reflect.Array;
import java.util.*;

public class Cartesian {
    public static <T> Iterable<T[]> cartesianProduct(Class<T> clazz, Iterable<? extends Iterable<? extends T>> sets) {
        return new Cartesian.Product(clazz, toArray(Iterable.class, sets));
    }

    public static <T> Iterable<List<T>> cartesianProduct(Iterable<? extends Iterable<? extends T>> sets) {
        return arraysAsLists(cartesianProduct(Object.class, sets));
    }

    private static <T> Iterable<List<T>> arraysAsLists(Iterable<Object[]> arrays) {
        return Iterables.transform(arrays, new Cartesian.GetList());
    }

    // STARFLOW-CHANGE
    @SuppressWarnings("unchecked")
    private static <T> T[] toArray(Class<? super T> clazz, Iterable<? extends T> it) {
        List<T> list = Lists.newArrayList(it);
        return list.toArray(createArray(clazz, list.size()));
    }

    // STARFLOW-CHANGE
    @SuppressWarnings("unchecked")
    private static <T> T[] createArray(Class<? super T> clazz, int length) {
        return (T[]) Array.newInstance(clazz, length);
    }

    static class GetList<T> implements Function<Object[], List<T>> {
        private GetList() {
        }

        public List<T> apply(Object[] p_apply_1_) {
            return Arrays.asList((T[]) p_apply_1_);
        }
    }

    static class Product<T> implements Iterable<T[]> {
        private final Class<T> clazz;
        private final Iterable<? extends T>[] iterables;

        private Product(Class<T> clazz, Iterable<? extends T>[] iterables) {
            this.clazz = clazz;
            this.iterables = iterables;
        }

        // STARFLOW-CHANGE
        public Iterator<T[]> iterator() {
            if (0 >= iterables.length) {
                // 创建一个空的 T[] 数组
                T[] empty = Cartesian.createArray(this.clazz, 0);
                // 直接用这个 T[] 构造 singleton list
                return Collections.singletonList(empty).iterator();
            } else {
                return new Cartesian.Product.ProductIterator<>(this.clazz, this.iterables);
            }
        }

        static class ProductIterator<T> extends UnmodifiableIterator<T[]> {
            private int index;
            private final Iterable<? extends T>[] iterables;
            private final Iterator<? extends T>[] iterators;
            private final T[] results;

            private ProductIterator(Class<T> clazz, Iterable<? extends T>[] iterables) {
                this.index = -2;
                this.iterables = iterables;
                this.iterators = Cartesian.createArray(Iterator.class, this.iterables.length);

                for (int i = 0; i < this.iterables.length; ++i) {
                    this.iterators[i] = iterables[i].iterator();
                }

                this.results = Cartesian.createArray(clazz, this.iterators.length);
            }

            private void endOfData() {
                this.index = -1;
                Arrays.fill(this.iterators, null);
                Arrays.fill(this.results, null);
            }

            public boolean hasNext() {
                if (-2 == index) {
                    this.index = 0;

                    for (Iterator<? extends T> iterator1 : this.iterators) {
                        if (!iterator1.hasNext()) {
                            this.endOfData();
                            break;
                        }
                    }

                    return true;
                } else {
                    if (this.index >= this.iterators.length) {
                        for (this.index = this.iterators.length - 1; 0 <= index; --this.index) {
                            Iterator<? extends T> iterator = this.iterators[this.index];

                            if (iterator.hasNext()) {
                                break;
                            }

                            if (0 == index) {
                                this.endOfData();
                                break;
                            }

                            iterator = this.iterables[this.index].iterator();
                            this.iterators[this.index] = iterator;

                            if (!iterator.hasNext()) {
                                this.endOfData();
                                break;
                            }
                        }
                    }

                    return 0 <= index;
                }
            }

            public T[] next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    while (this.index < this.iterators.length) {
                        this.results[this.index] = this.iterators[this.index].next();
                        ++this.index;
                    }

                    return this.results.clone();
                }
            }
        }
    }
}