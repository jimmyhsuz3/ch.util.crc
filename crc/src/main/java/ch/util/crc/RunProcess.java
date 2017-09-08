package ch.util.crc;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
public class RunProcess {
	public static void main(String[] args) {
		RunProcess runProcess = new RunProcess();
		String[] cmdarray = new String[]{"C:\\Program Files (x86)\\WinMerge\\WinMergeU.exe",
				"C:/Users/jimmy.shu/Desktop/保存/2_config.prop(0825.0558)",
				"D:\\vm/config.prop"};
		cmdarray = new String[]{"git", "diff", "da35ac7a4421eaebde385f7ff0b0a4041f6268de",
				"--", "src/main/webapp/easyGit.html"};
//		runProcess.exec(null, progArray);
//		runProcess.exec(null, "java");
		System.err.println(new String(runProcess.exec(null, "java")));
		System.out.println(new String(runProcess.exec("C:\\Users\\jimmy.shu\\git\\ch-test-easyweb", cmdarray)));
	}
	public byte[] exec(String dir, String... cmdarray){
		if (dir == null || dir.trim().isEmpty())
			dir = ".";
		java.io.File file = new java.io.File(dir);
		if (!file.exists() || !file.isDirectory())
			throw new RuntimeException(dir);
		final String charsetName = "utf8";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			baos.write((file.getAbsolutePath() + " 一 " + toString(cmdarray) + "\n").getBytes());
			Process p = Runtime.getRuntime().exec(cmdarray, null, file);
			write(baos, charsetName, p.getInputStream(), p.getErrorStream());
			baos.write(("p.waitFor() 二 " + p.waitFor()).getBytes());
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return baos.toByteArray();
	}
	private void write(OutputStream os, String charsetName, InputStream... iss){
		for (InputStream is : iss){
			BufferedReader br = null;
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new OutputStreamWriter(os));
				br = new BufferedReader(new InputStreamReader(is, charsetName));
				String line=null;
				while((line=br.readLine()) != null)
					pw.println(line);
				pw.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				if (br != null)
					try {
						br.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
			}
		}
	}
	private String toString(String[] progArray){
		StringBuilder builder = new StringBuilder();
		for (String prog : progArray)
			builder.append(prog).append(' ');
		return builder.toString();
	}
}
