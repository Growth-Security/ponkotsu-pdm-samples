# 検証記録

この文書は、ブログで配布した既存サンプルの検証結果と、GitHubでの公開時点の対応を記録する正本です。ローカルで実行済みの結果と、GitHub上の自動検証は分けて扱います。

## ブログ配布版の記録

2026年10月12日に確認したサンプルです。JDK 17.0.8、Maven 3.9.16で、合計80件のテストが成功しています。失敗・エラー・スキップはいずれも0件です。この数値は公開前の既存検証記録に基づき、GitHub公開作業で再実行した結果を意味しません。

| サンプル | テスト数 | 確認すること | 主な検証対象外 |
| --- | ---: | --- | --- |
| `spring/form-validation/01-validation-basics` | 17 | 制約・グループ、メッセージ、型変換、必須値、JSONと400応答 | 実ブラウザー、DB |
| `spring/form-validation/02-controller-service` | 7 | FormからCommandへの受け渡し、形式と業務判断、HTMLエラーとJSONの409、保存処理の呼出有無 | 本番DBへの保存、並行処理、一意制約 |
| `spring/form-validation/03-thymeleaf-redisplay` | 6 | 入力値・選択肢・選択値・エラー文の再表示、正常時の遷移、BindingResultの位置 | 実ブラウザー、JavaScript |
| `spring/form-validation/04-mockmvc-security` | 28 | CSRF・認証・権限、Form/JSON、HTTP応答とService到達 | 実ログイン、パスワード照合、全体のセキュリティ監査 |
| `testing/mockito-order-lab` | 22 | 注文取消4件、注文登録12件、mock/spyなどの比較6件 | Springコンテキスト、JPA、DB、トランザクション、並行実行 |

Spring Bootの依存管理は3.5.16です。フォーム例のHibernate Validatorは8.0.1.Final、Mockito例はJUnit Jupiter 5.12.2とMockito 5.17.0を使用しています。これらは記事を追試するための固定値です。

Mockito例は架空の注文システムです。「取消済み注文をもう一度取り消すと例外にする」「注文登録は99個が上限」は、このサンプルの仕様です。実案件共通の仕様ではありません。

## ブログで公開したZIPとの対応

| 配布ファイル | SHA-256 |
| --- | --- |
| `spring-form-validation-samples-20261012.zip` | `46743b01728aa5586820d445eb28858347c49ca690bf973dd92b15696e005d9f` |
| `mockito-order-lab.zip` | `63ef5cb848fbf995717d4edbb84979f4970c576f304d82600a72db2409870edc` |

GitHubではフォルダー配置と案内文を整理しています。上記の値は元のブログ配布ZIPのハッシュであり、GitHubの自動生成ZIPのハッシュではありません。公開用フォルダーへ移した5つのサンプルの `src/` と `pom.xml` は、元の配布版と全件一致することを確認しました。フォーム配布版の `SHA256SUMS.txt` に記載した41ファイルも一致しています。

## GitHub公開版

2026年10月13日、公開用の新しいフォルダー配置で5プロジェクトを再検証しています。JDK 17.0.8、Maven 3.9.16で、上表と同じ80件が成功し、失敗・エラー・スキップはいずれも0件でした。

初期公開の固定版参照先は [`samples-2026-10-13`](https://github.com/Growth-Security/ponkotsu-pdm-samples/tree/samples-2026-10-13) です。記事の追試時は、変更が続く最新ブランチと固定版を区別してください。

同日、独自作成のサンプルコード・テンプレート・説明文をMITライセンスで公開する方針を採用しました。MIT版の固定版参照先は [`samples-2026-10-13-mit`](https://github.com/Growth-Security/ponkotsu-pdm-samples/tree/samples-2026-10-13-mit) です。この変更は `LICENSE` の追加と案内文・検証記録の更新に限り、5つのサンプルの `src/` と `pom.xml` は初期公開コミット `d81b3a96433d8ea1b7996566cb48a87d26fe2e00` から変更していません。

初期タグと元のブログ配布ZIPは上書きしません。MIT版の利用条件は、新しい固定版に含まれる `LICENSE` を参照してください。上表の元ZIPのハッシュ値もそのまま維持します。

GitHub上の現在の検証状態は[Actionsの実行結果](https://github.com/Growth-Security/ponkotsu-pdm-samples/actions/workflows/verify-samples.yml)で確認できます。自動検証は、mainへのPush、Pull Request、手動実行を対象に、Java 17で5つのサンプルを個別にテストします。上記のローカル成功数を、GitHub上での実行結果に読み替えないでください。


## Javaのクラス設計・テスト入門（2026年10月23日）

Springを使い始める前の独立した2題材を追加しました。書籍の転載ではなく独自作成コードで、ルートのMITライセンスが適用されます。

- `java/class-design-basics`：JDK 17.0.8でREADMEのjavac/javaを実行。整理前と整理後はいずれも「請求額：6500円」、見積額6500円、小計6000円・送料500円を確認しました。検証の対象は料金計算と表示の分離で、Web・DB連携はありません。
- `testing/java-testing-basics`：JDK 17.0.8、Maven 3.9.5、JUnit Jupiter 5.12.2で `mvn -o test` を実行し9件成功、失敗・エラー・スキップ0件でした。既存キャッシュを用いた実行であり、空の依存キャッシュからの取得はこの検証に含みません。

既存サンプルのソース・依存バージョンには変更を加えていません。既存サンプルの再実行、GitHub Actionsでの新しい2例の実行は今回のローカル結果に含みません。
