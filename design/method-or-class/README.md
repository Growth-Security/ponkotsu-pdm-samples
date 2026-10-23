# Javaのメソッド分割とクラス分割

対応記事: [Javaの処理をどこで分けるか](https://syachiku-uz.info/how-to-split/)

正しく動く処理を分けるとき、入力・保存回数・例外を変えていないかを比較する独立サンプルです。架空のアカウント取込を扱います。

## 実行

JDK 17を使用します。Maven・JUnit・外部ライブラリ・DB・ネット接続は不要です。このREADMEがあるフォルダーで実行してください。

```sh
mkdir -p out
javac -encoding UTF-8 -d out src/*.java
java -cp out SampleVerification
```

最後に次の結果が出ます。失敗時はAssertionErrorで終了します。`-ea`は不要です。

```text
OK: baseline 11 cases x 3 versions = 33 checks; extension 6 checks; total 39 checks.
```

## 全文ファイルと読む順

1. [Account.java](src/Account.java): 保存へ渡す値。
2. [BeforeImporter.java](src/BeforeImporter.java): 変換と保存の順序が一つのメソッドにある分割前。
3. [MethodImporter.java](src/MethodImporter.java): 同じクラスのprivateメソッドへ一行の変換を抽出。現在の取込だけならこの形で足ります。
4. [AccountMapper.java](src/AccountMapper.java)と[ClassImporter.java](src/ClassImporter.java): 変換規則を独立した担当へ出す比較例。変換を複数の入口で使うなどの変更要求がある場合を想定します。行数が増えたら必ず作るクラスではありません。
5. [SampleVerification.java](src/SampleVerification.java): 同じ入力と明示した期待結果で3版を比較する実行入口。

## 分割で維持する契約

- 入力はCSV解析済みの2列のデータ行（メール、契約数）。ヘッダーは含めず、配列・各要素は非nullを前提とします。前後空白を自動除去しません。
- メールは空白だけでないことだけを確認します。完全なメール形式検証ではありません。
- 契約数はInteger.parseIntで読める1以上のint。標準版では101もInteger.MAX_VALUEも有効です。
- 全行の変換が成功してから、同じ入力順で保存関数を1回呼びます。
- 不正な行が先頭でも途中でも、保存関数を呼ぶ前に例外を返します。
- 空の入力でも空リストで保存を1回呼びます。

実行入口は標準11ケース（二行正常、不正先頭、不正途中、数値でない、0、負数、列不足、列過多、空入力、101、int最大値）を3版で確認します。保存回数、保存値と順序、例外の型とメッセージを照合します。

## 上限100は「分割」と別の変更要求

`new AccountMapper()`は従来の1以上を維持します。`new AccountMapper(100)`は、契約数を1〜100へ変更する追加要求の例です。分割だけの版と同じ仕様ではありません。

上限指定と変換規則の検証をAccountMapperが担当し、ClassImporterの全件変換→保存は変えません。0/1/100/101、途中の101、空入力の6ケースを別枠で検証します。今回の例では上限をコンストラクターで指定しますが、これをすべての業務規則の汎用化に広げる必要はありません。

## 検証していないこと

CSVファイル解析、引用符や文字コード、実DB、トランザクション、並行実行、実顧客情報は扱いません。保存関数を1回呼ぶことはDB書き込みの原子性を保証しません。保存関数の内部で失敗した場合のロールバックも対象外です。

検証環境: Java 17.0.8。数値変換例外のメッセージもこの環境で確認しています。詳細は[VERIFICATION.md](VERIFICATION.md)を参照してください。

独自作成コードは[MITライセンス](LICENSE)です。
