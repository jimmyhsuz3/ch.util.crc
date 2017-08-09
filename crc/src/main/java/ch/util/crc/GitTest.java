package ch.util.crc;
import org.eclipse.jgit.lib.Repository;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	public void test(boolean clean){
		if (clean)
			try {
				org.apache.commons.io.FileUtils.cleanDirectory(new java.io.File(TEMP_GIT));
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		System.out.println(GIT_REPOS.length);
		for (GitRepo gitRepo : GIT_REPOS)
			getRepoFromMap(gitRepo.getUrl());
	}
	public void close(){for(Repository repo : repoMap.values())repo.close();}
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
	public static void main(String[] args) {
		GitTest test = new GitTest();
		test.test(Boolean.valueOf(args[0]));
		System.out.println(test.testByDate("2017-7-30 0:0:0", null));
		test.close();
	}
}