package com.Harbinger.Spore.Core.utils.inventory;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class SporeEmptyItemStackNonNullList extends NonNullList<ItemStack> {
    @SuppressWarnings("unchecked")
    public static final Class<? extends NonNullList<ItemStack>> nonNullListClass =
            (Class<? extends NonNullList<ItemStack>>) BytecodeUtil.resolveHiddenClassOrSelf(
                    SporeEmptyItemStackNonNullList.class,
                    List.class,
                    ItemStack.class
            );
    private static MethodHandle constructor;

    static{
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                nonNullListClass,
                SporeEmptyItemStackNonNullList.class,
                List.class,
                ItemStack.class
        );
    }

    public static NonNullList<ItemStack> newInstance(List<ItemStack> items,ItemStack itemStack) {
        constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                constructor,
                nonNullListClass,
                SporeEmptyItemStackNonNullList.class,
                List.class,
                ItemStack.class
        );
        if(constructor!=null){
            try{
                return (NonNullList<ItemStack>) constructor.invoke(items,itemStack);
            }catch (Throwable e){
                LogUtil.error("failed to initialize SporeEmptyItemStackNonNullList");
            }
        }
        return new SporeEmptyItemStackNonNullList(items,itemStack);
    }

    private SporeEmptyItemStackNonNullList(List<ItemStack> items, @Nullable ItemStack defaultValue) {
        super(items, defaultValue);
    }

    @Override
    public @NotNull ItemStack get(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack set(int index, ItemStack element) {
        return ItemStack.EMPTY;
    }

    @Override
    public void add(int index, ItemStack element) {
    }

    @Override
    public ItemStack remove(int index) {
        this.clear();
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(ItemStack itemStack) {
        return false;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public boolean addAll(int index, Collection<? extends ItemStack> c) {
        return false;
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return EmptyIterator.newInstance();
    }

    @Override
    public ListIterator<ItemStack> listIterator() {
        return EmptyIterator.newInstance();
    }

    @Override
    public ListIterator<ItemStack> listIterator(int index) {
        return EmptyIterator.newInstance();
    }

    @Override
    public List<ItemStack> subList(int fromIndex, int toIndex) {
        return List.of();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        this.clear();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        return new Object[]{};
    }

    @Override
    public @NotNull <T> T[] toArray(@NotNull T[] a) {
        return Collections.emptyList().toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        this.clear();
        return true;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends ItemStack> c) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        this.clear();
        return true;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        this.clear();
        return true;
    }

    @Override
    public void replaceAll(@NotNull UnaryOperator<ItemStack> operator) {
        this.clear();
    }

    @Override
    public void sort(@Nullable Comparator<? super ItemStack> c) {
        this.clear();
    }

    @Override
    public @NotNull Spliterator<ItemStack> spliterator() {
        return EmptySpliterator.newInstance();
    }

    @Override
    public <T> T[] toArray(@NotNull IntFunction<T[]> generator) {
        return Collections.emptyList().toArray(generator.apply(0));
    }

    @Override
    public boolean removeIf(@NotNull Predicate<? super ItemStack> filter) {
        this.clear();
        return true;
    }

    @Override
    public @NotNull Stream<ItemStack> stream() {
        return Stream.empty();
    }

    @Override
    public @NotNull Stream<ItemStack> parallelStream() {
        return Stream.empty();
    }

    @Override
    public void forEach(Consumer<? super ItemStack> action) {
        this.clear();
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return UUID.randomUUID().clockSequence();
    }

    private static final class EmptyIterator<E> implements ListIterator<E> {
        @SuppressWarnings("unchecked")
        private static final Class<? extends ListIterator<?>> emptyIteratorClass =
                (Class<? extends ListIterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(EmptyIterator.class);
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                emptyIteratorClass,
                EmptyIterator.class
        );

        public static <E> ListIterator<E> newInstance() {
            constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    emptyIteratorClass,
                    EmptyIterator.class
            );
            if (constructor != null) {
                try {
                    return (ListIterator<E>) constructor.invoke();
                } catch (Throwable t) {
                    LogUtil.errorf("failed to create hidden EmptyIterator, %s", t.getMessage());
                }
            }
            return new EmptyIterator<>();
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            throw new NoSuchElementException();
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public E previous() {
            throw new NoSuchElementException();
        }

        @Override
        public int nextIndex() {
            return 0;
        }

        @Override
        public int previousIndex() {
            return 0;
        }

        @Override
        public void remove() {
        }

        @Override
        public void set(E e) {
        }

        @Override
        public void add(E e) {
        }
    }

    private static final class EmptySpliterator<E> implements Spliterator<E> {
        @SuppressWarnings("unchecked")
        private static final Class<? extends Spliterator<?>> emptySpliteratorClass =
                (Class<? extends Spliterator<?>>) BytecodeUtil.resolveHiddenClassOrSelf(EmptySpliterator.class);
        private static MethodHandle constructor = MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                emptySpliteratorClass,
                EmptySpliterator.class
        );

        public static <E> EmptySpliterator<E> newInstance(){
            constructor=MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    emptySpliteratorClass,
                    EmptySpliterator.class
            );
            if (constructor != null) {
                try{
                    return (EmptySpliterator<E>) constructor.invoke();
                }catch (Throwable t){
                    LogUtil.errorf("failed to create hidden EmptySpliterator, %s", t.getMessage());
                }
            }
            return new EmptySpliterator<>();
        }

        @Override
        public boolean tryAdvance(Consumer<? super E> action) {
            return false;
        }

        @Override
        public Spliterator<E> trySplit() {
            return EmptySpliterator.newInstance();
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public int characteristics() {
            return 0;
        }
    }
}
