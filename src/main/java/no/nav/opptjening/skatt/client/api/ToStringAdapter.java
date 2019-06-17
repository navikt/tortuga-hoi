package no.nav.opptjening.skatt.client.api;

import java.util.function.Function;

class ToStringAdapter<T> {
    private final Function<T, String> toString;
    private final T wrappedObject;

    private ToStringAdapter(Function<T, String> toString, T wrappedObject) {
        this.toString = toString;
        this.wrappedObject = wrappedObject;
    }

    static <T> Object of(T object, Function<T, String> toString){
        return new ToStringAdapter<>(toString, object);
    }

    @Override
    public String toString() {
        return toString.apply(wrappedObject);
    }
}
