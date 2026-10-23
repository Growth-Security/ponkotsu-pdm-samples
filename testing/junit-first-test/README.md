# JUnitの最初の実行（記事と同じ3ケース）

送料計算の本体とテストを動かし、失敗した値から修正箇所を判断するためのサンプルです。ブログ記事「JUnit 5入門｜Mavenで最初のテストを実行し、失敗を直す」の掲載コードと同じ、独自作成の3ケースを収録しています。

## ダウンロードして実行する

リポジトリの `downloads/junit-first-test-sample.zip` に、このフォルダーの `README.md`、`LICENSE`、`pom.xml`、本体、テストをまとめています。ZIPを展開し、`pom.xml` のある `junit-first-test` フォルダーで実行してください。

前提は **JDK 17、Maven 3.9系** です。JUnit Jupiterは `pom.xml` で **5.12.2** に固定しています。初回は依存ライブラリの取得にネットワーク接続が必要です。外部DBやAPIキーは不要です。

```sh
cd junit-first-test
java -version
mvn -version
mvn test
```

Mavenが使うJavaも17であることを確認してください。Gitで取得した場合は `testing/junit-first-test` に移動して同じコマンドを実行します。

正常終了の目安は **Tests run: 3, Failures: 0, Errors: 0, Skipped: 0** と **BUILD SUCCESS** です。結果は `target/surefire-reports/` に出ます。依存関係を取得済みなら `mvn -o test` でも実行できます。

## ファイルと役割

| ファイル | 役割 |
| --- | --- |
| `src/main/java/ShippingFee.java` | 小計から送料を計算する本体 |
| `src/test/java/ShippingFeeTest.java` | 3つの入力と期待結果を確かめるテスト |
| `pom.xml` | Java、JUnit、テスト実行に必要な設定 |

## 仕様と確認範囲

小計は整数の円で **0〜1,000,000円**。**10,000円以上なら送料0円、それ未満なら500円**。範囲外は `IllegalArgumentException` とします。

| 入力 | 期待結果 | テスト名 |
| --- | --- | --- |
| 9,999円 | 送料500円 | `subtotal9999Costs500` |
| 10,000円 | 送料0円 | `subtotal10000IsFree` |
| -1円 | `IllegalArgumentException` | `negativeSubtotalIsRejected` |

この3ケースは最初の実行の練習用です。入力範囲全体の網羅ではありません。上限1,000,000円・その直後、許容下限0円、HTTP・画面・DB・実際の決済は、このサンプルのテストでは確認しません。

## 1件だけ失敗させ、仕様に戻って直す

1. `src/main/java/ShippingFee.java` の `subtotal >= 10_000` を一時的に `subtotal > 10_000` へ変更します。
2. 同じフォルダーで `mvn test` を実行します。
3. **3件中1件**、`subtotal10000IsFree` が失敗し、`expected: <0> but was: <500>` と表示されることを確認します。
4. 「10,000円**以上**なら送料無料」という仕様と本体を照合し、比較を `>=` に戻します。
5. `mvn test` を再実行し、**全3件が成功**することを確認します。

期待値を500円へ変更して帳尻を合わせないでください。この例では入力も期待値も仕様に合っていて、誤っているのは本体の比較条件です。

## 次に、確認する入力を増やす

[9ケース版のテスト入門サンプル](https://github.com/ai-sato0828/ponkotsu-pdm-samples/tree/59b378a3a44758f9842ee760f90a45faa108264d/testing/java-testing-basics)では、許容範囲の下限・上限やその外側も確認します。本体と依存構成は共通ですが、今回の3ケース版とは別フォルダーです。まず今回の3件を実行し、各期待値の理由と、失敗したときの修正箇所を説明できてから進んでください。

## 検証記録とライセンス

2026年10月24日、JDK 17.0.8・Maven 3.9.5で `mvn -o test` を実行し、3件成功、上記の意図的な変更で1件失敗、比較条件を復旧して再び3件成功することを確認しました。ZIPには生成物・依存ライブラリ・認証情報を含めず、収録した5ファイルと元フォルダーの内容一致を確認しています。空の依存キャッシュからの初回ダウンロードは、この検証では確認していません。

独自コードと説明は、同梱の[MITライセンス](LICENSE)で利用できます。依存ライブラリにはそれぞれのライセンスが適用されます。
