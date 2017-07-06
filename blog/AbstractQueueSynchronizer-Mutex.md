# AbstractQueuedSynchronizer 之 互斥锁
## `acquire`
源码如下：
```Java
/**
 * Acquires in exclusive mode, ignoring interrupts.  Implemented
 * by invoking at least once {@link #tryAcquire},
 * returning on success.  Otherwise the thread is queued, possibly
 * repeatedly blocking and unblocking, invoking {@link
 * #tryAcquire} until success.  This method can be used
 * to implement method {@link Lock#lock}.
 *
 * @param arg the acquire argument.  This value is conveyed to
 *        {@link #tryAcquire} but is otherwise uninterpreted and
 *        can represent anything you like.
 */
public final void acquire(int arg) {
    // tryAcquire 尝试获取锁，如果获取成功就不会执行后边的方法
    // 获取不成功，先调用 addWaiter 方法将当前线程放入阻塞队列中，
    // 然后调用 acquireQueued 方法，根据新插入的结点寻找CLH队列的头结点，
    // 并且尝试获取锁，判断是否需要挂起，并且返回挂起标识
    // 如果需要挂起，那么当前线程执行 Thread.currentThread().interrupt();
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```
### `tryAcquire`
源码：
``` Java
protected boolean tryAcquire(int arg) {
    // 在这里直接抛出异常，需要继承 AQS 的类实现这个方法
    throw new UnsupportedOperationException();
}
```
### `addWaiter`
源码：
``` Java
/**
 * Creates and enqueues node for current thread and given mode.
 *
 * @param mode Node.EXCLUSIVE for exclusive, Node.SHARED for shared
 * @return the new node
 */
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        // 先尝试着用 CAS 插入到尾结点之后，如果不成功再用 enq 去插入
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    enq(node);
    return node;
}
```
### `enq`
源码：
``` Java
/**
 * Inserts node into queue, initializing if necessary. See picture above.
 * @param node the node to insert
 * @return node's predecessor
 */
private Node enq(final Node node) {
    for (;;) {  // 循环直到插入成功
        Node t = tail;
        if (t == null) { // Must initialize 如果尾结点为空，队列还没初始化，需要初始化
            if (compareAndSetHead(new Node())) // CAS 设置头结点为傀儡结点
                tail = head;                   // 尾结点也指向头结点
        } else { // 尾结点不为空，将结点插入队尾
            node.prev = t;
            if (compareAndSetTail(t, node)) {  // CAS 设置尾结点
                t.next = node;
                return t;
            }
        }
    }
}
```
### `acquireQueued`
``` Java
/**
 * Acquires in exclusive uninterruptible mode for thread already in
 * queue. Used by condition wait methods as well as acquire.
 *
 * @param node the node
 * @param arg the acquire argument
 * @return {@code true} if interrupted while waiting
 */
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor(); // 获取前驱结点
            if (p == head && tryAcquire(arg)) {
                setHead(node); // 获取锁成功之后将结点设为头结点，因为头结点总是已经获取到锁的结点
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            // 没有轮到当前结点运行，先检查当前结点是否应该停止，如果应该停止就停止并检查中断
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
### `shouldParkAfterFailedAcquire`
``` Java
/**
 * Checks and updates status for a node that failed to acquire.
 * Returns true if thread should block. This is the main signal
 * control in all acquire loops.  Requires that pred == node.prev.
 * 
 * @param pred node's predecessor holding status
 * @param node the node
 * @return {@code true} if thread should block
 */
// 只有当前驱节点的状态为Node.SIGNAL时返回true，即：应当挂起
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        /*
         * This node has already set status asking a release
         * to signal it, so it can safely park.
         */
        return true;
    if (ws > 0) {
        /*
         * Predecessor was cancelled. Skip over predecessors and
         * indicate retry.
         */
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        /*
         * waitStatus must be 0 or PROPAGATE.  Indicate that we
         * need a signal, but don't park yet.  Caller will need to
         * retry to make sure it cannot acquire before parking.
         */
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
```
### `parkAndCheckInterrupt`
``` Java
/**
 * Convenience method to park and then check if interrupted
 *
 * @return {@code true} if interrupted
 */
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);      // 中断
    return Thread.interrupted(); // 返回当前线程的中断状态
}
```
### `cancelAcquire`
``` Java
/**
 * Cancels an ongoing attempt to acquire.
 *
 * @param node the node
 */
// 取消请求锁，将当前结点从队列中删除
// 分3钟情况：当前节点是头节点之后的、是尾节点、不是头结点之后的也不是尾节点
private void cancelAcquire(Node node) {
    // Ignore if node doesn't exist
    if (node == null)
        return;

    node.thread = null;

    // Skip cancelled predecessors
    Node pred = node.prev;
    while (pred.waitStatus > 0) // while 循环跳过取消的前驱节点
        node.prev = pred = pred.prev;

    // predNext is the apparent node to unsplice. CASes below will
    // fail if not, in which case, we lost race vs another cancel
    // or signal, so no further action is necessary.
    Node predNext = pred.next;

    // Can use unconditional write instead of CAS here.
    // After this atomic step, other Nodes can skip past us.
    // Before, we are free of interference from other threads.
    node.waitStatus = Node.CANCELLED;

    // If we are the tail, remove ourselves.
    // 如果当前节点是 tail，那么将自己删掉，当前节点的前驱节点为 tail
    if (node == tail && compareAndSetTail(node, pred)) {
        compareAndSetNext(pred, predNext, null);
    } else {
        // If successor needs signal, try to set pred's next-link
        // so it will get one. Otherwise wake it up to propagate.
        int ws;
        if (pred != head &&
            ((ws = pred.waitStatus) == Node.SIGNAL ||
             (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
            pred.thread != null) {
            Node next = node.next;
            if (next != null && next.waitStatus <= 0)
                compareAndSetNext(pred, predNext, next);
        } else {
            unparkSuccessor(node);
        }

        node.next = node; // help GC
    }
}
```
## `release`
``` Java
public final boolean release(long arg) {
    // 尝试释放锁，如果成功就唤醒头节点的后继节点
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```