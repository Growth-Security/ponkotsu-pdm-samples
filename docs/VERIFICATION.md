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

GitHub上の現在の検証状態は[Actionsの実行結果](https://github.com/Growth-Security/ponkotsu-pdm-samples/actions/workflows/verify-samples.yml)で確認できます。自動検証は、mainへのPush、Pull Request、手動実行を対象に、Java 17で各サンプルを個別にテストします。上記のローカル成功数を、GitHub上での実行結果に読み替えないでください。


## 選書記事の理解度チェックサンプル（2026年10月20日）

新規の`spring/book-selection-check`を独立プロジェクトとして追加しました。既存5サンプルのソースと依存バージョンは変更していません。書籍コードの転載ではなく、当ブログの独自コードです。ルートのMITライセンスを適用します。

JDK 17.0.8、Maven 3.9.16、Spring Boot 3.5.16、Hibernate Validator 8.0.1.Finalで`mvn -B --no-transfer-progress verify`を実行し、6件成功、失敗・エラー・スキップ0件、実行可能JARの生成成功を確認しました。必要なSpring JDBC/H2依存を通常のMaven取得で追加しています。

| テスト | 件数 | 確認結果 |
| --- | ---: | --- |
| 正常登録 | 1 | MockMvc→Controller→Service→H2を通し、201、DB生成ID、数量、注文保存、在庫減算 |
| 入力不正 | 4 | 数量0、文字列abc、欠落、nullが400、注文・在庫に変更なし |
| 在庫不足 | 1 | 先行成功注文を保持し、後続の注文INSERT後の在庫不足が409、失敗注文のみロールバック、在庫維持 |

テスト本体は`@Transactional`を使わず、ServiceのSpringトランザクション確定後にH2を直接照会しています。DBとServiceはmock化していません。在庫不足のUPDATEは0件なので、そのUPDATEが在庫を変更してから戻ると説明するものではありません。ロールバックの検証対象は先行する注文INSERTです。

生成したJARを`java -jar target/book-selection-check-1.0.0.jar`で起動し、READMEのcurlで201と`{"orderId":1,"quantity":2}`、続く数量4のリクエストで409を確認しました。確認後にアプリを停止しています。

対象外は実ブラウザー、本番DB、並行処理、認証・認可、負荷、実運用の在庫管理です。GitHub Actionsのmatrixに新規サンプルを追加していますが、このローカル結果はGitHub上の成功を意味しません。既存5サンプルの再実行はこの追加作業では行っていません。
