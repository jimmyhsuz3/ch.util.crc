package ch.util.crc;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
public class GitUtil {
	public static final String COMMIT_ID = "commitId";
	public static final String OBJECT_ID = "objectId";
	public static final String COMMIT_TIME = "commitTime";
	public static final String FULL_MESSAGE = "fullMessage";
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
			} catch (RuntimeException re){
				if (repo != null)
					repo.close();
				throw re;
			}
		}
		return null;
	}
	// important-1*2:heads (commitId, commitTime)
	private boolean checkRepo(GitRepo gitRepo, Repository repo){
		for (String[] head : gitRepo.getHeads()){
			try {
				if (!new java.text.SimpleDateFormat(GitRepo.DATE_FORMAT).parse(head[1]).equals(
						getCommitTime(repo.parseCommit(repo.resolve(head[0])).getCommitTime())))
					throw new RuntimeException("fail: checkRepo: " + gitRepo.getUrl());
			} catch (java.text.ParseException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			Ref head = repo.exactRef("HEAD");
			// head=fetch_head=gitRepo.head
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
	// important-3*4:
	// Repository repo, String pathString, String commitId *4
	public List<String> treeWalkFolder(Repository repo, String pathString, String commitId, boolean recursive){
		List<String> list = new java.util.ArrayList<String>();
		TreeWalk walk = null;
		try {
			walk = new TreeWalk(repo);
			walk.addTree(repo.parseCommit(ObjectId.fromString(commitId)).getTree());
			walk.setRecursive(false);
			if (pathString == null || pathString.trim().isEmpty()){
				while (walk.next())
					if(walk.getDepth() == 0)
						list.add(walk.getPathString() + (walk.isSubtree() ? '/' : ""));
			} else
				while (walk.next()){
					if (walk.isSubtree())
						walk.enterSubtree();
					if (pathString.equals(walk.getPathString())){
						int depth = walk.getDepth();
						while (walk.next() && depth == walk.getDepth())
							list.add(walk.getPathString() + (walk.isSubtree() ? '/' : ""));
						break;
					}
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
		return list;
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
	private Date getCommitTime(int commitTime){return new Date(commitTime * 1000l);}
	public List<String> commitsFromDate(Repository repo, Date from, Date to){
		List<String> list = new java.util.ArrayList<String>();
		try {
			RevCommit commit = repo.parseCommit(repo.resolve("FETCH_HEAD"));
			// important-2*4-1:commit.getParentCount()
/*
easyweb=8-15-6
DiffEntry[ADD
HsitoryManager=5
/dev/null
ProfileSearchEngineTest=11
*/
			while (commit.getParentCount() >= 0){
				Date commitTime = getCommitTime(commit.getCommitTime());
				if ((from == null || commitTime.after(from)) && (to == null || commitTime.before(to)))
					list.add(commit.toObjectId().name());
				if (commit.getParentCount() == 0)
					break;
				commit = repo.parseCommit(commit.getParent(0).toObjectId());
			}
		} catch (RevisionSyntaxException e) {
			throw new RuntimeException(e);
		} catch (IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		} catch (MissingObjectException e) {
			throw new RuntimeException(e);
		} catch (AmbiguousObjectException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return list;
	}
	public Map<String, String> commitDiffs(Repository repo, String objectId){
		Map<String, String> map = new java.util.HashMap<String, String>();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DiffFormatter df = null;
		try {
			RevCommit commit = repo.parseCommit(ObjectId.fromString(objectId));
			df = new DiffFormatter(baos);
			df.setRepository(repo);
			// important-2*4-2:commit.getParentCount()
			for(DiffEntry entry : df.scan(commit.getParentCount() > 0 ? repo.parseCommit(commit.getParent(0)).getTree() : null, commit.getTree())){
				df.format(entry);
				baos.flush(); // no-op?
				map.put(entry.toString(), new String(baos.toByteArray()));
				System.out.println(new String(baos.toByteArray())); //
				baos.reset();
			}
			map.put("_1:commitTime", new java.text.SimpleDateFormat(GitRepo.DATE_FORMAT).format(getCommitTime(commit.getCommitTime())));
			map.put("_2:fullMessage", commit.getFullMessage());
		} catch (IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		} catch (MissingObjectException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (df != null)
				df.close();
			if (baos != null)
				try {
					baos.close(); // no-op?
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
		return map;
	}
	private String treeFile(Repository repo, String objectId, String pathString){
		TreeWalk walk = null;
		try {
			walk = new TreeWalk(repo);
			walk.addTree(repo.parseCommit(ObjectId.fromString(objectId)).getTree());
			walk.setRecursive(false);
			while (walk.next())
				if (walk.isSubtree())
					walk.enterSubtree();
				else if(pathString.equals(walk.getPathString()))
					return walk.getObjectId(0).name();
			return null;
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
	}
	// important-3*4:
	// String pathString, String url *4(his2.file.diff) + list
	// Repository repo, String pathString *3(his.file.diff)
	public List<Map<String, String>> getGitFileHis(Repository repo, String pathString, String commitId){ // getGit
		List<Map<String, String>> list = new java.util.ArrayList<Map<String, String>>();
		try {
			RevCommit commit = commitId != null && !commitId.trim().isEmpty() ?
					repo.parseCommit(repo.resolve(commitId)) : repo.parseCommit(repo.resolve("FETCH_HEAD"));
			commitId = commit.name();
			String objectId = null;
			String commitTime = null;
			String fullMessage = null;
			while (true) {
				String id = commit != null ? treeFile(repo, commit.name(), pathString) : null;
				if (objectId == null){
					objectId = id;
				} else if (objectId != null && (id == null || !id.equals(objectId))){
					Map<String, String> map = new java.util.HashMap<String, String>();
					map.put(COMMIT_ID, commitId);
					map.put(OBJECT_ID, objectId);
					map.put(COMMIT_TIME, commitTime);
					map.put(FULL_MESSAGE, fullMessage);
					list.add(map);
				}
				if (commit == null)
					break;
				commitId = commit.name();
				objectId = id;
				commitTime = new java.text.SimpleDateFormat(GitRepo.DATE_FORMAT).format(getCommitTime(commit.getCommitTime()));
				fullMessage = commit.getFullMessage();
				// important-2*4-3:commit.getParentCount()
				if (commit.getParentCount() > 0)
					commit = repo.parseCommit(commit.getParent(0).toObjectId());
				else commit = null;
			}
		} catch (RevisionSyntaxException e) {
			throw new RuntimeException(e);
		} catch (IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		} catch (MissingObjectException e) {
			throw new RuntimeException(e);
		} catch (AmbiguousObjectException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return list;
	}
	public java.io.InputStream getGitFile(Repository repo, String pathString, String commitId, String objectId){ // getGit
		TreeWalk walk = null;
		try {
			walk = new TreeWalk(repo);
			walk.addTree(repo.parseCommit(ObjectId.fromString(commitId)).getTree());
			walk.setRecursive(false);
			while (walk.next())
				if (walk.isSubtree())
					walk.enterSubtree();
				else if (pathString.equals(walk.getPathString()))
					if (objectId.equals(walk.getObjectId(0).name()))
						return new ShaUtil().hashObject(walk.getObjectReader().open(walk.getObjectId(0)).openStream(), objectId);
					else
						throw new RuntimeException("getFile");
			throw new RuntimeException("getFile");
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
	}
	public String getGitFileDiff(Repository repo, String pathString, String commitId1, String commitId2){ // getGit
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DiffFormatter df = null;
		try {
			df = new DiffFormatter(baos);
			df.setRepository(repo);
			// important-2*4-4:commit.getParentCount()
			RevCommit commit1 = commitId1 != null && !commitId1.trim().isEmpty() ? repo.parseCommit(ObjectId.fromString(commitId1)) : null;
			for(DiffEntry entry : df.scan(commit1, repo.parseCommit(ObjectId.fromString(commitId2)))){
				if (pathString.equals(entry.getNewPath()) &&
						(entry.getNewPath().equals(entry.getOldPath()) || "/dev/null".equals(entry.getOldPath()))){
					df.format(entry);
					baos.flush(); // no-op?
					return new String(baos.toByteArray());
				}
			}
		} catch (IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		} catch (MissingObjectException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (df != null)
				df.close();
			if (baos != null)
				try {
					baos.close(); // no-op?
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
		throw new RuntimeException("getGitFileDiff");
	}
	public String[] fetchLastCommit(String url, String filePath, boolean fetch){
		Repository repo = null;
		boolean clone = false;
		Matcher matcher = Pattern.compile("/([\\w-\\.]+).git").matcher(url);
		if (matcher.find()){
			File file = new File(new File(filePath), matcher.group(1));
			if (file.exists())
				try {
					repo = new FileRepositoryBuilder().setGitDir(file).readEnvironment().findGitDir().build();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			else {
				// fetchLastCommit(fetchRepo-repoBranchCommit-repoRefCommit)
				// 0824: GitUtil=setTransportConfigCallback,repoRefCommit, repoBranchCommit
				// 0824: GitUtil = fetchLastCommit + boolean clone*5, fetchRepo
				// FileRepositoryBuilder().setGitDir, Git.cloneRepository().setURI
				file.mkdirs();
			}
			if (repo == null || !repo.getObjectDatabase().exists()){
				try {
					repo = Git.cloneRepository().setURI(url).setDirectory(file).setBare(true).call().getRepository();
					clone = true;
				} catch (InvalidRemoteException e) {
					throw new RuntimeException(e);
				} catch (TransportException e) {
					throw new RuntimeException(e);
				} catch (IllegalStateException e) {
					throw new RuntimeException(e);
				} catch (GitAPIException e) {
					throw new RuntimeException(e);
				}
			}
		}
		if (repo != null){
			Git git = null;
			try {
				if (!clone){
					git = new Git(repo);
					if(fetch)
						System.out.println(git.fetch().call().getMessages());
				}
				RevCommit commit = repo.parseCommit(repo.resolve("FETCH_HEAD"));
				return new String[]{commit.name(), new java.text.SimpleDateFormat(GitRepo.DATE_FORMAT).format(getCommitTime(commit.getCommitTime()))};
			} catch (InvalidRemoteException e) {
				throw new RuntimeException(e);
			} catch (TransportException e) {
				throw new RuntimeException(e);
			} catch (GitAPIException e) {
				throw new RuntimeException(e);
			} catch (IncorrectObjectTypeException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (git != null)
					git.close();
				if (repo != null)
					repo.close();
				System.out.println("fetchLastCommit" + ".close = " + url);
			}
		}
		throw new RuntimeException("fetchLastCommit");
	}
	public Repository fetchRepo(String url, String filePath){
		Repository repo = null;
		Matcher matcher = Pattern.compile("/([\\w-\\.]+).git").matcher(url);
		if (matcher.find()){
			File file = new File(new File(filePath), matcher.group(1));
			if (file.exists())
				try {
					repo = new FileRepositoryBuilder().setGitDir(file).readEnvironment().findGitDir().build();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			else
				file.mkdirs();
			if (repo == null || !repo.getObjectDatabase().exists())
				try {
					if (url.startsWith("git@"))
					repo = Git.cloneRepository().setURI(url).setDirectory(file).setBare(true)
							.setTransportConfigCallback(JschUtil.config()).call().getRepository();
					else
						repo = Git.cloneRepository().setURI(url).setDirectory(file).setBare(true)
							.setCredentialsProvider(JschUtil.getCredentialsProvider()).call().getRepository();
				} catch (InvalidRemoteException e) {
					throw new RuntimeException(e);
				} catch (TransportException e) {
					throw new RuntimeException(e);
				} catch (IllegalStateException e) {
					throw new RuntimeException(e);
				} catch (GitAPIException e) {
					throw new RuntimeException(e);
				}
			else {
				Git git = null;
				try {
					git = new Git(repo);
					System.out.println(git.fetch().setTransportConfigCallback(JschUtil.config()).call().getMessages());
				} catch (InvalidRemoteException e) {
					throw new RuntimeException(e);
				} catch (TransportException e) {
					throw new RuntimeException(e);
				} catch (GitAPIException e) {
					throw new RuntimeException(e);
				} finally {
					if (git != null)
						git.close();
				}
			}
		}
		return repo;
	}
	private String[] repoRefCommit(Repository repo, String refName){
		RevCommit commit;
		try {
			commit = repo.parseCommit(repo.resolve(refName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new String[]{commit.name(), new java.text.SimpleDateFormat(GitRepo.DATE_FORMAT).format(getCommitTime(commit.getCommitTime()))};
	}
	public Map<String, String[]> repoBranchCommit(Repository repo){
		Map<String, String[]> map = new java.util.HashMap<String, String[]>();
		for (java.util.Iterator<java.util.Map.Entry<String, Ref>> iter = repo.getAllRefs().entrySet().iterator(); iter.hasNext();){
			java.util.Map.Entry<String, Ref> entry = iter.next();
			map.put(entry.getValue().getName(), repoRefCommit(repo, entry.getValue().getName()));
		}
		return map;
	}
}