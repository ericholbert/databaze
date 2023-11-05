# Databaze
A non-relational DBMS operable from the interactive shell using custom query language. The main objective was to allow quick and easy data manipulation regardless of CLI limitations. For this reason, the database engine doesn’t yet offer any special attitude to handle large amounts of data.
![intro](/../main/assets/intro_01.png)

## Requirements
* Java 14 or above

## Getting started
1. Download the latest jar from [the release page](https://github.com/ericholbert/databaze/releases)
2. `cd` into the file’s parent directory
3. Run `java -jar ericholbert.databaze.Main`

## How to use
A basic use case will follow. For all possible options, refer to built-in documentation (in Czech only).

After running the program, the last database will be loaded. Since we haven’t created a database yet, run
```
priddat <database name>
```
to create one.
![use case 1](/../main/assets/use_case_01.png)
Now we need to fill in a few records. Every record consists of parameter groups and each parameter group consists of parameters. When creating a record, you have to also specify at least one parameter group within it:
```
pridzaz <parameter group>[ <another parameter group>...]
```
Let’s assume that parameter groups will be the same or at least similar in all records. Faster way to add a new record is then to use “-u” flag when defying the first record:
```
pridzaz -u <parameter group>...
```
After that, you can add a new record using the previous set of parameter groups by simply running `pridzaz`.
![use case 2](/../main/assets/use_case_02.png)
Every newly added record is at the same time printed to the terminal. This means that you can now add a new parameter to such a record by running
```
pridpar <parameter group> <parameter>
```
or a little bit more faster using
```
pridpar <parameter group 1> <parameter>, <parameter group 1 or 2> <parameter>[, ...]
```
If you want to fill the same parameter in multiple records using only one command, you have to find those records first. In our example, we have created two records and we can print them both by running
```
naczaz
```
Now, if we use the first `pridpar` command, all records with the specified parameter group will obtain the specified parameter, but we can still change only one record using the “id” argument:
```
pridpar <record id> <parameter group> <parameter>
```
Let’s populate both records using a combination of the three listed use cases of the `pridpar` command. The result is the following:
![use case 3](/../main/assets/use_case_03.png)
As the database grows, the user may find it useful to print its entire content in such a manner that only exclusive elements per record are displayed along with the occurrence counts. This can be done by running
```
naczaz -d
```
![use case 4](/../main/assets/use_case_04.png)
As we can see, I have created two more records and now I want to make some changes to all of them at once. Since “country” is always the same, I’ll delete the entire parameter group along with its parameters. Then, I’ll add the “sex” parameter group and set it to male for the reason that three of our subjects are male. This of course means that the “Lucy” record should be tweaked a bit afterwards.
But that’s a lot of typing! Or at least it would be if we were working on a larger scale. What if we find all records with the “-i” flag instead? Now, after running
```
naczaz -i
```
all elements can be manipulated using their indices in square brackets as substitutions, which is much faster.
![use case 5](/../main/assets/use_case_05.png)
Let’s introduce two more commands
```
smazskup [id] <parameter group>[, <another parameter group>...]
pridskup [id] <parameter group>[, <another parameter group>...]
```
and use them to make the first two changes:
![use case 6](/../main/assets/use_case_06.png)
Now, let’s find the “Lucy” record and change its “sex” to “female”:
![use case 7](/../main/assets/use_case_07.png)
After you are done, don’t forget to run
```
uldat
```
to save the database to disk.

## Features
### Pupulate the database
* Create multiple databases, store their contents on disk and toggle between them freely while the program is open
* Set a custom path to a database
* Create, delete and copy records
* Add group parameters to record(s), create record(s) with previously saved group parameters or delete parameter groups from record(s)
* Add parameters to parameter group(s), change and delete them
* Add files to parameter group(s), open them or delete their references

### Search the database
* Find all records at once or print them in compressed form when only unique elements and their occurrence counts are displayed
* Find records that contain all of the passed elements or at least one of them
* Find records regardless of diacritics, letter case and input completeness (only a substring of a query may be sufficient)
* Find records that don’t contain any specified elements
* Save records for quick back-reference
* Hide specified group parameters from the search result

### Keep it simple
* Substitute all elements with indices to allow faster typing
* Enjoy the clear text layout with line breaks between words and content printed in columns
* Set the width of the text layout to the current width of the terminal window to keep proper formatting
