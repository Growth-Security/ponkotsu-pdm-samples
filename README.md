# ポンコツPdMの開発メモ — サンプルコード

[ポンコツPdMの開発メモ](https://syachiku-uz.info/)の記事で紹介した実装やテストを、自分の環境で確かめるためのサンプル集です。Java・Springの改修を任された新人・若手エンジニアを中心に、原因の切り分け、修正、テスト結果の説明を練習できます。

サンプルはそれぞれ独立して実行できます。クラス設計の入門例はjavacで、それ以外は各フォルダーのMavenで実行します。必要なフォルダーに移動して実行してください。すべてを組み合わせた一つのアプリケーションではありません。

## サンプルを選ぶ

| やりたいこと | サンプル | 対応記事 |
| --- | --- | --- |
| メソッド抽出とクラス分割を、保存回数まで保って比較する | [メソッドかクラスか](design/method-or-class/)・[サンプルZIP](downloads/method-or-class-sample.zip) | [Javaの処理をどこで分けるか](https://syachiku-uz.info/how-to-split/) |
| 計算と表示を分け、変更するクラスを判断する | [Javaのクラス設計](java/class-design-basics/) | 公開準備中 |
| JUnitで最初の3件を実行し、1件の失敗を直す | [JUnitの最初の実行](testing/junit-first-test/)・[サンプルZIP](downloads/junit-first-test-sample.zip) | JUnit 5入門（記事は2026年10月24日8:00公開予定） |
| 仕様から入力・期待値を選び、境界をJUnitで確認する | [Javaのテスト入門](testing/java-testing-basics/) | 公開準備中 |
| 入力制約、検証グループ、エラーメッセージ、400応答を確認する | [入力検証の基本](spring/form-validation/01-validation-basics/) | [@Validと@Validated](https://syachiku-uz.info/spring-valid-validated-difference/)・[メッセージが表示されない](https://syachiku-uz.info/spring-validation-message-not-displayed/)・[400の原因](https://syachiku-uz.info/spring-400-bad-request-causes/) |
| FormからServiceへ渡す値と、業務上のチェックを分ける | [ControllerとServiceの分担](spring/form-validation/02-controller-service/) | [FormとDTO](https://syachiku-uz.info/spring-form-dto-difference/)・[ControllerとService](https://syachiku-uz.info/spring-validation-controller-service/) |
| 入力エラー後も値・選択肢・メッセージを表示する | [Thymeleafの再表示](spring/form-validation/03-thymeleaf-redisplay/) | [入力エラー後に値が消える](https://syachiku-uz.info/spring-thymeleaf-form-value-lost/) |
| MockMvcでPOSTしたときのCSRF・認証・入力エラーを切り分ける | [MockMvcとSecurity](spring/form-validation/04-mockmvc-security/) | [MockMvcのPOSTが403・400になる](https://syachiku-uz.info/spring-mockmvc-post-403-400/) |
| Serviceの単体テスト、mock / spy、when / doReturnを試す | [JUnit・Mockito](testing/mockito-order-lab/) | [ServiceをJUnit・Mockitoでテストする](https://syachiku-uz.info/spring-service-junit-mockito/) |

フォーム改修の読む順は[Spring Bootのフォーム入力・バリデーション実践ガイド](https://syachiku-uz.info/spring-form-validation-guide/)を参照してください。

## 最初の実行

JDK 17とMaven 3.9系を用意してください。初回は依存ライブラリを取得するため、インターネット接続が必要です。外部DBやAPIキーは不要です。

```sh
git clone https://github.com/Growth-Security/ponkotsu-pdm-samples.git
cd ponkotsu-pdm-samples/spring/form-validation/01-validation-basics
mvn test
```

結果は各サンプルの `target/surefire-reports/` に出力されます。依存関係を取得済みなら `mvn -o test` でオフライン実行できます。ほかのサンプルも、そのサンプルの `pom.xml` があるフォルダーで `mvn test` を実行します。

失敗例は「期待どおりエラーや例外になること」をテストしています。そのため、正常例と失敗例の両方を確認していても、テスト全体の結果は成功になります。

## 記事と検証版

記事で確認した依存バージョン、テスト数、検証範囲は[検証記録](docs/VERIFICATION.md)にまとめています。記事を追試するときは、同記録が示す固定版とサンプルのREADMEを確認してください。既存のコードを大きく書き換える場合は、記事の説明と合わなくなる点を記録します。

最新の実務環境に合わせた完全なアプリケーションではありません。特にDBへの永続化、実ブラウザー、実際のログインやパスワード照合、同時実行や負荷は、別途検証が必要です。

## サンプルの追加・問題の報告

追加時の構成や変更時の確認事項は[サンプルの管理方針](CONTRIBUTING.md)に記載しています。誤りを見つけた場合は、対応記事、サンプル名、環境、再現手順を添えてIssueでお知らせください。会社名・個人名・認証情報・社内資料・業務コードは投稿しないでください。

## 利用について

このリポジトリの独自作成のサンプルコード、テンプレート、説明文は[MITライセンス](LICENSE)で公開しています。著作権表示とライセンス文を保持する条件で、学習・社内練習に加え、改変・再配布・商用利用も可能です。詳しい条件と無保証の定めは `LICENSE` を確認してください。

依存ライブラリのバイナリは同梱していません。依存ライブラリには、それぞれのライセンスが適用されます。
