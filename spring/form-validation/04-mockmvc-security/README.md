# MockMvcとSecurityのPOST検証

Securityフィルターを有効にして、CSRF、認証、権限、FormとJSONの違いを確認します。200・302・400・401・403・415の応答と、Serviceへ処理が届いたかを対応付けます。

対応記事：[MockMvcのPOSTが403・400になる原因](https://syachiku-uz.info/spring-mockmvc-post-403-400/)。

JDK 17とMaven 3.9系を用意し、このフォルダーで実行します。

```sh
mvn test
```

既存の検証版は28件成功、失敗・エラー・スキップ0件です。`src/test/java/` の `MockMvcPostTest` と `PublishedSnippetTest` を読み、リクエストの違いと期待する応答を確認してください。

認証済みリクエストはテスト用の `user()` で用意します。実際のログイン、パスワード照合、アプリ全体のセキュリティ監査は対象外です。依存バージョンと固定版は[検証記録](../../../docs/VERIFICATION.md)を参照してください。
