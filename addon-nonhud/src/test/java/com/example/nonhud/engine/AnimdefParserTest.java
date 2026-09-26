package com.example.nonhud.engine;

import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** animdef JSON 的 injector 角色解析（README 1382：true→V，字符串 V/A/M）。 */
class AnimdefParserTest {

    @Test
    void booleanTrueMeansGenericV() {
        String json = """
                {"actors": [{"label": "wolf", "injector": true}]}
                """;
        assertEquals(Set.of("V"), AnimdefParser.injectorRoles(json));
    }

    @Test
    void stringRolesAreParsedCaseInsensitive() {
        String json = """
                {"actors": [
                  {"label": "a", "injector": "A"},
                  {"label": "m", "injector": "m"},
                  {"label": "v", "injector": "V"}
                ]}
                """;
        assertEquals(Set.of("A", "M", "V"), AnimdefParser.injectorRoles(json));
    }

    @Test
    void falseAndMissingAreIgnored() {
        String json = """
                {"actors": [
                  {"label": "x", "injector": false},
                  {"label": "y"}
                ]}
                """;
        assertTrue(AnimdefParser.injectorRoles(json).isEmpty());
    }

    @Test
    void missingActorsYieldsEmpty() {
        assertEquals(Set.of(), AnimdefParser.injectorRoles("{\"content_tags\": [\"doggy\"]}"));
    }

    @Test
    void malformedJsonThrows() {
        assertThrows(JsonParseException.class, () -> AnimdefParser.injectorRoles("{not json"));
    }

    @Test
    void multipleActorsUnion() {
        String json = """
                {"actors": [
                  {"label": "player", "injector": "V"},
                  {"label": "wolf", "injector": "M"}
                ]}
                """;
        assertEquals(Set.of("V", "M"), AnimdefParser.injectorRoles(json));
    }
}
