# JUnit・MockitoでServiceの単体テストを動かす

これは架空の注文システムを使った学習用サンプルです。実際の顧客・会社・案件・業務コードは含みません。

既存記事「Spring BootのServiceをJUnit・Mockitoでテストする方法」の注文取消例を、手元で実行できる形にしています。補足として、mock と spy、when と doReturn の違いも試せます。Spring・DB・Webサーバーを起動する必要はありません。

## 最初に動かす

JDK 17 と Maven 3.9 系を用意してください。初回は依存ライブラリを取得するためのインターネット接続が必要です。

このREADMEと `pom.xml` がある `testing/mockito-order-lab/` フォルダーで実行します。

```sh
mvn test
```

依存取得済みなら `mvn -o test` でも実行できます。正常終了の目安は **22 tests、0 failures、0 errors、0 skipped** です。

失敗を再現するテストも `assertThrows` で「期待どおり例外になる」と確認するため、全体は成功します。「失敗例が無視された」という意味ではありません。

## 読む順番

1. `src/main/java/info/syachiku/learning/cancel/OrderService.java` と `Order.java` を読む。
2. `src/test/java/info/syachiku/learning/cancel/CancelOrderServiceTest.java` の4ケースを実行する。
3. `templates/test-scope-example.csv` と `test-cases-example.csv` を見て、確認範囲を対応付ける。
4. `レビュー例.md` で、テスト結果をどのように説明するか確認する。
5. `MockitoBehaviorTest.java` で mock / spy の違いを試す。

特定の実験だけ実行する場合：

```sh
mvn -Dtest=CancelOrderServiceTest test
mvn -Dtest=MockitoBehaviorTest test
```

## この教材で確認すること

| テスト | ケース数 | 確認対象 |
| --- | ---: | --- |
| CancelOrderServiceTest | 4 | 取消成功、発送済み、対象なし、取消済みの再取消 |
| OrderServiceTest | 12 | 注文登録の入力境界、対象なし、利用停止、保存失敗 |
| MockitoBehaviorTest | 6 | mock / spy、when / doReturn、戻り値の型の違い |

注文取消と注文登録は別パッケージの独立した例です。最初は `cancel` パッケージだけで構いません。

### mock / spy の観察結果

| 準備 | スタブ設定中の実メソッド | 結果 |
| --- | --- | --- |
| `mock` に `when(...).thenReturn(...)` | 実行しない | 指定した顧客を返す |
| 未設定の `spy` | 呼び出したときに実行 | 台帳を実際に検索する |
| 空の台帳の `spy` に `when(...)` | 実行する | `thenReturn` に進む前に対象なし例外 |
| 同じ `spy` に `doReturn(...)` | 実行しない | 台帳の検索回数は0のまま |
| 登録済み顧客の `spy` に `when(...)` | 実行する | 設定は成功するが検索はすでに1回行われる |
| `doReturn` に誤った型を渡す | 実メソッドは実行しない | 設定時に `WrongTypeOfReturnValue` |

通常の Service 単体テストでは、テスト対象は実物を生成し、依存先を mock にします。Service 自身を mock にして「設定した戻り値が返った」と確認しても、業務ロジックの検証にはなりません。

spy は既存の実処理を一部残す必要がある場合の補足です。副作用を起こしたくないメソッドを差し替えるなら `doReturn` を使えますが、通常の mock のスタブを一律に置き換える必要はありません。`doReturn` は戻り値の型をコンパイル時に確認しにくい点にも注意してください。

## 記事本文との違い・検証しないこと

- 記事の Repository は `JpaRepository` ですが、教材では必要な2メソッドだけのインターフェースにしています。JPA の設定・マッピング・DB保存は確認しません。
- `@Service` と `@Transactional` は付けていません。普通のJavaオブジェクトとして実行するため、付けてもSpringのトランザクションは動きません。ロールバックは別の結合テストが必要です。
- 取消済み注文を再び取り消すと例外にするのは、この教材で追加した仕様です。再実行を成功扱いにする設計もあるため、実案件では合意した仕様に合わせてください。
- 認証、所有者の確認、CSRF、HTTPの応答、楽観ロック、並行実行、メール送信は対象外です。
- 注文登録の上限99個は境界値の学習用の仮定です。業界共通の仕様ではありません。

## 環境と実行上の注意

2026年10月12日に JDK 17.0.8 / Maven 3.9.16 で確認しました。既存のフォーム・MockMvc教材と同じ Spring Boot 3.5.16 の依存管理を再利用し、JUnit Jupiter 5.12.2 / Mockito 5.17.0 を使用しています。Bootはバージョン管理に使うだけで、アプリケーションは起動しません。

Mockitoの動的自己アタッチが利用できない環境でも動かせるよう、テスト用JVM起動時にMockitoを `-javaagent` で読み込む設定を `pom.xml` に記載しています。特定PCの絶対パスは設定していません。JDKのクラス共有に関する警告が出る場合がありますが、この環境では22ケースが正常に完了しています。

この教材は検証版を固定したものです。運用アプリへそのままコピーする際は、利用中のJDK・依存バージョン・脆弱性対応・チーム規約を別途確認してください。

記事で検証した版との対応は[検証記録](../../docs/VERIFICATION.md)を参照してください。同梱の `evidence/test-results.json` は2026年10月12日の既存検証結果であり、利用者の環境で実行した結果ではありません。

## CSV の使い方

`test-scope-blank.csv` は「どこまで確認するか」、`test-cases-blank.csv` は「どう確認するか」を記入する空欄版です。記入例の「成功」は同梱の取消テスト4件の実行結果であり、利用者の環境の結果ではありません。自分の案件に転用するときは、ケース・結果を置き換えてください。

文字化けする場合は、表計算ソフトのテキスト/CSV取込で UTF-8 を指定してください。機密情報や実顧客情報をブログの問い合わせ欄へ送信する必要はありません。

## 一次資料

- [Mockito 5.17.0 公式ソース・Javadoc](https://github.com/mockito/mockito/blob/v5.17.0/mockito-core/src/main/java/org/mockito/Mockito.java) — spy のスタブと実メソッド呼び出し
- [JUnit 5.12.2 User Guide](https://docs.junit.org/5.12.2/user-guide/) — 例外の検証・パラメータ化テスト
- [元記事](https://syachiku-uz.info/spring-service-junit-mockito/)

## 利用について

同梱する独自作成のサンプルコード、テンプレート、説明文は[MITライセンス](../../LICENSE)で公開しています。著作権表示とライセンス文を保持する条件で、改変・再配布・商用利用も可能です。詳しい条件と無保証の定めは `LICENSE` を確認してください。

依存ライブラリ自体は配布物へ同梱していません。各ライブラリにはそれぞれのライセンスが適用されます。
