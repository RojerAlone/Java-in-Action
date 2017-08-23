# AbstractQueuedSynchronizer 之 共享锁
## `acquireShared`
``` Java
/**
 * Acquires in shared mode, ignoring interrupts.  Implemented by
 * first invoking at least once {@link #tryAcquireShared},
 * returning on success.  Otherwise the thread is queued, possibly
 * repeatedly blocking and unblocking, invoking {@link
 * #tryAcquireShared} until success.
 *
 * 获取共享锁，忽略中断。执行时先调用 truAcquireShared 尝试获取锁，
 * 如果获取成功就返回，否则当前线程入队，可能会被重复阻塞、唤醒
 */
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        doAcquireShared(arg);
}
```
## `tryAcquireShared`
``` Java
/**
 * 同 tryAcquire 一样，需要子类去实现
 */
protected int tryAcquireShared(int arg) {
    throw new UnsupportedOperationException();
}
```
## `doReleaseShared`
``` Java
private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head) { // 如果前驱结点是头结点，尝试获取共享锁
                int r = tryAcquireShared(arg);
                if (r >= 0) { // 如果获取成功
                    setHeadAndPropagate(node, r); // 设置头结点为当前线程，并且释放共享锁
                    p.next = null; // help GC
                    if (interrupted)
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }
            // 判断获取锁失败后是否要挂起当前线程
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```
## `releaseShared`
``` Java
public final boolean releaseShared(int arg) {
    if (tryReleaseShared(arg)) { // 先尝试释放锁，如果释放成功再唤醒等待的线程
        doReleaseShared();
        return true;
    }
    return false; // 释放失败返回 false
}
```
## `tryReleaseShared`
``` Java
/**
 * 同 tryRelease 一样，需要子类去实现
 */
protected boolean tryReleaseShared(int arg) {
    throw new UnsupportedOperationException();
}
```
## `doReleaseShared`
``` Java
private void doReleaseShared() {
    for (;;) { // 死循环，直到所有的唤醒所有的等待线程才结束
        Node h = head;
        // 如果有线程在等待
        if (h != null && h != tail) {
            int ws = h.waitStatus;
            if (ws == Node.SIGNAL) { // 如果头结点状态为 SIGNAL，就唤醒继任结点
                if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)) // 唤醒失败就循环唤醒
                    continue;            // loop to recheck cases
                unparkSuccessor(h); // 唤醒成功后解除阻塞
            }
            // 如果是 0，那么所有的锁都已经释放了，将 h 的状态设置为 PROPAGATE，表示传播唤醒
            // 队列中阻塞的线程在
            else if (ws == 0 &&
                     !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                continue;                // loop on failed CAS
        }
        // 如果头结点没有变，也就是 h == null 或者 h == tail 成立，那么退出
        if (h == head)                   // loop if head changed
            break;
    }
}
```