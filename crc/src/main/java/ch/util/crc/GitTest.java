package ch.util.crc;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
public class GitTest {
	private static final GitRepo[] gitRepos = {
			new GitRepo("https://github.com/jimmyhsuz3/ch-test-http.git", "jimmy.hsu", "jimmy.hsu@104.com.tw"
					,"216a62a68373e8bf75e3bde7613b8870372d66a8"
					),
			new GitRepo("https://github.com/jimmyhsuz3/ch.util.crc.git", "jimmyshu", "jimmy.shu@104.com.tw"
					,"a9e70397f50b8cea7a5725f494f8f4d6808eaf84"
					),
	};
	private static final String TEMP_GIT = "C:/Users/jimmy.shu/Downloads/temp_git";
	private final Map<String, Repository> repoMap = new HashMap<String, Repository>();
	private Repository getRepo(GitRepo gitRepo){
		Matcher matcher = Pattern.compile("/([\\w-\\.]+).git").matcher(gitRepo.getUrl());
		if (matcher.find()){
			String tempFolder = matcher.group(1);
			File file = new File(new File(TEMP_GIT), tempFolder);
			if (!file.exists())
				file.mkdirs();
			Repository repo = null;
			try {
//				repo = FileRepositoryBuilder.create(file); //:11
//				repo.create();
				repo = new FileRepositoryBuilder().setGitDir(file).readEnvironment().findGitDir().build(); //:23
//				Git git = Git.init().setDirectory(file).call(); //:44
//				repo = git.getRepository();
//				System.out.println(repo.getObjectDatabase().exists()); //:5
				
//				exactRef, getRef, resolve
//				repo.open.getBytes(), .copyTo(System.out)

				if (!repo.getObjectDatabase().exists())
					repo = Git.cloneRepository().setURI(gitRepo.getUrl()).setDirectory(file).setBare(true).call().getRepository();
				
				if (!checkRepo(gitRepo, repo)) {
					Git git = null;
					try {
						git = new Git(repo);
						System.out.println(git.fetch().call().getMessages());
					} finally {
						if (git != null)
							git.close();
					}
				}
				else
					return repo;
				
				if (checkRepo(gitRepo, repo))
					return repo;
				else
					throw new RuntimeException("fail: checkRepo");
				
//				Git git = new Git(localRepository);
//				StoredConfig config = git.getRepository().getConfig();
//				config.setString("remote", "origin", "url", "https://github.com/jimmyhsuz3/ch-test-http.git");
//				config.save();
				
//				System.out.println(new Git(repo).pull().call().getFetchResult().getMessages());
//				System.out.println(new Git(repo).fetch().setCheckFetchedObjects(false).call().getMessages());
//				System.out.println(repo.getConfig().getString("remote","origin","url"));
	            
//	            System.out.println("========================================================================");
//	            ObjectLoader loader = repo.open(repo.resolve("de67bfb90ce1134d265b02769c1c7543a0566926"));
//	            loader.copyTo(System.out);
//	            System.out.println();
//	            loader = repo.open(repo.resolve("0578b51a6d722f9eb1efba6c0a0941eb3f6d55f2"));
//	            loader.copyTo(System.out);
//	            System.out.println("\n========================================================================");			
//				ObjectId id = repo.resolve("f0326cc:http/pom.xml");
//				System.out.println(new String(repo.open(id).getBytes()));
//				System.out.println("========================================================================");
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (InvalidRemoteException e) {
				throw new RuntimeException(e);
			} catch (TransportException e) {
				throw new RuntimeException(e);
			} catch (GitAPIException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	private boolean checkRepo(GitRepo gitRepo, Repository repo) throws IOException{
		Ref head = repo.exactRef("HEAD");
		if (head.getObjectId().equals(repo.resolve("FETCH_HEAD")))
			if (gitRepo.getHead().equals(head.getObjectId().name())) {
				RevCommit commit = repo.parseCommit(head.getObjectId());
				PersonIdent committer = commit.getCommitterIdent();
				System.out.println(committer.getName());
				if (gitRepo.getName().equals(committer.getName()) && gitRepo.getEmailAddress().equals(committer.getEmailAddress()))
					return true;
			}
		return false;
	}
	private Repository getRepoFromMap(String url){
		if (repoMap.containsKey(url)){
			System.out.println(url + " from repoMap");
			return repoMap.get(url);
		}
		for (GitRepo gitRepo : gitRepos)
			if (gitRepo.getUrl().equals(url)){
				Repository repo = getRepo(gitRepo);
				repoMap.put(gitRepo.getUrl(), repo);
				System.out.println(url + " from getRepo");
				return repo;
			}
		throw new RuntimeException("fail: getRepo");
	}
	public void test(boolean clean){
		if (clean)
			try {
				FileUtils.cleanDirectory(new File(TEMP_GIT));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		System.out.println(gitRepos.length);
		for (GitRepo gitRepo : gitRepos)
			getRepoFromMap(gitRepo.getUrl());
	}
	public static void main(String[] args) {
		new GitTest().test(false);
	}
}