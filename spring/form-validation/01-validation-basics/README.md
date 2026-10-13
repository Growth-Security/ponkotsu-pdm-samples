# 入力検証の基本

`@Valid`・`@Validated`、検証グループ、メッセージ表示、入力の型変換・必須値・JSONの400応答を確認します。

対応記事：[制約の使い分け](https://syachiku-uz.info/spring-valid-validated-difference/)・[メッセージが表示されない](https://syachiku-uz.info/spring-validation-message-not-displayed/)・[400の原因](https://syachiku-uz.info/spring-400-bad-request-causes/)。

JDK 17とMaven 3.9系を用意し、このフォルダーで実行します。

```sh
mvn test
```

既存の検証版は17件成功、失敗・エラー・スキップ0件です。まず `src/test/java/info/syachiku/blog/ArticleVerificationTest.java` の送信値と期待結果を読み、テストを動かしてください。

実ブラウザー、JavaScript、外部DB、実ネットワーク通信、負荷や同時実行は検証しません。依存バージョンと固定版は[検証記録](../../../docs/VERIFICATION.md)を参照してください。
