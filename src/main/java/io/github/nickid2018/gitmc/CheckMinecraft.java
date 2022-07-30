package io.github.nickid2018.gitmc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.nickid2018.mcde.FileProcessor;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public class CheckMinecraft {

    private static MainSelector mainSelector;
    private static final Map<String, VersionSelector> selectorMap = new HashMap<>();
    private static final Map<String, VersionSelector> versionMap = new HashMap<>();
    private static JsonObject versionManifest;
    private static final List<String> supportVersions = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        initVersions(args[0] + "/version.json");
        initSelectorMap();
        MinecraftVersion pair = select("");

        String version = pair.version();

        StringBuilder sb = new StringBuilder();
        if (processRemap(version)) {
            sb.append("echo \"branch=").append(pair.branch()).append("\" >> $GITHUB_ENV\n");
            sb.append("echo \"version=").append(version).append("\" >> $GITHUB_ENV\n");
        } else
            sb.append("echo \"fail=true\" >> $GITHUB_ENV\n");

        FileWriter writer = new FileWriter("output.sh");
        writer.write(sb.toString());
        writer.close();
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

    private static MinecraftVersion select(String path) throws IOException {
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

        return new MinecraftVersion(selector.branch, version);
    }

    private static boolean processRemap(String version) {
        if (version == null)
            return false;

        String url = null;
        for (JsonElement element : versionManifest.getAsJsonArray("versions")) {
            JsonObject object = element.getAsJsonObject();
            if (object.get("id").getAsString().equals(version)) {
                url = object.get("url").getAsString();
                break;
            }
        }

        if (url == null)
            return false;

        try {
            JsonObject versionData = JsonParser.parseReader(
                    new InputStreamReader(new URL(url).openStream())).getAsJsonObject();
            JsonObject downloads = versionData.getAsJsonObject("downloads");

            String clientURL = downloads.getAsJsonObject("client").get("url").getAsString();
            String mappingURL = downloads.getAsJsonObject("client_mappings").get("url").getAsString();

            IOUtils.copy(new URL(clientURL), new File("client.jar"));
            IOUtils.copy(new URL(mappingURL), new File("mapping.txt"));

            try (ZipFile file = new ZipFile(new File("client.jar"))) {
                FileProcessor.process(file, new File("mapping.txt"), new File("remapped.jar"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
