package android.support.v4.util;

public class Pools$SimplePool<T> implements Pools$Pool<T> {
    private final Object[] mPool;
    private int mPoolSize;

    public Pools$SimplePool(int i) {
        if (i <= 0) {
            throw new IllegalArgumentException("The max pool size must be > 0");
        }
        this.mPool = new Object[i];
    }

    private boolean isInPool(T t) {
        for (int i = 0; i < this.mPoolSize; i++) {
            if (this.mPool[i] == t) {
                return true;
            }
        }
        return false;
    }

    public T acquire() {
        if (this.mPoolSize <= 0) {
            return null;
        }
        int i = this.mPoolSize - 1;
        T t = this.mPool[i];
        this.mPool[i] = null;
        this.mPoolSize--;
        return t;
    }

    public boolean release(T t) {
        if (isInPool(t)) {
            throw new IllegalStateException("Already in the pool!");
        } else if (this.mPoolSize >= this.mPool.length) {
            return false;
        } else {
            this.mPool[this.mPoolSize] = t;
            this.mPoolSize++;
            return true;
        }
    }
}
