package com.rhw.weburlcopy.util;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.Alarm;

/**
 * Disposer 工具类，用于处理 IntelliJ IDEA 2024.3.5 版本中的 Disposer 兼容性问题
 * 
 * @author renhao.wang
 * @since 2025-03-23
 */
public class DisposerUtil {
    
    /**
     * 创建与父级关联的 Alarm 对象，确保正确释放
     * 
     * @param parentDisposable 父级 Disposable 对象
     * @param threadToUse Alarm 线程模式
     * @return 新创建的 Alarm 对象
     */
    public static Alarm createAlarm(Disposable parentDisposable, Alarm.ThreadToUse threadToUse) {
        Alarm alarm = new Alarm(threadToUse);
        Disposer.register(parentDisposable, alarm);
        return alarm;
    }
    
    /**
     * 创建与父级关联的 Alarm 对象，确保正确释放
     * 
     * @param parentDisposable 父级 Disposable 对象
     * @return 新创建的 Alarm 对象
     */
    public static Alarm createAlarm(Disposable parentDisposable) {
        return createAlarm(parentDisposable, Alarm.ThreadToUse.SWING_THREAD);
    }
    
    /**
     * 确保对象被释放
     * 
     * @param disposable 需要释放的对象
     */
    public static void dispose(Disposable disposable) {
        if (disposable != null) {
            Disposer.dispose(disposable);
        }
    }
}