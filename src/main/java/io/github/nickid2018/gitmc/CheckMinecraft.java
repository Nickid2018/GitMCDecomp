package io.github.nickid2018.gitmc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckMinecraft {

    private static MainSelector mainSelector;
    private static final Map<String, VersionSelector> selectorMap = new HashMap<>();
    private static final Map<String, VersionSelector> versionMap = new HashMap<>();
    private static JsonObject versionManifest;
    private static final List<String> supportVersions = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        initVersions(args[0] + "/version.json");
        initSelectorMap();
        StringPair pair = select("");
        System.out.println(System.getenv());
    }

    private static void initVersions(String path) throws IOException {
        versionManifest = JsonParser.parseReader(new FileReader(path)).getAsJsonObject();
        versionManifest.getAsJsonArray("versions").forEach(
                e -> supportVersions.add(e.getAsJsonObject().get("id").getAsString()));
    }

    private static void initSelectorMap() throws IOException {
        JsonObject object = JsonParser.parseString(IOUtils.toString(
                new FileReader("version_branches.json"))).getAsJsonObject();
        JsonArray branches = object.getAsJsonArray("branches");
        branches.forEach(branch -> {
            String name = branch.getAsString();
            JsonObject data = object.getAsJsonObject(name);
            String type = data.get("type").getAsString();
            VersionSelector selector = VersionSelector.create(type, name, data);
            if (selector instanceof MainSelector main)
                mainSelector = main;
            selectorMap.put(name, selector);
            versionMap.put(selector.startUse(), selector);
        });
    }

    private static StringPair select(String path) throws IOException {
        VersionSelector selector;

        File file = new File(path);
        JsonObject lastSuccess = null;

        if (file.exists()) {
            lastSuccess = JsonParser.parseReader(new FileReader(path)).getAsJsonObject();
            String lastVersion = lastSuccess.get("last_version").getAsString();

            if (versionMap.containsKey(lastVersion))
                selector = versionMap.get(lastVersion);
            else {
                String branch = lastSuccess.get("branch").getAsString();
                selector = selectorMap.get(branch);
            }
        } else selector = mainSelector;

        String version = selector.nextVersion(supportVersions, lastSuccess);
        if (version == null && selector != mainSelector)
            version = (selector = mainSelector).nextVersion(supportVersions, lastSuccess);

        return new StringPair(selector.branch, version);
    }
}
