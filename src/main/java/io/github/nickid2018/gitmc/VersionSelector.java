package io.github.nickid2018.gitmc;

import com.google.gson.JsonObject;

import java.util.List;

public abstract class VersionSelector {

    protected final String branch;

    public VersionSelector(String branch) {
        this.branch = branch;
    }

    public abstract String nextVersion(List<String> supportVersions, String lastSuccessVersion);

    public abstract String startUse();

    public abstract String[] versionToDelete();

    public static VersionSelector create(String type, String branch, JsonObject data) {
        return switch (type) {
            case "main" -> new MainSelector(branch, data);
            case "hang" -> new HangSelector(branch, data);
            default -> null;
        };
    }
}
