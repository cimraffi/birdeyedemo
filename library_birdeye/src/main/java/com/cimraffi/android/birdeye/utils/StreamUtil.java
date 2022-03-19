package com.cimraffi.android.birdeye.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream相关工具类
 * @author xiaruri
 *
 */
public class StreamUtil {

	/**
	 * 通过InputStream获取byte[]
	 * @param is
	 * @return			可能为null		
	 */
	public static byte[] getBytesFromStream(InputStream is) {
		return getBytesFromStream(is, 1024 * 2);
	}
	
	public static byte[] getBytesFromStream(InputStream is, int length) {
		if(is == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] data = new byte[length];
		try {
			int ch = -1;
			while ((ch = is.read(data)) > -1) {
				baos.write(data, 0, ch);
			}
			data = null;
			data = baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			data = null;
		} finally {
			closeStream(is);
			closeStream(baos);
		}
		return data;
	}
	
	public static void closeStream(Closeable c) {
		if(c != null) {
			try {
				c.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
