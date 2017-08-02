package ch.util.crc;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
public class GitUtil {
	public Repository getRepo(GitRepo gitRepo, String filePath){
		Matcher matcher = Pattern.compile("/([\\w-\\.]+).git").matcher(gitRepo.getUrl());
		if (matcher.find()){
			String tempFolder = matcher.group(1);
			File file = new File(new File(filePath), tempFolder);
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
					throw new RuntimeException("fail: checkRepo: " + gitRepo.getUrl());
				
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
	private boolean checkRepo(GitRepo gitRepo, Repository repo){
		try {
			Ref head = repo.exactRef("HEAD");
			if (head.getObjectId().equals(repo.resolve("FETCH_HEAD")))
				if (gitRepo.getHead().equals(head.getObjectId().name())) {
					RevCommit commit = repo.parseCommit(head.getObjectId());
					if (gitRepo.getLastCommitTime().equals(new java.util.Date(commit.getCommitTime() * 1000l))){
						PersonIdent committer = commit.getCommitterIdent();
						System.out.println(String.format("%s, %s, %s, %s, %s\n",
								gitRepo.getUrl(), committer.getName(), committer.getEmailAddress(), gitRepo.getHead(), gitRepo.getLastCommitTime()));
						if (gitRepo.getName().equals(committer.getName()) && gitRepo.getEmailAddress().equals(committer.getEmailAddress())){
							System.out.println(treeWalk(repo, commit.toObjectId().name()));
							System.out.println(diff(repo, commit.toObjectId().name()));
							return true;
						}
					}
				}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return false;
	}
	private String treeWalk(Repository repo, String objectId){
		StringBuilder builder = new StringBuilder();
		TreeWalk walk = null;
		try {
			walk = new TreeWalk(repo);
			walk.addTree(repo.parseCommit(ObjectId.fromString(objectId)).getTree());
			walk.setRecursive(false);
			while (walk.next()){
				if (walk.isSubtree())
					walk.enterSubtree();
				else
					builder.append(walk.getPathString()).append('\n');
			}
		} catch (MissingObjectException e) {
			throw new RuntimeException(e);
		} catch (IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		} catch (CorruptObjectException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (walk != null)
				walk.close();
		}
		return builder.toString();
	}
	private String diff(Repository repo, String objectId){
		StringBuilder builder = new StringBuilder();
		DiffFormatter df = null;
		try {
			RevCommit commit = repo.parseCommit(ObjectId.fromString(objectId));
			df = new DiffFormatter(new java.io.ByteArrayOutputStream());
			df.setRepository(repo);
			for(DiffEntry entry : df.scan(commit.getParentCount() > 0 ? repo.parseCommit(commit.getParent(0)).getTree() : null, commit.getTree()))
				builder.append(entry).append('\n');
		} catch (IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		} catch (MissingObjectException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (df != null)
				df.close();
		}
		return builder.toString();
	}
}