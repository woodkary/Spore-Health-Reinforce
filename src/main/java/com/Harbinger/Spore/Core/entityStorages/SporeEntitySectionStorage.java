package com.Harbinger.Spore.Core.entityStorages;

import com.Harbinger.Spore.Core.utils.BytecodeUtil;
import com.Harbinger.Spore.Core.utils.LogUtil;
import com.Harbinger.Spore.Core.utils.MethodHandleUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.Visibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public final class SporeEntitySectionStorage<T extends EntityAccess> extends EntitySectionStorage<T> implements BiFunction<Long,EntitySection<T>,EntitySection<T>>, LongFunction<EntitySection<T>>, Predicate<EntitySection<T>> {
    public static final Class<? extends EntitySectionStorage<? extends EntityAccess>> entitySectionStorageClass = (Class<? extends EntitySectionStorage<? extends EntityAccess>>) BytecodeUtil.resolveHiddenClassOrSelf(
            SporeEntitySectionStorage.class,
            Class.class,
            Long2ObjectFunction.class
    );
    public SporeEntitySectionStorage(Class<T> p_156855_, Long2ObjectFunction<Visibility> p_156856_) {
        super(p_156855_, p_156856_);
    }

    @Override
    public EntitySection<T> getOrCreateSection(long p_156894_) {
        return this.sections.compute(p_156894_, this);
    }

    @Override
    public @Nullable EntitySection<T> getSection(long p_156896_) {
        return super.getSection(p_156896_);
    }

    @Override
    public EntitySection<T> createSection(long p_156902_) {
        return new SporeEntitySection<>(super.createSection(p_156902_));
    }

    @Override
    public EntitySection<T> apply(Long sectionKey, EntitySection<T> mapValue) {
        if(mapValue==null){
            return createSection(sectionKey);
        }
        if(mapValue.getClass()!=SporeEntitySection.class){
            return new SporeEntitySection<>(mapValue);
        }
        return mapValue;
    }
    @Override
    public Stream<EntitySection<T>> getExistingSectionsInChunk(long p_156889_) {
        LongStream var10000 = this.getExistingSectionPositionsInChunk(p_156889_);
        return NoCMEStream.newInstance(var10000.mapToObj(this).filter(this));
    }

    @Override
    public EntitySection<T> apply(long secKey) {
        return Objects.requireNonNull(this.sections).get(secKey);
    }

    @Override
    public boolean test(EntitySection<T> sec) {
        return Objects.nonNull(sec);
    }
    private static final class NoCMEStream<T extends EntityAccess> implements Stream<EntitySection<T>> {
        private static final Class<? extends Stream<EntitySection<? extends EntityAccess>>> streamClass= (Class<? extends Stream<EntitySection<? extends EntityAccess>>>) BytecodeUtil.resolveHiddenClassOrSelf(
                NoCMEStream.class,
                Stream.class
        );
        private static MethodHandle constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                null,
                streamClass,
                NoCMEStream.class,
                Stream.class
        );
        private static<T extends EntityAccess> Stream<EntitySection<T>> newInstance(Stream<EntitySection<T>> owner){
            constructor= MethodHandleUtil.INSTANCE.ensureConstructor(
                    constructor,
                    streamClass,
                    NoCMEStream.class,
                    Stream.class
            );
            if(constructor!=null){
                try{
                    return (Stream<EntitySection<T>>) constructor.invoke(owner);
                } catch (Throwable e) {
                    LogUtil.errorf("failed to new NOCMEStream %s",e.getMessage());
                }
            }
            return new NoCMEStream<>(owner);
        }
        private final Stream<EntitySection<T>> owner;
        private NoCMEStream(Stream<EntitySection<T>> owner) {
            this.owner = owner;
        }

        @Override
        public Stream<EntitySection<T>> filter(Predicate<? super EntitySection<T>> predicate) {
            return owner.filter(predicate);
        }

        @Override
        public <R> Stream<R> map(Function<? super EntitySection<T>, ? extends R> mapper) {
            return owner.map(mapper);
        }

        @Override
        public IntStream mapToInt(ToIntFunction<? super EntitySection<T>> mapper) {
            return owner.mapToInt(mapper);
        }

        @Override
        public LongStream mapToLong(ToLongFunction<? super EntitySection<T>> mapper) {
            return owner.mapToLong(mapper);
        }

        @Override
        public DoubleStream mapToDouble(ToDoubleFunction<? super EntitySection<T>> mapper) {
            return owner.mapToDouble(mapper);
        }

        @Override
        public <R> Stream<R> flatMap(Function<? super EntitySection<T>, ? extends Stream<? extends R>> mapper) {
            return owner.flatMap(mapper);
        }

        @Override
        public IntStream flatMapToInt(Function<? super EntitySection<T>, ? extends IntStream> mapper) {
            return owner.flatMapToInt(mapper);
        }

        @Override
        public LongStream flatMapToLong(Function<? super EntitySection<T>, ? extends LongStream> mapper) {
            return owner.flatMapToLong(mapper);
        }

        @Override
        public DoubleStream flatMapToDouble(Function<? super EntitySection<T>, ? extends DoubleStream> mapper) {
            return owner.flatMapToDouble(mapper);
        }

        @Override
        public Stream<EntitySection<T>> distinct() {
            return owner.distinct();
        }

        @Override
        public Stream<EntitySection<T>> sorted() {
            return owner.sorted();
        }

        @Override
        public Stream<EntitySection<T>> sorted(Comparator<? super EntitySection<T>> comparator) {
            return owner.sorted(comparator);
        }

        @Override
        public Stream<EntitySection<T>> peek(Consumer<? super EntitySection<T>> action) {
            return owner.peek(action);
        }

        @Override
        public Stream<EntitySection<T>> limit(long maxSize) {
            return owner.limit(maxSize);
        }

        @Override
        public Stream<EntitySection<T>> skip(long n) {
            return owner.skip(n);
        }

        @Override
        public void forEach(Consumer<? super EntitySection<T>> action) {
            try {
                owner.forEach(action);
            }catch (ConcurrentModificationException cme){
                LogUtil.errorf("CME Occurred,%s,skipping forEach",cme.getMessage());
            }
        }

        @Override
        public void forEachOrdered(Consumer<? super EntitySection<T>> action) {
            try {
                owner.forEachOrdered(action);
            }catch (ConcurrentModificationException cme){
                LogUtil.errorf("CME Occurred,%s,skipping forEach",cme.getMessage());
            }
        }

        @Override
        public @NotNull Object[] toArray() {
            return owner.toArray();
        }

        @Override
        public @NotNull <A> A[] toArray(IntFunction<A[]> generator) {
            return owner.toArray(generator);
        }

        @Override
        public EntitySection<T> reduce(EntitySection<T> identity, BinaryOperator<EntitySection<T>> accumulator) {
            return owner.reduce(identity, accumulator);
        }

        @Override
        public @NotNull Optional<EntitySection<T>> reduce(BinaryOperator<EntitySection<T>> accumulator) {
            return owner.reduce(accumulator);
        }

        @Override
        public <U> U reduce(U identity, BiFunction<U, ? super EntitySection<T>, U> accumulator, BinaryOperator<U> combiner) {
            return owner.reduce(identity, accumulator, combiner);
        }

        @Override
        public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super EntitySection<T>> accumulator, BiConsumer<R, R> combiner) {
            return owner.collect(supplier, accumulator, combiner);
        }

        @Override
        public <R, A> R collect(Collector<? super EntitySection<T>, A, R> collector) {
            return owner.collect(collector);
        }

        @Override
        public @NotNull Optional<EntitySection<T>> min(Comparator<? super EntitySection<T>> comparator) {
            return owner.min(comparator);
        }

        @Override
        public @NotNull Optional<EntitySection<T>> max(Comparator<? super EntitySection<T>> comparator) {
            return owner.max(comparator);
        }

        @Override
        public long count() {
            return owner.count();
        }

        @Override
        public boolean anyMatch(Predicate<? super EntitySection<T>> predicate) {
            return owner.anyMatch(predicate);
        }

        @Override
        public boolean allMatch(Predicate<? super EntitySection<T>> predicate) {
            return owner.allMatch(predicate);
        }

        @Override
        public boolean noneMatch(Predicate<? super EntitySection<T>> predicate) {
            return owner.noneMatch(predicate);
        }

        @Override
        public @NotNull Optional<EntitySection<T>> findFirst() {
            return owner.findFirst();
        }

        @Override
        public @NotNull Optional<EntitySection<T>> findAny() {
            return owner.findAny();
        }

        @Override
        public @NotNull Iterator<EntitySection<T>> iterator() {
            return owner.iterator();
        }

        @Override
        public @NotNull Spliterator<EntitySection<T>> spliterator() {
            return owner.spliterator();
        }

        @Override
        public boolean isParallel() {
            return owner.isParallel();
        }

        @Override
        public @NotNull Stream<EntitySection<T>> sequential() {
            return owner.sequential();
        }

        @Override
        public @NotNull Stream<EntitySection<T>> parallel() {
            return owner.parallel();
        }

        @Override
        public @NotNull Stream<EntitySection<T>> unordered() {
            return owner.unordered();
        }

        @Override
        public @NotNull Stream<EntitySection<T>> onClose(@NotNull Runnable closeHandler) {
            return owner.onClose(closeHandler);
        }

        @Override
        public void close() {
            owner.close();
        }
    }
}
