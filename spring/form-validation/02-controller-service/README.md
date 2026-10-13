# ControllerとServiceの分担

FormからCommandへ渡す値、形式チェックとメール重複の業務判断、HTMLの項目エラーとJSONの409、保存処理の呼出有無を確認します。

対応記事：[FormとDTOを分ける判断](https://syachiku-uz.info/spring-form-dto-difference/)・[ControllerとServiceの分担](https://syachiku-uz.info/spring-validation-controller-service/)。

JDK 17とMaven 3.9系を用意し、このフォルダーで実行します。

```sh
mvn test
```

既存の検証版は7件成功、失敗・エラー・スキップ0件です。`src/test/java/` の `RegistrationFlowTest` を開き、ControllerからServiceへ何が渡り、保存処理が呼ばれるかを確認してください。

本番DBへの永続化、並行登録の競合、DBの一意制約は保証しません。依存バージョンと固定版は[検証記録](../../../docs/VERIFICATION.md)を参照してください。
