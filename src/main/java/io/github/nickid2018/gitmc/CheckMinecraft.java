package io.github.nickid2018.gitmc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.nickid2018.mcde.FileProcessor;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipFile;

public class CheckMinecraft {
    private static MainSelector mainSelector;
    private static final Map<String, VersionSelector> selectorMap = new HashMap<>();
    private static final Map<String, VersionSelector> versionMap = new HashMap<>();
    private static JsonObject versionManifest;
    private static JsonObject versionData;
    private static final List<String> supportVersions = new ArrayList<>();
    public static final Properties CONFIG = new Properties();

    public static void main(String[] args) throws IOException {
        CONFIG.load(Objects.requireNonNull(CheckMinecraft.class.getResourceAsStream("/config.properties")));

        initVersions();
        initSelectorMap();

        VersionControlTag pair = null;
        StringBuilder sb = new StringBuilder();
        FailCause failCause = new FailCause(false, true);

        while (failCause.noMapping()) {
            pair = select();
            String version = pair.version();
            failCause = processRemap(version);
        }

        if (!failCause.latest()) {
            recreateBuildGradle();
            sb.append("echo \"branch_read=").append(pair.fromBranch()).append("\" >> $GITHUB_ENV\n");
            sb.append("echo \"branch_write=").append(pair.branch()).append("\" >> $GITHUB_ENV\n");
            sb.append("echo \"version=").append(pair.version()).append("\" >> $GITHUB_ENV\n");
            sb.append("echo \"decompiler=").append(CONFIG.get("gitmc.decompiler")).append("\" >> $GITHUB_ENV\n");
            sb.append("echo \"rename_var=").append(CONFIG.get("gitmc.renameVar")).append("\" >> $GITHUB_ENV\n");
            sb.append("echo \"fail=false\" >> $GITHUB_ENV\n");
        } else {
            sb.append("echo \"version=").append(pair.lastSuccess()).append("\" >> $GITHUB_ENV\n");
            sb.append("echo \"fail=true\" >> $GITHUB_ENV\n");
            System.out.println("Minecraft version is latest");
        }

        FileWriter writer = new FileWriter("output.sh");
        writer.write(sb.toString());
        writer.close();
    }

    private static void initVersions() throws IOException {
        versionManifest = JsonParser.parseReader(new InputStreamReader(
                new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").openStream())).getAsJsonObject();
        versionManifest.getAsJsonArray("versions").forEach(
                e -> {
                    JsonObject object = e.getAsJsonObject();
                    supportVersions.add(object.get("id").getAsString());
                });
    }

    private static void initSelectorMap() throws IOException {
        JsonObject object = JsonParser.parseString(IOUtils.toString(
                new InputStreamReader(Objects.requireNonNull(
                        CheckMinecraft.class.getResourceAsStream("/version_branches.json"))))).getAsJsonObject();
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
            supportVersions.removeAll(Arrays.asList(selector.versionToDelete()));
        });
    }

    private static VersionControlTag select() throws IOException {
        VersionSelector selector;

        File file = new File("version/version_store.json");
        String lastSuccessVersion = null;
        String sourceBranch = "master";

        if (file.exists()) {
            JsonObject lastSuccess = JsonParser.parseReader(new FileReader("version/version_store.json")).getAsJsonObject();
            lastSuccessVersion = lastSuccess.get("last_version").getAsString();

            if (versionMap.containsKey(lastSuccessVersion)) {
                sourceBranch = "master";
                selector = versionMap.get(lastSuccessVersion);
            } else {
                String branch = lastSuccess.get("branch").getAsString();
                sourceBranch = branch;
                selector = selectorMap.get(branch);
            }
        } else selector = mainSelector;

        String version = selector.nextVersion(supportVersions, lastSuccessVersion);
        if (version == null && selector != mainSelector) {
            lastSuccessVersion = selector.startUse();
            version = (selector = mainSelector).nextVersion(supportVersions, lastSuccessVersion);
            sourceBranch = "master";
        }

        FileWriter writer = new FileWriter(file);
        JsonObject store = new JsonObject();
        store.addProperty("last_version", version);
        store.addProperty("branch", selector.branch);
        writer.write(store.toString());
        writer.close();

        return new VersionControlTag(sourceBranch, selector.branch, version, lastSuccessVersion);
    }

    private static FailCause processRemap(String version) {
        if (version == null)
            return new FailCause(true, false);

        String url = null;
        for (JsonElement element : versionManifest.getAsJsonArray("versions")) {
            JsonObject object = element.getAsJsonObject();
            if (object.get("id").getAsString().equals(version)) {
                url = object.get("url").getAsString();
                break;
            }
        }

        if (url == null)
            return new FailCause(true, false);

        try {
            System.out.println("Start remapping " + version);
            versionData = JsonParser.parseReader(
                    new InputStreamReader(new URL(url).openStream())).getAsJsonObject();
            JsonObject downloads = versionData.getAsJsonObject("downloads");

            if (!downloads.has("client_mappings")) {
                System.out.println(version + " has no mappings, skipped.");
                return new FailCause(false, true);
            }

            String clientURL = downloads.getAsJsonObject("client").get("url").getAsString();
            String mappingURL = downloads.getAsJsonObject("client_mappings").get("url").getAsString();

            IOUtils.copy(new URL(clientURL), new File("client.jar"));
            IOUtils.copy(new URL(mappingURL), new File("mapping.txt"));

            try (ZipFile file = new ZipFile(new File("client.jar"))) {
                FileProcessor.process(file, new File("mapping.txt"), new File("remapped.jar"));
            }
        } catch (Exception e) {
            System.out.println("Remap " + version + " failed");
            e.printStackTrace();
            return new FailCause(false, false);
        }

        return new FailCause(false, false);
    }

    private static void recreateBuildGradle() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("plugins {\n");
        sb.append("    id 'java'\n");
        sb.append("}\n");
        sb.append("\n");
        sb.append("repositories {\n");
        sb.append("    mavenCentral()\n");
        sb.append("    maven {\n");
        sb.append("        name = \"minecraft\"\n");
        sb.append("        url = \"https://libraries.minecraft.net/\"\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("\n");
        sb.append("dependencies {\n");
        for (JsonElement element : versionData.getAsJsonArray("libraries")) {
            JsonObject object = element.getAsJsonObject();
            String name = object.get("name").getAsString();
            sb.append("    implementation '").append(name).append("'\n");
        }
        sb.append("}\n");
        try (FileWriter writer = new FileWriter("gen-build.gradle")) {
            writer.write(sb.toString());
        }
    }
}
