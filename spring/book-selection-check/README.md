# 読む前に説明できるかを確かめる注文登録サンプル

本を読む前に、短い実コードでどこまで説明できるかを確かめるサンプルです。ブログ「ポンコツPdMの開発メモ」の[Spring Bootの本選び](https://syachiku-uz.info/spring-boot-books-practical/)に対応します。書籍コードの転載ではなく、当ブログで独自に作成しています。

## 先に考える3問

1. HTTP入力はController、Service、DB保存をどう通り、どの値が応答になるか。
2. どのURL・HTTPメソッド・JSONを送るか。数量が不正な場合の400は何を知らせるか。
3. 注文INSERT後に在庫確保が失敗したら、注文と在庫はどうなるか。`@Transactional`がどこで働くか。

読む順は [OrderController](src/main/java/info/syachiku/books/OrderController.java) → [OrderRequest](src/main/java/info/syachiku/books/OrderRequest.java) → [OrderService](src/main/java/info/syachiku/books/OrderService.java) → [schema.sql](src/main/resources/schema.sql) → [OrderFlowTest](src/test/java/info/syachiku/books/OrderFlowTest.java) です。DI、JSON変換、トランザクションはSpringが担当し、注文と在庫のSQLはServiceに置いています。学習対象を短くするためRepositoryは分けていません。

## 実行

JDK 17、Maven 3.9系を用意し、この`pom.xml`があるフォルダーで実行します。Spring Boot 3.5.16、Spring JDBC、H2を使用します。Maven Wrapperは同梱していません。初回は依存関係を取得するネット接続が必要です。外部DB、アカウント、APIキーは不要です。

```sh
mvn verify
mvn spring-boot:run
```

テストがすべて成功したら`BUILD SUCCESS`が表示されます。アプリ起動後、別のターミナルから次を送ります。

```sh
curl -i -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' -d '{"quantity":2}'
```

起動直後なら201と`{"orderId":1,"quantity":2}`が返ります。IDはDB生成値なので、操作履歴によって変わります。成功時は注文を1件保存し、在庫5から2を引きます。商品は1種類で、商品IDは1に固定しています。H2はメモリーDBなのでアプリを再起動するとデータは消え、在庫5に戻ります。

## 応答とDBの結果

| 入力・条件 | 応答 | DBの結果 |
| --- | --- | --- |
| `{"quantity":2}`、在庫5 | 201、注文IDと数量 | 注文1件、在庫3 |
| `{"quantity":0}` | 400 | 入力検証に失敗し、Serviceは呼ばれない |
| `{"quantity":"abc"}` | 400 | `Integer`へのJSON変換に失敗し、Serviceは呼ばれない |
| `{}` または `{"quantity":null}` | 400 | 必須値の検証に失敗し、Serviceは呼ばれない |
| `{"quantity":4}`、在庫3 | 409 | 先にINSERTした失敗注文は残らず、在庫3を維持 |

400はリクエストをそのまま処理できなかったことを伝えます。0や欠落を拒む制約は[OrderRequest](src/main/java/info/syachiku/books/OrderRequest.java)とControllerの`@Valid`、文字列`abc`の変換はSpring MVCのJSON変換が担当します。400の詳細エラー本文はこのサンプルの契約に含めていません。

在庫不足はこのサンプルの業務条件です。`available >= quantity`を満たさないUPDATEは0件となり、[InsufficientStockException](src/main/java/info/syachiku/books/InsufficientStockException.java)を投げます。例外に付けた`@ResponseStatus(CONFLICT)`によりHTTPでは409とします。400/409の選択をすべてのアプリ共通の仕様にはしていません。

## ロールバックの前提と確認方法

ControllerはコンストラクターDIで受け取ったSpring管理のServiceを呼びます。Serviceのpublicメソッドには`@Transactional`があり、同一DataSourceの`JdbcTemplate`で注文INSERTと在庫UPDATEを実行します。Serviceの外へ`RuntimeException`が抜けるため、Springの既定ルールによりこのトランザクションをロールバックします。同じクラス内からの自己呼び出しや、例外を握りつぶす実装は扱っていません。

在庫不足のUPDATEはもともと0件で、在庫を変更してから戻しているわけではありません。ロールバックを確かめる対象は、その前に成功していた注文INSERTです。[テスト](src/test/java/info/syachiku/books/OrderFlowTest.java)は先に2個の注文を成功させ、在庫3に対する4個の注文で409を確認します。その後、実際のH2を読み、注文が成功分の1件だけ、在庫が3のままであることを確認します。テスト自体には`@Transactional`を付けていないため、テスト終了時の自動ロールバックをServiceの結果と取り違えません。DBやServiceのmockは使用していません。採番値が連番になることは検証しません。

## 本で補う箇所

- 作る流れを補う：『作って学ぶSpring Boot入門』。
- HTTPの要求と応答を補う：『Webを支える技術』。Spring固有の注釈や例外処理の実装は、このコードとSpringの資料で確認します。
- DI・AOP・トランザクションを補う：『Spring徹底入門 第2版』。対象はSpring Framework 5のため、このSpring Boot 3のコードをそのまま再現する本とは区別してください。

## 検証範囲・利用条件

自動テストは正常登録1件、入力不正4件、実DBでのロールバック1件の計6件です。実行結果は[検証記録](../../docs/VERIFICATION.md)に記録します。MockMvcからSpring MVC・Service・H2を通して確認します。実ブラウザー、本番DB、並行実行、認証・認可、負荷、実運用の在庫管理は検証対象外です。JSONの小数や数値文字列の厳密な受け入れ制御も対象外で、Jacksonの既定変換を使用します。学習専用で、公開サービスへの配備を目的とした構成ではありません。

Copyright (c) 2026 Growth Security. 独自コード・説明文は[MITライセンス](../../LICENSE)です。依存ライブラリには各ライセンスが適用されます。
