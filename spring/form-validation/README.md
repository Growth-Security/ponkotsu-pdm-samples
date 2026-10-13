# Springフォーム・入力検証

フォームの入力形式、Serviceの判断、画面への再表示、Securityによる拒否を、独立した4つのサンプルで確かめます。

全体の読む順は[Spring Bootのフォーム入力・バリデーション実践ガイド](https://syachiku-uz.info/spring-form-validation-guide/)を参照してください。

| サンプル | 主な問い |
| --- | --- |
| [01-validation-basics](01-validation-basics/) | 入力制約や検証グループは、どこで効くか |
| [02-controller-service](02-controller-service/) | 入力形式と業務判断を、どこで分けるか |
| [03-thymeleaf-redisplay](03-thymeleaf-redisplay/) | 入力エラー後に、値や選択肢が消えるのはなぜか |
| [04-mockmvc-security](04-mockmvc-security/) | POSTが403や400になったとき、どこで拒否されたか |

JDK 17とMaven 3.9系が必要です。各サンプルのフォルダーへ移動し、`mvn test` を実行してください。詳しい前提・試し方は[配布版の実行案内](README.html)、依存バージョンと結果は[検証記録](../../docs/VERIFICATION.md)を参照してください。
