package ch.util.crc;
public enum GitRepoEnum {
	ch_test_browsinghis("https://github.com/jimmyhsuz3/ch-test-browsinghis.git", "jimmyhsianghsu", "jimmy.hsu@promeritage.com.tw"
			, new String[]{"1c31968c6ff63baf1f128354a73c1e2548c13b7d", "2017-07-31 10:59:07"}
			),
	ch_util_crc("https://github.com/jimmyhsuz3/ch.util.crc.git", "jimmyshu", "jimmy.shu@104.com.tw"
			, new String[]{"e6a488767513a22daee7c0b12ccb1cf384bce53b", "2017-08-01 16:35:37"}
			),
	ch_test_http("https://github.com/jimmyhsuz3/ch-test-http.git", "jimmy.hsu", "jimmy.hsu@104.com.tw"
			, new String[]{"216a62a68373e8bf75e3bde7613b8870372d66a8", "2017-08-01 14:34:35"}
			),
	ch_test_redis("https://github.com/jimmyhsuz3/ch-memo.git", "jimmy.hsu", "jimmy.hsu@104.com.tw"
			, new String[]{"da98579fe56e6315a085d039177fc63e60492c5e", "2017-07-31 09:52:25"}
			),
	ch_memo("https://github.com/jimmyhsuz3/ch-test-redis.git", "jimmyhsianghsu", "jimmy.hsu@104.com.tw"
			, new String[]{"024069b116a4bac7946d140b3314599b37510082", "2017-07-31 10:31:04"}
			),
	ch_test_messaging_rabbitmq("https://github.com/jimmyhsuz3/ch-test-messaging-rabbitmq.git", "jimmyhsuz3", "carboboopz3@gmail.com"
			, new String[]{"c5943bcc8d6fd3c7e5b40d183cb1118856a0b7a4", "2017-7-25 13:47:52"}
			),
	ch_test_parent("https://github.com/jimmyhsuz3/ch.test.parent.git", "jimmy.shu", "jimmy.shu@104.com.tw"
			, new String[]{"a62f91003d382d1715d77de21ceaca0f288f97df", "2017-7-21 18:08:21"}
			),
	ch_test_mongodb("https://github.com/jimmyhsuz3/ch-test-mongodb.git", "jimmy.hsu", "jimmy.hsu@104.com.tw"
			, new String[]{"99dcfd6158fd725bdcf413712c0ec5dd1abb9c46", "2017-7-13 17:48:34"}
			, new String[]{"99dcfd6158fd725bdcf413712c0ec5dd1abb9c46", "2017-7-13 17:48:34"}
			),
	;
	private GitRepo gitRepo;
	private GitRepoEnum(String url, String name, String emailAddress, String[]... heads){
		gitRepo = new GitRepo(url, name, emailAddress, heads);
	};
	public GitRepo gitRepo(){
		return gitRepo;
	}
}