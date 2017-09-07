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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
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
	private class CommitComparator implements java.util.Comparator<String> {
		private Repository repo;
		private CommitComparator(Repository repo){this.repo = repo;}
		@Override
		public int compare(String commitId1, String commitId2) {
			Date commitTime1;
			Date commitTime2;
			try {
				commitTime1 = getCommitTime(repo.parseCommit(repo.resolve(commitId1)).getCommitTime());
				commitTime2 = getCommitTime(repo.parseCommit(repo.resolve(commitId2)).getCommitTime());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return commitTime2.compareTo(commitTime1);
		}
	}
private class RepoCache {
	private Map<String, Integer> ladderMap = new HashMap<String, Integer>();
	private Set<String> getAllRefsParent(Repository repo, String commitId, Date from, Date to, boolean self, Map<String, Set<String>> cache){
		boolean first = false;
		if (cache == null){
			cache = new HashMap<String, Set<String>>();
			first = true;
			ladderMap.clear();
		} else if (cache.containsKey(commitId)){
			ladderMap.put(commitId, ladderMap.containsKey(commitId) ? ladderMap.get(commitId) + 1 : 1);
			return cache.get(commitId);
		}
		Set<String> parents = null; //:parents, set
		RevCommit commit;
		try {
			commit = repo.parseCommit(repo.resolve(commitId));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (self){
			Date commitTime = getCommitTime(commit.getCommitTime());
			if ((from == null || !commitTime.before(from)) && (to == null || !commitTime.after(to))){ //:from-to
				parents = new HashSet<String>();
				parents.add(commit.name());
			}
		}
		// 5=(ant-1*3, ant-2*5, allrefs*3) + /*5*/, =5=11
		// 5=(ant-1*4, ant-2*6, allrefs*3) + /*5*/, =5=11 + ant-3*4
		for (int i = 0; i < commit.getParentCount(); i++){ // ant-2*6-5
			RevCommit parent;
			try {
				parent = repo.parseCommit(commit.getParent(i));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			Date commitTime = getCommitTime(parent.getCommitTime());
			if (from == null || !commitTime.before(from)){ //:from-to
				if (to == null || !commitTime.after(to)){
					if (parents == null)
						parents = new HashSet<String>();
					parents.add(parent.getName());
				}
				Set<String> set = getAllRefsParent(repo, parent.getName(), from, to, false, cache); //:parents, set
				if (set != null && set.size() > 0)
					if (parents == null)
						parents = set;
					else
						parents.addAll(set);
			}
		}
		if (first){
			System.out.println(String.format("[%s]%s", repo.toString(), commitId));
			for (String id : ladderMap.keySet())
				if (ladderMap.get(id) > 0)
					System.out.println(id + " = " + ladderMap.get(id));
		}
		cache.put(commitId, parents);
		return parents;
	}
	private Map<String, Map<String, Map<String, Set<String>>>> repoRefCommitMap = new HashMap<String, Map<String, Map<String, Set<String>>>>();
	private final String heads = "refs/heads/";
	private final String commits = "commits";
	private final String leafs = "leafs";
	private List<String> getAllRefsByCommit(Repository repo, String commitId){
		Map<String, Map<String, Set<String>>> refMap = repoRefCommitMap.get(repo.toString());
		if (refMap == null){
			refMap = new HashMap<String, Map<String, Set<String>>>();
			for (Ref ref : getAllRefs(repo.getAllRefs().values())){
				String name = ref.getName();
				if (name.startsWith(heads))
					name = name.substring(heads.length());
				Map<String, Set<String>> commitMap = new HashMap<String, Set<String>>();
				commitMap.put(commits, new HashSet<String>());
				commitMap.get(commits).add(ref.getObjectId().name());
				commitMap.put(leafs, new HashSet<String>());
				commitMap.get(leafs).add(ref.getObjectId().name());
				refMap.put(name, commitMap);
			}
			repoRefCommitMap.put(repo.toString(), refMap);
		}
		List<String> refs = new java.util.ArrayList<String>();
		try {
			Date from = getCommitTime(repo.parseCommit(repo.resolve(commitId)).getCommitTime());
			for (String name : refMap.keySet())
				if (refMap.get(name).get(commits).contains(commitId))
					refs.add(name);
				else {
					// commits.leafs+temps.lefts
					List<String> temps = new LinkedList<String>(refMap.get(name).get(leafs));
					List<String> lefts = new LinkedList<String>();
					boolean match = false;
					for (int t = 0; t < temps.size(); t++){
						String leaf = temps.remove(t--);
						lefts.add(leaf); // remove then add
						try {
							RevCommit commit = repo.parseCommit(repo.resolve(leaf));
							if (!getCommitTime(commit.getCommitTime()).before(from)){ //:from-to
								for (int i = 0; i < commit.getParentCount(); i++){ // ant-2*6-6
									RevCommit parent = repo.parseCommit(commit.getParent(i));
									refMap.get(name).get(commits).add(parent.name()); // add commits
									temps.add(parent.name()); // add temps
									if (parent.name().equals(commitId)){
										refs.add(name);
										match = true;
									}
								}
								lefts.remove(leaf); // for-all then remove lefts
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						if (match)
							break;
					}
					refMap.get(name).get(leafs).clear();
					refMap.get(name).get(leafs).addAll(temps);
					refMap.get(name).get(leafs).addAll(lefts);
				}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// important-1*4-4: getAllRefsByCommit=getAllRefsParent
//		if (refs.size() > 0)return refs;
		List<String> list = new java.util.ArrayList<String>();
		RevCommit commit;
		try {
			commit = repo.parseCommit(repo.resolve(commitId));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Map<String, Set<String>> cache = new HashMap<String, Set<String>>();
		for (Ref ref : getAllRefs(repo.getAllRefs().values())){
			Set<String> set = getAllRefsParent(repo, ref.getName(), getCommitTime(commit.getCommitTime()), null, true, cache); //:parents, set
			if (set != null && set.contains(commitId)){
				String name = ref.getName();
				if (name.startsWith(heads))
					name = name.substring(heads.length());
				list.add(name);
			}
		}
		for (String test : refs)
			if (!list.remove(test))
				throw new RuntimeException("getAllRefsByCommit=getAllRefsParent");
		if (list.size() > 0)
			throw new RuntimeException("getAllRefsByCommit=getAllRefsParent");
		return refs;
	}
	private Set<Ref> getAllRefs(java.util.Collection<Ref> refs){
		Set<Ref> set = new HashSet<Ref>();
		for (Ref ref : refs)
			set.add(ref.getLeaf());
		return set;
	}
	private Map<String, Map<String, JsonNode>> repoCommitNodeMap = new HashMap<String, Map<String, JsonNode>>();
	private Map<String, Map<String, Set<String>>> repoCommitLeafMap = new HashMap<String, Map<String, Set<String>>>();
	private ObjectNode getNode(Repository repo, String commitId, Date from){
		Map<String, JsonNode> nodeMap = repoCommitNodeMap.get(repo.toString());
		if (nodeMap == null){
			nodeMap = new HashMap<String, JsonNode>();
			repoCommitNodeMap.put(repo.toString(), nodeMap);
		}
		Map<String, Set<String>> leafMap = repoCommitLeafMap.get(repo.toString());
		if (leafMap == null){
			leafMap = new HashMap<String, Set<String>>();
			repoCommitLeafMap.put(repo.toString(), leafMap);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		if (nodeMap.get(commitId) != null){
			List<String> temps = new LinkedList<String>(leafMap.get(commitId));
			List<String> lefts = new LinkedList<String>();
			boolean match = false;
			for (int t = 0; t < temps.size(); t++){
				String leaf = temps.remove(t--);
				lefts.add(leaf);
				ObjectNode node = (ObjectNode) nodeMap.get(leaf);
				node.withArray("parents");
				try {
					RevCommit commit = repo.parseCommit(repo.resolve(leaf));
					if (!getCommitTime(commit.getCommitTime()).before(from)){
						for (int i = 0; i < commit.getParentCount(); i++){
							RevCommit parent = repo.parseCommit(commit.getParent(i));
							temps.add(parent.name());
						}
						lefts.remove(leaf);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			leafMap.get(commitId).clear();
			leafMap.get(commitId).addAll(temps);
			leafMap.get(commitId).addAll(lefts);
		} else {
			RevCommit commit;
			try {
				commit = repo.parseCommit(repo.resolve(commitId));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			ObjectNode node = mapper.createObjectNode();
			nodeMap.put(commitId, node);
			Date commitTime = getCommitTime(commit.getCommitTime());
			node.put("commitTime", new java.text.SimpleDateFormat(GitRepo.DATE_FORMAT).format(commitTime));
			ArrayNode parents = mapper.createArrayNode();
			node.set("parents", parents);
			
			Set<String> leafs = new HashSet<String>();
			leafMap.put(commitId, leafs);
			
			if (commitTime.before(from) || commit.getParentCount() == 0){
				leafs.add(commitId);
				return node;
			}
			
			for (int i = 0; i < commit.getParentCount(); i++){
				RevCommit parent;
				try {
					parent = repo.parseCommit(commit.getParent(i));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				JsonNode parentNode = getNode(repo, parent.getName(), from);
				parents.add(parentNode);
				leafs.addAll(leafMap.get(parent.getName()));
			}
			return node;
		}
	}
}
	private RepoCache repoCache = new RepoCache();
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
				//:from-to
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
		Set<String> parents = new java.util.TreeSet<String>(new CommitComparator(repo)); //:parents, set
		for (Ref ref : repoCache.getAllRefs(repo.getAllRefs().values())){
			Set<String> set = repoCache.getAllRefsParent(repo, ref.getName(), from, to, true, null); //:parents, set
			if (set != null && set.size() > 0)
				parents.addAll(set);
		}
		// important-1*4-3: getAllRefsParent=getAllRefs+RevWalk
		List<String> testList = new java.util.LinkedList<String>(parents);
		Set<String> testSet = new HashSet<String>();
		for (Ref ref : repoCache.getAllRefs(repo.getAllRefs().values())){
			RevWalk revWalk = new RevWalk(repo);
			try {
				revWalk.markStart(repo.parseCommit(ref.getObjectId()));
				for (RevCommit commit : revWalk){
					Date commitTime = getCommitTime(commit.getCommitTime());
					if ((from == null || !commitTime.before(from)) && (to == null || !commitTime.after(to))){ //:from-to
						boolean exist = !testSet.add(commit.name());
						if (!exist)
							if (testList.remove(commit.name()))
								exist = true;
						if (!exist)
							throw new RuntimeException("getAllRefsParent=getAllRefs+RevWalk");
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				revWalk.close();
			}
		}
		if (testList.size() > 0)
			throw new RuntimeException("getAllRefsParent=getAllRefs+RevWalk");
		list.clear();
		list.addAll(parents);
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
			List<String> parents = new LinkedList<String>();
			for (int i = 0; i < commit.getParentCount() || (i == 0 && commit.getParentCount() == 0); i++){
				RevCommit parent = commit.getParentCount() > 0 ? repo.parseCommit(commit.getParent(i)) : null;
				RevTree tree = parent != null ? parent.getTree() : null;
			for(DiffEntry entry : df.scan(tree, commit.getTree())){
				df.format(entry);
				baos.flush(); // no-op?
				map.put(entry.toString().replace("DiffEntry", parent != null ? parent.name().substring(0, 6) : "null"),
						new String(baos.toByteArray()));
				System.out.println(new String(baos.toByteArray())); //
				baos.reset();
			}
				parents.add(parent != null ? parent.name().substring(0, 6) : null);
			}
			map.put("_1:commitTime", new java.text.SimpleDateFormat(GitRepo.DATE_FORMAT).format(getCommitTime(commit.getCommitTime())));
			map.put("_2:fullMessage", commit.getFullMessage());
			map.put("_3:refs", repoCache.getAllRefsByCommit(repo, objectId).toString());
			map.put("_4:parents", parents.toString());
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
	public JsonNode commitsFromDateDiffs(Repository repo, Date from, Date to){
		return null;
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
					if (url.startsWith("git@"))
					System.out.println(git.fetch().setTransportConfigCallback(JschUtil.config()).call().getMessages());
					else
						System.out.println(git.fetch().setCredentialsProvider(JschUtil.getCredentialsProvider()).call().getMessages());
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
			System.out.println(entry.getValue().getName() + '=' + entry.getValue().isSymbolic());
			map.put(entry.getValue().getName(), repoRefCommit(repo, entry.getValue().getName()));
		}
		return map;
	}
	public static void main(String[] args){
		Repository repo = new GitUtil().fetchRepo("git@github.com:104corp/104plus-Opengine.git", "C:/Users/jimmy.shu/Downloads/temp_git");
		try {
			System.out.println(repo.parseCommit(repo.resolve("6d22a3")).getParentCount());
			System.out.println(repo.parseCommit(repo.resolve("e29473")).getParentCount());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		TreeWalk walk = null;
		try {
			walk = new TreeWalk(repo);
			walk.addTree(repo.parseCommit(ObjectId.fromString("6d22a3fb1a3f13a67cce6d12ccc8e0dda6ccf663")).getTree());
			walk.addTree(repo.parseCommit(repo.resolve("e29473")).getTree());
			walk.setRecursive(false);
			while (walk.next()){
				if (walk.isSubtree() && !walk.getObjectId(0).equals(walk.getObjectId(1)))
					System.out.println(String.format("%s = %s, %s", walk.getPathString(), walk.getObjectId(0).name(), walk.getObjectId(1).name()));
				if (walk.isSubtree())
					walk.enterSubtree();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (walk != null)
				walk.close();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DiffFormatter df = null;
		try {
			df = new DiffFormatter(baos);
			df.setRepository(repo);
			for(DiffEntry entry : df.scan(repo.resolve("8647eb56453bc689c709998edfce147f2758f62e"),
					ObjectId.fromString("71d4cd0fae15a756a1fbd3350760d4399ab4d1b7"))){
				df.format(entry);
				// System.out.println(new String(baos.toByteArray()));
				System.out.println(entry);
				baos.reset();
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
					baos.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}
}