package com.dell.dfs.io;

import java.io.Flushable;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

public interface ICSVWriter extends Flushable, Closeable {
	void write(Collection<String> record) throws IOException;
	void write(String record) throws IOException;
}