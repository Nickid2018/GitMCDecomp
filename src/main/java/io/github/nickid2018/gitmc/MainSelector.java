package io.github.nickid2018.gitmc;

import com.google.gson.JsonObject;

import java.util.List;

public class MainSelector extends VersionSelector {

    private final String startVersion;

    public MainSelector(String branch, JsonObject config) {
        super(branch);
        startVersion = config.get("version_start").getAsString();
    }

    @Override
    public String nextVersion(List<String> supportVersions, String lastSuccessVersion) {
        if (lastSuccessVersion != null) {
            int find = supportVersions.indexOf(lastSuccessVersion);
            int low = supportVersions.indexOf(startVersion);
            if (low < find)
                return startVersion;
            return find > 0 ? supportVersions.get(find - 1) : null;
        } else return startVersion;
    }

    @Override
    public String startUse() {
        return startVersion;
    }

    @Override
    public String[] versionToDelete() {
        return new String[0];
    }
}
