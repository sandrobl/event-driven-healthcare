package com.eventdriven.healthcare.streamprocessor.util;

public class NfcFormatter {
  /**
   * Accepts strings like "ID 0x04 0xDA 0xF2 …"
   * and returns "04DAF28AB45780"
   */
  public static String format(String raw) {
    if (raw == null || !raw.startsWith("ID ")) return null;
    return raw.replace("ID ", "")
            .replaceAll("0x", "")
            .replaceAll("\\s+", "");
  }
}