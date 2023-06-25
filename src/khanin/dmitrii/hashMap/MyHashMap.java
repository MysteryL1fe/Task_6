package khanin.dmitrii.hashMap;

import java.lang.reflect.Array;
import java.util.Arrays;

public class MyHashMap<K, V> {
    private EntryListItem[] table;
    private int size = 0;

    public MyHashMap() {
        table = (EntryListItem[]) Array.newInstance(EntryListItem.class, 10);
    }

    public MyHashMap(int capacity) {
        table = (EntryListItem[]) Array.newInstance(EntryListItem.class, capacity);
    }

    public void put(K key, V value) {
        EntryListItem item = getEntry(key);
        if (item != null) item.setValue(value);
        else {
            int index = getIndex(key);
            table[index] = new EntryListItem(key, value, table[index]);
            size++;
            if (size > table.length * 0.75) {
                resize();
            }
        }
    }

    public V get(K key) {
        EntryListItem item = getEntry(key);
        return item == null ? null : item.value;
    }

    public void remove(K key) {
        int index = getIndex(key);
        EntryListItem parent = null;
        for (EntryListItem cur = table[index]; cur != null; cur = cur.next) {
            if (key.equals(cur.key)) {
                if (parent == null) {
                    table[index] = cur.next;
                } else {
                    parent.next = cur.next;
                }
                size--;
            }
            parent = cur;
        }
    }

    public void clear() {
        Arrays.fill(table, null);
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size <= 0;
    }

    public boolean containsKey(K key) {
        return getEntry(key) != null;
    }

    private void resize() {
        EntryListItem[] tmp = (EntryListItem[]) Array.newInstance(EntryListItem.class, table.length * 2);
        for (int i = 0; i < table.length; i++) {
            EntryListItem cur = table[i], moving;
            while (cur != null) {
                moving = cur;
                cur = cur.next;
                int newIndex = moving.getKey().hashCode() % tmp.length;
                if (newIndex < 0) {
                    newIndex += tmp.length;
                }
                moving.setNext(tmp[newIndex]);
                tmp[newIndex] = moving;
            }
        }
        table = tmp;
    }

    private int getIndex(K key) {
        int index = key.hashCode() % table.length;
        if (index < 0) {
            index += table.length;
        }
        return index;
    }

    private EntryListItem getEntry(K key) {
        int index = getIndex(key);
        for (EntryListItem cur = table[index]; cur != null; cur = cur.next) {
            if (key.equals(cur.key)) {
                return cur;
            }
        }
        return null;
    }

    private class EntryListItem {
        public K key;
        public V value;
        public EntryListItem next;

        public EntryListItem(K key, V value, EntryListItem next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public void setNext(EntryListItem next) {
            this.next = next;
        }
    }
}
