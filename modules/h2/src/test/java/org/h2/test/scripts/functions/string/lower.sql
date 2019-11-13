-- Copyright 2004-2019 H2 Group. Multiple-Licensed under the MPL 2.0,
-- and the EPL 1.0 (http://h2database.com/html/license.html).
-- Initial Developer: H2 Group
--

select lower(null) en, lower('Hello') hello, lower('ABC') abc;
> EN   HELLO ABC
> ---- ----- ---
> null hello abc
> rows: 1

select lcase(null) en, lcase('Hello') hello, lcase('ABC') abc;
> EN   HELLO ABC
> ---- ----- ---
> null hello abc
> rows: 1