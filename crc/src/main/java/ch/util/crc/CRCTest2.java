package ch.util.crc;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class CRCTest2 {
	private List<String[]> testlist = new ArrayList<String[]>();
	private List<String> deleteFiles = new ArrayList<String>();
	private java.util.Map<String, String[]> matchMap = new java.util.LinkedHashMap<String, String[]>();
	public static void main(String[] args){
		new CRCTest2().test();
		
	}
	private boolean match(String... paths){
		long value = CRCUtil.crcFile(new File(paths[0])).getValue();
		for (int i = 1; i < paths.length; i++)
			if (value != CRCUtil.crcFile(new File(paths[i])).getValue())
				return false;
		return true;
	}
	private boolean exist(String target, String... paths){
		for (String path : paths)
			if (path.equals(target))
				return true;
		return false;
	}
	private CRCTest2(){
		put("C:/Users/jimmy.shu/git/ch-memo/b/sample-pom.xml",
				"D:/sts/workspace/samples/pom.xml");
		put("C:/Users/jimmy.shu/git/ch-memo/b/java_first_spring_support-pom.xml",
				"D:/sts/workspace/samples/java_first_spring_support/pom.xml");
		put("C:/Users/jimmy.shu/git/ch-memo/b/cxf-servlet.xml",
				"D:/sts/workspace/samples/java_first_spring_support/src/main/webapp/WEB-INF/cxf-servlet.xml");
		put("C:/Users/jimmy.shu/git/ch-memo/b/client-beans.xml",
				"D:/sts/workspace/samples/java_first_spring_support/src/main/resources/client-beans.xml");
		put("C:/Users/jimmy.shu/git/ch-memo/b/HelloWorld.java",
				"D:/sts/workspace/samples/java_first_spring_support/src/main/java/demo/spring/service/HelloWorld.java");
		put("C:/Users/jimmy.shu/git/ch-memo/b/HelloWorldImpl.java",
				"D:/sts/workspace/samples/java_first_spring_support/src/main/java/demo/spring/service/HelloWorldImpl.java");
		put("C:/Users/jimmy.shu/git/ch-memo/b/XRayType.java",
				"D:/sts/workspace/samples/java_first_spring_support/src/main/java/demo/spring/service/XRayType.java");
		put("C:/Users/jimmy.shu/git/ch-memo/a/note0720.xlsx", "C:/Users/jimmy.shu/Desktop/保存/活頁簿1.xlsx");
		add("C:/Users/jimmy.shu/git/test-parent/parent/.gitignore");
		add("C:/Users/jimmy.shu/git/crc/crc/.gitignore",
				"C:/Users/jimmy.shu/git/test-mongodb/mongodb/.gitignore",
				"C:/Users/jimmy.shu/git/ch-test-http/http/.gitignore",
				"C:/Users/jimmy.shu/git/ch-test-redis/.gitignore");
		add("C:/Users/jimmy.shu/git/test-parent/parent/axis2/.gitignore",
				"C:/Users/jimmy.shu/git/test-parent/parent/cxf/.gitignore",
				"C:/Users/jimmy.shu/git/test-parent/parent/simpleweb/.gitignore");
		add("C:/Users/jimmy.shu/git/ch-memo/a/.gitignore",
				"C:/Users/jimmy.shu/git/ch-memo/b/.gitignore");
		add("C:/Users/jimmy.shu/git/ch-memo/a/a1_RabbitMQ",
				"C:/Users/jimmy.shu/git/ch-memo/a/a1_RabbitMQ(0706.0611).txt",
				"C:/Users/jimmy.shu/git/ch-memo/a/a1_RabbitMQ_");
		add("C:/Users/jimmy.shu/git/ch-memo/a/.profile",
				"C:/Users/jimmy.shu/.profile");
		add("C:/Users/jimmy.shu/git/ch-memo/a/a1_RabbitMQ",
				"C:/Users/jimmy.shu/Desktop/保存/a1_RabbitMQ");
		add("C:/Users/jimmy.shu/git/ch-memo/a/a1_RabbitMQ(0706.0611).txt",
				"C:/Users/jimmy.shu/Desktop/保存/a1_RabbitMQ(0706.0611).txt");
		add("C:/Users/jimmy.shu/git/ch-memo/a/a1_RabbitMQ_",
				"C:/Users/jimmy.shu/Desktop/保存/a1_RabbitMQ_");
		add("C:/Users/jimmy.shu/git/ch-memo/a/a2_804.txt",
				"C:/Users/jimmy.shu/Desktop/保存/a2_804.txt");
		add("C:/Users/jimmy.shu/git/ch-memo/a/a3_git_tree.txt",
				"C:/Users/jimmy.shu/Desktop/保存/a3_git_tree.txt");
		add("C:/Users/jimmy.shu/git/ch-memo/a/a4_git_porcelain.txt",
				"C:/Users/jimmy.shu/Desktop/保存/a4_git_porcelain.txt");
	}
	private void test(){
		for (String target : matchMap.keySet()){
			String[] paths = matchMap.get(target);
			String[] allPaths = new String[1 + paths.length];
			allPaths[0] = target;
			for (int i = 1; i <= paths.length; i++)
				allPaths[i] = paths[i-1];
			boolean match = match(allPaths);
			System.out.println(String.format("%s = %s", target, match));
			if (!match)
				throw new RuntimeException("matchMap not match!");
		}
		System.out.println("-------------------------------------------------------");
		Scanner s = null;
		try {
			s = new Scanner(System.in);
			for (String[] ary : testlist){
				for (String path : ary)
					System.out.print(String.format("%s%s", path, (char) 10));
				boolean match = false;
				try {
					match = match(ary);
				} catch (RuntimeException re){
					System.out.println(re.getLocalizedMessage());
				}
				System.out.println(match);
				String target;
				if (match && s.hasNextLine() && !((target = s.nextLine()).isEmpty()) && exist(target, ary)){
					deleteFiles.add(target);
					System.out.print(String.format("delete: %s = %s%s", target, new File(target).delete(), (char) 10));
				}
				System.out.println();
			}
		} finally {
			if (s != null)
				s.close();
		}
		for (String deleteFile : deleteFiles)
			System.out.println(String.format("delete: %s", deleteFile));
	}
	private void add(String...  paths){
		testlist.add(paths);
	}
	private void put(String target, String...  paths){
		matchMap.put(target, paths);
	}
}