package gameplay;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import backend.publicvars.PublicVar;

public class Player {

    private final String name;

    private final ArrayList<File> playerFiles;// TODO: Tree? This could have some limitations.
    private final File playerFolder;
    private final File globalVarsDir;
    private final File publicVarsDir;

    private final HashMap<String, PublicVar> publicVars;

    public Player(String name, Path playerSpacePath) {
        this.name = name;

        playerFiles = new ArrayList<File>();

        playerFolder = playerSpacePath.resolve(name).toFile();
        playerFiles.add(playerFolder);

        globalVarsDir = Path.of(playerFolder.getAbsolutePath()).resolve("globalVars").toFile();
        playerFiles.add(globalVarsDir);

        publicVarsDir = Path.of(playerFolder.getAbsolutePath()).resolve("publicVars").toFile();
        playerFiles.add(publicVarsDir);

        publicVars = new HashMap<>();
    }

    public static Player fromFile(File playerFile, Path playerSpacePath) {
        // Add more functionality when theres more than a name
        return new Player(playerFile.getName(), playerSpacePath);
    }

    public File[] files() {
        return playerFiles.toArray(new File[playerFiles.size()]);
    }

    public String getName() {
        return name;
    }

    public File getPlayerFolder() {
        return playerFolder;
    }

    public void addVariable(PublicVar var) {
        publicVars.put(var.getName(), var);
    }

    public Optional<PublicVar> getVariable(String name) {
        if (publicVars.get(name) == null) {
            if (Path.of(publicVarsDir.getPath()).resolve(name).toFile().exists()) {
                PublicVar importedVar = PublicVar.fromFile(this, Path.of(publicVarsDir.getPath()).resolve(name).toFile());
                publicVars.put(name, importedVar);
                return Optional.of(importedVar);
            } else {
                System.out.println("DEBUG: File is silly: " + Path.of(publicVarsDir.getAbsolutePath()).resolve(name).toString());
                return Optional.empty();
            }
        }
        return Optional.ofNullable(publicVars.get(name));
    }

    @Override
    public boolean equals(Object other) {
        return ((Player) other).getName().equals(name);
    }

    @Override
    public String toString(){
        return getName();
    }
}
