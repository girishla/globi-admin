package com.globi;

import java.lang.reflect.Method;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

	@Override
	public void handleUncaughtException(Throwable throwable, Method arg1, Object... arg2) {

		log.info(throwable.getMessage());

	}

}
