package com.engineersbox.memtable;

import java.util.*;

public class Memtable {

    public static class Entry {

        public final String id;
        public final Map<String, String> labels;
        public final Object value;
        public Entry next;

        public Entry(final String id,
                     final Map<String, String> labels,
                     final Object value,
                     final Entry next) {
            this.id = id;
            this.labels = labels;
            this.value = value;
            this.next = next;
        }

        public Entry(final String id,
                     final Map<String, String> labels,
                     final Object value) {
            this(id, labels, value, null);
        }

        public boolean before(final Entry other) {
            return this.id.compareTo(other.id) < 0;
        }

    }

    private Entry head;
    private Entry tail;
    private final Map<String, List<Entry>> index;

    public Memtable() {
        this.head = null;
        this.tail = null;
        this.index = new HashMap<>();
    }

    private String encode(final String label,
                          final String value) {
        return String.format("%s::%s", label, value);
    }

    private List<Entry> intersect(final List<Entry> a,
                                  final List<Entry> b) {
        final List<Entry> out = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < a.size() && j < b.size()) {
            final Entry ai = a.get(i);
            final Entry bj = b.get(j);
            if (ai.id.equals(bj.id)) {
                out.add(ai);
                i++;
                j++;
            } else if (ai.before(bj)) {
                i++;
            } else {
                j++;
            }
        }
        return out;
    }

    public void write(final String id,
                      final Object value,
                      final Map<String, String> labels) {
        final Entry entry = new Entry(
                id,
                labels,
                value
        );
        labels.forEach((final String k, final String v) -> {
            final String key = encode(k ,v);
            this.index.computeIfAbsent(key, (ignored) -> new ArrayList<>()).add(entry);
        });
        if (this.tail == null) {
            this.tail = this.head = entry;
        } else {
            this.tail.next = entry;
            this.tail = this.tail.next;
        }
    }

    public List<Object> read(final Map<String, String> labels) {
        final List<List<Entry>> entriesGroup = new ArrayList<>();
        labels.forEach((final String k, final String v) -> {
            final String key = encode(k, v);
            final List<Entry> entries = this.index.get(key);
            if (entries != null) {
                entriesGroup.add(entries);
            }
        });
        if (entriesGroup.isEmpty()) {
            return List.of();
        }
        List<Entry> intersectedEntries = entriesGroup.get(0);
        for (int i = 1; i < entriesGroup.size(); i++) {
            intersectedEntries = intersect(
                    intersectedEntries,
                    entriesGroup.get(i)
            );
        }
        return intersectedEntries.stream()
                .map((final Entry entry) -> entry.value)
                .toList();
    }

}
