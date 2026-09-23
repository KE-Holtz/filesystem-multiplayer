package backend.publicvars;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

import gameplay.Player;

public class PublicVar<T> {
    private final String name;
    private final File varFile;

    private final Function<String, T> valueParser;

    public static final int MAX_LENGTH = 160;

    public enum Tag {
        DEFAULT,
        OVERFLOW,
        BOOL,
        DOUBLE,
        INT,
        STRING,
    }

    public PublicVar(Player player, String name, Function<String, T> valueParser) {
        this.name = name;
        this.varFile = Path.of(player.getPlayerFolder().getAbsolutePath()).resolve("publicVars").resolve(name).toFile();
        this.valueParser = valueParser;
        if (!varFile.mkdir()) {
            System.out.println("[DEBUG] " + name + " Failed");
        }
        setValue(valueParser.apply("0"), Tag.DEFAULT);
    }

    public PublicVar(Player player, String name, Function<String, T> valueParser, T value) {
        this.name = name;
        this.varFile = Path.of(player.getPlayerFolder().getAbsolutePath()).resolve("publicVars").resolve(name).toFile();
        this.valueParser = valueParser;
        if (!varFile.mkdir()) {
            System.out.println("[DEBUG] " + name + " Failed");
        }
        setValue(value);
    }

    // ? Returns null if no value is found - is this ok?

    public Optional<T> getValue() {
        String[] varFileContents = varFile.list();
        if (varFileContents.length == 0) {
            return Optional.empty();
        }
        String value = varFileContents[0];
        // System.out.println(value);
        ArrayList<Tag> tags = getTags(value);
        if (tags.contains(Tag.DEFAULT)) {
            return Optional.empty();
        } else if (tags.contains(Tag.OVERFLOW)) {
            return Optional.ofNullable(valueParser.apply(readOverflow(varFile)));
        } else {
            return Optional.ofNullable(valueParser.apply(valueOf(value)));
        }
    }

    public static String readOverflow(File file) {
        ArrayList<Tag> tags = getTags(file.getName());
        if (tags.contains(Tag.OVERFLOW)) {
            return valueOf(file.getName()) + readOverflow(file.listFiles()[0]);
        } else {
            return valueOf(file.getName());
        }
    }

    public void writeOverflow(File parent, String value) {
        File currentParent = parent;
        String currentValue = value;
        while (currentValue.length() > 0) {
            String tag = "(";
            if (tag.length() + currentValue.toString()
                    .length()
                    + 1 > MAX_LENGTH) {
                tag += Tag.OVERFLOW + ")";
            } else {
                tag += ")";
            }

            File nextFile;
            if (tag.contains(Tag.OVERFLOW.toString())) {
                nextFile = Path.of(currentParent.getAbsolutePath())
                        .resolve(tag + currentValue.substring(0, MAX_LENGTH - tag.length())).toFile();
                currentParent = nextFile;
                currentValue = currentValue.substring(MAX_LENGTH - tag.length());
                // System.out.println(nextFile.getName());
                // System.out.println(nextFile.mkdir());
                nextFile.mkdir();
            } else {
                nextFile = Path.of(currentParent.getAbsolutePath()).resolve(tag + currentValue).toFile();
                currentValue = "";
                // System.out.println(nextFile.getName());
                // System.out.println(nextFile.mkdir());
                nextFile.mkdir();
            }
        }
    }

    public static ArrayList<Tag> getTags(String str) {
        String tagStr = str.substring(str.indexOf("(") + 1, str.indexOf(")"));
        ArrayList<Tag> tags = new ArrayList<>();
        for (String s : tagStr.split(",")) {
            if (!s.isEmpty()) {
                tags.add(Tag.valueOf(s));
            }
        }
        return tags;
    }

    public void setValue(T value) {
        setValue(value, new Tag[0]);
    }

    public void setValue(T value, Tag... tags) {
        deleteContents(varFile);

        String tag = "(";
        if (tags != null) {
            for (Tag t : tags) {
                tag += t + ",";
            }
        }
        if (tag.length() + value.toString()
                .length() > MAX_LENGTH && !tag.contains(Tag.OVERFLOW.toString())) {
            tag = tag.substring(tag.length() - 1) + Tag.OVERFLOW + ",";
        }
        tag += ")";
        if (tag.contains(Tag.OVERFLOW.toString())) {
            File newFile = Path.of(varFile.getPath())
                    .resolve(tag + value.toString().substring(0, MAX_LENGTH - tag.length())).toFile();
            newFile.mkdir();
            writeOverflow(newFile, value.toString()
                    .substring(MAX_LENGTH - tag.length()));
        } else {
            File newFile = Path.of(varFile.getPath()).resolve(tag + value).toFile();
            newFile.mkdir();
        }
    }

    private void deleteContents(File file) {
        if (file.listFiles() != null) {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    deleteContents(f);
                }
                f.delete();
            }
        }

    }

    private static String valueOf(String value) {
        return value.substring(value.indexOf(")") + 1);
    }

    public String getName() {
        return name;
    }

    public static PublicVar fromFile(Player player, File file) {
        String[] varFileContents = file.list();
        while (varFileContents.length == 0) {
            varFileContents = file.list();
        }
        String val = varFileContents[0];
        if (getTags(val).contains(Tag.BOOL)) {
            return new PublicBoolean(player, file.getName());
        } else if (getTags(val).contains(Tag.INT)) {
            return new PublicInt(player, file.getName());
        } else if (getTags(val).contains(Tag.DOUBLE)) {
            return new PublicDouble(player, file.getName());
        } else {
            return new PublicString(player, file.getName());
        }
    }
}
