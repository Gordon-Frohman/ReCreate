package net.minecraftforge.common.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LazyOptional<T> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final LazyOptional<Object> EMPTY = new LazyOptional<Object>(null);

    @SuppressWarnings("unchecked")
    @Nonnull
    public static <T> LazyOptional<T> empty() {
        return (LazyOptional<T>) EMPTY;
    }

    @Nonnull
    public static <T> LazyOptional<T> of(@Nullable final Supplier<T> instanceSupplier) {
        return instanceSupplier == null ? empty() : new LazyOptional<>(instanceSupplier);
    }

    private final Object lock = new Object();
    private final Supplier<T> supplier;
    private T resolved;
    private Consumer<LazyOptional<T>> invalidateListeners = null;
    private boolean isValid = true;

    private LazyOptional(@Nullable Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public boolean isPresent() {
        return isValid && supplier != null;
    }

    public Optional<T> resolve() {
        return isPresent() ? Optional.ofNullable(getValue()) : Optional.empty();
    }

    @Nonnull
    public T orElseThrow(@Nonnull Supplier<? extends RuntimeException> exceptionSupplier) {
        T res = getValue();
        if (res == null) {
            throw exceptionSupplier.get();
        }
        return res;
    }

    @Nullable
    private T getValue() {
        if (!isValid || supplier == null) {
            return null;
        }
        if (resolved == null) {
            synchronized (lock) {
                if (resolved == null) {
                    resolved = supplier.get();
                    if (resolved == null) {
                        LOGGER.error("LazyOptional with provider {} returns null. This is unacceptable!", supplier);
                        isValid = false;
                    }
                }
            }
        }
        return resolved;
    }

    public void ifPresent(@Nonnull Consumer<? super T> consumer) {
        T res = getValue();
        if (res != null) {
            consumer.accept(res);
        }
    }

    @Nonnull
    public <U> LazyOptional<U> map(@Nonnull Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isPresent()) {
            return LazyOptional.of(() -> mapper.apply(getValue()));
        }
        return empty();
    }

    @Nonnull
    public LazyOptional<T> filter(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent() || predicate.test(getValue())) {
            return this;
        }
        return empty();
    }

    @Nonnull
    public T orElse(@Nonnull T other) {
        T res = getValue();
        return res != null ? res : other;
    }

    public void addListener(@Nonnull Consumer<LazyOptional<T>> listener) {
        if (isValid) {
            synchronized (lock) {
                if (isValid) {
                    if (invalidateListeners == null) {
                        invalidateListeners = listener;
                    } else {
                        Consumer<LazyOptional<T>> old = invalidateListeners;
                        invalidateListeners = lo -> {
                            old.accept(lo);
                            listener.accept(lo);
                        };
                    }
                    return;
                }
            }
        }
        listener.accept(this);
    }

    public void invalidate() {
        if (isValid) {
            synchronized (lock) {
                if (isValid) {
                    isValid = false;
                    if (invalidateListeners != null) {
                        invalidateListeners.accept(this);
                    }
                }
            }
        }
    }
}
