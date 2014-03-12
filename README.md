Android ContentProvider Maven Plugin
====================================

This project is based on [Android ContentProvider Generator](https://github.com/BoD/android-contentprovider-generator)
by [Benoit Lubek](https://github.com/BoD)


A Maven Plugin to generate an Android ContentProvider.
It takes a set of entity (a.k.a "table") definitions as the input, and generates:
- a `ContentProvider` class
- a `SQLiteOpenHelper` class
- one `BaseColumns` interface per entity 
- one `Cursor` class per entity
- one `ContentValues` class per entity
- one `Selection` class per entity

How to use
----------

### The configure the Plugin

This is where you declare a few parameters that will be used to generate the code.

These are self-explanatory so here is an example:
```xml
<plugin>
  <groupId>com.thoughtsonmobile.android</groupId>
  <artifactId>thoughtsonmobile-contentprovider-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
    <configuration>
      <outputDir>${project.build.directory}/generated-sources</outputDir>
      <inputDir>${project.basedir}/sample</inputDir>
      <providerClassName>ExampleProvider</providerClassName>
      <databaseFileName>example.db</databaseFileName>
      <enableForeignKeys>true</enableForeignKeys>
      <packageId>de.example</packageId>
      <targetPackage>de.example.generated</targetPackage>
      <sqliteHelperClassName>ExampleSqliteHelper</sqliteHelperClassName>
      <authority>de.example.entities</authority>
   </configuration>
</plugin>
```

### Entity files

Create one file per entity, naming it `<entity name>.json`.
Inside each file, declare your fields (a.k.a "columns") with a name and a type.
You can also optionally declare a default value, an index flag and a nullable flag.

Currently the type can be:
- `String` (SQLite type: `TEXT`)
- `Integer` (`INTEGER`)
- `Long` (`INTEGER`)
- `Float` (`REAL`)
- `Double` (`REAL`) 
- `Boolean` (`INTEGER`)
- `Date` (`INTEGER`)
- `byte[]` (`BLOB`)
- `enum` (`INTEGER`).

You can also optionally declare table contraints.

Here is a `person.json` file as an example:

```json
{
	"fields": [
		{
			"name": "first_name",
			"type": "String",
			"default_value": "John"
		},
		{
			"name": "last_name",
			"type": "String",
			"nullable": true,
			"default_value": "Doe"
		},
		{
			"name": "age",
			"type": "Integer",
			"index": true
		},
		{
			"name": "gender",
			"type": "enum",
			"enumName": "Gender",
			"enumValues": [
				"MALE",
				"FEMALE",
				"OTHER",
			],
			"nullable": false,
		},
	],
	
	"constraints": [
		{
			"name": "unique_name",
			"definition": "unique (first_name, last_name) on conflict replace"
		}
	]
}
```

### The `header.txt` file (optional)

If a `header.txt` file is present, its contents will be inserted at the top of every generated file.

### Get the plugin

Download the jar from here:
http://github.com/ljanzik/android-contentprovider-generator/releases/latest

### Run the plugin

`mvn content-provider:generate`

### Use the generated files

- When querying a table, use the corresponding `Selection` class as shown in this example:

```java
PersonSelection where = new PersonSelection();
where.firstName("John").or().age(42);
Cursor c = context.getContentResolver().query(PersonColumns.CONTENT_URI, projection,
        where.sel(), where.args(), null);
```
- When using the results of a query, wrap the resulting `Cursor` in the corresponding wrapper class.  You can then use
the generated getters directly as shown in this example:

```java
PersonCursor person = new PersonCursor(c);
String lastName = person.getLastName();
Long age = person.getAge();
```
- You can also conveniently combine these two facilities by using the `query` (or `delete`) method:

```java
PersonSelection where = new PersonSelection();
where.firstName("John").or().age(42);
PersonCursor person = where.query(getContentResolver());
String lastName = person.getLastName();
Long age = person.getAge();
```
- When updating or inserting into a table, use the corresponding `ContentValues` class as shown in this example:

```java
PersonContentValues values = new PersonContentValues();
values.putFirstName("John").putAge(42);
context.getContentResolver().update(personUri, values.values(), null, null);
```


Building
--------

You need maven to build this app.

`mvn install`

This will install the plugin to your local repository


Licence
-------

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*Just to be absolutely clear, this license applies to this program itself,
not to the source it will generate!*
