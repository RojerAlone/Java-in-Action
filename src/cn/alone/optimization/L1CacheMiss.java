package cn.alone.optimization;

/**
 * Created by RojerAlone on 2018-04-10
 * 从Java视角理解系统结构（二）CPU缓存
 * http://ifeve.com/from-javaeye-cpu-cache/
 */
public class L1CacheMiss {

    private static final int RUNS = 10;
    private static final int DIMENSION_1 = 1024 * 1024;
    private static final int DIMENSION_2 = 62;

    private static long[][] longs;

    public static void main(String[] args) {
        longs = new long[DIMENSION_1][];
        for (int i = 0; i < DIMENSION_1; i++) {
            longs[i] = new long[DIMENSION_2];
            for (int j = 0; j < DIMENSION_2; j++) {
                longs[i][j] = 0L;
            }
        }
        long fast = fast();
        long slow = slow();
        System.out.println("fast duration : " + fast);
        System.out.println("slow duration : " + slow);
        System.out.println("time disparity : " + (slow - fast));
    }

    private static long fast() {
        long start = System.nanoTime();
        long sum = 0L;
        for (int r = 0; r < RUNS; r++) {
            for (int i = 0; i < DIMENSION_1; i++) {
                for (int j = 0; j < DIMENSION_2; j++) {
                    sum += longs[i][j];
                }
            }
        }
        return System.nanoTime() - start;
    }

    private static long slow() {
        long start = System.nanoTime();
        long sum = 0L;
        for (int r = 0; r < RUNS; r++) {
            for (int j = 0; j < DIMENSION_2; j++) {
                for (int i = 0; i < DIMENSION_1; i++) {
                    sum += longs[i][j];
                }
            }
        }
        return System.nanoTime() - start;
    }

}
