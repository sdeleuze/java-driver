/*
 * Copyright (C) 2017-2017 DataStax Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.core.type.codec;

import com.datastax.oss.driver.api.core.ProtocolVersion;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.util.Strings;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeCodec implements TypeCodec<LocalTime> {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS");

  @Override
  public GenericType<LocalTime> getJavaType() {
    return GenericType.LOCAL_TIME;
  }

  @Override
  public DataType getCqlType() {
    return DataTypes.TIME;
  }

  @Override
  public boolean accepts(Object value) {
    return value instanceof LocalTime;
  }

  @Override
  public boolean accepts(Class<?> javaClass) {
    return javaClass == LocalTime.class;
  }

  @Override
  public ByteBuffer encode(LocalTime value, ProtocolVersion protocolVersion) {
    return (value == null)
        ? null
        : TypeCodecs.BIGINT.encodePrimitive(value.toNanoOfDay(), protocolVersion);
  }

  @Override
  public LocalTime decode(ByteBuffer bytes, ProtocolVersion protocolVersion) {
    if (bytes == null || bytes.remaining() == 0) {
      return null;
    } else {
      long nanosOfDay = TypeCodecs.BIGINT.decodePrimitive(bytes, protocolVersion);
      return LocalTime.ofNanoOfDay(nanosOfDay);
    }
  }

  @Override
  public String format(LocalTime value) {
    return (value == null) ? "NULL" : Strings.quote(FORMATTER.format(value));
  }

  @Override
  public LocalTime parse(String value) {
    if (value == null || value.isEmpty() || value.equalsIgnoreCase("NULL")) {
      return null;
    }

    // enclosing single quotes required, even for long literals
    if (!Strings.isQuoted(value)) {
      throw new IllegalArgumentException("time values must be enclosed by single quotes");
    }
    value = value.substring(1, value.length() - 1);

    if (Strings.isLongLiteral(value)) {
      try {
        return LocalTime.ofNanoOfDay(Long.parseLong(value));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            String.format("Cannot parse time value from \"%s\"", value), e);
      }
    }

    try {
      return LocalTime.parse(value);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException(
          String.format("Cannot parse time value from \"%s\"", value), e);
    }
  }
}
