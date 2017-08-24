package ch.util.crc;
import org.eclipse.jgit.lib.Repository;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
public class GitTest {
	private static final String TEMP_GIT = "C:/Users/jimmy.shu/Downloads/temp_git";
	private static final GitRepo[] GIT_REPOS;
	private final GitUtil gitUtil = new GitUtil();
	private final java.util.Map<String, Repository> repoMap = new java.util.HashMap<String, Repository>();
	static {
		GitRepoEnum[] enums = GitRepoEnum.values();
		GIT_REPOS = new GitRepo[enums.length];
		for (int i = 0; i < GIT_REPOS.length; i++)
			GIT_REPOS[i] = enums[i].gitRepo();
	}
	synchronized
	private Repository getRepoFromMap(String url){
		if (repoMap.containsKey(url)){
			System.out.println(url + " from repoMap\n");
			return repoMap.get(url);
		}
		for (GitRepo gitRepo : GIT_REPOS)
			if (gitRepo.getUrl().equals(url)){
				Repository repo = gitUtil.getRepo(gitRepo, TEMP_GIT);
				repoMap.put(gitRepo.getUrl(), repo);
				System.out.println(url + " from getRepo\n");
				return repo;
			}
		throw new RuntimeException("fail: getRepo: " + url);
	}
	public int test(boolean clean){
		if (clean)
			close();
		try {
			return dotest(clean);
		} catch (RuntimeException re){
			close();
			try {
				org.apache.commons.io.FileUtils.cleanDirectory(new java.io.File(TEMP_GIT));
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
			throw re;
		}
	}
	private int dotest(boolean clean){
		if (clean)
			try {
				org.apache.commons.io.FileUtils.cleanDirectory(new java.io.File(TEMP_GIT));
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		System.out.println(GIT_REPOS.length);
		for (GitRepo gitRepo : GIT_REPOS)
			getRepoFromMap(gitRepo.getUrl());
		return GIT_REPOS.length;
	}
	public void close(){for(Repository repo : repoMap.values())repo.close();repoMap.clear();}
	public String testByDate(String strFrom, String strTo){
		Date from = null;
		Date to = null;
		if (strFrom != null && !strFrom.trim().isEmpty())
			try {
				from = new SimpleDateFormat(GitRepo.DATE_FORMAT).parse(strFrom.trim());
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		if (strTo != null && !strTo.trim().isEmpty())
			try {
				to = new SimpleDateFormat(GitRepo.DATE_FORMAT).parse(strTo.trim());
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		for (GitRepo gitRepo : GIT_REPOS){
			ObjectNode gNode = mapper.createObjectNode();
			for (String objectId : gitUtil.commitsFromDate(getRepoFromMap(gitRepo.getUrl()), from, to)){
				java.util.Map<String, String> map = gitUtil.commitDiffs(getRepoFromMap(gitRepo.getUrl()), objectId);
				ArrayNode cNode = mapper.createArrayNode();
				for (String key : map.keySet())
					cNode.add(mapper.createObjectNode().put(key, map.get(key)));
				gNode.set(objectId, cNode);
			}
			node.set(gitRepo.getUrl(), gNode);
		}
		try {
			return mapper.writeValueAsString(node);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	// getGitFileList->doGetGitFileList->getGitFileHis(ids)->gitUtil.getGitFileHis->list/builder
	public String getGitFileList(){
		try {
			return doGetGitFileList();
		} catch (RuntimeException re){
			final Map<String, Object> map = new java.util.HashMap<String, Object>();
			map.put("builder", readTree(re.getLocalizedMessage()));
			return writeValueAsString(map);
		}
	}
	private String doGetGitFileList(){
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		for (String[][] props : PropGitFile.props){
			String pathString = props[0][0];
			String url = props[0][1];
			ObjectNode pNode = mapper.createObjectNode();
			pNode.put("url", url);
			ArrayNode cNode = mapper.createArrayNode();
			String[][] ids = new String[props.length - 1][];
			for (int i = 1; i < props.length; i++)
				ids[i-1] = props[i];
			for (Map<String, String> map : getGitFileHis(pathString, url, ids))
				cNode.add(mapper.createObjectNode()
						.put(GitUtil.COMMIT_ID, map.get(GitUtil.COMMIT_ID))
						.put(GitUtil.OBJECT_ID, map.get(GitUtil.OBJECT_ID))
						.put(GitUtil.COMMIT_TIME, map.get(GitUtil.COMMIT_TIME))
						.put(GitUtil.FULL_MESSAGE, map.get(GitUtil.FULL_MESSAGE)));
			pNode.set("commitObjects", cNode);
			node.set(pathString, pNode);
		}
		try {
			return mapper.writeValueAsString(node);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public String getGitFileHis(String pathString, String url){
		try {
			return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(gitUtil.getGitFileHis(getRepoFromMap(url), pathString));
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	private List<Map<String, String>> getGitFileHis(String pathString, String url, String[][] ids){
		List<Map<String, String>> list = gitUtil.getGitFileHis(getRepoFromMap(url), pathString);
		StringBuilder builder = new StringBuilder();
		List<String[]> idList = new java.util.ArrayList<String[]>(java.util.Arrays.<String[]>asList(ids));
		for (Map<String, String> map : list){
			String commitId = map.get(GitUtil.COMMIT_ID);
			String objectId =  map.get(GitUtil.OBJECT_ID);
			String[] id = null;
			for (int i = 0; i < idList.size(); i++)
				if (commitId.equals(idList.get(i)[0]) && objectId.equals(idList.get(i)[1])){
					id = idList.remove(i);
					break;
				}
			if (id == null)
				// javaForm(commitId, objectId)
				builder.append('\n').append(String.format("[\"%s\" ,\"%s\"],", commitId, objectId));
		}
		if (builder.length() > 0){
			builder.setLength(builder.length() - 1);
			throw new RuntimeException(builder.insert(0, '[').append(']').toString());
		}
		if (idList.size() > 0)
			throw new RuntimeException("[[\"getGitFileHis\", \"idList.size() > 0\"]]");
		return list;
	}
	public void getGitFile(String pathString, String url, String commitId, String objectId, java.io.OutputStream os){
		java.io.InputStream is = null;
		try {
			is = gitUtil.getGitFile(getRepoFromMap(url), pathString, commitId, objectId);
			byte[] b = new byte[8192];
			int len;
			while ((len = is.read(b)) != -1)
				os.write(b, 0, len);
			os.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}
	public void getGitFileDiff(String pathString, String url, String commitId1, String commitId2, java.io.OutputStream os){
		try {
			os.write(gitUtil.getGitFileDiff(getRepoFromMap(url), pathString, commitId1, commitId2).getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static void main(String[] args) {
		GitTest test = new GitTest();
		test.test(Boolean.valueOf(args[0]));
		System.out.println(test.testByDate("2017-7-30 0:0:0", null));
		System.out.println(test.getGitFileList());
		test.testStream();
		System.out.println(test.getGitFileHis("parent/simpleweb/src/main/java/simpleweb/TestServlet.java",
				"https://github.com/jimmyhsuz3/ch.test.parent.git"));
		test.close();
	}
	private void testStream(){
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		getGitFileDiff("parent/simpleweb/src/main/java/simpleweb/TestServlet.java",
				"https://github.com/jimmyhsuz3/ch.test.parent.git",
				"84aa78312866201134d2f56d73109935f3bafab4", "aa7b7fdef57ef077530298e240a4bd1ee0f213e1", baos);
		try {
			baos.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println(new String(baos.toByteArray()));
		baos.reset();
	}
	private Object readTree(String obj){
		try {
			return new com.fasterxml.jackson.databind.ObjectMapper().readTree(obj);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private String writeValueAsString(Object obj){
		try {
			return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	public String getAllUrl(){
		String[] urls = new String[GIT_REPOS.length];
		for (int i = 0; i < urls.length; i++)
			urls[i] = GIT_REPOS[i].getUrl();
		return writeValueAsString(urls);
	}
	public String fetchLastCommit(String url){
		String[] head = gitUtil.fetchLastCommit(url, TEMP_GIT, false);
		String[] fetchHead = gitUtil.fetchLastCommit(url, TEMP_GIT, true);
		// javaForm(commitId, commitTime)
		if (head[0].equals(fetchHead[0]) && head[1].equals(fetchHead[1]))
			return writeValueAsString(new String[][]{head});
		else
			return writeValueAsString(new String[][]{head, fetchHead});
	}
	public String fetchAllLastCommit(){
		java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(GIT_REPOS.length);
		final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(GIT_REPOS.length);
		final Map<String, Object> map = new java.util.HashMap<String, Object>();
		final Thread thread = Thread.currentThread();
		for (int i = 0; i < GIT_REPOS.length; i++){
			final int n = i;
			executor.execute(new Runnable(){
				@Override
				public void run() {
					try {
						map.put(GIT_REPOS[n].getUrl(), readTree(fetchLastCommit(GIT_REPOS[n].getUrl())));
					} catch (Exception e) {
						thread.interrupt();
					}
					latch.countDown();
				}
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			executor.shutdown();
		}
		return writeValueAsString(map);
	}
}