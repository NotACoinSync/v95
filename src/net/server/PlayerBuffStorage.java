package net.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerBuffStorage {

    private int id = (int) (Math.random() * 100);
    private final Lock mutex = new ReentrantLock();
    private Map<Integer, List<PlayerBuffValueHolder>> buffs = new HashMap<Integer, List<PlayerBuffValueHolder>>();

    public void addBuffsToStorage(int chrid, List<PlayerBuffValueHolder> toStore) {
        mutex.lock();
        try {
            buffs.put(chrid, toStore);// Old one will be replace if it's in here.
        } finally {
            mutex.unlock();
        }
    }

    public List<PlayerBuffValueHolder> getBuffsFromStorage(int chrid) {
        mutex.lock();
        try {
            return buffs.remove(chrid);
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlayerBuffStorage other = (PlayerBuffStorage) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
