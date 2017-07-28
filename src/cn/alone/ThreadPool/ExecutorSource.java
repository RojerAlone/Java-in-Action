package cn.alone.ThreadPool;

import java.util.concurrent.RejectedExecutionException;

/**
 * Created by RojerAlone on 2017-07-28.
 */
public interface ExecutorSource {
        /**
         * Executes the given command at some time in the future.  The command
         * may execute in a new thread, in a pooled thread, or in the calling
         * thread, at the discretion of the {@code Executor} implementation.
         *
         * @param command the runnable task
         * @throws RejectedExecutionException if this task cannot be
         * accepted for execution
         * @throws NullPointerException if command is null
         *
         * 在未来某个时间执行参数中的命令，这个命令可能在一个新的线程、线程池中的线程或者一个调用线程中被执行
         */
        void execute(Runnable command);
}
