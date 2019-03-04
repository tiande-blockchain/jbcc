package cn.tdchain.jbcc;

import java.util.concurrent.Callable;

public class TimerUtil {

    /**
     * 重复间隔的执行代码,直到返回结果
     *
     * @param mills    间隔时间
     * @param times    执行次数
     * @param callable 逻辑代码
     * @param <T>
     * @return
     */
    public static <T> T exec(long mills, int times, Callable<T> callable) {
        T call = null;
        for (int i = 0; i < times; i++) {
            try {
                call = callable.call();
            } catch (Exception e) {
                e.printStackTrace();
                call = null;
            }
            if (call != null) {
                return call;
            }
            if (mills <= 0) {
                continue;
            }
            try {
                Thread.sleep(mills);
            } catch (InterruptedException e) {
            }
        }
        return call;
    }

}
