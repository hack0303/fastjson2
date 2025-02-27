package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

final class FieldReaderAtomicReferenceField<T>
        extends FieldReaderAtomicReference<T> implements FieldReaderReadOnly<T> {

    final Field field;
    final boolean readOnly;

    FieldReaderAtomicReferenceField(String fieldName, Type fieldType, Class fieldClass, int ordinal, Field field) {
        super(fieldName, fieldType, fieldClass, ordinal, 0, null);
        this.field = field;

        readOnly = Modifier.isFinal(field.getModifiers());
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            return;
        }

        try {
            if (readOnly) {
                AtomicReference atomic = (AtomicReference) field.get(object);
                atomic.set(value);
            } else {
                field.set(object
                        , new AtomicReference(value));
            }
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
