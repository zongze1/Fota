package com.coagent.jac.s7.fota.Dialog;

public interface ScheduleListener {
    /**
     * 计划时间已设定，回调该方法
     * @param timeMillis 该参数是目标时间的标准时间
     */
    void schedule(long timeMillis);
}
