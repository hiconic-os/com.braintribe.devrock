package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * represents a lock that doesn't do anything, an NOP lock- i.e. it doesn't lock, but always succeeds in locking,
 *
 * @author pit
 *
 */
public class PassthruLock implements Lock {

	@Override
	public void lock() {
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public boolean tryLock() {
		return true;
	}

	@Override
	public boolean tryLock(long arg0, TimeUnit arg1) throws InterruptedException {
		return true;
	}

	@Override
	public void unlock() {
	}

}
