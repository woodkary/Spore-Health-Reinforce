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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
    private final List<ItemStack> deafItemList=List.of(SporeEmptyInventory.deafItem);
    private SporeEmptyItemStackNonNullList(List<ItemStack> items, @Nullable ItemStack defaultValue) {
        super(items, defaultValue);
    }

    @Override
    public @NotNull ItemStack get(int index) {
        return SporeEmptyInventory.deafItem;
    }

    @Override
    public ItemStack set(int index, ItemStack element) {
        return SporeEmptyInventory.deafItem;
    }

    @Override
    public void add(int index, ItemStack element) {
    }

    @Override
    public ItemStack remove(int index) {
        this.clear();
        return SporeEmptyInventory.deafItem;
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
        return deafItemList.iterator();
    }

    @Override
    public ListIterator<ItemStack> listIterator() {
        return deafItemList.listIterator();
    }

    @Override
    public ListIterator<ItemStack> listIterator(int index) {
        return deafItemList.listIterator();
    }

    @Override
    public List<ItemStack> subList(int fromIndex, int toIndex) {
        return deafItemList;
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
        return deafItemList.toArray();
    }

    @Override
    public @NotNull <T> T[] toArray(@NotNull T[] a) {
        return deafItemList.toArray(a);
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
        return deafItemList.spliterator();
    }

    @Override
    public <T> T[] toArray(@NotNull IntFunction<T[]> generator) {
        return deafItemList.toArray(generator.apply(0));
    }

    @Override
    public boolean removeIf(@NotNull Predicate<? super ItemStack> filter) {
        this.clear();
        return true;
    }

    @Override
    public @NotNull Stream<ItemStack> stream() {
        return deafItemList.stream();
    }

    @Override
    public @NotNull Stream<ItemStack> parallelStream() {
        return deafItemList.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super ItemStack> action) {
        this.clear();
        deafItemList.forEach(action);
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return UUID.randomUUID().clockSequence();
    }

}
