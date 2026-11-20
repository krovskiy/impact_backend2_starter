import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

// Run from the repository root: java scripts/CheckFrontend.java
class CheckFrontend {
    static final String REVISION = "b4c14698ec0a0e06513816930eee12bf6b926ba8";
    static final String SHA256 = "cd01c1b459128b2355b536a13680be5c6bcc1740a50ab601571100f89f41201b";
    public static void main(String[] args) throws Exception {
        String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(Path.of("src/main/resources/static/index.html"))));
        if (!SHA256.equals(actual)) throw new IllegalStateException("Bundled frontend differs from the recorded upstream file");
        Process process = new ProcessBuilder("git", "ls-remote", "https://github.com/Victoras23/impact_2_year_fe.git", "refs/heads/main")
                .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        String output = new String(process.getInputStream().readAllBytes()).trim();
        if (process.waitFor() != 0 || output.isEmpty()) throw new IllegalStateException("Could not check GitHub; verify network and Git installation");
        String latest = output.split("\\s+")[0];
        if (!REVISION.equals(latest)) {
            System.err.println("Frontend update available: bundled " + REVISION + ", upstream " + latest);
            System.exit(1);
        }
        System.out.println("Frontend matches upstream main: " + REVISION);
    }
}
