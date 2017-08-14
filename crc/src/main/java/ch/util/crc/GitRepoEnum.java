package ch.util.crc;
public enum GitRepoEnum {
	ch_test_browsinghis("https://github.com/jimmyhsuz3/ch-test-browsinghis.git", "jimmyhsianghsu", "jimmy.hsu@promeritage.com.tw"
			, new String[]{"1c31968c6ff63baf1f128354a73c1e2548c13b7d", "2017-07-31 10:59:07"}
			, new String[]{"0f158a4d396e8b452963e0f692106f522ec9ea59", "2017-08-02 15:15:49"}
			),
	ch_util_crc("https://github.com/jimmyhsuz3/ch.util.crc.git", "jimmyshu", "jimmy.shu@104.com.tw"
			, new String[]{"e6a488767513a22daee7c0b12ccb1cf384bce53b", "2017-08-01 16:35:37"}
			, new String[]{"ea943c31c68d4100f60153d649b25348ea38eb0c", "2017-8-2 13:31:04"}
			, new String[]{"285f6be12d30a998996a27152e58588a28b3ff91", "2017-8-2 13:54:33"}
			, new String[]{"dba10bdd055575df209e822686aa56755b4e0078", "2017-08-02 14:07:24"}
			, new String[]{"c8c75db11a6bde67aeece426b16f921ddb5408a5", "2017-08-02 14:51:50"}
			, new String[]{"5bc3a4ecc3a6c363fd324db1fd0e0e9aae00ecca", "2017-08-02 14:57:29"}
			, new String[]{"8747b3ed15393846bc30696d865d5c19529e0ba3", "2017-8-9 13:14:53"}
			, new String[]{"68b60dc626016698445edfa2b37e34736eaa3448", "2017-08-14 13:26:30"}
			),
	ch_test_http("https://github.com/jimmyhsuz3/ch-test-http.git", "jimmy.hsu", "jimmy.hsu@104.com.tw"
			, new String[]{"216a62a68373e8bf75e3bde7613b8870372d66a8", "2017-08-01 14:34:35"}
			),
	ch_memo("https://github.com/jimmyhsuz3/ch-memo.git", "jimmy.hsu", "jimmy.hsu@104.com.tw"
			, new String[]{"da98579fe56e6315a085d039177fc63e60492c5e", "2017-07-31 09:52:25"}
			, new String[]{"2e6ff7c8f5b1f97ea80d2ebfbb14644d1f84f808", "2017-08-02 15:03:24"}
			, new String[]{"55b7f9c6cfcedad04a1f813eeca88ad715cf3db5", "2017-08-04 17:49:04"}
			),
	ch_test_redis("https://github.com/jimmyhsuz3/ch-test-redis.git", "jimmyhsianghsu", "jimmy.hsu@104.com.tw"
			, new String[]{"024069b116a4bac7946d140b3314599b37510082", "2017-07-31 10:31:04"}
			),
	ch_test_messaging_rabbitmq("https://github.com/jimmyhsuz3/ch-test-messaging-rabbitmq.git", "jimmyhsuz3", "carboboopz3@gmail.com"
			, new String[]{"c5943bcc8d6fd3c7e5b40d183cb1118856a0b7a4", "2017-7-25 13:47:52"}
			),
	ch_test_parent("https://github.com/jimmyhsuz3/ch.test.parent.git", "jimmy.shu", "jimmy.shu@104.com.tw"
			, new String[]{"a62f91003d382d1715d77de21ceaca0f288f97df", "2017-7-21 18:08:21"}
			, new String[]{"33a49973fec38ec46b9db2601c0bcb707eef6630", "2017-08-09 12:43:54"}
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