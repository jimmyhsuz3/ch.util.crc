package ch.util.crc;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import org.apache.commons.io.FileUtils;
public class CRCUtil{
	public static class CRCValue {
		private long value;
		private long time;
		private CRCValue(long value, long time) {
			this.value = value;
			this.time = time;
		}
		public long getValue(){return value;}
		public long getTime(){return time;}
		@Override
		public String toString(){return String.format("%s (%s)", value, time);}
		@Override
		public boolean equals(Object obj) {
			return obj instanceof CRCValue ? this.value == ((CRCValue) obj).value : false;
		}
	}
	public static CRCValue crcFile(File file){
		long start = System.currentTimeMillis();
		long value = crc32File(file);
		return new CRCValue(value, System.currentTimeMillis() - start);
	}
	public static CRCValue crcStream(File file){
		long start = System.currentTimeMillis();
		long value = crc32Stream(file);
		return new CRCValue(value, System.currentTimeMillis() - start);
	}
	public static CRCValue crcStream(FileInputStream fis){
		long start = System.currentTimeMillis();
		long value = crc32Stream(fis);
		return new CRCValue(value, System.currentTimeMillis() - start);
	}
	public CRCValue CRCBytes(byte[] b){
		long start = System.currentTimeMillis();
		CRC32 testCRC = new CRC32();
		testCRC.update(b);
		return new CRCValue(testCRC.getValue(), System.currentTimeMillis() - start);
	}
	private static long crc32File(File file){
		try {
			return FileUtils.checksumCRC32(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private static long crc32Stream(File file){
		long value = 0;
		try {
			FileInputStream fis = new FileInputStream(file);
			value = crc32Stream(fis);
			fis.available();
			throw new RuntimeException("test: not closed!");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			if ("Stream Closed".equals(e.getLocalizedMessage()))
				return value;
			else
				throw new RuntimeException(e);
		}
	}
	private static long crc32Stream(FileInputStream fis){
		CheckedInputStream checked = null;
		try {
			checked = new CheckedInputStream(new BufferedInputStream(fis), new CRC32());
			CRC32 testCRC = new CRC32();
			byte[] b = new byte[8192];
			int len = 0;
			while((len = checked.read(b)) != -1)
				testCRC.update(b, 0, len);
			if (testCRC.getValue() == checked.getChecksum().getValue())
				return checked.getChecksum().getValue();
			else 
				throw new RuntimeException("test: checksum wrong!");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (checked != null)
				try {
					checked.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}
}