# Data
## Classファイル
解析対象となるクラスの情報をまとめたjsonファイルを配置する。

このjsonファイルはソースコードの解析ツールによるアウトプットを配置することを想定しているが、具体的にどのツールを使用するかという部分に関しては未定義なので、暫定で以下の仕様のダミーファイルを配置するようにしている。

もし解析に使用するツールを決定した場合、`core/src/model/service/PackageFactory.java` を回収すること。

### Classファイルの仕様
- `classname`
    - クラスの名前
- `properties`
    - クラスのプロパティのリスト
    - 要素は以下。
        - `name`
            - プロパティ名
        - `type`
            - プロパティの型
- `methods`
    - クラスのメソッドのリスト
    - 要素は以下。
        - `name`
            - メソッド名
        - `return`
            - 戻り値の型
        - `argcnt`
            - 引数の数
        - `args`
            - 引数のリスト
            - `name`
                - 引数名
            - `type`
                - 引数の型
- `dependencies`
    - 依存関係
    - `mm`
        - メソッドからメソッドに対する依存関係のリスト
        - `src`
            - 依存元になるメソッド名
        - `dst`
            - 依存先になるメソッド名
    - `mv`
        - メソッドからプロパティに対する依存関係のリスト
        - `src`
            - 依存元になるメソッド名
        - `dst`
            - 依存先になるプロパティ名

### 例
```json
{
    "classname":"c2",
    "properties": [
        {"name":"v21","type":"int"},
        {"name":"v22","type":"int"}
    ],
    "methods": [
        {
			"name":"m21",
			"return":"void",
			"argcnt":1,
			"args":[{"name":"x","type":"int"}]
        },
		{
			"name":"m22",
			"return":"void",
			"argcnt":1,
			"args":[{"name":"y","type":"int"}]		
		}
    ],
	"dependencies":
		{
			"mm":[
				{"src":"m21","dst":"m22"}
			],
			"mv":[
				{"src":"m21","dst":"v21"},
				{"src":"m22","dst":"v22"}
			]
		}
}
```