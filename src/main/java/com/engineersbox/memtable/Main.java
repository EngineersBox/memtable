package com.engineersbox.memtable;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(final String[] args) {
        final Memtable memtable = new Memtable();
        memtable.write(
                "test",
                123,
                Map.of(
                        "label 1", "value 1",
                        "label 2", "value 2"
                )
        );
        memtable.write(
                "other",
                new String[]{"more"},
                Map.of(
                        "label 2", "value 2",
                        "label 4", "value 4"
                )
        );
        final List<Object> results = memtable.read(Map.of(
                "label 2", "value 2"
        ));
        results.forEach(System.out::println);
    }

}