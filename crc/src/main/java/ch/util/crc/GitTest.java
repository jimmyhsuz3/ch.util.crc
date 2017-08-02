package ch.util.crc;
import org.eclipse.jgit.lib.Repository;
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
		for (Repository repo : repoMap.values())
			repo.close();
	}
	public static void main(String[] args) {
		new GitTest().test(Boolean.valueOf(args[0]));
	}
}