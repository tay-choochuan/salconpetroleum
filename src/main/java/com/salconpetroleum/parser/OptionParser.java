package com.salconpetroleum.parser;

import lombok.Data;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * @author Phua Ging Sheng (phua.gingsheng@gmail.com)
 * @version 1.0
 */
@Data
public class OptionParser {

    private static final String DASH = "-";
    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String BACKSLASH = "/";
    private static final String EQUAL = "=";
    private static final String SHORT_COMMAND = ".*I.*|.*f.*|.*t.*|.*h.*|.*v.*";
    private static final String VALID_COMMAND = ".*filetype.*|.*file.*|.*help.*|.*version.*|.*I.*|.*f.*|.*t.*|.*h.*|.*v.*|.*lib.*|.*cpp.*";
    private static final String INCOMPLETE_COMMAND = "\\bfiletype\\b|\\bI\\b";
    private static final String CONCATENATED_COMMAND = "\\bhv\\b";
    private static final String SPECIAL_COMMAND = "[abcd]";
    private static final Pattern INCOMPLETE_PATTERN = Pattern.compile(INCOMPLETE_COMMAND);
    private static final Pattern VALID_PATTERN = Pattern.compile(VALID_COMMAND);
    private static final Pattern SPECIAL_PATTERN = Pattern.compile(SPECIAL_COMMAND);
    private static final Pattern CONCATENATED_PATTERN = Pattern.compile(CONCATENATED_COMMAND);
    private static final Map<String, String> COMMAND_MAP = new LinkedHashMap<String, String>() {{ put("h", "help");put("v", "version");}};
    private Set<String> flags = new LinkedHashSet<>();

    public void addStringOption(String flags) {
        String[] stringList = flags.split(SPACE);
        this.flags.addAll(Arrays.asList(stringList));
    }

    public void addBoolOption(String flags) {
        String[] stringList = flags.split(SPACE);
        this.flags.addAll(Arrays.asList(stringList));
    }

    public boolean isSet(String flag) {
        Pattern shortPattern = Pattern.compile(SHORT_COMMAND);
        Set<String> flags = this.flags.stream()
                .map(e -> (shortPattern.matcher(e).matches() && Objects.nonNull(COMMAND_MAP.get(e))) ?
                        COMMAND_MAP.get(e) : e).collect(Collectors.toSet());

        return flags.stream()
                .anyMatch(e -> e.contains(flag));
    }

    public String[] parse(String[] argv) {

        Set<String> removableList = new LinkedHashSet<>();
        Set<String> addableList = new LinkedHashSet<>();

        String[] finalArgv = argv;
        argv = IntStream
                .range(0, finalArgv.length)
                .mapToObj(i -> {
                    finalArgv[i] = finalArgv[i].replace(DASH, EMPTY);
                    if (INCOMPLETE_PATTERN.matcher(finalArgv[i]).matches()) {
                        finalArgv[i] = finalArgv[i] + EQUAL + finalArgv[i + 1];
                        removableList.add(finalArgv[i + 1]);
                    }
                    if (CONCATENATED_PATTERN.matcher(finalArgv[i]).matches()) {
                        addableList.add(finalArgv[i].substring(1, 2));
                        finalArgv[i] = finalArgv[i].substring(0, 1);
                    }
                    return finalArgv[i];
                })
                .toArray(String[]::new);

        Stream<String> validStream = Arrays.stream(argv)
                .filter(e -> VALID_PATTERN.matcher(e).matches());

        Stream<String> restStream = Arrays.stream(argv)
                .filter(e -> SPECIAL_PATTERN.matcher(e).matches());

        flags = validStream.collect(Collectors.toSet());
        String[] rest = restStream.toArray(String[]::new);
        flags.removeAll(removableList);
        flags.addAll(addableList);
        return rest;

    }


    public String get(String flag) {
        return flags
                .stream()
                .filter(e -> e.contains(flag) && e.contains(BACKSLASH))
                .map(s -> (s.contains(BACKSLASH)) ? s.substring(s.indexOf(BACKSLASH)) : s)
                .collect(Collectors.joining());
    }

    public String[] getAll(String flag) {

        String[] rest = flags
                .stream()
                .filter(e -> e.contains(flag))
                .map(s -> (s.contains(BACKSLASH)) ? s.substring(s.indexOf(BACKSLASH)) : s)
                .map(s -> (s.contains(EQUAL)) ? s.substring(s.indexOf(EQUAL) + 1) : s)
                .toArray(String[]::new);

        flags = flags
                .stream()
                .filter(e -> !e.contains(flag))
                .collect(Collectors.toSet());

        return rest;
    }

    public void reset() {
        flags.clear();
    }

}
