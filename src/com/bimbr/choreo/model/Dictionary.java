package com.bimbr.choreo.model;

import static com.google.common.io.CharStreams.readLines;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;

/**
 * Dictionary of dance moves.
 *
 * @author mmakowski
 */
public class Dictionary {
    private final Map<String, String> nameToSymbol;

    public static Dictionary fromInputStream(final InputStream input) throws IOException {
        final DictionaryFileProcessor reader = new DictionaryFileProcessor();
        readLines(new InputSupplier<Reader>() {
                        @Override
                        public Reader getInput() { return new InputStreamReader(input); }
                  },
                  reader);
        return new Dictionary(reader.getResult());
    }

    private Dictionary(final Map<String, String> nameToSymbol) {
        this.nameToSymbol = nameToSymbol;
    }

    public Iterable<String> allMoveNames() {
        return nameToSymbol.keySet();
    }

    public String symbolFor(final String name) {
        return nameToSymbol.get(name);
    }

    private static final class DictionaryFileProcessor implements LineProcessor<Map<String, String>> {
        private static final int NAME = 0, SYMBOL = 1;

        final Map<String, String> nameToSymbol = new LinkedHashMap<String, String>();

        @Override
        public boolean processLine(final String line) throws IOException {
            final String[] parts = line.split("\t");
            if (parts.length == 2) nameToSymbol.put(parts[NAME], parts[SYMBOL]);
            return true;
        }

        @Override
        public Map<String, String> getResult() {
            return nameToSymbol;
        }
    }
}
