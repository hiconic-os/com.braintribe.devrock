package com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;

public interface LockFactory {

	ReadWriteLock getLockInstance(File fileToLock);
}
