package io.github.hostadam.persistence.data.codec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ObjectCodecBuilder<T> {

    private final List<FieldCodec<T, ?>> fields;

    protected ObjectCodecBuilder() {
        this.fields = new ArrayList<>();
    }

    protected <F> void addField(FieldCodec<T, F> codec) {
        this.fields.add(codec);
    }

    public List<FieldCodec<T, ?>> fields() {
        return Collections.unmodifiableList(this.fields);
    }

    public abstract DataCodec<T> build();
}
