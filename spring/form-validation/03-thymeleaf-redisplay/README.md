# Thymeleafの入力エラー後の再表示

異常POST後の入力値、選択肢、選択状態、エラー文を、生成されたHTMLで確認します。正常時の遷移とBindingResultの引数位置も比較します。

対応記事：[入力エラー後に値が消える原因](https://syachiku-uz.info/spring-thymeleaf-form-value-lost/)。

JDK 17とMaven 3.9系を用意し、このフォルダーで実行します。

```sh
mvn test
```

既存の検証版は6件成功、失敗・エラー・スキップ0件です。`src/test/java/info/syachiku/formkeep/FormKeepTest.java` から、参照データがない場合と復元した場合を比較してください。確認用HTMLは `target/evidence/` に出力されます。

MockMvcとJsoupを使う検証であり、実ブラウザーやJavaScriptの操作は確認しません。依存バージョンと固定版は[検証記録](../../../docs/VERIFICATION.md)を参照してください。
