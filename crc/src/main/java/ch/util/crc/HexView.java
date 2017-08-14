package ch.util.crc;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
public class HexView {
	public static void main(String[] args){
		char[] name = "許家祥".toCharArray();
		for (char c : name)
			System.out.print(Integer.toHexString(c) + ',');
		System.out.println("" + (char) 0x8a31 + (char) 0x5bb6 + (char) 0x7965);

		String filePath = "C:/Users/jimmy.shu/git/jimmy-test-example/src/main/java/jimmy/test/example/README.md";
		System.out.println(showHex(new File(filePath)));
	}
	public static String showHex(File file){
		StringBuilder builder = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null){
				char[] ary = line.toCharArray();
				for (int i = 0; i < ary.length; i++)
					builder.append(ary[i]);
				builder.append('\n');
				for (int i = 0; i < ary.length; i++){
					builder.append(Integer.toHexString(ary[i]));
					if (i < ary.length - 1)
						builder.append(',');
				}
				builder.append('\n');
				for (int i = 0; i < ary.length; i++){
					byte[] bytes = String.valueOf(ary[i]).getBytes("UTF-8");
					for (int j = 0; j < bytes.length; j++){
						builder.append(Integer.toHexString(bytes[j]));
						if (j < bytes.length - 1)
							builder.append('|');
					}
					if (i < ary.length - 1)
						builder.append(',');
				}
				builder.append('\n').append('\n');
			}
		} catch (java.io.FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
		}
		return builder.toString();
	}
}