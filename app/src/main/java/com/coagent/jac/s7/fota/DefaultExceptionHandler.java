package com.coagent.jac.s7.fota;

import android.content.Context;
import android.util.Log;

/**
 * Created by meisl on 2015/7/16.
 */
public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

	private String TAG = getClass().getSimpleName();

	private Context context;

	public DefaultExceptionHandler(Context context) {
		this.context = context;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// 收集异常信息 并且发送到服务器
		sendCrashReport(ex);
		// 处理异常
		handleException();
	}

	private void sendCrashReport(Throwable ex) {
//		StringBuilder exceptionStr = new StringBuilder();
//		exceptionStr.append(ex.getMessage());
//		StackTraceElement[] elements = ex.getStackTrace();
//		for (StackTraceElement element : elements) {
//			exceptionStr.append(element.toString());
//		}
		// TODO
		// 发送收集到的Crash信息到服务器
		Log.e(TAG, ex.getMessage(), ex);
	}

	private void handleException() {
		// TODO
		// 这里可以对异常进行处理。
		// 比如提示用户程序崩溃了。
		// 比如记录重要的信息，尝试恢复现场。
		// 或者干脆记录重要的信息后，直接杀死程序。
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}