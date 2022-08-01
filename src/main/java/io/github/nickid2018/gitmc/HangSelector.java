package io.github.nickid2018.gitmc;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class HangSelector extends VersionSelector {

    private final String base;
    private final List<String> versions = new ArrayList<>();

    public HangSelector(String branch, JsonObject config) {
        super(branch);
        base = config.get("base").getAsString();
        config.getAsJsonArray("versions").forEach(e -> versions.add(e.getAsString()));
    }

    @Override
    public String nextVersion(List<String> supportVersions, String lastSuccessVersion) {
        if (lastSuccessVersion != null) {
            if (lastSuccessVersion.equals(base))
                return versions.get(0);
            int find = versions.indexOf(lastSuccessVersion);
            return find >= versions.size() - 1 ? null : versions.get(find + 1);
        } else return base;
    }

    @Override
    public String startUse() {
        return base;
    }

    @Override
    public String[] versionToDelete() {
        return versions.toArray(String[]::new);
    }
}
