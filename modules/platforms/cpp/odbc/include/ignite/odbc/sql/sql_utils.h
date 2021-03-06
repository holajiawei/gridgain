/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef _IGNITE_ODBC_SQL_SQL_UTILS
#define _IGNITE_ODBC_SQL_SQL_UTILS

#include <string>

#include <ignite/odbc/odbc_error.h>
#include <ignite/odbc/sql/sql_token.h>

namespace ignite
{
    namespace odbc
    {
        namespace sql_utils
        {
            /**
             * Parse token to boolean value.
             *
             * @return Boolean value.
             */
            inline OdbcExpected<bool> TokenToBoolean(const SqlToken& token)
            {
                std::string lower = token.ToLower();

                if (lower == "1" || lower == "on")
                    return true;

                if (lower == "0" || lower == "off")
                    return false;

                return OdbcError(SqlState::S42000_SYNTAX_ERROR_OR_ACCESS_VIOLATION,
                    "Unexpected token: '" + token.ToString() + "', ON, OFF, 1 or 0 expected.");
            }

            /**
             * Check if the SQL is internal command.
             *
             * @param sql SQL request string.
             * @return @c true if internal.
             */
            bool IsInternalCommand(const std::string& sql);
        }
    }
}

#endif //_IGNITE_ODBC_SQL_SQL_UTILS