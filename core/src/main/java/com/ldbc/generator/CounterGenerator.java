package com.ldbc.generator;

import org.apache.commons.math3.random.RandomDataGenerator;

import com.ldbc.util.NumberHelper;

public class CounterGenerator<T extends Number> extends Generator<T>
{
    private final NumberHelper<T> number;
    private final T incrementBy;
    private final T max;
    private T count;

    CounterGenerator( RandomDataGenerator random, T start, T incrementBy, T max )
    {
        super( random );
        this.count = start;
        this.incrementBy = incrementBy;
        this.max = max;
        number = NumberHelper.createNumberHelper( start.getClass() );
    }

    @Override
    protected T doNext()
    {
        if ( null != max && number.gt( count, max ) )
        {
            return null;
        }
        T next = count;
        count = number.sum( count, incrementBy );
        return next;
    }
}
